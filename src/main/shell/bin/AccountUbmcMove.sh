#!/bin/sh
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=AccountUbmcMove.sh
reader_list=tianxin
datestr=`date +%Y%m%d`
LOG_FILE=${LOG_PATH}/AccountUbmcMove_${datestr}.log

#param propertiesFilePath
java -Xms1024m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy.Copy $1>> ${LOG_FILE}