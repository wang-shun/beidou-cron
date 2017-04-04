#!/bin/bash

#@file: get_audit_log.sh
#@author: genglei01
#@date: 2013-01-06
#@version: 1.0.0.0
#@brief: get audit log daily from audit-machine(tc-mgr00)
#@input format: none || ${yyyyMMdd}

CONF_SH="../conf/get_audit_log.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="./beidou_lib.sh"
[ -f "${LIB_FILE}" ] && source ${LIB_FILE} || echo "ERROR: not exist ${LIB_FILE}"

program=get_audit_log.sh
reader_list=genglei01

mkdir -p ${AUDIT_LOG_DEST_PATH}
mkdir -p ${AUDIT_LOG_TEMP_PATH}

function getYestAuditLog()
{
	if [ -z $1 ]
	then
		yestTime=`date -d "1 day ago" +%Y%m%d`
	else
		yestTime=$1
	fi
	
	tmpFile=${AUDIT_LOG_TEMP_PATH}/${AUDIT_LOG_NAME}
	rm -rf ${tmpFile}
	
	wget --limit-rate=${LIMIT_RATE} -t ${MAX_RETRY} ${AUDIT_LOG_URL}${AUDIT_LOG_PATH}/${AUDIT_LOG_NAME}.${yestTime} -O ${tmpFile}
	if [ $? -ne 0 ]
	then
		wget --limit-rate=${LIMIT_RATE} -t ${MAX_RETRY} ${AUDIT_LOG_URL}${AUDIT_LOG_PATH}/${AUDIT_LOG_NAME} -O ${tmpFile}
	fi
	
	if ! [ -f ${tmpFile} ]
	then
		log "ERROR" "Fail to download audit log!"
		SendMail "get_audit_log: Fail to download audit log." "${MAIL_LIST}"
		return 1
	fi
	
	cd ${AUDIT_LOG_DEST_PATH}
	destFile=${AUDIT_LOG_NAME}.${yestTime}
	cp ${tmpFile} ${destFile}
	md5sum ${destFile} > ${destFile}.md5
}

getYestAuditLog $1
exit $?
