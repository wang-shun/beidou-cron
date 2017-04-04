#!/bin/sh
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=beidouAuditTaskPatrol.sh
reader_list=wangxiongjie
datestr=`date +%Y%m%d`
LOG_FILE=${LOG_PATH}/beidouAuditTaskPatrol_${datestr}.log

java -Xms1024m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.AuditUnitPatrol 180 >> ${LOG_FILE} 2>&1