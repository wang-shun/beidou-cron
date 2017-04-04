#!/bin/sh

#@file:importAotDB.sh
#@author:yangyun
#@date:2010-12-06
#@version:1.0.0.0
#@brief:

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAotDB.sh

LOG_PATH=${LOG_PATH}/aot
LOG_FILE=${LOG_PATH}/importAotDB.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

#首先处理tmp表
msg="drop planstat的临时表失败"
runsql_xdb "use aot;drop table if exists cproplanstat_tmp;"
alert $? "${msg}"

msg="drop groupstat的临时表失败"
runsql_xdb "use aot;drop table if exists cprogroupstat_tmp;"
alert $? "${msg}"

msg="建立planstat的临时表失败"
runsql_xdb "use aot; create table cproplanstat_tmp like cproplanstat;"
alert $? "${msg}"

msg="建立groupstat的临时表失败"
runsql_xdb "use aot; create table cprogroupstat_tmp like cprogroupstat;"
alert $? "${msg}"

msg="账户优化导入数据库数据失败"
java -Xms6144m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.aot.ImportAotDB >> ${LOG_FILE} 2>&1
alert $? ${msg}

