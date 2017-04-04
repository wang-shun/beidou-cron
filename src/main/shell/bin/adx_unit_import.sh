#!/bin/bash

#@file: adx_unit_import.sh
#@author: lixukun
#@intention: common adx import script

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

# 获取当前最新版本的
function getCurrentPlanIdWhiteListFile() {
	ls -1rt ${DSP_PATH} | grep "${PLANID_WHITELIST_FILE}" | tail -1
}

function usage() {
	echo "adx_unit_import (experiment versions)"
	echo "Version : 1.0.0 (build 20140101)"
	echo "write by lixukun@baidu.com "
	echo "Copyright (C) 2013 Baidu.com "
	echo "USAGE: adx_unit_import [company]" 
}

if [ $# -lt 1 ];then
	usage
	exit 1
fi

program=adx_unit_import.sh

LOG_NAME=adx_unit_import
COMPANY=$1
UCOMPANY=$(echo $1|tr [a-z] [A-Z])

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${WORK_PATH}
mkdir -p ${DSP_PATH}
eval mkdir -p \${${UCOMPANY}_WORK_PATH}

#export变量，子进程可以获取
export COMPANY
export WORK_PATH
export DSP_PATH
export BIN_PATH
eval CURRENT_BITTAG=\${${UCOMPANY}_BITTAG}
eval export ${UCOMPANY}_BITTAG

open_log	
log "INFO" "adx_unit_import ${COMPANY} start at `date +%F\ %T`"

curr_date=`date +"%Y-%m-%d"`
src_file_name=`getCurrentPlanIdWhiteListFile`
src_file=${DSP_PATH}/${src_file_name}
publish_planid_file="publish_planId_whitelist"

if [ ! -f "${src_file}" ] 
then
echo "${src_file} is not exist."
fi 

java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.GenerateMaterFile ${COMPANY} ${src_file} > ${LOG_PATH}/generateMater_${COMPANY}.${curr_date}.log 2>&1 || alert $? "[Error]GenerateMater_${COMPANY}" 
log "INFO" "adx_unit_import ${COMPANY} generate mater finish at `date +%F\ %T`"
if [ "${COMPANY}" != "publish" ]
then
java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.CrossCheckAdxUnit ${COMPANY} > ${LOG_PATH}/crosscheck_${COMPANY}.${curr_date}.log 2>&1 || alert $? "[Error]CrossCheck_${COMPANY}" 
log "INFO" "adx_unit_import ${COMPANY} cross check finish at `date +%F\ %T`"
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.AdxDataPreparation ${COMPANY} > ${LOG_PATH}/dataprepare_${COMPANY}.${curr_date}.log 2>&1 || alert $? "[Error]DataPrepare_${COMPANY}" 
log "INFO" "adx_unit_import ${COMPANY} data prepare finish at `date +%F\ %T`"
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.DBOperator ${COMPANY} > ${LOG_PATH}/dboperator_${COMPANY}.${curr_date}.log 2>&1 || alert $? "[Error]DBOperator_${COMPANY}" 
fi

# 处理完物料后，发布planid文件，保持planid文件和物料文件的一致，这个文件发布给检索端使用（重要）
cd ${DSP_PATH}
cp ${src_file_name} ${publish_planid_file}
md5sum ${publish_planid_file} > ${publish_planid_file}.md5

log "INFO" "adx_unit_import ${COMPANY} end at `date +%F\ %T`"
close_log 0

