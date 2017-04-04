#!/bin/bash

#@file: adx_unit_crosscheck.sh
#@author: lixukun
#@intention: common adx cross check script, invoke by java process

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

COMPANY=$1
UCOMPANY=$(echo $1|tr [a-z] [A-Z])
WHITELIST_UNIT_FILE=$2
ADX_UNIT_TO_ADD_FILE=$3
ADX_UNIT_TO_DEL_FILE=$4
EXISTING_ADX_UNIT_FILE="${WORK_PATH}/${COMPANY}/existing_adx_unit"

eval CURRENT_BITTAG=\${${UCOMPANY}_BITTAG}

# 获取“已经存在的的创意”，用于交叉对比
function getExistingAdxUnit(){
	QUERY_EXISTING_ADX_UNIT_SQL="select adid, userid from beidou.cprounitadx? where [userid] and (adx_type & ${CURRENT_BITTAG})>0 order by adid"
	
	#query db
	EXISTING_ADX_UNIT_UNSORTED_FILE=${EXISTING_ADX_UNIT_FILE}.unsorted
	
	rm -f ${EXISTING_ADX_UNIT_UNSORTED_FILE}
	rm -f ${EXISTING_ADX_UNIT_FILE}
	
	runsql_sharding_read "${QUERY_EXISTING_ADX_UNIT_SQL}" "${EXISTING_ADX_UNIT_UNSORTED_FILE}" "${TAB_UNIT_SLICE}"
	
	if [ -s ${EXISTING_ADX_UNIT_UNSORTED_FILE} ]
	then
		cat ${EXISTING_ADX_UNIT_UNSORTED_FILE} | sort -k1n,1 | uniq  > ${EXISTING_ADX_UNIT_FILE}
	else
		touch ${EXISTING_ADX_UNIT_FILE}
	fi
}

#crossCheckAdxUnit
#比对后生成增量的ADD_FILE和DEL_FILE
function crossCheckAdxUnit() {
	rm -f ${ADX_UNIT_TO_ADD_FILE}
	rm -f ${ADX_UNIT_TO_DEL_FILE}
	
	if [ ! -s ${EXISTING_ADX_UNIT_FILE} ]
	then
		touch ${ADX_UNIT_TO_DEL_FILE}
		cat ${WHITELIST_UNIT_FILE} > ${ADX_UNIT_TO_ADD_FILE}
	else
		awk 'ARGIND==1{map[$1]=$0}ARGIND==2{if(map[$2]"X"=="X"){print $0}}' ${EXISTING_ADX_UNIT_FILE} ${WHITELIST_UNIT_FILE} > ${ADX_UNIT_TO_ADD_FILE}
		
		awk 'ARGIND==1{map[$2]=$0}ARGIND==2{if(map[$1]"X"=="X"){print $0}}' ${WHITELIST_UNIT_FILE} ${EXISTING_ADX_UNIT_FILE} > ${ADX_UNIT_TO_DEL_FILE}
	fi
}

getExistingAdxUnit
crossCheckAdxUnit

