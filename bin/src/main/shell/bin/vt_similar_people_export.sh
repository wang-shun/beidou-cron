#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LOG_FILE=${LOG_PATH}/vt_similar_people_export.log
PARAM_FILE=${CONF_PATH}/vt_similar_people_export.conf

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="cd {BIN_PATH} failed"
cd ${BIN_PATH}
alert $? ${msg}

java -classpath ${CUR_CLASSPATH} com.baidu.unbiz.mysqlexport.Export ${PARAM_FILE}>> ${LOG_FILE}

exit 0