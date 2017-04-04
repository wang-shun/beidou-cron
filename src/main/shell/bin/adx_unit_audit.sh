#!/bin/bash

#@file: adx_unit_audit.sh
#@author: lixukun
#@intention: call api to audit beidou units

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function usage() {
	echo "adx_unit_audit (experiment versions)"
	echo "Version : 1.0.0 (build 20140101)"
	echo "write by lixukun@baidu.com "
	echo "Copyright (C) 2014 Baidu.com "
	echo "USAGE: adx_unit_audit company [updateDate]" 
}

AUDIT_API_DAY=`date +"%Y-%m-%d" -d'-1 day'`

if [ $# -ge 2 ]; then
	AUDIT_API_DAY=$2
fi

program=adx_unit_audit.sh

LOG_NAME=adx_unit_audit

COMPANY=$1
UCOMPANY=$(echo $1|tr [a-z] [A-Z])
UPDATED_UNIT_FILE="${WORK_PATH}/${COMPANY}/updated_unit"
EXISTING_ADX_UNIT_FILE="${WORK_PATH}/${COMPANY}/existing_adx_unit"
ADX_UNIT_TO_AUDIT_FILE="${WORK_PATH}/${COMPANY}/adx_unit_to_audit"
ADX_UNIT_TO_ADD_FILE="${WORK_PATH}/${COMPANY}/adx_unit_to_add"

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

# 获取“改动过的创意”列表
function getUpdatedAdxUnit(){
	QUERY_SQL=${QUERY_SQL}"select s.uid, s.id, m.wuliaoType, m.mcId, m.mcVersionId, m.targetUrl "
	QUERY_SQL=${QUERY_SQL}"    from beidou.cprounitstate? s join beidou.cprounitmater? m on s.id=m.id "
	QUERY_SQL=${QUERY_SQL}"    where s.state=0 and s.chaTime>'${AUDIT_API_DAY}' order by id"
	
	#query db
	UPDATED_UNIT_FILE_UNSORTED=${UPDATED_UNIT_FILE}.unsorted
	
	rm -f ${UPDATED_UNIT_FILE_UNSORTED}
	rm -f ${UPDATED_UNIT_FILE}
	
	runsql_sharding_read "${QUERY_SQL}" "${UPDATED_UNIT_FILE_UNSORTED}" "${TAB_UNIT_SLICE}"
	
	if [ -s ${UPDATED_UNIT_FILE_UNSORTED} ]
	then
		cat ${UPDATED_UNIT_FILE_UNSORTED} | sort -k1n,1 | uniq  > ${UPDATED_UNIT_FILE}
	else
		touch ${UPDATED_UNIT_FILE}
	fi
}

# “改动过的创意”列表和已存在的创意对比，生成已存在的创意中改动的创意列表
# 将adx_unit_to_add加入
function generateAdxUnitToAuditFile() {
	rm -f ${ADX_UNIT_TO_AUDIT_FILE}
	
	if [ ! -s ${EXISTING_ADX_UNIT_FILE} ]
	then
		touch ${ADX_UNIT_TO_AUDIT_FILE}
	else
		awk 'ARGIND==1{map[$1]=$0}ARGIND==2{if(map[$2]"X"!="X"){print $0}}' ${EXISTING_ADX_UNIT_FILE} ${UPDATED_UNIT_FILE} > ${ADX_UNIT_TO_AUDIT_FILE}
	fi
	
	if [ -s ${ADX_UNIT_TO_ADD_FILE} ]
	then
		echo -e "\n" >> ${ADX_UNIT_TO_AUDIT_FILE}
		cat ${ADX_UNIT_TO_ADD_FILE} >> ${ADX_UNIT_TO_AUDIT_FILE}
	fi
}

open_log	
log "INFO" "adx_unit_audit ${COMPANY} start at `date +%F\ %T`"
getUpdatedAdxUnit
generateAdxUnitToAuditFile

#调用api, 对adx物料进行审核
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.AdxAuditService ${COMPANY} ${ADX_UNIT_TO_AUDIT_FILE} > ${LOG_PATH}/${LOG_FILE} 2>&1

log "INFO" "adx_unit_audit ${COMPANY} end at `date +%F\ %T`"
close_log 0
