#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=updateInterestIdToNew.sh
reader_list=hewei18

LOG_FILE=${LOG_PATH}/updateInterestIdToNew.log
EXP_USER_ID_FILE=${CONF_PATH}/interest_experiment_userid.conf
OLD_TO_NEW_MAP_FILE=${CONF_PATH}/interest_old_to_new_map.conf

java -Xms2048m -Xmx8192m -classpath ${CUR_CLASSPATH} com.baidu.beidou.code.InterestConvertMain ${EXP_USER_ID_FILE} ${OLD_TO_NEW_MAP_FILE} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf