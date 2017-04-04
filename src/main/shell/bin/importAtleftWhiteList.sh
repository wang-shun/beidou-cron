#!/bin/sh

#@file:importAtleftWhiteList.sh
#@author:caichao
#@version:1.0.0.0
#@brief:import atleft whitelist to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importAtleftWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAtleftWhiteList.sh

TODAY=`date +%Y%m%d`
LOG_FILE=${LOG_PATH}/importAtleftWhiteList.log.${TODAY}

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
CONF_SH=${WHITE_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#抓取文件并验证MD5
msg="wget文件${WHITE_FILE}失败"
wget -q  ${WHITE_URL}/${WHITE_FILE} -O ${WHITE_FILE}
alert $? "${msg}"

msg="wget文件${WHITE_FILE}.md5失败"
wget  -q ${WHITE_URL}/${WHITE_FILE}.md5 -O ${WHITE_FILE}.md5
alert $? "${msg}"

msg="${WHITE_FILE}文件的md5校验失败"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

#去除空行后判断是否有数据
sed -i '/^$/d' ${WHITE_FILE}
num=`wc -l ${WHITE_FILE} | awk '{print $1}'`
if [ "${num}" = "0" ];then
	exit 0
fi

awk -F'	' -v white_type=${WHITE_TYPE} 'BEGIN{OFS="	"} $0 ~ /^[0-9]+$/ {print white_type,$1}' ${WHITE_FILE} > ${WHITE_FILE_NEW}
alert $? "awk 白名单失败"

runsql_cap "delete from beidoucap.whitelist where type=${WHITE_TYPE}" >> ${LOG_FILE} 2>&1
alert $? "删除数据库atleft白名单失败"

runsql_cap "load data local infile '${WHITE_FILE_NEW}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
alert $? "导入atleft白名单失败"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}

