#!/bin/sh
#filename: push_online_unit_info.sh
#@auther:  genglei
#@fuction: wget unit info(contain url) from ubmc, and then push unit data into hadoop-fs
#@date:    2015-06-23
#@version: 1.0.0

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/online_unit_info.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=push_online_unit_info.sh
reader_list=genglei01

mkdir -p ${ONLINE_UNIT_INFO_PATH}
mkdir -p ${UNIT_OUTPUT_PATH}
mkdir -p ${UNIT_INPUT_PATH}
mkdir -p ${UNIT_LOG_PATH}

TODAY_DATE=`date +%Y%m%d`

LOG_NAME=push_online_unit_info
LOG_INFO_FILE=${UNIT_LOG_PATH}/${LOG_NAME}_${TODAY_DATE}.log

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $LOG_INFO_FILE
}

PRINT_LOG "begin to wget all unit info from ubmc"


#download file for data md5

SOURCE_FILE_NAME_MD5=${SOURCE_FILE_PREFIX}.${TODAY_DATE}.md5
DOWN_SUCCESS=0
while ((1>0)); do
	CUR_HOUR=`date +%H`
	if [ ${CUR_HOUR} -ge ${KILL_TIME} ]; then
		msg="[ERROR]download unit file md5 for beidou-unit-patrol {"$TODAY_DATE"} failed"
		PRINT_LOG "$msg"
		alert 1 "$msg"
	fi

	for (( i=0; i<$RETRY_NUM; i++)){
		wget ${SOURCE_FTP_PATH}/${SOURCE_FILE_NAME_MD5} -nd -nH  --limit-rate=30M -O ${UNIT_INPUT_PATH}/${SOURCE_FILE_NAME_MD5}
		if [ $? -eq 0 ]; then
			DOWN_SUCCESS=1
			break
		fi
	}
	if [ $DOWN_SUCCESS -eq 1 ]; then
		PRINT_LOG "download unit file md5 for beidou-unit-patrol {"$TODAY_DATE"} success"
		break
	fi
	
	sleep ${SLEEP_TIME}
done

#download file for data
SOURCE_FILE_NAME=${SOURCE_FILE_PREFIX}.${TODAY_DATE}
DOWN_SUCCESS=0
for (( i=0; i<$RETRY_NUM; i++)){
	wget ${SOURCE_FTP_PATH}/${SOURCE_FILE_NAME} -nd -nH  --limit-rate=30M -O ${UNIT_INPUT_PATH}/${SOURCE_FILE_NAME}
	if [ $? -eq 0 ]; then
		DOWN_SUCCESS=1
		break
	fi
}

if [ $DOWN_SUCCESS -eq 1 ]; then
	PRINT_LOG "download unit file for beidou-unit-patrol {"$TODAY_DATE"} success"
else
	msg="[ERROR]download unit file for beidou-unit-patrol {"$TODAY_DATE"} failed"
	PRINT_LOG "$msg"
	alert 1 "$msg"
fi


#check md5
cd ${UNIT_INPUT_PATH}
md5sum -c ${SOURCE_FILE_NAME}.md5 > /dev/null
if [ $? -ne 0 ]; then
	msg="check md5 for date "$TODAY_DATE" failed"
	PRINT_LOG "$msg"
	alert 1 "$msg"
fi

PRINT_LOG "end to get all unit info file(contain url) from ubmc"

cd ${BIN_PATH}
${HADOOP_CLIENT} jar ../lib/beidou-unit-patrol*.jar com.baidu.beidou.audit.beidou.patrol.UnitInfoPatrol "text" ${TEXT_UNIT_INFO_OUTPUT_FILE}.${TODAY_DATE} \
	${HADOOP_TEXT_TARGET_PATH}/${TODAY_DATE} ${MAX_THREAD_NUM} >> ${LOG_INFO_FILE} 2>&1
if [ $? -ne 0 ]; then
	msg="[ERROR]push text unit info into hdfs {"$TODAY_DATE"} failed"
	PRINT_LOG "$msg"
	alert 1 "$msg"
fi

${HADOOP_CLIENT} jar ../lib/beidou-unit-patrol*.jar com.baidu.beidou.audit.beidou.patrol.UnitInfoPatrol "image" ${UNIT_INPUT_PATH}/${SOURCE_FILE_NAME} \
	${HADOOP_IMAGE_TARGET_PATH}/${TODAY_DATE} ${MAX_THREAD_NUM} >> ${LOG_INFO_FILE} 2>&1
if [ $? -ne 0 ]; then
	msg="[ERROR]push image/flash/icon unit info into hdfs {"$TODAY_DATE"} failed"
	PRINT_LOG "$msg"
	alert 1 "$msg"
fi

PRINT_LOG "end to push unit info data into hadoop filesystem normally!"