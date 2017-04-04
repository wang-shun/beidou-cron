#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

TIME_YYYYMMDD=`date -d 'yesterday' +%Y%m%d`
LOG_FILE=${LOG_PATH}/transferFundPerMargin.${TIME_YYYYMMDD}.log

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=transferFundPerMargin.sh
reader_list=zhangpingan

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/mfc_import.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

########################

#gen file
msg="beidou-cron/transferFundPerMargin error"
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.TransferFundPerMargin >> ${LOG_FILE}
alert $? "${msg}"
