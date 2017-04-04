#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importInterest.sh
reader_list=hewei18

LOG_FILE=${LOG_PATH}/importInterest.log
INTEREST_FILE=${CONF_PATH}/interest.data
VALID_INTEREST_FILE=${CONF_PATH}/interest_valid.conf

java -Xms2048m -Xmx8192m -classpath ${CUR_CLASSPATH} com.baidu.beidou.code.InterestImportMain ${INTEREST_FILE} ${VALID_INTEREST_FILE} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf