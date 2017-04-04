#!/bin/bash

#@file: sitekv_import_redis.sh
#@author: zhangxu
#@date: 2013-03-15
#@version: 1.0.0.0
#@brief: download site/mainsite kv data from cprostat server and import into redis
#@modify: zhangxu at 2013-03-15 for project cpweb575

DEBUG_MOD=0
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH=../conf/redis.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
program=sitekv_import_redis.sh
reader_list=zhangxu


WORK_PATH=${ROOT_PATH}
CONF_FILE="${WORK_PATH}/conf/sitekv_import.conf"
LIB_FILE="${WORK_PATH}/bin/beidou_lib.sh"

cd $WORK_PATH
if [ $? -ne 0 ]
then
	echo "Cannot cd work path($WORK_PATH)!"
else
	echo "$0 is working under Path[$WORK_PATH]!"
fi

source $CONF_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load conffile[$CONF_FILE]!"
	exit 1
fi
export LOG_NAME=sitekv_import_redis
LOCAL_TEMP=${DATA_PATH}/sitekv_redis/sitekv_temp
LOCAL_BACK=${DATA_PATH}/sitekv_redis/sitekv_back

#database
DB_URL=${BEIDOU_DB_IP_WRITE}
DB_PORT=${BEIDOU_DB_PORT_WRITE}
DB_NAME=beidoustat
DB_USER=${BEIDOU_DB_USER_WRITE}
DB_PWD=${BEIDOU_DB_PASSWORD_WRITE}
MAIL_LIST=${MAILLIST}
MOBILE_LIST=$MOBILELIST

source $LIB_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!"
	exit 1
fi

function check_all_path()
{
	if ! [ -w $LOCAL_TEMP ]
	then
		if ! [ -e $LOCAL_TEMP ]
		then
			mkdir -p $LOCAL_TEMP
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$LOCAL_TEMP]!"
				return 1
			fi
		else
			log "FATAL" "Path[$LOCAL_TEMP] is not writable!"
			return 1
		fi
	fi

	if ! [ -w $LOCAL_BACK ]
	then
		if ! [ -e $LOCAL_BACK ]
		then
			mkdir -p $LOCAL_BACK
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$LOCAL_BACK]!"
				return 1
			fi
		else
			log "FATAL" "Path[$LOCAL_BACK] is not writable!"
			return 1
		fi
	fi
	
	return 0	
}

function check_conf()
{
	if ! [[ $FILETYPE ]]
	then
		echo "Conf[FILETYPE] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $DATA_PREFIX ]]
	then
		echo "Conf[DATA_PREFIX] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $MD5_PREFIX ]]
	then
		echo "Conf[MD5_PREFIX] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $FIX_DELAY ]]
	then
		echo "Conf[FIX_DELAY] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $MAX_RETRY ]]
	then
		echo "Conf[MAX_RETRY] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $MAIL_LIST ]]
	then
		echo "Conf[MAIL_LIST] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $MOBILE_LIST ]]
	then
		echo "Conf[MOBILE_LIST] is empty or its value is invalid"
		return 1
	fi


	if ! [[ $AUTO_RECOVERABLE_HOURS_NO_ALERT  ]] ||  [ $AUTO_RECOVERABLE_HOURS_NO_ALERT -lt 0 ]
	then
		echo "Conf[AUTO_RECOVERABLE_HOURS_NO_ALERT] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $AUTO_RECOVERABLE_HOURS_WITH_ALERT  ]] ||  [ $AUTO_RECOVERABLE_HOURS_WITH_ALERT -lt 0 ]
	then
		echo "Conf[AUTO_RECOVERABLE_HOURS_WITH_ALERT] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $CHECK_DATA_IN_HOW_MANY_DAYS  ]] || [ $CHECK_DATA_IN_HOW_MANY_DAYS -lt 0 ]
	then
		echo "Conf[CHECK_DATA_IN_HOW_MANY_DAYS] is empty or its value is invalid"
		return 1
	fi

	return 0	
}

#import sitekv data into DB
#$1: sitekv file
function import_sitekv()
{

	retryCount=0
	sucFlag=0
	while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
	do
		retryCount=$(($retryCount+1))
        awk '{print "*4\r\n$4\r\nHSET\r\n$6\r\nsitekv\r\n$"length($1)"\r\n"$1"\r\n$"length($2)"\r\n"$2"\r"}' $1 | ${REDIS_CLI_BIN} ${REDIS_GROUP1_SERVER1_IP_PORT} --pipe && awk '{print "*4\r\n$4\r\nHSET\r\n$6\r\nsitekv\r\n$"length($1)"\r\n"$1"\r\n$"length($2)"\r\n"$2"\r"}' $1 | ${REDIS_CLI_BIN} ${REDIS_GROUP1_SERVER2_IP_PORT} --pipe
		if [ $? -eq 0 ]
		then
			sucFlag=1
		else
			sleep 5
		fi
	done
	
	if [ $sucFlag -eq 0 ]
	then
		return 1
	fi
	
	return 0
}

#download sitekv data of one hour
#$1: YYYYmmddHH
#$2: alertType[0:send both mail and message; 1:only send mail; 2:only send message; 3:no mail or message]
function sitekv_import_hour()
{
	curTime=$1
	curFullTime=$1"0000"
	curDate=${curTime:0:8}
	
	alertType=$2
	if [ -z $2 ]
	then
		alertType=0
	fi
	
	sitekvFileName=sitekv.$curTime.normal.0.0
	localSitekvTempFile=$LOCAL_TEMP/$sitekvFileName
	#remoteSitekvFile=$SERVER_ROOT/normal/$curDate/$sitekvFileName

	local localStatDestFile=$LOCAL_BACK/$sitekvFileName
	if [ -f $localStatDestFile ] && [ -f $localStatDestFile.md5 ]
	then 
	    log "INFO" "$localStatDestFile already exists,ignore to download!"
		return 0
	fi
	
	#added by wangchongjie to fix re-download problem
	rm -f $LOCAL_TEMP/*.normal.*
	
	#wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$remoteSitekvFile -P $LOCAL_TEMP
	wget -t $MAX_RETRY -q "${DATA_PREFIX}&date=${curFullTime}&item=${FILETYPE}" -O $localSitekvTempFile
	
	if [ $? -ne 0 ] || ! [ -f $localSitekvTempFile ]
	then
		log "FATAL" "Fail to download sitekvfile[$sitekvFileName]!"
		if [ $alertType -eq 0 ] || [ $alertType -eq 1 ]
		then
			SendMail "sitekv: Fail to download sitekvfile [$sitekvFileName]." "${MAIL_LIST}"
		fi
		if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
		then
			SendMessage "sitekv: Fail to download sitekvfile [$sitekvFileName]." "${MOBILE_LIST}"
		fi
		
		#如果alertType=1，那么返回0，以免被CT捕捉报警，by zhangpingan
		if [ $alertType -eq 1 ];then
		return 0
        else
        return 1
        fi
	fi
	
	#wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$remoteSitekvFile".md5" -P $LOCAL_TEMP
	
	wget -t $MAX_RETRY -q "${MD5_PREFIX}&date=${curFullTime}&item=${FILETYPE}" -O $localSitekvTempFile".md5.tmp"
	if [ $? -ne 0 ] || ! [ -f $localSitekvTempFile".md5.tmp" ]
	then
		log "FATAL" "Fail to download sitekvfile md5[$localSitekvTempFile.md5]!"
		if [ $alertType -eq 0 ] || [ $alertType -eq 1 ]
		then
			SendMail "sitekv: Fail to download md5 for sitekvfile [$sitekvFileName].md5" "${MAIL_LIST}"
		fi
		if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
		then
			SendMessage "sitekv: Fail to download md5 for sitekvfile [$sitekvFileName].md5" "${MOBILE_LIST}"
		fi
		
		#如果alertType=1，那么返回0，以免被CT捕捉报警，by zhangpingan
		if [ $alertType -eq 1 ];then
		return 0
        else
        return 1
        fi
		
	fi

	awk -vfname="$sitekvFileName" '{print $2 "  " fname}' $localSitekvTempFile".md5.tmp" > $localSitekvTempFile".md5"
	rm $localSitekvTempFile".md5.tmp"

	
	cd $LOCAL_TEMP
	md5sum -c $sitekvFileName".md5" > /dev/null
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to check sitekvfile md5[$localSitekvTempFile.md5]!"
		if [ $alertType -eq 0 ] || [ $alertType -eq 1 ]
		then
			SendMail "sitekv: Fail to check md5 for sitekvfile [$sitekvFileName]." "${MAIL_LIST}"
		fi
		if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
		then
			SendMessage "sitekv: Fail to check md5 for sitekvfile [$sitekvFileName]." "${MOBILE_LIST}"
		fi
		
		#如果alertType=1，那么返回0，以免被CT捕捉报警，by zhangpingan
		if [ $alertType -eq 1 ];then
		return 0
        else
        return 1
        fi
		
	fi
	cd $WORK_PATH
	
	import_sitekv $localSitekvTempFile
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to import sitekv data."
		if [ $alertType -eq 0 ] || [ $alertType -eq 1 ]
		then
			SendMail "sitekv: Fail to import sitekv data [$sitekvFileName]." "${MAIL_LIST}"
		fi
		if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
		then
			SendMessage "sitekv: Fail to import sitekv data [$sitekvFileName]." "${MOBILE_LIST}"
		fi
		
		#如果alertType=1，那么返回0，以免被CT捕捉报警，by zhangpingan
		if [ $alertType -eq 1 ];then
		return 0
        else
        return 1
        fi
		
	fi
	
	mv $LOCAL_TEMP/*.normal.* $LOCAL_BACK
	
	return 0
}

#download recovery sitekv data
function sitekv_import_recovery()
{
	#leave empty
	return 0
}

#main function
#$1: starting time, YYYYmmddHH, without format validation
function sitekv_import()
{
	check_conf
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	open_log

	check_all_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	timeFix=`date -d "$FIX_DELAY hour ago" +%Y%m%d%H` && \
	timeNow=`date +%Y%m%d%H`
	
	# no params means manual operation
	local isInManualOperation=1 
	
	timeStarting=$1
	timeEnding=$2

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
	
	#only send mail for auto-run 
	alertType=1
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
	while [ $scanTime -gt $timeEnding ]
	do
		((numHour++))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
	
	while [ $scanTime -ge $timeStarting ]
	do
		sitekv_import_hour $scanTime $alertType
		if [ $? -ne 0 ]
		then
			returnFlag=1
		fi
		
		((numHour++))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
	
	#recovery
	if [ $CHECK_DATA_IN_HOW_MANY_DAYS -gt 0 ] && [ $isInManualOperation -ne 1 ]
	then
		check_downloaded_data_before $timeStarting
	fi
	
	if [ $? -ne 0 ]
	then
		returnFlag=1
	fi
	
	rm $LOCAL_TEMP/* > /dev/null
	
	close_log $returnFlag
	
	return $returnFlag
}

#DESC: check downloaded stat data within 2 days (that day and the day before), before the time passed in.
#PARAMS:
#    $1: YYYYmmddHH
#NOTICE: $1 should never be after NOW.
function check_downloaded_data_before()
{
	local curTime=$1
	local numHour=0
	local timeEnding=`date +%Y%m%d%H`
	while [ $timeEnding -ge $curTime ]
	do
		((numHour++))
		timeEnding=`date -d "$numHour hour ago" +%Y%m%d%H`
	done

	local howManydaysAgo=0
	let "howManydaysAgo= $CHECK_DATA_IN_HOW_MANY_DAYS - 1"
	local timeStarting=`date -d "-$howManydaysAgo day" +%Y%m%d00`
	local ableToRetryCount=$(($AUTO_RECOVERABLE_HOURS_WITH_ALERT+$AUTO_RECOVERABLE_HOURS_NO_ALERT))
	local checkCounter=0
	local scanTime=$timeEnding
	while [ $scanTime -ge $timeStarting ]
	do
		((checkCounter++))
		
		#send both message and mail
		alertType=0
		#only send mail
		if [ $checkCounter -le $AUTO_RECOVERABLE_HOURS_NO_ALERT ]
		then
			alertType=1
		fi
		
		## check file of $scanTime complete or not
		local needToDownload=0
		local statFileName=sitekv.$scanTime.normal.0.0
		local localStatDestFile=$LOCAL_BACK/$statFileName
		if [ -f $localStatDestFile ]
		then
			needToDownload=0
		else
			needToDownload=1
		fi
		## check file of $scanTime complete or not ends
 
		if [ $needToDownload -eq 1  ] 
		then
			if [ $checkCounter -le $ableToRetryCount ]
			then
				## try re-download
				sitekv_import_hour $scanTime $alertType
				if [ $? -eq 0 ] #successful
				then
					log "TRACE" "data of $scanTime has been re-downloaded successfully."
					SendMail "sitekv: data of $scanTime has been re-downloaded successfully." "${MAIL_LIST}"
					if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
					then
						SendMessage "sitekv: data of $scanTime has been re-downloaded successfully." "${MOBILE_LIST}"
					fi
				fi
			else
				log "WARNING" "data of $scanTime needs manually re-downloading."
				SendMail "sitekv: data of $scanTime needs manually re-downloading." "${MAIL_LIST}"
				if [ $alertType -eq 0 ] || [ $alertType -eq 2 ]
				then
					SendMessage "sitekv: data of $scanTime needs manually re-downloading." "${MOBILE_LIST}"
				fi
			fi
		fi
		
		((numHour++))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
} # check_downloaded_data_before ends

sitekv_import $1 $2
if [ $? -ne 0 ]
then
    echo "$0 failed!"
    exit 1  
fi
echo "$0 finished!"
exit 0
