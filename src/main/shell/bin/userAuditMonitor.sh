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
alert $? "建立日志目录${LOG_PATH}失败"
mkdir -p ${MONITOR_DATA_PATH}
alert $? "建立数据目录${MONITOR_DATA_PATH}失败"

cd ${BIN_PATH}
alert $? "进入工作目录${BIN_PATH}失败"

java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.ImportAuditMonitor -m ${MONITOR_REASON} -o${MONITOR_INFO} >> ${LOG_FILE} 2>&1
alert $? "更新销售管理员的综述信息发生异常"

cd ${MONITOR_DATA_PATH}
sort -u ${MONITOR_INFO} > ${MONITOR_INFO}.u
alert $? "sort -u ${MONITOR_INFO}失败"
mv ${MONITOR_INFO}.u ${MONITOR_INFO}
alert $? "mv ${MONITOR_INFO}.u ${MONITOR_INFO} 失败"


runsql_xdb "use beidouext;create table userauditmonitor_tmp like beidouext.userauditmonitor;
					load data local infile '${MONITOR_INFO}' into table beidouext.userauditmonitor_tmp;
					drop table beidouext.userauditmonitor; rename table userauditmonitor_tmp to beidouext.userauditmonitor;
					"
alert $? "更新数据库userauditmonitor发生异常"
