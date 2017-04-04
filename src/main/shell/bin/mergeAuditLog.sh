#!/bin/sh

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/mergeAuditLog.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${LOG_PATH}
mkdir -p ${AUDIT_LOG_PATH}
mkdir -p ${AUDIT_LOG_TMP_PATH}

function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $LOG_FILE
}

CURR_DATETIME=`date +%F\ %T`
PRINT_LOG "running at "$CURR_DATETIME 

#clean tmp log
rm -rf ${AUDIT_LOG_TMP_PATH}/*

#go to workspace
cd ${AUDIT_LOG_PATH}

#param check 
#no param means use yestoday
if [ $# -eq 0 ]; then
	DAT=`date -d 'yesterday' +%Y%m%d`
#1 param support given date
elif [ $# -eq 1 ]; then
	if [[ $1 =~ "^[0-9]{8}$" ]]; then
		DAT=$1;
	else
		PRINT_LOG "******illegal param for date, please input yyyymmdd!!******"
		alert 1 "date is invalid for "$1
		exit 1
	fi
#other means error
else
	PRINT_LOG "******illegal param num!! exit******"
	alert 1 "parameter is invalid!!!"
	exit 1
fi

audit_file=${AUDIT_LOG_FILE}.${DAT}

for((idx=0;idx<${MACHINE_NUM};idx++))
do
	wget -t 3 --limit-rate=10M ftp://${AUDIT_MACHINE_IP[$idx]}${AUDIT_LOG_REMOTE_PATH}/${DAT}/${audit_file} -O ${AUDIT_LOG_TMP_PATH}/${audit_file}.${idx}
	if [ $? -ne 0 ]; then
		PRINT_LOG "******get remote audit log failed!!!******"
		alert 1 "get remote audit log failed from "${AUDIT_MACHINE_IP[$idx]}
		exit 1
	fi
done
cat ${AUDIT_LOG_TMP_PATH}/${audit_file}.* > ${audit_file}
md5sum ${audit_file} > ${audit_file}".md5"
	
CURR_DATETIME=`date +%F\ %T`
echo "[INFO]end to merge audit log "$CURR_DATETIME >> ${LOG_FILE}
