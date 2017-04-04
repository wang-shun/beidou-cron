#!/bin/bash

#@file: odp_download_srchs.sh
#@author: wangchongjie
#@date: 2012-07-12
#@version: 3.0.0
#@brief: srchs data download & split 

DEBUG_MOD=0

CONF_FILE="./conf/odp_download_srchs.conf"
LIB_FILE="./lib/beidou_lib.sh"

source $CONF_FILE
if [ $? -ne 0 ];then
	echo "Conf error: Fail to load conffile[$CONF_FILE]!" && exit 1
fi
mkdir -p $LOG_PATH

source $LIB_FILE
if [ $? -ne 0 ];then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!" && exit 1
fi
mkdir -p $WORK_PATH && cd $WORK_PATH

function dowload_file()
{
        result=1
        if [ $# -le 0 ]
        then
                return $result
        fi
        for ((i=0;i<10;i++))
        do
                `wget -q --read-timeout=3 -T 1800 "$1" -O "$2"`
                if [ $? -eq 0 ]
                then
                        result=0
                        break
                fi
                sleep 1
        done
        return $result
}

function check_all_path()
{
	mkdir -p $LOCAL_TEMP
	mkdir -p $DAYFILE_SUCCESS_PATH
	mkdir -p $HOURFILE_SUCCESS_PATH
	mkdir -p $LOCAL_DEST/normal
}

#send mail or message whith alertType
#$1: mail and message content
#$2: alertType[0:send both mail and message; 1:only send mail; 2:only send message; 3:no mail or message]
function SendMailAndMessage()
{
	local content=$1
	local alertType=$2
	if [ -z $2 ];then
		alertType=0
	fi
	
	log "FATAL" "$content"
	if [ $alertType -eq 0 ] || [ $alertType -eq 1 ]
	then
		SendMail "$content" "${MAIL_LIST}"
	fi
	if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
	then
		SendMessage "$content" "${MOBILE_LIST}"
	fi
}

#download stat data file: bd_997_it_stat, bd_996_dt_stat and etc.
#$1: filetype(FILETYPE or QT_FILETYPE or IT_FILETYPE or DT_FILETYPE or KEYWORD_FILETYPE)
#$2: alertType [0:send both mail and message; 1:only send mail; 2:only send message; 3:no mail or message]
#$3: hadoop cluster name: kun or ston, default is kun
function download_stat_hour()
{
	local fileType=$1
	local alertType=$2
	local clusterName=$3
	local dataPrefix=${DATA_PREFIX_KUN}
	local md5Prefix=${MD5_PREFIX_KUN}
	
	# step0: set data prefix and md5 prefix for different hadoop cluster
	if [[ "$clusterName" == "ston" ]];then
		dataPrefix=${DATA_PREFIX_STON}
		md5Prefix=${MD5_PREFIX_STON}
		maniftPrefix=${MANIFEST_PREFIX_STON}
		maniftMd5Prefix=${MANIFEST_MD5_PREFIX_STON}
	elif [[ "$clusterName" == "kun" ]];then
		dataPrefix=${DATA_PREFIX_KUN}
		md5Prefix=${MD5_PREFIX_KUN}
		maniftPrefix=${MANIFEST_PREFIX_KUN}
		maniftMd5Prefix=${MANIFEST_MD5_PREFIX_KUN}
	fi
	
	# step1: download data file from log platform
	#wget -t $MAX_RETRY -q "${dataPrefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile
	dowload_file "${dataPrefix}&date=${curFullTime}&item=${fileType}" "$localStatTempFile"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to download statfile [$statFileName]." $alertType
		return 1
	fi
	
#	#download md5 file from log platform
#	wget -t $MAX_RETRY -T 1800 -q "${md5Prefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".md5.tmp"
#	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".md5.tmp" ]
#	then
#		SendMailAndMessage "odp_beidou_srchs: Fail to download statfile md5[$statFileName.md5]." $alertType
#		return 1
#	fi
#	awk -vfname="$statFileName" '{print $2 "  " fname}' $localStatTempFile".md5.tmp" > $localStatTempFile".md5"
#	rm $localStatTempFile".md5.tmp"
	
	# step2: download manifest file from log platform
	#wget -t $MAX_RETRY -T 1800 -q "${maniftPrefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".manifest"
	dowload_file "${maniftPrefix}&date=${curFullTime}&item=${fileType}" $localStatTempFile".manifest"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".manifest" ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to download statfile md5[$statFileName.manifest]." $alertType
		return 1
	fi
	
	# step3: download manifest.md5 file from log platform
	#wget -t $MAX_RETRY -T 1800 -q "${maniftMd5Prefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".manifest.md5"
	dowload_file "${maniftMd5Prefix}&date=${curFullTime}&item=${fileType}" $localStatTempFile".manifest.md5"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".manifest.md5" ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to download statfile md5[$statFileName.manifest.md5]." $alertType
		return 1
	fi
	
	# step4: check manifest.md5 file
	offline_manifest_md5=`md5sum $localStatTempFile".manifest" | awk '{print $1}'` #manifest md5 offline
	online_manifest_md5=`awk '{print $1}' $localStatTempFile".manifest.md5"`   #manifest md5 online
	if [ "${offline_manifest_md5}" != "${online_manifest_md5}" ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to check md5 of $localStatTempFile manifest file !" $alertType
		return 1
	fi
	
	# step5: user manifest to check date file
	offline_file_line=`ls -al $localStatTempFile | awk '{print $5}'` #line count offline
	online_file_line=`awk '{print $3}' $localStatTempFile".manifest"`   #line count online
	if [ "${offline_file_line}" != "${online_file_line}" ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to check line count of $localStatTempFile file !" $alertType
		return 1
	fi
	
	rm $localStatTempFile".manifest"
	rm $localStatTempFile".manifest.md5"
}

function touch_no_used_empty_file()
{
	curTime=$1
	echo "touch no used file!"
	mkdir -p ${localDestPath}
	cd ${localDestPath}
		
	statFileName=${STAT_TAX_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	touch $localStatTempFile
	md5sum $statFileName > "$statFileName"".md5"
	
	statFileName=${STAT_ANTI_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	touch $localStatTempFile
	md5sum $statFileName > "$statFileName"".md5"
}

#download dayfile(trans/holmes/uv) data of one hour
#$1: YYYYmmddHH
function download_dayfile_hour()
{
	curTime=$1
	alertType=$2
	curDate=${curTime:0:8}
	#curFullTime change to Date for download day file
	#curFullTime=$1"0000"	
	formatCurDate=${curTime:0:4}"-"${curTime:4:2}"-"${curTime:6:2}
	curFullTime=`date -d "$formatCurDate -1 days" +%Y%m%d`
	
	localDestPath=$LOCAL_DEST/normal/$curDate
	local curHour=`date +%H`
	
	cd $WORK_PATH	
	if ! [ -d $localDestPath ]
	then
		mkdir -p $localDestPath
	fi
	cd $localDestPath

	#deal with trans_user ------ begin
	statFileName=${TRANS_USER_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_USER_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_USER_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_USER_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_USER_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_USER_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_USER_FILE_PRE}$curDate".isSuccess"
		fi	
	else
		#if TRANS_USER_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_user switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_user ------ end
	
	#deal with trans_trade ------ begin
	statFileName=${TRANS_TRADE_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_TRADE_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_TRADE_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_TRADE_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_TRADE_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_TRADE_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_TRADE_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_TRADE_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_trade switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_trade ------ end
	
	#deal with trans_reg ------ begin
	statFileName=${TRANS_REG_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_REG_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_REG_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_REG_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_REG_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_REG_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_REG_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_REG_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_reg switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_reg ------ end
	
	#deal with trans_pack ------ begin
	statFileName=${TRANS_PACK_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_PACK_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_PACK_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_PACK_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_PACK_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_PACK_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_PACK_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_PACK_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_pack switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_pack ------ end
	
	#deal with trans_keyword ------ begin
	statFileName=${TRANS_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_KEYWORD_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_KEYWORD_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_KEYWORD_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_KEYWORD_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_KEYWORD_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_KEYWORD_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_KEYWORD_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_keyword switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_keyword ------ end
	
	#deal with trans_it ------ begin
	statFileName=${TRANS_IT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_IT_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_IT_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_IT_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_IT_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_IT_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_IT_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_IT_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_it switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_it ------ end
	
	#deal with trans_dt ------ begin
	statFileName=${TRANS_DT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_DT_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_DT_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_DT_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_DT_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_DT_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_DT_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_DT_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_dt switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_dt ------ end
	
	#deal with trans_plan ------ begin
	statFileName=${TRANS_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_PLAN_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_PLAN_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_PLAN_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_PLAN_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_PLAN_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_PLAN_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_PLAN_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_plan switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_plan ------ end
	
	#deal with trans_group ------ begin
	statFileName=${TRANS_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_GROUP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_GROUP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_GROUP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_GROUP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_GROUP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_GROUP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_GROUP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_group switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_group ------ end
	
	#deal with trans_ad ------ begin
	statFileName=${TRANS_AD_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_AD_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_AD_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_AD_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_AD_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_AD_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_AD_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_AD_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_ad switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_ad ------ end
	
	#deal with trans_site ------ begin
	statFileName=${TRANS_SITE_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_SITE_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_SITE_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_SITE_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_SITE_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_SITE_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_SITE_FILE_PRE}$curDate".isSuccess" 
		fi	
	else
		#if TRANS_SITE_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_site switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_site ------ end
	
	#deal with trans_at ------ begin
	statFileName=${TRANS_AT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $TRANS_AT_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${TRANS_AT_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${TRANS_AT_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $TRANS_AT_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$TRANS_AT_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${TRANS_AT_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if TRANS_AT_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If trans_at switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with trans_at ------ end
	
	#deal with holmes_data ------ begin
	statFileName=${HOLMES_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $HOLMES_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${HOLMES_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${HOLMES_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $HOLMES_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$HOLMES_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${HOLMES_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if HOLMES_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If holmes_data switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with holmes_data ------ end
	
	#deal with uv_user ------ begin
	statFileName=${UV_USER_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_USER_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_USER_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_USER_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_USER_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_USER_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_USER_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_USER_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_user switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_user ------ end
	
	#deal with uv_trade ------ begin
	statFileName=${UV_TRADE_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_TRADE_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_TRADE_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_TRADE_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_TRADE_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_TRADE_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_TRADE_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_TRADE_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_trade switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_trade ------ end
	
	#deal with uv_site ------ begin
	statFileName=${UV_SITE_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_SITE_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_SITE_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_SITE_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_SITE_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_SITE_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_SITE_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_SITE_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_site switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_site ------ end
	
	#deal with uv_reg_user ------ begin
	statFileName=${UV_REG_USER_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_REG_USER_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_REG_USER_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_REG_USER_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_REG_USER_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_REG_USER_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_REG_USER_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_REG_USER_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_reg_user switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_reg_user ------ end
	
	#deal with uv_reg_plan ------ begin
	statFileName=${UV_REG_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_REG_PLAN_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_REG_PLAN_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_REG_PLAN_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_REG_PLAN_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_REG_PLAN_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_REG_PLAN_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_REG_PLAN_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_reg_plan switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_reg_plan ------ end
	
	#deal with uv_reg_group ------ begin
	statFileName=${UV_REG_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_REG_GROUP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_REG_GROUP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_REG_GROUP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_REG_GROUP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_REG_GROUP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_REG_GROUP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_REG_GROUP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_reg_group switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_reg_group ------ end
	
	#deal with uv_reg_group ------ begin
	statFileName=${UV_REG_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_REG_GROUP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_REG_GROUP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_REG_GROUP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_REG_GROUP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_REG_GROUP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_REG_GROUP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_REG_GROUP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_reg_group switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_reg_group ------ end
	
	#deal with uv_plan ------ begin
	statFileName=${UV_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_PLAN_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_PLAN_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_PLAN_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_PLAN_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_PLAN_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_PLAN_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_PLAN_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_plan switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_plan ------ end
	
	#deal with uv_pack ------ begin
	statFileName=${UV_PACK_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_PACK_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_PACK_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_PACK_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_PACK_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_PACK_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_PACK_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_PACK_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_pack switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_pack ------ end
	
	#deal with uv_keyword ------ begin
	statFileName=${UV_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_KEYWORD_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_KEYWORD_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_KEYWORD_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_KEYWORD_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_KEYWORD_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_KEYWORD_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_KEYWORD_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_keyword switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_keyword ------ end
	
	#deal with uv_it ------ begin
	statFileName=${UV_IT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_IT_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_IT_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_IT_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_IT_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_IT_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_IT_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_IT_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_it switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_it ------ end
	
	#deal with uv_it_user ------ begin
	statFileName=${UV_IT_USER_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_IT_USER_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_IT_USER_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_IT_USER_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_IT_USER_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_IT_USER_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_IT_USER_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_IT_USER_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_it_user switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_it_user ------ end
	
	#deal with uv_it_plan ------ begin
	statFileName=${UV_IT_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_IT_PLAN_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_IT_PLAN_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_IT_PLAN_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_IT_PLAN_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_IT_PLAN_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_IT_PLAN_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_IT_PLAN_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_it_plan switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_it_plan ------ end
	
	#deal with uv_it_group ------ begin
	statFileName=${UV_IT_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_IT_GROUP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_IT_GROUP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_IT_GROUP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_IT_GROUP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_IT_GROUP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_IT_GROUP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_IT_GROUP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_it_group switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_it_group ------ end
	
	#deal with uv_group ------ begin
	statFileName=${UV_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_GROUP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_GROUP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_GROUP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_GROUP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_GROUP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_GROUP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_GROUP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_group switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_group ------ end
	
	#deal with uv_dt_user ------ begin
	statFileName=${UV_DT_USER_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_DT_USER_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_DT_USER_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_DT_USER_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_DT_USER_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_DT_USER_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_DT_USER_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_DT_USER_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_dt_user switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_dt_user ------ end
	
	#deal with uv_dt_plan ------ begin
	statFileName=${UV_DT_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_DT_PLAN_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_DT_PLAN_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_DT_PLAN_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_DT_PLAN_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_DT_PLAN_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_DT_PLAN_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_DT_PLAN_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_dt_plan switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_dt_plan ------ end
	
	#deal with uv_dt_group ------ begin
	statFileName=${UV_DT_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_DT_GROUP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_DT_GROUP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_DT_GROUP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_DT_GROUP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_DT_GROUP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_DT_GROUP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_DT_GROUP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_dt_group switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_dt_group ------ end
	
	#deal with uv_dt_ad ------ begin
	statFileName=${UV_AD_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_AD_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_AD_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_AD_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_AD_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_AD_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_AD_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_AD_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_dt_ad switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_dt_ad ------ end
	
	#deal with uv_app ------ begin
	statFileName=${UV_APP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_APP_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_APP_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_APP_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_APP_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_APP_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_APP_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_APP_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_app switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_app ------ end
	
	#deal with uv_device ------ begin
	statFileName=${UV_DEVICE_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_DEVICE_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_DEVICE_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_DEVICE_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_DEVICE_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_DEVICE_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_DEVICE_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_DEVICE_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_device switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_device ------ end
	
	#deal with uv_at ------ begin
	statFileName=${UV_AT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$localDestPath/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $UV_AT_SWITCH -eq 1 ]
	then
		if [ -e $DAYFILE_SUCCESS_PATH/${UV_AT_FILE_PRE}$curDate".isSuccess" ]  
        then
			touch $localStatTempFile
		elif [ ${curDate} -eq `date   +%Y%m%d` ] && [ ${curHour} -lt ${UV_AT_EXE_TIME} ] 
		then 
			touch $localStatTempFile
		else 
			download_stat_hour $UV_AT_FILETYPE $alertType "kun"
			if [ $? -ne 0 ]
			then
				SendMailAndMessage "odp_beidou_srchs: Fail to download [$UV_AT_FILETYPE]." $alertType
				return 1
			fi
			touch $DAYFILE_SUCCESS_PATH/${UV_AT_FILE_PRE}$curDate".isSuccess"
		fi
	else
		#if UV_AT_SWITCH is off, then touch empty file
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If uv_at switch is off, then touch empty file[$localStatTempFile]!"
	fi	
	md5sum $statFileName > $localStatTempFile".md5"
	#deal with uv_device ------ end
}

#download srchs stat data of one hour
#$1: YYYYmmddHH
function download_beidou_hour()
{
	curTime=$1
	alertType=$2
	curDate=${curTime:0:8}
	curFullTime=$1"0000"	
	localDestPath=$LOCAL_DEST/normal/$curDate

	rm -rf $LOCAL_TEMP/*
	cd $LOCAL_TEMP
	
	#if list file exists already, then return 0
	listFileName=${LIST_FILE_PRE}${curTime}${FILE_SUF}
	localDestPath=${LOCAL_DEST}/normal/${curDate}
	localListDestFile=$localDestPath/$listFileName
	if [ -f $localListDestFile ] || [ -f $HOURFILE_SUCCESS_PATH/"statfile"${curTime}".isSuccess" ]
	then
		log "WARNING" "File[$localListDestFile] exists already!"
		return 0
	fi
	
	statFileName=${STAT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName

	download_stat_hour $FILETYPE $alertType "kun"
	if [ $? -ne 0 ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType
		return 1
	fi		
	md5sum $statFileName > $localStatTempFile".md5"
	
	#deal with it srchs stat ------ begin
	statFileName=${ITSTAT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $ITSTAT_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $IT_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if ITSTAT_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If it srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with it srchs stat ------ end
	
	#deal with dt srchs stat ------ begin
	statFileName=${DTSTAT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $DTSTAT_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $DT_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if DTSTAT_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If dt srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with dt srchs stat ------ end
	
	#deal with new kt srchs stat ------ begin
	statFileName=${KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $KEYWORD_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $KEYWORD_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if KEYWORD_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If new kt srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with new kt srchs stat ------ end
	
	#deal with reg srchs stat ------ begin
	statFileName=${STAT_REG_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $STAT_REG_SWITCH -eq 1 ]
	then
		download_stat_hour $STAT_REG_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if STAT_REG_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If new kt srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with reg srchs stat ------ end
	
	#deal with pack srchs stat ------ begin
	statFileName=${STAT_PACK_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $STAT_PACK_SWITCH -eq 1 ]
	then
		download_stat_hour $STAT_PACK_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if STAT_PACK_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If new kt srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with pack srchs stat ------ end
	
	#deal with app srchs stat ------ begin
	statFileName=${STAT_APP_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $STAT_APP_SWITCH -eq 1 ]
	then
		download_stat_hour $STAT_APP_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if STAT_APP_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If new app srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with app srchs stat ------ end
	
	#deal with device srchs stat ------ begin
	statFileName=${STAT_DEVICE_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $STAT_DEVICE_SWITCH -eq 1 ]
	then
		download_stat_hour $STAT_DEVICE_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if STAT_DEVICE_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If new device srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with device srchs stat ------ end
	
	#deal with si keyword srchs stat ------ begin
	statFileName=${SI_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $SI_KEYWORD_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $SI_KEYWORD_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if SI_KEYWORD_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If si keyword srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with si keyword srchs stat ------ end
	
	#deal with si product srchs stat ------ begin
	statFileName=${SI_PRODUCT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $SI_PRODUCT_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $SI_PRODUCT_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if SI_PRODUCT_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If si product srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with si product srchs stat ------ end
	
	#deal with at srchs stat ------ begin
	statFileName=${ATSTAT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $ATSTAT_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $AT_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if ATSTAT_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If at srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with at srchs stat ------ end
	
	#deal with attach srchs stat ------ begin
	statFileName=${ATTACHSTAT_FILE_PRE}${curTime}${FILE_SUF}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	
	if [ $ATTACHSTAT_SRCHS_SWITCH -eq 1 ]
	then
		download_stat_hour $ATTACH_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "odp_beidou_srchs: Fail to download [$statFileName]." $alertType 
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
	else
		#if ATTACHSTAT_SRCHS_SWITCH is off, then touch empty file, and record log, and go on
		cat /dev/null > $statFileName
		md5sum $statFileName > $localStatTempFile".md5"
		log "WARN" "If attach srchs stat switch is off, then touch empty file[$localStatTempFile]!"
	fi
	#deal with attach srchs stat ------ end
	
	#touch empty file for no used doris table
	touch_no_used_empty_file ${curTime}
	
	cd $WORK_PATH	
	
	if ! [ -d $localDestPath ]
	then
		mkdir -p $localDestPath
	fi
	
	mv -f $LOCAL_TEMP/* $localDestPath/
	if [ $? -ne 0 ]
	then
		SendMailAndMessage "odp_beidou_srchs: Fail to mv statfile[$curTime] to dest." $alertType
		return 1
	else
		log "TRACE" "Suc to mv stat file to [$localDestPath]."
	fi
	
	touch $HOURFILE_SUCCESS_PATH/"statfile"${curTime}".isSuccess"
}

#function: output the final file list, include click data, srchs data
#$1: YYYYmmddHH
function output_filelist()
{
	curTime=$1
	alertType=$2
	curDate=${curTime:0:8}
	localDestPath=${LOCAL_DEST}/normal/${curDate}
	listFileName=${LIST_FILE_PRE}${curTime}${FILE_SUF}
	localListDestFile=${localDestPath}/${listFileName}
	cat /dev/null > ${localListDestFile}
	
	cd ${localDestPath}
	
	#echo click file name into the filelist
	statFileName=${STAT_FILE_PRE}${curTime}${FILE_SUF}
	echo ${statFileName} >> ${localListDestFile}
	
	#echo it srchs file names into the filelist
    itSrchFileName=${ITSTAT_FILE_PRE}${curTime}${FILE_SUF}
    echo ${itSrchFileName} >> ${localListDestFile}
	
	#echo dt srchs file names into the filelist
    dtSrchFileName=${DTSTAT_FILE_PRE}${curTime}${FILE_SUF}
    echo ${dtSrchFileName} >> ${localListDestFile}
	
	#echo new kt srchs file names into the filelist
    newktSrchFileName=${KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
    echo ${newktSrchFileName} >> ${localListDestFile}
	
	#echo new reg srchs file names into the filelist
    regSrchFileName=${STAT_REG_FILE_PRE}${curTime}${FILE_SUF}
    echo ${regSrchFileName} >> ${localListDestFile}
	
	#echo new pack srchs file names into the filelist
    packSrchFileName=${STAT_PACK_FILE_PRE}${curTime}${FILE_SUF}
    echo ${packSrchFileName} >> ${localListDestFile}
    
	#echo app srchs file names into the filelist
    appSrchFileName=${STAT_APP_FILE_PRE}${curTime}${FILE_SUF}
    echo ${appSrchFileName} >> ${localListDestFile}
	
	#echo device srchs file names into the filelist
    deviceSrchFileName=${STAT_DEVICE_FILE_PRE}${curTime}${FILE_SUF}
    echo ${deviceSrchFileName} >> ${localListDestFile}
	
	fileName=${STAT_TAX_FILE_PRE}${curTime}${FILE_SUF}
    echo ${fileName} >> ${localListDestFile}
	fileName=${STAT_ANTI_FILE_PRE}${curTime}${FILE_SUF}
    echo ${fileName} >> ${localListDestFile}
    
    siKtSrchFileName=${SI_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
    echo ${siKtSrchFileName} >> ${localListDestFile}
    
    siProdSrchFileName=${SI_PRODUCT_FILE_PRE}${curTime}${FILE_SUF}
    echo ${siProdSrchFileName} >> ${localListDestFile}
	
	fileName=${ATSTAT_FILE_PRE}${curTime}${FILE_SUF}
    echo ${fileName} >> ${localListDestFile}
	
	fileName=${ATTACHSTAT_FILE_PRE}${curTime}${FILE_SUF}
    echo ${fileName} >> ${localListDestFile}
	
	transFileName=${TRANS_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_AD_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_SITE_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_USER_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_TRADE_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_REG_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_PACK_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_IT_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_DT_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	transFileName=${TRANS_AT_FILE_PRE}${curTime}${FILE_SUF}
	echo ${transFileName} >> ${localListDestFile}
	
	uvFileName=${UV_USER_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_SITE_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_TRADE_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_REG_USER_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_REG_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_REG_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_PACK_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_IT_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_IT_USER_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_IT_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_IT_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_DT_USER_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_DT_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_DT_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_AD_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_APP_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_DEVICE_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	uvFileName=${UV_AT_FILE_PRE}${curTime}${FILE_SUF}
	echo ${uvFileName} >> ${localListDestFile}
	
	holmesFileName=${HOLMES_FILE_PRE}${curTime}${FILE_SUF}
	echo ${holmesFileName} >> ${localListDestFile}

	md5sum $listFileName > $localListDestFile".md5"
}

#main function
#$1: starting time, YYYYmmddHH
#$2: ending time, YYYYmmddHH
function download_beidou()
{
	open_log
	check_all_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	timeFix=`date -d "$FIX_DELAY hour ago" +%Y%m%d%H` && \
	timeNow=`date +%Y%m%d%H`
	
	timeStarting=$1
	timeEnding=$2

	local isInManualOperation=1 # have params means manual operation
	if [ -z $timeStarting ]
	then
		#no param
		timeStarting=$timeFix
		timeEnding=$timeFix
		isInManualOperation=0
	else
		if [ -z $timeEnding ]
		then
			#1 param
			timeEnding=$timeFix
			if [ $timeStarting -gt $timeFix ]
			then
				timeStarting=$timeFix
			fi
		else
			#2 param
			if [ $timeStarting -gt $timeEnding ]
			then
				timeTmp=$timeEnding
				timeEnding=$timeStarting
				timeStarting=$timeTmp
			fi
		fi
	fi
	
	#send both mail and message for manual operation
	if [[ $isInManualOperation -eq 1 ]]
	then
		alertType=0
	fi
	
	#begin real work
	log "TRACE" "$0 start running"
	returnFlag=0
	
	#hour by hour
	scanTime=$timeFix
	numHour=$FIX_DELAY
	while [ $scanTime -gt $timeStarting ]
	do
		((numHour++))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
	
	# isInManualOperation then download statfile between timeStarting and timeEnding
	if [ $isInManualOperation -eq 1 ]
	then
		while [ $scanTime -le $timeEnding ]
		do
			flag=0
			download_beidou_hour $scanTime $alertType
			if [ $? -ne 0 ]
			then
				flag=1
			fi
			#download trans data
			if [ ${flag} -eq 0 ]
			then
				download_dayfile_hour $scanTime $alertType
				if [ $? -ne 0 ]
				then
					flag=2
				fi		
			fi
			#generate filelist
			if [ ${flag} -eq 0 ]
			then
				output_filelist $scanTime $alertType
			elif [ $returnFlag -eq 0 ]
			then
				returnFlag=$flag
			fi
			
			((numHour--))
			scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
		done
	fi
	
	# is NOT InManualOperation then check previous data files and download it 
	if [ $CHECK_DATA_IN_HOW_MANY_DAYS -gt 0 ] && [ $isInManualOperation -ne 1 ]
	then
		check_data_before_and_download
	fi
		
	close_log $returnFlag
	
	return $returnFlag
}

#DESC: check downloaded stat data within 2 days (that day and the day before), before the time passed in.
#PARAMS:
#    $1: YYYYmmddHH
#NOTICE: $1 should never be after NOW.
function check_data_before_and_download()
{
	timeFix=`date -d "$FIX_DELAY hour ago" +%Y%m%d%H`
	
	local howManydaysAgo=0
	let "howManydaysAgo= $CHECK_DATA_IN_HOW_MANY_DAYS - 1"
	local timeStarting=`date -d "-$howManydaysAgo day" +%Y%m%d00`
	local timeEnding=$timeFix

	local noAlertNumHour=$(($FIX_DELAY+$DOWNLOAD_HOURS_NO_ALERT))
	local numHour=$FIX_DELAY
	local scanTime=$timeFix
	while [ $scanTime -gt $timeStarting ]
	do
		((numHour++))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
	
	while [ $scanTime -le $timeEnding ]
	do
		#send both message and mail
		alertType=0
		#only send mail
		if [ $numHour -lt $noAlertNumHour ]
		then
			alertType=1
		fi
		
		## check file of $scanTime complete or not
		local needToDownload=0
		listFileName=${LIST_FILE_PRE}${scanTime}${FILE_SUF}
		localDestPath=${LOCAL_DEST}/normal/${scanTime:0:8}
		localListDestFile=$localDestPath/$listFileName
		if [ -f $localListDestFile ]
		then
			needToDownload=0
		else
			needToDownload=1
		fi
		## check file of $scanTime complete or not ends
 
		if [ $needToDownload -eq 1  ] 
		then
			## try re-download
			local flag=1
			download_beidou_hour $scanTime $alertType
			if [ $? -eq 0 ] #successful 
			then
				flag=0
				if [ $scanTime -ne $timeFix ] #not FIX_DELAY hour data
				then
					SendMailAndMessage "odp_beidou_srchs: srchs data of $scanTime has been re-downloaded successfully." 1
				fi
			fi
			#re-download trans data
			if [ ${flag} -eq 0 ]
			then
				download_dayfile_hour $scanTime $alertType
				if [ $? -eq 0 ] 
				then
					flag=0
					if [ $scanTime -ne $timeFix ]
					then
						SendMailAndMessage "odp_beidou_srchs: trans data of $scanTime has been re-downloaded successfully." 1
					fi	
				else
					flag=1
				fi
			fi
			#re-gerenate filelist
			if [ ${flag} -eq 0 ]
			then
				output_filelist $scanTime $alertType					
			fi
		fi
		
		#revise numHour if run across more than one hour
		if [ $scanTime -lt `date -d "$numHour hour ago" +%Y%m%d%H` ]
		then
			((numHour++))
			((noAlertNumHour++))
		fi
		
		((numHour--))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
} # check_data_before_and_download ends

download_beidou $1 $2

exit $?
