#!/bin/sh
#cleanAuditTask.sh
# 清理审核任务
#sh cleanAuditTask.sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/cleanAuditTask.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${LOG_PATH}
LOG=${LOG_PATH}/cleanAuditTask.log

function cleanOneTask()
{
	del_sql="delete from audit.one_task;"
	runsql_audit "${del_sql}"
}

function cleanBeidouTask()
{
    del_sql="delete from audit.beidou_task;"
    runsql_audit "${del_sql}"
}

function cleanMessageQueue()
{
    ${REDIS_CLI_BIN} ${REDIS_GROUP1_SERVER1_IP_PORT} DEL ${BEIDOU_AUDIT_KEY}
    ${REDIS_CLI_BIN} ${REDIS_GROUP1_SERVER1_IP_PORT} DEL ${ONE_AUDIT_KEY}

}

echo "clean one task" >> $LOG
cleanOneTask

echo "clean beidou task" >> $LOG
cleanBeidouTask


#echo "clean redis queue" >> $LOG
#cleanMessageQueue

echo "clean task finished" >> $LOG
