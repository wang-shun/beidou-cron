#!/bin/bash

#@file: tencent_audit_result.sh
#@author: caichao
#@intention: call tencent api to get audit result

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



#调用tencent api, 获取广告主审核结果
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.user.service.GetTencentUserAuditResultMgr > ${LOG_PATH}/get_tencent_audit_result.${curr_date}.log 2>&1 || alert $? "[Error]get_tencent_audit_result error"