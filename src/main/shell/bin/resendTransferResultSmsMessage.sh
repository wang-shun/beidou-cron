#!/bin/sh
# 
# code migration
# @author liangshimu
# @version 1.0.0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

LOG_FILE=${LOG_PATH}/resendTransferResultSmsMessage.log

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=resendTransferResultSmsMessage.sh
reader_list=liangshimu

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

msg="执行补发转账短信失败"
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.ResendTransferResult  &> ${LOG_FILE}
alert $? "${msg}"
