#!/bin/bash

#@file: sitekv_init_redis.sh
#@author: zhangxu
#@date: 2013-03-15
#@version: 1.0.0.0
#@brief: select * from sitekv and import into redis

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH=../conf/redis.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
program=sitekv_init_redis.sh
reader_list=zhangxu


WORK_PATH=${ROOT_PATH}
CONF_FILE="${WORK_PATH}/conf/sitekv_import.conf"
LIB_FILE="${WORK_PATH}/bin/beidou_lib.sh"

LOG=${WORK_PATH}/log/sitekv_init_redis.log
SITEKV_MYSQL2REDIS_COMMAND=${WORK_PATH}/data/sitekv_init_redis.cmd
SQL_FILE=${WORK_PATH}/bin/mysql2redis.sql

echo "Export sitekv data from MySQL" > ${LOG}
echo `date` >> ${LOG} && /home/work/beidou/mysql/bin/mysql -h10.46.208.58 -ubeidoudb -pcAnghAiYisHeNgxiAo -P3306 --skip-column-names --raw < ${SQL_FILE} > ${SITEKV_MYSQL2REDIS_COMMAND} &&  echo `date` >> ${LOG}


echo "Import sitekv data to redis" >> ${LOG}
echo `date` >> ${LOG} && cat ${SITEKV_MYSQL2REDIS_COMMAND} | ${REDIS_CLI_BIN} ${REDIS_GROUP1_SERVER1_IP_PORT} --pipe >> ${LOG}  &&  echo `date` >> ${LOG}
alert $? "Import into redis ${REDIS_GROUP1_SERVER1_IP_PORT} error"

echo `date` >> ${LOG} && cat ${SITEKV_MYSQL2REDIS_COMMAND} | ${REDIS_CLI_BIN} ${REDIS_GROUP1_SERVER2_IP_PORT} --pipe >> ${LOG}  &&  echo `date` >> ${LOG}
alert $? "Import into redis ${REDIS_GROUP1_SERVER2_IP_PORT} error"

