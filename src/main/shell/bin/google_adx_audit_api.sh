#!/bin/bash

#@file: google_adx_audit_api.sh
#@author: kanghongwei
#@intention: call google api to audit beidou units

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

CONF_SH="../conf/google_adx_audit_api.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

GOOGLE_AUDIT_API_DAY=`date +"%Y-%m-%d" -d'-1 day'`

if [ $# -ge 1 ]; then
   GOOGLE_AUDIT_API_DAY=$1
fi

#调用google api, 对可投放google adx物料进行审核
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.service.google.GoogleAdxAuditApi ${GOOGLE_AUDIT_API_DAY} > ${LOG_PATH}/${LOG_FILE} 2>&1
