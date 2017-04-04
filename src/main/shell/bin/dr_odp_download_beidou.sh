#!/bin/bash

#@file: dr_odp_download_beidou.sh
#@author: yanjie
#@date: 2009-04-08
#@version: 1.0.0.0
#@brief: download beidoustat data from cprostat server

DEBUG_MOD=0
WORK_PATH="/home/test/beidou-cron-1.1.3"

CONF_FILE="./conf/dr_odp_download_beidou.conf"
LIB_FILE="./lib/beidou_lib.sh"

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
			mkdir $LOCAL_TEMP
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

	if ! [ -w $LOCAL_DEST/normal ]
	then
		if ! [ -e $LOCAL_DEST/normal ]
		then
			mkdir $LOCAL_DEST/normal
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$LOCAL_DEST/normal]!"
				return 1
			fi
		else
			log "FATAL" "Path[$LOCAL_DEST/normal] is not writable!"
			return 1
		fi
	fi
	
	if ! [ -w $LOCAL_DEST/recovery ]
	then
		if ! [ -e $LOCAL_DEST/recovery ]
		then
			mkdir $LOCAL_DEST/recovery
			if [ $? -ne 0 ]
			then
				log "FATAL" "Fail to mkdir [$LOCAL_DEST/recovery]!"
				return 1
			fi
		else
			log "FATAL" "Path[$LOCAL_DEST/recovery] is not writable!"
			return 1
		fi
	fi
	
	return 0	
}

function check_conf()
{
	if ! [[ $SERVER_URL ]]
	then
		echo "Conf[SERVER_URL] is empty or its value is invalid"
		return 1
	fi
	
	if ! [[ $SERVER_USER ]]
	then
		echo "Conf[SERVER_USER] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $SERVER_PWD ]]
	then
		echo "Conf[SERVER_PWD] is empty or its value is invalid"
		return 1
	fi

	if ! [[ $SERVER_ROOT ]]
	then
		echo "Conf[SERVER_ROOT] is empty or its value is invalid"
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

	return 0	
}

#download stat data of one hour
#$1: YYYYmmddHH
function download_beidou_hour()
{
	curTime=$1
	curDate=${curTime:0:8}
	
	listFileName=filelist.$curTime.normal.0.0
	localDestPath=$LOCAL_DEST/normal/$curDate
	localListDestFile=$localDestPath/$listFileName
	localListTempFile=$LOCAL_TEMP/$listFileName
	remoteListFile=$SERVER_ROOT/normal/$curDate/$listFileName
	
	if [ -f $localListDestFile ]
	then
		log "WARNING" "File[$localListDestFile] exists already!"
		return 0
	fi
	
	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$remoteListFile -P $LOCAL_TEMP
	if [ $? -ne 0 ] || ! [ -f $localListTempFile ]
	then
		log "WARNING" "Fail to download listfile[$localListTempFile]!"
		SendMail "odp_beidou: Fail to download listfile [$listFileName]." "${MAIL_LIST}"
		SendMessage "odp_beidou: Fail to download listfile [$listFileName]." "${MOBILE_LIST}"
		return 1
	fi
	
	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$remoteListFile".md5" -P $LOCAL_TEMP
	if [ $? -ne 0 ] || ! [ -f $localListTempFile".md5" ]
	then
		log "WARNING" "Fail to download listfile md5[$localListTempFile.md5]!"
		SendMail "odp_beidou: Fail to download listfile md5[$listFileName.md5]." "${MAIL_LIST}"
		SendMessage "odp_beidou: Fail to download listfile md5[$listFileName.md5]." "${MOBILE_LIST}"
		return 1
	fi

	cd $LOCAL_TEMP
	md5sum -c $listFileName".md5" > /dev/null
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to check listfile md5[$localListTempFile.md5]!"
		SendMail "odp_beidou: Fail to check md5 for listfile [$listFileName]." "${MAIL_LIST}"
		SendMessage "odp_beidou: Fail to check md5 for listfile [$listFileName]." "${MOBILE_LIST}"
		return 1
	fi
	cd $WORK_PATH	
	
	while read line ; do
		statFileName=$line
		localStatTempFile=$LOCAL_TEMP/$statFileName
		remoteStatFile=$SERVER_ROOT/normal/$curDate/$statFileName
		
		wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$remoteStatFile -P $LOCAL_TEMP
		if [ $? -ne 0 ] || ! [ -f $localStatTempFile ]
		then
			log "FATAL" "Fail to download statfile[$localStatTempFile]!"
			SendMail "odp_beidou: Fail to download statfile [$statFileName]." "${MAIL_LIST}"
			SendMessage "odp_beidou: Fail to download statfile [$statFileName]." "${MOBILE_LIST}"
			return 1
		fi
		
		wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$remoteStatFile".md5" -P $LOCAL_TEMP
		if [ $? -ne 0 ] || ! [ -f $localStatTempFile".md5" ]
		then
			log "FATAL" "Fail to download statfile md5[$localStatTempFile.md5]!"
			SendMail "odp_beidou: Fail to download statfile md5 [$statFileName.md5]." "${MAIL_LIST}"
			SendMessage "odp_beidou: Fail to download statfile md5 [$statFileName.md5]." "${MOBILE_LIST}"
			return 1
		fi
	done < $localListTempFile
	
	if ! [ -d $localDestPath ]
	then
		mkdir $localDestPath
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to mkdir [$localDestPath]!"
			SendMail "odp_beidou: Fail to mkdir [$localDestPath]!" "${MAIL_LIST}"
			SendMessage "odp_beidou: Fail to mkdir [$localDestPath]!" "${MOBILE_LIST}"
			return 1
		fi
	
		log "TRACE" "Suc to mkdir [$localDestPath]."
	fi
	
	mv -f $LOCAL_TEMP/* $localDestPath/
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to mv [`ls $LOCAL_TEMP`] to [$localDestPath]."
		SendMail "odp_beidou: Fail to mv statfile[$curTime] to dest." "${MAIL_LIST}"
		SendMessage "odp_beidou: Fail to mv statfile[$curTime] to dest." "${MOBILE_LIST}"
		return 1
	else
		log "TRACE" "Suc to mv [`ls $LOCAL_TEMP`] to [$localDestPath]."
	fi
}

#download recovery stat data
function download_beidou_recovery()
{
	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/recovery/* -P $LOCAL_TEMP
	if [ $? -ne 0 ]
	then
		# maybe no remote recovery directory
		log "WARNING" "Fail to download, maybe no recovery data."
		return 0
	fi
	
	reclist=`ls $LOCAL_TEMP`
	mv -f $LOCAL_TEMP/* $LOCAL_DEST/recovery/
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to mv recovery data [$reclist] to [$LOCAL_DEST/recovery]!"
		SendMail "odp_beidou: Fail to mv recovery data to dest." "${MAIL_LIST}"
		SendMessage "odp_beidou: Fail to mv recovery data to dest." "${MOBILE_LIST}"
		return 1
	else
		log "TRACE" "Suc to mv recovery data[$reclist]."
	fi

	#delete remote file
	#lftp -c "open $SERVER_USER:$SERVER_PWD@$SERVER_URL; mrm $remoteRecoveryPath/*"
	#if [ $? -ne 0 ]
	#then
	#	log "WARNING" "Fail to remove remote recovery data!"
	#	return 1
	#fi
}

#main function
#$1: starting time, YYYYmmddHH
function download_beidou()
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
	
	timeStarting=$1
	if [ -z $timeStarting ] || [ $timeStarting -gt $timeFix ]
	then
		timeStarting=$timeFix
	fi
	
	#begin real work
	log "TRACE" "$0 start running"
	returnFlag=0
	
	#hour by hour
	scanTime=$timeFix
	numHour=$FIX_DELAY
	while [ $scanTime -ge $timeStarting ]
	do
		download_beidou_hour $scanTime
		if [ $? -ne 0 ]
		then
			returnFlag=1
		fi
		
		((numHour++))
		scanTime=`date -d "$numHour hour ago" +%Y%m%d%H`
	done
	
	#recovery
	download_beidou_recovery
	if [ $? -ne 0 ]
	then
		returnFlag=1
	fi
		
	close_log $returnFlag
	
	return $returnFlag
}

download_beidou $1
echo "ODP[beidou] stopped!"

exit $?