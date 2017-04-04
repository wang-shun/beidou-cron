#!/bin/bash

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/exportWirelessApp.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=exportWirelessApp.sh
reader_list=zhuxiaoling

LOG_FILE=${EXPORT_LOG_DIR}/exportWirelessApp.log
FULL_EXPORT_FILE_NAME=${EXPORT_FILE_NAME}.$(date +"%Y%m%d")

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

mkdir -p ${EXPORT_LOG_DIR}
mkdir -p ${EXPORT_DATA_DIR}
alert $? "make export beidoucode.app dir failed."

msg="cd ${EXPORT_DATA_DIR} failed"
cd ${EXPORT_DATA_DIR}
alert $? "${msg}"

CONF_SH=${EXPORT_FILE_NAME}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#in case of slave master delay
sleep 10

retryCount=0
sucFlag=0
while [[ $retryCount -lt ${MAX_RETRY} ]] && [[ $sucFlag -eq 0 ]]
do
	retryCount=$(($retryCount+1))
	runsql_cap_read "select sid, name, first_trade, second_trade from beidoucode.app;" "${EXPORT_FILE_NAME}"
	runsql_cap_read "select * from beidoucode.app;" "${FULL_EXPORT_FILE_NAME}.tmp"
	if [ $? -eq 0 ];then
		sucFlag=1
	else
		sleep 3
	fi
done

msg="export beidoucode.app failed."
if [ $sucFlag -eq 0 ];then
	echo "${msg}" >> ${LOG_FILE}
    alert 1 "${msg}"
fi

#make md5 file
md5sum ${EXPORT_FILE_NAME} > ${EXPORT_FILE_NAME}.md5
alert $? "md5sum ${EXPORT_FILE_NAME} failed."

iconv -f CP936 -t UTF-8 ${FULL_EXPORT_FILE_NAME}.tmp -o ${FULL_EXPORT_FILE_NAME}
rm -f ${FULL_EXPORT_FILE_NAME}.tmp
md5sum ${FULL_EXPORT_FILE_NAME} > ${FULL_EXPORT_FILE_NAME}.md5
alert $? "md5sum ${FULL_EXPORT_FILE_NAME} failed."

#delete full export file 7 days ago
rm -f ${EXPORT_FILE_NAME}.$(date +"%Y%m%d" -d " 7 days ago")
rm -f ${EXPORT_FILE_NAME}.$(date +"%Y%m%d" -d " 7 days ago").md5