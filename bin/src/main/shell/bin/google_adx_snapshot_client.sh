#!/bin/bash

#@file: google_adx_snapshot_client.sh
#@author: kanghongwei
#@intention: call snapshot service for admaker flash materials

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/google_adx_snapshot_client.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${SNAPSHOT_FILE_PATH}

SNAPSHOT_DAY=`date +"%Y-%m-%d" -d'-1 day'`

if [ $# -ge 1 ]; then
   SNAPSHOT_DAY=$1
fi

#调用截图服务，为admaker制作的flash截图，从而为google adx提供投放服务
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.service.google.GoogleAdxSnapshot ${SNAPSHOT_DAY} > ${LOG_PATH}/${LOG_FILE} 2>&1
