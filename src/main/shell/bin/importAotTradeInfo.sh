#!/bin/sh

#@file:importAotTradeInfo.sh
#@author:yangyun
#@date:2010-12-06
#@version:1.0.0.0
#@brief:import aot plan and group tradeinfo to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/aotTradeInfo.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAotTradeInfo.sh


LOG_FILE=${LOG_PATH}/importAotTradeInfo.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"


CONF_SH=${PLAN_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${GROUP_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${PLAN_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${GROUP_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#抓取文件并验证MD5
msg="wget文件${PLAN_FILE}失败"
wget -q  ${PLAN_URL}/${PLAN_FILE} -O ${PLAN_FILE}
alert $? "${msg}"

msg="wget文件${PLAN_FILE}.md5失败"
wget  -q ${PLAN_URL}/${PLAN_FILE}.md5 -O ${PLAN_FILE}.md5
alert $? "${msg}"

msg="${PLAN_FILE}文件的md5校验失败"
md5sum -c ${PLAN_FILE}.md5
alert $? "${msg}"

msg="wget文件${GROUP_FILE}失败"
wget -q  ${GROUP_URL}/${GROUP_FILE} -O ${GROUP_FILE}
alert $? "${msg}"

msg="wget文件${GROUP_FILE}.md5失败"
wget  -q ${GROUP_URL}/${GROUP_FILE}.md5 -O ${GROUP_FILE}.md5
alert $? "${msg}"

msg="${GROUP_FILE}文件的md5校验失败"
md5sum -c ${GROUP_FILE}.md5
alert $? "${msg}"


awk -F'	'  'BEGIN{OFS="	"}{print $1,$2,$3,$4/100000}' ${PLAN_FILE}>>${PLAN_FILE_NEW}
alert $? "awk推广计划行业信息失败"

awk -F'	'  'BEGIN{OFS="	"}{print $1,$2,$3,$4,$5/100000}' ${GROUP_FILE}>>${GROUP_FILE_NEW}
alert $? "awk推广组行业信息失败"


runsql_xdb "delete from aot.cproplantradeinfo">>${LOG_FILE} 2>&1
alert $? "删除数据库账户优化推广计划分行业信息失败"

runsql_xdb "load data local infile '${PLAN_FILE_NEW}' into table aot.cproplantradeinfo CHARACTER SET gbk FIELDS TERMINATED BY '\t' ENCLOSED BY '' LINES TERMINATED BY '\n'">>${LOG_FILE} 2>&1
alert $? "导入账户优化推广计划分行业信息失败"

runsql_xdb "delete from aot.cprogrouptradeinfo">>${LOG_FILE} 2>&1
alert $? "删除数据库账户优化推广组分行业信息失败"

runsql_xdb "use aot; set charset utf8; load data local infile '${GROUP_FILE_NEW}' into table aot.cprogrouptradeinfo CHARACTER SET gbk FIELDS TERMINATED BY '\t' ENCLOSED BY '' LINES TERMINATED BY '\n'">>${LOG_FILE} 2>&1
alert $? "导入账户优化推广组分行业信息失败"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}

