#!/bin/bash

#@file: adx_user_import.sh
#@author: caichao
#@intention: common adx import script

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=adx_user_import.sh

WORK_PATH=${DATA_PATH}/adx_work/user
LOG_NAME=adx_user_import


mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${WORK_PATH}


#export变量，子进程可以获取
export WORK_PATH
export BIN_PATH

# 获取当前最新版本的
function getCurrentPlanIdWhiteListFile() {
	ls -1rt ${DSP_PATH} | grep "${PLANID_WHITELIST_FILE}" | tail -1
}

open_log
log "INFO" "adx_user_import start at `date +%F\ %T`"

curr_date=`date +"%Y-%m-%d"`
src_file=${DSP_PATH}/`getCurrentPlanIdWhiteListFile`
uid_file=${WORK_PATH}/whitelist_userid

cut -f 2 ${src_file}|sort -n|uniq > ${uid_file}

#提交审核
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.AdvertiserSubmiter ${uid_file} > ${LOG_PATH}/importUserAdxGate.${curr_date}.log 2>&1 || alert $? "[Error]import_user_adxgate" 

log "INFO" "adx_user_import end at `date +%F\ %T`"

close_log 0

