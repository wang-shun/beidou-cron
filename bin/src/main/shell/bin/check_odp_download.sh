#!/bin/bash

#@file: check_odp_download.sh
#@author: wangchongjie
#@date: 2012-10-25
#@version: 1.0.0
#@brief: check odp_download data 

DEBUG_MOD=0

CONF_FILE="./conf/check_odp_download.conf"
LIB_FILE="./lib/beidou_lib.sh"

source $CONF_FILE
if [ $? -ne 0 ];then
	echo "Conf error: Fail to load conffile[$CONF_FILE]!" && exit 1
fi

source $LIB_FILE
if [ $? -ne 0 ];then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!" && exit 1
fi

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

#$1: starting time, YYYYmmddHH
#$2: ending time, YYYYmmddHH
function check_beidou()
{
	open_log

	timeFix=`date -d "$FIX_DELAY hour ago" +%Y%m%d%H` && \
	timeNow=`date +%Y%m%d%H`
	
	timeStarting=$1
	timeEnding=$2
	
	local isInManualOperation=1
	
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
	if [ $isInManualOperation -eq 1 ]
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
			check_beidou_hour $scanTime $alertType
			if [ $? -ne 0 ]
			then
				flag=1
			fi
			((numHour--))
			scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
		done
	fi
	
	# is NOT InManualOperation then check previous data files and download it 
	if [ $isInManualOperation -ne 1 ]
	then
		check_beidou_hour $timeFix 1
	fi	
		
	close_log $returnFlag
	return $returnFlag
}

function check_beidou_hour()
{
	curTime=$1
	alertType=$2
	curDate=${curTime:0:8}
	curFullTime=$1"0000"	
	
	formatCurDate=${curTime:0:4}"-"${curTime:4:2}"-"${curTime:6:2}
	lastDate=`date -d "$formatCurDate -1 days" +%Y%m%d`
	lastTime=$lastDate${curTime:8:2}
	lastFullTime=$lastDate"0000"
	
	localDestPath=${LOCAL_DEST}/normal/${curDate}
	cd $localDestPath
	
	statallFileName=${STAT_FILE_PRE}${curTime}${FILE_SUF}
	statliteFileName=${STATLITE_FILE_PRE}${curTime}${FILE_SUF}
	statItFileName=${ITSTAT_FILE_PRE}${curTime}${FILE_SUF}
	statDtFileName=${DTSTAT_FILE_PRE}${curTime}${FILE_SUF}
	statRegFileName=${STAT_REG_FILE_PRE}${curTime}${FILE_SUF}
	statQtFileName=${QTSTAT_FILE_PRE}${curTime}${FILE_SUF}
	statKtFileName=${KTSTAT_FILE_PRE}${curTime}${FILE_SUF}
	statTargettypeFileName=${TARGET_TYPE_FILE_PRE}${curTime}${FILE_SUF}
	statNewKtFileName=${KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	statPackFileName=${STAT_PACK_FILE_PRE}${curTime}${FILE_SUF}

	transPlanFileName=${TRANS_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	transGroupFileName=${TRANS_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	transAdFileName=${TRANS_AD_FILE_PRE}${curTime}${FILE_SUF}
	transSiteFileName=${TRANS_SITE_FILE_PRE}${curTime}${FILE_SUF}
	transUserFileName=${TRANS_USER_FILE_PRE}${curTime}${FILE_SUF}
	transTradeFileName=${TRANS_TRADE_FILE_PRE}${curTime}${FILE_SUF}
	transRegFileName=${TRANS_REG_FILE_PRE}${curTime}${FILE_SUF}
	transPackFileName=${TRANS_PACK_FILE_PRE}${curTime}${FILE_SUF}
	transKeywordFileName=${TRANS_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	transItFileName=${TRANS_IT_FILE_PRE}${curTime}${FILE_SUF}
	transDtFileName=${TRANS_DT_FILE_PRE}${curTime}${FILE_SUF}

	uvUserFileName=${UV_USER_FILE_PRE}${curTime}${FILE_SUF}
	uvSiteFileName=${UV_SITE_FILE_PRE}${curTime}${FILE_SUF}
	uvTradeFileName=${UV_TRADE_FILE_PRE}${curTime}${FILE_SUF}
	uvRegUserFileName=${UV_REG_USER_FILE_PRE}${curTime}${FILE_SUF}
	uvRegPlanFileName=${UV_REG_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	uvRegGroupFileName=${UV_REG_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	uvPlanFileName=${UV_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	uvPackFileName=${UV_PACK_FILE_PRE}${curTime}${FILE_SUF}
	uvKeywordFileName=${UV_KEYWORD_FILE_PRE}${curTime}${FILE_SUF}
	uvItFileName=${UV_IT_FILE_PRE}${curTime}${FILE_SUF}
	uvItUserFileName=${UV_IT_USER_FILE_PRE}${curTime}${FILE_SUF}
	uvItPlanFileName=${UV_IT_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	uvItGroupFileName=${UV_IT_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	uvGroupFileName=${UV_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	uvDtUserFileName=${UV_DT_USER_FILE_PRE}${curTime}${FILE_SUF}
	uvDtPlanFileName=${UV_DT_PLAN_FILE_PRE}${curTime}${FILE_SUF}
	uvDtGroupFileName=${UV_DT_GROUP_FILE_PRE}${curTime}${FILE_SUF}
	uvAdFileName=${UV_AD_FILE_PRE}${curTime}${FILE_SUF}
	holmesFileName=${HOLMES_FILE_PRE}${curTime}${FILE_SUF}
	
	lastStatallFileName=${STAT_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatliteFileName=${STATLITE_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatItFileName=${ITSTAT_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatDtFileName=${DTSTAT_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatRegFileName=${STAT_REG_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatQtFileName=${QTSTAT_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatKtFileName=${KTSTAT_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatTargettypeFileName=${TARGET_TYPE_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatNewKtFileName=${KEYWORD_FILE_PRE}${lastTime}${FILE_SUF}
	lastStatPackFileName=${STAT_PACK_FILE_PRE}${lastTime}${FILE_SUF}

	lastTransPlanFileName=${TRANS_PLAN_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransGroupFileName=${TRANS_GROUP_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransAdFileName=${TRANS_AD_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransSiteFileName=${TRANS_SITE_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransUserFileName=${TRANS_USER_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransTradeFileName=${TRANS_TRADE_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransRegFileName=${TRANS_REG_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransPackFileName=${TRANS_PACK_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransKeywordFileName=${TRANS_KEYWORD_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransItFileName=${TRANS_IT_FILE_PRE}${lastTime}${FILE_SUF}
	lastTransDtFileName=${TRANS_DT_FILE_PRE}${lastTime}${FILE_SUF}

	lastUvUserFileName=${UV_USER_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvSiteFileName=${UV_SITE_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvTradeFileName=${UV_TRADE_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvRegUserFileName=${UV_REG_USER_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvRegPlanFileName=${UV_REG_PLAN_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvRegGroupFileName=${UV_REG_GROUP_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvPlanFileName=${UV_PLAN_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvPackFileName=${UV_PACK_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvKeywordFileName=${UV_KEYWORD_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvItFileName=${UV_IT_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvItUserFileName=${UV_IT_USER_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvItPlanFileName=${UV_IT_PLAN_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvItGroupFileName=${UV_IT_GROUP_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvGroupFileName=${UV_GROUP_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvDtUserFileName=${UV_DT_USER_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvDtPlanFileName=${UV_DT_PLAN_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvDtGroupFileName=${UV_DT_GROUP_FILE_PRE}${lastTime}${FILE_SUF}
	lastUvAdFileName=${UV_AD_FILE_PRE}${lastTime}${FILE_SUF}
	lastHolmesFileName=${HOLMES_FILE_PRE}${lastTime}${FILE_SUF}
	
	statallFileSum=`awk 'BEGIN{sum=0}{sum+=$12;sum+=$11}END{print sum}' $statallFileName` 
#	statliteFileSum=`awk 'BEGIN{sum=0}{sum+=$7;sum+=$6}END{print sum}' $statliteFileName`
	statItFileSum=`awk 'BEGIN{sum=0}{sum+=$8;sum+=$7}END{print sum}' $statItFileName`
	statDtFileSum=`awk 'BEGIN{sum=0}{sum+=$7;sum+=$6}END{print sum}' $statDtFileName`
	statRegFileSum=`awk 'BEGIN{sum=0}{sum+=$8;sum+=$7}END{print sum}' $statRegFileName`
	
	##check sum consistency begin##
	if [ $statallFileSum -ne $statItFileSum ] \
	|| [ $statallFileSum -ne $statDtFileSum ] || [ $statallFileSum -ne $statRegFileSum ]
	then
		SendMailAndMessage "$SH_NAME: sum ${curTime} is not consistency: statall-($statallFileSum),IT-($statItFileSum),DT-($statDtFileSum),Reg=($statRegFileSum)." $alertType
	fi
	##check sum consistency end##
	
	##check file does not have blank begin##
	for i in `ls *${curTime}*`;
	do
		isBlank=`grep -P "\t\t" $i|head -1`;
		if ! [ -z "$isBlank" ]
		then
			SendMailAndMessage "$SH_NAME: $i has blank: $isBlank"
		fi
	done
	##check file does not have blank end##

	##check file size begin##
	check_file_size $curTime $statallFileName $lastStatallFileName $alertType
#	check_file_size $curTime $statliteFileName $lastStatliteFileName$alertType
	check_file_size $curTime $statItFileName $lastStatItFileName $alertType
	check_file_size $curTime $statDtFileName $lastStatDtFileName $alertType
	check_file_size $curTime $statRegFileName $lastStatRegFileName $alertType
	check_file_size $curTime $statNewKtFileName $lastStatNewKtFileName $alertType
	check_file_size $curTime $statPackFileName $lastStatPackFileName $alertType
	
	check_day_file_size $curTime $TRANS_PLAN_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_GROUP_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_SITE_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_USER_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_TRADE_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_REG_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_PACK_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_KEYWORD_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_IT_FILE_PRE $alertType
	check_day_file_size $curTime $TRANS_DT_FILE_PRE $alertType
	check_day_file_size $curTime $UV_USER_FILE_PRE $alertType
	check_day_file_size $curTime $UV_SITE_FILE_PRE $alertType
	check_day_file_size $curTime $UV_TRADE_FILE_PRE $alertType
	check_day_file_size $curTime $UV_REG_USER_FILE_PRE $alertType
	check_day_file_size $curTime $UV_REG_PLAN_FILE_PRE $alertType
	check_day_file_size $curTime $UV_REG_GROUP_FILE_PRE $alertType
	check_day_file_size $curTime $UV_PLAN_FILE_PRE $alertType
	check_day_file_size $curTime $UV_KEYWORD_FILE_PRE $alertType
	check_day_file_size $curTime $UV_PACK_FILE_PRE $alertType
	check_day_file_size $curTime $UV_IT_FILE_PRE $alertType
	check_day_file_size $curTime $UV_IT_PLAN_FILE_PRE $alertType
	check_day_file_size $curTime $UV_IT_GROUP_FILE_PRE $alertType
	check_day_file_size $curTime $UV_GROUP_FILE_PRE $alertType
	check_day_file_size $curTime $UV_DT_USER_FILE_PRE $alertType
	check_day_file_size $curTime $UV_DT_PLAN_FILE_PRE $alertType
	check_day_file_size $curTime $UV_DT_GROUP_FILE_PRE $alertType
	check_day_file_size $curTime $UV_AD_FILE_PRE $alertType
	check_day_file_size $curTime $HOLMES_FILE_PRE $alertType
	##check file size end##
}

function check_file_size()
{
	curTime=$1
	fileName=$2
	lastFileName=$3
	alertType=$4
	curDate=${curTime:0:8}
	curFullTime=$1"0000"	
	
	formatCurDate=${curTime:0:4}"-"${curTime:4:2}"-"${curTime:6:2}
	lastDate=`date -d "$formatCurDate -1 days" +%Y%m%d`
	lastFullTime=$lastDate"0000"
	
	localDestPath=${LOCAL_DEST}/normal/${curDate}
	cd $localDestPath
	curFileSize=`du -b $fileName|awk '{print $1}'` 
	
	localDestPath=${LOCAL_DEST}/normal/${lastDate}
	cd $localDestPath
	lastFileSize=`du -b $lastFileName|awk '{print $1}'` 
	
	if [ -z $lastFileSize ] || [ $lastFileSize -eq 0 ]
	then
		return
	fi
	
	sizeRange=`echo $(($curFileSize*10/$lastFileSize))` 
	if [ $sizeRange -lt 6 ] || [ $sizeRange -gt 15 ]
	then
		SendMailAndMessage "$SH_NAME: $fileName file size change too much." $alertType
	fi
}

function check_day_file_size()
{
	curTime=$1
	filePre=$2
	alertType=$3
	curDate=${curTime:0:8}
	curFullTime=$1"0000"	
	
	formatCurDate=${curTime:0:4}"-"${curTime:4:2}"-"${curTime:6:2}
	lastDate=`date -d "$formatCurDate -1 days" +%Y%m%d`
	lastFullTime=$lastDate"0000"
	
	curHour=`date +%H`
	if [ ${curHour} -lt 23 ] 
	then 
		return
	fi
	
	localDestPath=${LOCAL_DEST}/normal/${curDate}
	cd $localDestPath
	curFileSize=`du -b $filePre*${FILE_SUF}|awk '{sum+=$1} END{print sum}'` 
	
	localDestPath=${LOCAL_DEST}/normal/${lastDate}
	cd $localDestPath
	lastFileSize=`du -b $filePre*${FILE_SUF}|awk '{sum+=$1} END{print sum}'`  
	
	if [ -z $lastFileSize ] || [ $lastFileSize -eq 0 ]
	then
		return
	fi
	
	sizeRange=`echo $(($curFileSize*10/$lastFileSize))` 
	if [ $sizeRange -lt 6 ] || [ $sizeRange -gt 15 ]
	then
		SendMailAndMessage "$SH_NAME: $filePre file size change too much." $alertType
	fi
}

check_beidou $1 $2

exit $?
