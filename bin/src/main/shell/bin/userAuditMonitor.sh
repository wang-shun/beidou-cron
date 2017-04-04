#!/bin/sh
# This program is used to import user audit monitor info.

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=userAuditMonitor.sh
reader_list=zengyunfeng

LOG_FILE=${LOG_PATH}/userAuditMonitor.log.`date +%Y%m%d`
MONITOR_DATA_PATH=${DATA_PATH}/userAuditMonitor/
MONITOR_REASON=${CONF_PATH}/"audit_monitor_reason.conf";
MONITOR_INFO=${MONITOR_DATA_PATH}/"userauditmonitor.txt"

mkdir -p ${LOG_PATH}
alert $? "������־Ŀ¼${LOG_PATH}ʧ��"
mkdir -p ${MONITOR_DATA_PATH}
alert $? "��������Ŀ¼${MONITOR_DATA_PATH}ʧ��"

cd ${BIN_PATH}
alert $? "���빤��Ŀ¼${BIN_PATH}ʧ��"

java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.ImportAuditMonitor -m ${MONITOR_REASON} -o${MONITOR_INFO} >> ${LOG_FILE} 2>&1
alert $? "�������۹���Ա��������Ϣ�����쳣"

cd ${MONITOR_DATA_PATH}
sort -u ${MONITOR_INFO} > ${MONITOR_INFO}.u
alert $? "sort -u ${MONITOR_INFO}ʧ��"
mv ${MONITOR_INFO}.u ${MONITOR_INFO}
alert $? "mv ${MONITOR_INFO}.u ${MONITOR_INFO} ʧ��"


runsql_xdb "use beidouext;create table userauditmonitor_tmp like beidouext.userauditmonitor;
					load data local infile '${MONITOR_INFO}' into table beidouext.userauditmonitor_tmp;
					drop table beidouext.userauditmonitor; rename table userauditmonitor_tmp to beidouext.userauditmonitor;
					"
alert $? "�������ݿ�userauditmonitor�����쳣"
