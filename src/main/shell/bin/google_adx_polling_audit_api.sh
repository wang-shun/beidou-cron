#!/bin/bash

#@file: google_adx_polling_audit_api.sh
#@author: kanghongwei
#@intention: get google audit result,and update db

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

CONF_SH="../conf/google_adx_polling_audit_api.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

#调用google api, 轮训获取审核之后的结果
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.service.google.GoogleAdxPollingAuditApi > ${LOG_PATH}/${LOG_FILE} 2>&1
