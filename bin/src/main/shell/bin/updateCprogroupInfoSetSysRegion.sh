#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=updateCprogroupInfoSetSysRegion.sh
reader_list=wangyu45

LOG_FILE=${LOG_PATH}/updateCprogroupInfoSetSysRegion.log
MAP_FILE=${CONF_PATH}/zonemap.conf

java -Xms4096m -Xmx8192m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprogroup.UpdateCprogroupInfoSetSysRegion ${MAP_FILE} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf