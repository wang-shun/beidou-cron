#!/bin/bash

#@file: odp_srchs_click_verify.sh
#@author: wangchongjie
#@date: 2012-06-13
#@version: 1.0.0
#@brief: verify 24hour srchs and click data into doris


CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/odp_srchs_click_verify.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

YESTERDAY=`date -d yesterday +%Y%m%d`
mkdir -p ${TMP_FILE_PATH}

FAIL_COUNTER=0
MAX_FAIL_ATTEMPT=50
FAIL_ATTEMPT_SLEEPTIME=300


function alarm(){
#	status=$1
#	msg=$2
#	if [[ ${status} -ne 0 ]]
#    then
    	importDorisSemaphore ${DORIS_DATA_NOT_READY}
#   	alert ${status} ${msg}
#	fi
}

#下载点击的filelist的文件
#$1：小时戳
function download_click_filelist()
{
	fileTimeHour=$1
	dataTime=${fileTimeHour:0:8}
	fileListName=filelist.$fileTimeHour.normal.0.0
	remoteFile=${CLICK_SERVER_ROOT}/${dataTime}/$fileListName

	msg="下载${fileTimeHour}点击filelist文件失败"
	wget -t $MAX_RETRY -q --limit-rate=$LIMIT_RATE ftp://$CLICK_SERVER_URL/$remoteFile -O $TMP_FILE_PATH/$fileListName
	if [ $? -ne 0 ]
	then
		alarm
		return 1
	fi
	
	msg="下载${fileTimeHour}点击filelist文件md5失败"
	wget -t $MAX_RETRY -q --limit-rate=$LIMIT_RATE ftp://$CLICK_SERVER_URL/$remoteFile".md5" -O $TMP_FILE_PATH/$fileListName".md5"
	if [ $? -ne 0 ]
	then
		alarm
		return 1
	fi
		
	cd $TMP_FILE_PATH
	msg="点击${fileTimeHour}文件md5校验失败"
	md5sum -c $fileListName".md5" > /dev/null
	if [ $? -ne 0 ]
	then
		alarm
		return 1
	fi
}


#下载点击的filelist的文件
#$1：小时戳
function download_srchs_filelist()
{
	fileTimeHour=$1
	dataTime=${fileTimeHour:0:8}
	fileListName=filelist.$fileTimeHour.normal.0.0
	remoteFile=${SRCHS_SERVER_ROOT}/${dataTime}/$fileListName

	msg="下载${fileTimeHour}点击filelist文件失败"
	wget -t $MAX_RETRY -q --limit-rate=$LIMIT_RATE ftp://$SRCHS_SERVER_URL/$remoteFile -O $TMP_FILE_PATH/$fileListName
	if [ $? -ne 0 ]
	then
		alarm
		return 1
	fi
		
	msg="下载${fileTimeHour}点击filelist文件md5失败"
	wget -t $MAX_RETRY -q --limit-rate=$LIMIT_RATE ftp://$SRCHS_SERVER_URL/$remoteFile".md5" -O $TMP_FILE_PATH/$fileListName".md5"
	if [ $? -ne 0 ]
	then
		alarm
		return 1
	fi
	
	cd $TMP_FILE_PATH
	msg="点击${fileTimeHour}文件md5校验失败"
	md5sum -c $fileListName".md5" > /dev/null
	if [ $? -ne 0 ]
	then
		alarm
		return 1
	fi
}

function importDorisSemaphore(){
	DORIS_DATA_TAG=$1

	MSG="设置IS_DORIS_DATA_READY到beidoureport.sysnvtab中失败."
	SQL="use beidoureport; replace into sysnvtab(name, value) values ('IS_DORIS_DATA_READY', ${DORIS_DATA_TAG});"
	runsql_xdb "${SQL}"
	alert $? "${MSG}"
	
	MSG="设置DORIS_DATA_DATE到beidoureport.sysnvtab中失败."
	DATA_IMPORT_DATE=`date -d "-1 day" "+%Y-%m-%d"`
	SQL="use beidoureport; replace into sysnvtab(name, value) values ('DORIS_DATA_DATE', "\"${DATA_IMPORT_DATE}\"");"
	runsql_xdb "${SQL}"
	alert $? "${MSG}"
}

function main()
{
	cd $TMP_FILE_PATH
	rm filelist*
	
	FILE_EXIST_PIVOT=0
	while [ $FAIL_COUNTER -le $MAX_FAIL_ATTEMPT ]
	do	
		FILE_EXIST_PIVOT=0
		echo "retry times="$FAIL_COUNTER
		for (( i=0; i<24; i++ ))
		do
			if [[ ${#i} -eq 1 ]]
			then
			TIME_HOUR="0"${i}
			else
				TIME_HOUR=$i
			fi

			fileTimeHour=${YESTERDAY}${TIME_HOUR}
			download_click_filelist $fileTimeHour
			if [ $? -ne 0 ]
			then
				FILE_EXIST_PIVOT=1
			fi
			download_srchs_filelist $fileTimeHour
			if [ $? -ne 0 ]
			then
				FILE_EXIST_PIVOT=1
			fi
		done
		if [ $FILE_EXIST_PIVOT -ne 0 ]
		then
			sleep $FAIL_ATTEMPT_SLEEPTIME
		else
			importDorisSemaphore ${DORIS_DATA_READY}
			return 0
		fi
		((FAIL_COUNTER=FAIL_COUNTER+1))
	done
	
	if [ $FILE_EXIST_PIVOT -ne 0 ]
	then
		return 1
	fi
}

main
exit $?
