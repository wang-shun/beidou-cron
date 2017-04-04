#!/bin/bash

#@file: srchs_download_hourly.sh
#@author: wangchongjie
#@date: 2013-11-27
#@version: 1.0.0
#@brief: srchs & clk data download 

DEBUG_MOD=0

CONF_FILE="../conf/srchs_download_hourly.conf"
LIB_FILE="../lib/beidou_lib.sh"

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

function check_all_path()
{
	mkdir -p $LOCAL_TEMP
	mkdir -p $LOCAL_DEST
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
	wget -t $MAX_RETRY -q "${dataPrefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile ]
	then
		SendMailAndMessage "srchs_download_hourly: Fail to download statfile [$statFileName]." $alertType
		return 1
	fi
	
#	#download md5 file from log platform
#	wget -t $MAX_RETRY -T 1800 -q "${md5Prefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".md5.tmp"
#	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".md5.tmp" ]
#	then
#		SendMailAndMessage "srchs_download_hourly: Fail to download statfile md5[$statFileName.md5]." $alertType
#		return 1
#	fi
#	awk -vfname="$statFileName" '{print $2 "  " fname}' $localStatTempFile".md5.tmp" > $localStatTempFile".md5"
#	rm $localStatTempFile".md5.tmp"
	
	# step2: download manifest file from log platform
	wget -t $MAX_RETRY -T 1800 -q "${maniftPrefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".manifest"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".manifest" ]
	then
		SendMailAndMessage "srchs_download_hourly: Fail to download statfile md5[$statFileName.manifest]." $alertType
		return 1
	fi
	
	# step3: download manifest.md5 file from log platform
	wget -t $MAX_RETRY -T 1800 -q "${maniftMd5Prefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".manifest.md5"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".manifest.md5" ]
	then
		SendMailAndMessage "srchs_download_hourly: Fail to download statfile md5[$statFileName.manifest.md5]." $alertType
		return 1
	fi
	
	# step4: check manifest.md5 file
	offline_manifest_md5=`md5sum $localStatTempFile".manifest" | awk '{print $1}'` #manifest md5 offline
	online_manifest_md5=`awk '{print $1}' $localStatTempFile".manifest.md5"`   #manifest md5 online
	if [ "${offline_manifest_md5}" != "${online_manifest_md5}" ]
	then
		SendMailAndMessage "srchs_download_hourly: Fail to check md5 of $localStatTempFile manifest file !" $alertType
		return 1
	fi
	
	# step5: user manifest to check date file
	offline_file_line=`ls -al $localStatTempFile | awk '{print $5}'` #line count offline
	online_file_line=`awk '{print $3}' $localStatTempFile".manifest"`   #line count online
	if [ "${offline_file_line}" != "${online_file_line}" ]
	then
		SendMailAndMessage "srchs_download_hourly: Fail to check line count of $localStatTempFile file !" $alertType
		return 1
	fi
	
	rm $localStatTempFile".manifest"
	rm $localStatTempFile".manifest.md5"
}

#download srchs stat data of one hour
#$1: YYYYmmddHH
function download_beidou_hour()
{
	curTime=$1
	alertType=$2
	curDate=${curTime:0:8}
	curFullTime=$1"0000"	
	localDestPath=$LOCAL_DEST

	rm -rf $LOCAL_TEMP/*
	cd $LOCAL_TEMP
	
	#if list file exists already, then return 0
	listFileName=${LIST_FILE_PRE}${curTime}
	localDestPath=${LOCAL_DEST}
	localListDestFile=$localDestPath/$listFileName
	if [ -f $localListDestFile ]
	then
		log "WARNING" "File[$localListDestFile] exists already!"
		return 0
	fi
	
	statFileName=${SRCH_STAT_FILE_PRE}${curTime}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	needDowloadSrch=1
	#if there is no click_file then download it
	if [[ -f ${localStatDestFile} ]] && [[ -f ${localStatDestFile}".md5" ]]
	then
		cd $localDestPath
		md5sum -c ${localStatDestFile}".md5" > /dev/null
		if [[ $? -eq 0 ]]
		then	
			needDowloadSrch=0
		fi
		cd $LOCAL_TEMP
	fi
	if [[ $needDowloadSrch -eq 1 ]]
	then
		download_stat_hour $SRCH_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "srchs_download_hourly: Fail to download [$statFileName]." $alertType
			return 1
		fi		
		#ckeck file & remove trade info
		mv $localStatTempFile $localStatTempFile".old"
		ERROR_TRACE_FILE=$LOCAL_TEMP/error_trace.out
		processStatFile $localStatTempFile".old" $localStatTempFile
		if [[ $? -ne 0 ]]
		then
			local errorMessage=`cat $ERROR_TRACE_FILE`
			SendMailAndMessage "$errorMessage" $alertType
			rm -f $ERROR_TRACE_FILE
			return 1
		fi
		rm -f $localStatTempFile".old"
		cd $LOCAL_TEMP
		md5sum $statFileName > $statFileName".md5"
	fi
	
	#if need download clk file then download it
	statFileName=${CLK_STAT_FILE_PRE}${curTime}
	localStatTempFile=$LOCAL_TEMP/$statFileName
	localStatDestFile=$localDestPath/$statFileName
	needDowloadClick=1
	#if there is no click_file then download it
	if [[ -f ${localStatDestFile} ]] && [[ -f ${localStatDestFile}".md5" ]]
	then
		cd $localDestPath
		md5sum -c ${localStatDestFile}".md5" > /dev/null
		if [[ $? -eq 0 ]]
		then	
			needDowloadClick=0
		fi
		cd $LOCAL_TEMP
	fi
	
	if [[ $needDowloadClick -eq 1 ]]
	then
		download_stat_hour $CLK_FILETYPE $alertType "kun"
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "srchs_download_hourly: Fail to download [$statFileName]." $alertType
			return 1
		fi
		
		cd $LOCAL_TEMP
		mv $localStatTempFile $localStatTempFile".old"
		processClkStatFile $localStatTempFile".old" $localStatTempFile
		if [ $? -ne 0 ]
		then
			SendMailAndMessage "srchs_download_hourly: Fail to process clk statfile $localStatTempFile." $alertType
			return 1
		fi
		md5sum $statFileName > $localStatTempFile".md5"
		rm $localStatTempFile".old"
	fi
	
	cd $WORK_PATH	
	
	if ! [ -d $localDestPath ]
	then
		mkdir -p $localDestPath
	fi
	
	mv -f $LOCAL_TEMP/* $localDestPath/
	if [ $? -ne 0 ]
	then
		SendMailAndMessage "srchs_download_hourly: Fail to mv statfile[$curTime] to dest." $alertType
		return 1
	else
		log "TRACE" "Suc to mv stat file to [$localDestPath]."
	fi
}

#function: output the final file list, include click data, srchs data
#$1: YYYYmmddHH
function output_filelist()
{
	curTime=$1
	alertType=$2
	curDate=${curTime:0:8}
	localDestPath=${LOCAL_DEST}
	listFileName=${LIST_FILE_PRE}${curTime}
	localListDestFile=${localDestPath}/${listFileName}
	cat /dev/null > ${localListDestFile}
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

#process the stat file "sourceFile" and write it to "targetFile"
#$1: sourceFile
#$2: targetFile
#return: return 1 if error occurs. the error message is printed into ERROR_TRACE_FILE
function processStatFile()
{
	local sourceFile=$1
	local targetFile=$2

	awk -v LINE_CRITERIA=$TOO_SMALL_DATA_FILE_LINE_COUNT_CRITERIA \
	-v ERROR_TRACE_FILE=$ERROR_TRACE_FILE \
	'
	BEGIN {
		exception=0
	}
	{
		if ( NF == 16  &&  $11 ~ /^[[:digit:]]+$/  &&  $11 > 0 )
		{
			print $1"\t"$2"\t"$3"\t"$4"\t"$5"\t"$6"\t"$7"\t"$8"\t"$9"\t"$10"\t"$11"\t"0"\t"0;
		} else {
			alarmContent = "srchs_download_hourly: statfile "  FILENAME  " has invalid data at line "  FNR  "."
			print alarmContent > ERROR_TRACE_FILE
			exception=1
			exit 1
		}
	}
	END{
		if (exception == 0 && NR <= LINE_CRITERIA) {
			alarmContent = "srchs_download_hourly: statfile " FILENAME  " has no more than " LINE_CRITERIA  " lines."
			print alarmContent > ERROR_TRACE_FILE
			exit 1
		}
	}
	' $sourceFile > $targetFile

	return $?
} #processStatFile ends

function processClkStatFile()
{
	local sourceFile=$1
	local targetFile=$2

	awk '
	{
		if ( NF == 16  &&  $12 ~ /^[[:digit:]]+$/  &&  $12 > 0 )
		{
			print $1"\t"$2"\t"$3"\t"$4"\t"$5"\t"$6"\t"$7"\t"$8"\t"$9"\t"$10"\t"0"\t"$12"\t"$13;
		} 
	}
	' $sourceFile > $targetFile
	
	return $?
} #processStatFile ends

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
		listFileName=${LIST_FILE_PRE}${scanTime}
		localDestPath=${LOCAL_DEST}
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
					SendMailAndMessage "srchs_download_hourly: srchs data of $scanTime has been re-downloaded successfully." 1
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
