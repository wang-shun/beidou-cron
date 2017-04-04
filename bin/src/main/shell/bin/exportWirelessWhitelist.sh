#!/bin/bash

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/exportWirelessWhitelist.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=exportWirelessWhitelist.sh
reader_list=zhuxiaoling

LOG_FILE=${EXPORT_LOG_DIR}/exportWirelessWhitelist.log

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

mkdir -p ${EXPORT_LOG_DIR}
mkdir -p ${EXPORT_DATA_DIR}
alert $? "make export beidoucap.whitelist dir failed."

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
	runsql_cap_read "select distinct id from beidoucap.whitelist where type='${WHITE_TYPE}';" "${EXPORT_FILE_NAME}"
	if [ $? -eq 0 ];then
		sucFlag=1
	else
		sleep 3
	fi
done

msg="export beidoucap.whitelist failed."
if [ $sucFlag -eq 0 ];then
	echo "${msg}" >> ${LOG_FILE}
    alert 1 "${msg}"
fi

#make md5 file
md5sum ${EXPORT_FILE_NAME} > ${EXPORT_FILE_NAME}.md5
alert $? "md5sum ${EXPORT_FILE_NAME} failed."
