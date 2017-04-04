#!/bin/sh

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

java -classpath ${CUR_CLASSPATH} com.baidu.beidou.util.cdndriver.service.impl.CdnDriverImpl $@


