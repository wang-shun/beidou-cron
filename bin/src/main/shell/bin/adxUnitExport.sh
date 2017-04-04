#!/bin/bash

#@file: calculateInterest.sh
#@author: wangxiongjie, lixukun
#@date: 2013-10-15
#@version: 1.0.0.0
#@brief: export adx audited creative to dts for bidding;


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adxUnitExport.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATA_PATH=${DATA_PATH}/adxUnitExport
BACKUP_PATH=${DATA_PATH}/backup
mkdir -p ${DATA_PATH}
mkdir -p ${BACKUP_PATH}
mkdir -p ${LOG_PATH}

LOG_FILE=${LOG_PATH}/adxUnitExport.log

function cleanOldAdxUnitData() {
	remain=500
	total=`ls -1 ${BACKUP_PATH}|grep "${AD_INFO_FILE}"|wc -l`
	if [ ${total} -gt ${remain} ]
	then
		ls -1 ${BACKUP_PATH}|sort -n|head -$((total - remain))|sed "s;^;${BACKUP_PATH}/;"|xargs rm -f
	fi
}

function backupAdxUnitData()
{
	if [ -f "${DATA_PATH}/${AD_INFO_FILE}" ]
	then 
		newfilename=${AD_INFO_FILE}_`date  +%Y%m%d%H%M`
		mv ${DATA_PATH}/${AD_INFO_FILE} ${BACKUP_PATH}/${newfilename}
		echo "finished backup old adx unit file,file name is ${newfilename}" >> ${LOG_FILE}
	fi
}

function exportAdxUnitByState()
{
	blacklist_file=${DATA_PATH}/blacklist
	wget -t ${MAX_RETRY} -q ${BLACK_LIST_URL} -O ${blacklist_file}
	for ((tableIndex=0;tableIndex<8;tableIndex++))
	do
		query_sql="select adid, audit_adx_type, userid from beidou.cprounitadx${tableIndex} where audit_adx_type!=0 and [userid];"
		runsql_sharding_read "${query_sql}" ${DATA_PATH}/${AD_INFO_FILE}_tmp
		if [ -f "${DATA_PATH}/${AD_INFO_FILE}_tmp" ]
		then
			if [ -f "${blacklist_file}" ] && [ -s "${blacklist_file}" ]
			then
				java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.AdBlacklistFilter ${DATA_PATH}/${AD_INFO_FILE}_tmp ${blacklist_file} ${DATA_PATH}/${AD_INFO_FILE_PROCESS} > ${LOG_PATH}/blacklist_filter.${curr_date}.log 2>&1
			else
				cat ${DATA_PATH}/${AD_INFO_FILE}_tmp >> ${DATA_PATH}/${AD_INFO_FILE_PROCESS}
			fi
		fi
		echo "finished export adx unit info from table${tableIndex}" >>${LOG_FILE}
	done
	rm ${DATA_PATH}/${AD_INFO_FILE}_tmp
	
	backupAdxUnitData
	if [[ $? -eq 0 ]]
	then
		mv ${DATA_PATH}/${AD_INFO_FILE_PROCESS} ${DATA_PATH}/${AD_INFO_FILE}
	else
		echo "backup ${AD_INFO_FILE} fail" >> ${LOG_FILE}
		exit 1
	fi
}

function publishResult()
{
	cd ${DATA_PATH}
	md5sum ${AD_INFO_FILE} > ${AD_INFO_FILE}.md5
	md5=`getMd5FileMd5 ${DATA_PATH}/${AD_INFO_FILE}.md5`
	msg="fail to register adx unit file to dts"
	noahdt add ${ADXUNITEXPORT_ADXUNITFILE} -m md5=${md5} bscp://${DATA_PATH}/${AD_INFO_FILE}
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
		exit 1
	else
		echo "register adx unit file to dts finished" >> ${LOG_FILE}
	fi
	msg="fail to register adx unit md5 file to dts"
	noahdt add ${ADXUNITEXPORT_ADXUNITFILE_MD5} bscp://${DATA_PATH}/${AD_INFO_FILE}.md5
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
		exit 1
	else
		echo "register adx unit md5 file to dts finished" >> ${LOG_FILE}
	fi
}

echo "export adx unit task start at `date  "+%Y-%m-%d %H:%M"`" >> ${LOG_FILE}
exportAdxUnitByState
publishResult
cleanOldAdxUnitData
echo "export adx unit task end at `date  "+%Y-%m-%d %H:%M"`" >> ${LOG_FILE}

