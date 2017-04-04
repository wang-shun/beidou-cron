#!/bin/sh

#@file:importAotWhiteList.sh
#@author:yangyun
#@date:2010-12-06
#@version:1.0.0.0
#@brief:import aot whitelist and blacklist to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/aotWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAotWhiteList.sh

LOG_FILE=${LOG_PATH}/importAotWhiteList.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"


CONF_SH=${WHITE_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${BLACK_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${WHITE_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${BLACK_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}


msg="wget文件${BLACK_FILE}失败"
wget -q  ${BLACK_URL}/${BLACK_FILE} -O ${BLACK_FILE}
alert $? "${msg}"

msg="wget文件${BLACK_FILE}.md5失败"
wget  -q ${BLACK_URL}/${BLACK_FILE}.md5 -O ${BLACK_FILE}.md5
alert $? "${msg}"

msg="${BLACK_FILE}文件的md5校验失败"
md5sum -c ${BLACK_FILE}.md5
alert $? "${msg}"


awk -F'	' -v black_type=${BLACK_TYPE} 'BEGIN{OFS="	"} $0 ~ /^[0-9]+$/ {print black_type,$1}' ${BLACK_FILE}>>${BLACK_FILE_NEW}
alert $? "awk 白名单失败"


runsql_cap "delete from beidoucap.whitelist where type=${BLACK_TYPE}" >> ${LOG_FILE} 2>&1
alert $? "删除数据库账户优化黑名单失败"

runsql_cap "load data local infile '${BLACK_FILE_NEW}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
alert $? "导入账户优化黑名单失败"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}

