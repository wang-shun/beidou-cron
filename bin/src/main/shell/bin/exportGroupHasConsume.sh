#!/bin/bash

#@file: exportGroupHasConsume.sh
#@author: wangxiongjie
#@date: 2013-09-05
#@version: 1.0.0.0
#@brief: export group and userid usertradeid file,these groups must have consume in last 7 days;

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/exportGroupHasConsume.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=exportGroupHasConsume.sh
reader_list=wangxiongjie

DATA_PATH=${DATA_PATH}/exportGroupHasConsume
mkdir -p ${DATA_PATH}
mkdir -p ${LOG_PATH}

TEMP_FILE=${DATA_PATH}/temp
LOG_FILE=${LOG_PATH}/exportGroupHasConsume.log

function downloadUserTradeidFile()
{
	cd ${DATA_PATH}
	if [[ -f ${USER_TRADEID_FILENAME}.md5 ]]
	then
		rm ${USER_TRADEID_FILENAME}.md5
	fi
	if [[ -f ${USER_TRADEID_FILENAME} ]]
	then
		rm ${USER_TRADEID_FILENAME}
	fi
	msg="Failed to get user_trade md5 for exportGroupHasConsume"
	wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME}.md5 -O ${USER_TRADEID_FILENAME}.md5
	if [[ $? -ne 0 ]]
	then
		echo "$msg" >> ${LOG_FILE}
		exit 1
	fi
	
	msg="Failed to get user_trade for exportGroupHasConsume"
	wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME} -O ${USER_TRADEID_FILENAME}
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
	fi
	
	md5sum -c ${USER_TRADEID_FILENAME}.md5 > /dev/null
	if [[ $? -ne 0 ]]
	then
		echo "Failed to check user_trade md5" >> ${LOG_FILE}
		exit 1
	fi
	echo "finished down user trade file" >> ${LOG_FILE}
}

# export group info from beidouurl.click
# argument1 group info file
function exportGroupHasConsume()
{	
	startdate=`date -d "-1 week" +%Y-%m-%d`
	echo "start to export group info from beidouurl.click" >> ${LOG_FILE}
	cat /dev/null > ${1}
	for((table_index=0;table_index<${CLICK_TABLE_NUM};table_index++))
	do
		query_sql="select distinct gid, uid from beidouurl.click${table_index} where clickdate>='${startdate}'"
		runsql_xdb_read "${query_sql}" ${TEMP_FILE}
		cat ${TEMP_FILE} >> ${1}
		echo "finished export groupid from beidouurl.click${table_index}" >> ${LOG_FILE}
	done
}

function addTrade2GroupFile()
{
	cut -f2 ${DATA_PATH}/${GROUP_INFO_FILE} | sort -u > ${DATA_PATH}/userids.txt
	
	# filter user_trade file, delete tradeid=0 and tradeid=9900,filter userid with consume
	# file: userid usertradeid
	awk -F'\t' 'ARGIND==1{useridMap[$1]=$1}ARGIND==2{if(($1 in useridMap) && $2>0 && $2<9900) print $1"\t"$2}' ${DATA_PATH}/userids.txt ${DATA_PATH}/${USER_TRADEID_FILENAME} > ${DATA_PATH}/${USER_TRADEID_FILENAME}.filtered
	
	# file: groupid userid usertradeid
	awk -F'\t' 'ARGIND==1{userMap[$1]=$2}ARGIND==2{if($2 in userMap) print $1"\t"$2"\t"userMap[$2]}' ${DATA_PATH}/${USER_TRADEID_FILENAME}.filtered ${DATA_PATH}/${GROUP_INFO_FILE} > ${DATA_PATH}/${GROUP_USER_TRADE_FILE}

	echo "finished add user trade info into group file, and filter users without tradeid" >> ${LOG_FILE}
}

function publishResult()
{
	# gene md5 file
	cd ${DATA_PATH}
	md5sum ${GROUP_USER_TRADE_FILE} > ${GROUP_USER_TRADE_FILE}.md5

	#regist file to dts
    msg="regist DTS for ${DATA_PATH}/${GROUP_USER_TRADE_FILE} failed."
	md5=`getMd5FileMd5 ${DATA_PATH}/${GROUP_USER_TRADE_FILE}.md5`
	noahdt add ${EXPORTGROUPHASCONSUME_GROUPUSERTRADE} -m md5=${md5}  bscp://${DATA_PATH}/${GROUP_USER_TRADE_FILE}
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
		exit 1
	fi
	echo "finished publish file ${GROUP_USER_TRADE_FILE} to dts" >> ${LOG_FILE}
}

downloadUserTradeidFile 
exportGroupHasConsume ${DATA_PATH}/${GROUP_INFO_FILE}
addTrade2GroupFile
publishResult

