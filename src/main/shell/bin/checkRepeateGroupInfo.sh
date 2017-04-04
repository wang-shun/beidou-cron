#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=checkRepeateGroupInfo.sh
reader_list=zhangpeng

LOG_FILE=${LOG_PATH}/checkRepeateGroupInfo.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="cd ${BIN_PATH} failed."
cd ${BIN_PATH}
alert $? "${msg}"

msg="execute CheckRepeateGroupInfo failed."
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprogroup.CheckRepeateGroupInfo >> ${LOG_FILE}

# if the relt of "java" is wrong then send error message
alert $? "${msg}"
