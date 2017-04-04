#!/bin/bash

#@file: mfc_import.sh
#@author: yanjie
#@date: 2009-12-20
#@version: 1.0.0.0
#@brief: get balance/invest from mfc and update DB

DEBUG_MOD=0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=mfc_import.sh
reader_list=zhangpeng

CONF_SH="../conf/mfc_import.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="./beidou_lib.sh"

source $LIB_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load libfile[$LIB_FILE]!"
	exit 1
fi

function check_path()
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

	if ! [ -w $LOCAL_BACK ]
	then
		if ! [ -e $LOCAL_BACK ]
		then
			mkdir $LOCAL_BACK
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
	
	mkdir -p $BALANCE_PATH
	
	return 0	
}

function mfc_import()
{
	open_log
	
	check_path
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	timeNow=`date +%Y%m%d%H%M`
	
	returnFlag=0
	
	wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/$IMPORT_FILE -P $LOCAL_TEMP
	if [ $? -ne 0 ]
	then
		log "FATAL" "Fail to download [$IMPORT_FILE]."
		SendMail "mfc: Fail to download [$IMPORT_FILE]." "${MAILLIST}"
		SendMessage "mfc: Fail to download [$IMPORT_FILE]." "${MOBILELIST}"
		returnFlag=1
	else
		wget -t $MAX_RETRY -q ftp://$SERVER_USER:$SERVER_PWD@$SERVER_URL/$SERVER_ROOT/${IMPORT_FILE}.md5 -P $LOCAL_TEMP
		cd $LOCAL_TEMP
		md5sum -c ${IMPORT_FILE}.md5 > /dev/null
		if [ $? -ne 0 ]
		then
			log "FATAL" "Fail to check md5 for [$IMPORT_FILE]."
			SendMail "mfc: Fail to check md5 for [$IMPORT_FILE]." "${MAILLIST}"
			SendMessage "mfc: Fail to download [$IMPORT_FILE]." "${MOBILELIST}"
			returnFlag=1
		else
			if [ -s $IMPORT_FILE ]
			then
				#filter
				awk '{print $1"\t"$11"\t"$10}' $IMPORT_FILE > $IMPORT_FILE.filter
				log "TRACE" "filter end & import begin"
				
				retryCount=0
				sucFlag=0
				while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
				do
					retryCount=$(($retryCount+1))
					runsql_xdb "use beidouext;create table userbalance_tmp like userbalance;
					load data local infile '$LOCAL_TEMP/$IMPORT_FILE.filter' into table userbalance_tmp;
					drop table userbalance; rename table userbalance_tmp to userbalance;
					"
					if [ $? -eq 0 ]
					then
						sucFlag=1
					else
						sleep 0.5
					fi	
				done
				log "TRACE" "import end"
				if [ $sucFlag -eq 0 ]
				then
					log "FATAL" "Import error."
					SendMail "mfc: Import Error." "${MAILLIST}"
					SendMessage "mfc: Import Error." "${MOBILELIST}"
					returnFlag=1
				fi
			else
				log "WARNING" "[$IMPORT_FILE] is empty."
				SendMail "[$IMPORT_FILE] is empty." "${MAILLIST}"
				returnFlag=1
			fi
			
			mv $LOCAL_TEMP/$IMPORT_FILE $LOCAL_BACK/$IMPORT_FILE.$timeNow
			cd $LOCAL_BACK
			md5sum $IMPORT_FILE.$timeNow > $IMPORT_FILE.$timeNow.md5
			today=`date +%Y%m%d`
			mv $LOCAL_TEMP/$IMPORT_FILE.filter $BALANCE_PATH/$BALANCE_FILE.$today
			cd $BALANCE_PATH
			md5sum $BALANCE_FILE.$today > $BALANCE_FILE.$today.md5
			
			
		fi
	fi

	cd $ROOT_PATH
	rm $LOCAL_TEMP/*
	close_log $returnFlag
	
	return $returnFlag
}

mfc_import

exit $?
