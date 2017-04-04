#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LOG_FILE=${LOG_PATH}/DelInvalidDelInfo.log

mkdir -p ${LOG_PATH}

java -Xms1024m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.tool.DelInvalidDelInfo 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf