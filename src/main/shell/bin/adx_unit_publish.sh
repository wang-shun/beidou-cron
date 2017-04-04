#!/bin/bash

#@file: adx_unit_publish.sh
#@author: lixukun
#@date: 2014-04-14
#@version: 1.0.0.0
#@brief: export adx audited creative to dts for bidding;


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_unit_publish.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

BACKUP_PATH=${WORK_PATH}/backup
mkdir -p ${WORK_PATH}
mkdir -p ${BACKUP_PATH}
mkdir -p ${LOG_PATH}

LOG_FILE=${LOG_PATH}/adx_unit_publish.log
curr_date=`date +"%Y-%m-%d"`

function cleanOldAdxUnitData() {
	remain=500
	total=`ls -1 ${BACKUP_PATH}|grep "${AD_INFO_FILE}"|wc -l`
	if [ ${total} -gt ${remain} ]
	then
		ls -rt1 ${BACKUP_PATH}|grep "${AD_INFO_FILE}"|head -$((total - remain))|sed "s;^;${BACKUP_PATH}/;"|xargs rm -f
	fi
	
	total=`ls -1 ${BACKUP_PATH}|grep "${AUDIT_PASS_READY_PUBLISH_FILE}"|wc -l`
	if [ ${total} -gt ${remain} ]
    then
        ls -rt1 ${BACKUP_PATH}|grep "${AUDIT_PASS_READY_PUBLISH_FILE}"|head -$((total - remain))|sed "s;^;${BACKUP_PATH}/;"|xargs rm -f
    fi
}

function backupAdxUnitData() {
	if [ -f "${WORK_PATH}/${AD_INFO_FILE}" ]
	then 
		newfilename=${AD_INFO_FILE}_`date  +%Y%m%d%H%M`
		mv ${WORK_PATH}/${AD_INFO_FILE} ${BACKUP_PATH}/${newfilename}
		echo "finished backup old adx unit file,file name is ${newfilename}" >> ${LOG_FILE}
	fi
}

function backupAdPublishData() {
    if [ -f "${WORK_PATH}/${AUDIT_PASS_READY_PUBLISH_FILE}" ]
    then 
        newfilename=${AUDIT_PASS_READY_PUBLISH_FILE}_`date  +%Y%m%d%H%M`
        mv ${WORK_PATH}/${AUDIT_PASS_READY_PUBLISH_FILE} ${BACKUP_PATH}/${newfilename}
        echo "finished backup old adx unit file,file name is ${newfilename}" >> ${LOG_FILE}
    fi
}

function getAdxGatePublishFiles() {
	# 获取AdxGate发布的审核通过文件
	wget -t ${MAX_RETRY} -q ${PUBLISH_CREATIVES_URL} -O ${PUBLISH_CREATIVES_FILE}
	
	# 获取AdxGate发布的待审核状态的文件
	wget -t ${MAX_RETRY} -q ${PUBLISH_CREATIVES_READY_URL} -O ${PUBLISH_CREATIVES_READY_FILE}
	
	# 获取黑名单文件
	wget -t ${MAX_RETRY} -q ${BLACK_LIST_URL} -O ${BLACKLIST_FILE}
}

function filterCreatives() {
	awk 'ARGIND==1{map[$2]=$1}ARGIND==2{if(map[$1]"X"!="X"){print $1"\t"$2"\t"map[$1]}}' ${WHITE_ADX_UNIT_FILE} ${PUBLISH_CREATIVES_FILE} > "${WORK_PATH}/${AD_INFO_FILE}_tmp"

	if [ -f "${WORK_PATH}/${AD_INFO_FILE}_tmp" ]
	then
		if [ -f "${BLACKLIST_FILE}" ] && [ -s "${BLACKLIST_FILE}" ]
		then
			java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.AdBlacklistFilter ${WORK_PATH}/${AD_INFO_FILE}_tmp ${BLACKLIST_FILE} ${WORK_PATH}/${AD_INFO_FILE_PROCESS} > ${LOG_PATH}/blacklist_filter.${curr_date}.log 2>&1
		else
			cat ${WORK_PATH}/${AD_INFO_FILE}_tmp >> ${WORK_PATH}/${AD_INFO_FILE_PROCESS}
		fi
	fi
	
	echo "finished export adx unit info from table${tableIndex}" >>${LOG_FILE}
	rm ${WORK_PATH}/${AD_INFO_FILE}_tmp
	
	backupAdxUnitData
	if [[ $? -eq 0 ]]
	then
		mv ${WORK_PATH}/${AD_INFO_FILE_PROCESS} ${WORK_PATH}/${AD_INFO_FILE}
	else
		echo "backup ${AD_INFO_FILE} fail" >> ${LOG_FILE}
		exit 1
	fi
}

function publishResult() {
	cd ${WORK_PATH}
	md5sum ${AD_INFO_FILE} > ${AD_INFO_FILE}.md5
	md5=`getMd5FileMd5 ${WORK_PATH}/${AD_INFO_FILE}.md5`
	msg="fail to register adx unit file to dts"
	noahdt add ${ADXUNITEXPORT_ADXUNITFILE} -m md5=${md5} bscp://${WORK_PATH}/${AD_INFO_FILE}
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
		exit 1
	else
		echo "register adx unit file to dts finished" >> ${LOG_FILE}
	fi
	msg="fail to register adx unit md5 file to dts"
	noahdt add ${ADXUNITEXPORT_ADXUNITFILE_MD5} bscp://${WORK_PATH}/${AD_INFO_FILE}.md5
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
		exit 1
	else
		echo "register adx unit md5 file to dts finished" >> ${LOG_FILE}
	fi
}

function publishBiddingCreatives() {
    AD_FILE_MERGE=ad_pass_ready_merge
    AD_FILE_TEMP=ad_pass_ready_temp
	awk 'ARGIND==1 {map[$1]=$2}ARGIND==2{tag="0";if(map[$1]"X"!="X"){tag=map[$1];}print $1"\t"tag"\t"$2"\t"$4;}' ${PUBLISH_CREATIVES_FILE} ${PUBLISH_CREATIVES_READY_FILE} > ${WORK_PATH}/${AD_FILE_MERGE} 
    awk 'ARGIND==1 {map[$1]=$2}ARGIND==2{if(map[$1]"X"=="X"){print $1"\t"$2"\t0\t"$4;}}' ${PUBLISH_CREATIVES_READY_FILE} ${PUBLISH_CREATIVES_FILE} >> ${WORK_PATH}/${AD_FILE_MERGE} 
	if [ -f "${WORK_PATH}/${AD_FILE_MERGE}" ]
	then
		if [ -f "${BLACKLIST_FILE}" ] && [ -s "${BLACKLIST_FILE}" ]
		then
		    rm -rf ${WORK_PATH}/${AD_FILE_TEMP}
			java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.AdBlacklistFilter ${WORK_PATH}/${AD_FILE_MERGE} ${BLACKLIST_FILE} ${WORK_PATH}/${AD_FILE_TEMP} > ${LOG_PATH}/blacklist_filter.${curr_date}.log 2>&1
		else
			cat ${WORK_PATH}/${AD_FILE_MERGE} > ${WORK_PATH}/${AD_FILE_TEMP}
		fi
	fi
	java -Xms1024m -Xmx1800m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.GenerateBiddingCreatives ${WORK_PATH}/${AD_FILE_TEMP} ${WORK_PATH}/${AUDIT_PASS_READY_FILE} > ${LOG_PATH}/audit_file_strategy.${curr_date}.log 2>&1
	if [ -f "${WORK_PATH}/${AUDIT_PASS_READY_FILE}" ]
	then
	   backupAdPublishData
	   mv ${WORK_PATH}/${AUDIT_PASS_READY_FILE} ${WORK_PATH}/${AUDIT_PASS_READY_PUBLISH_FILE}
	   cd ${WORK_PATH}
	   md5sum ${AUDIT_PASS_READY_PUBLISH_FILE} > ${AUDIT_PASS_READY_PUBLISH_FILE}.md5
    fi
}

echo "export adx unit task start at `date  "+%Y-%m-%d %H:%M"`" >> ${LOG_FILE}
getAdxGatePublishFiles
filterCreatives
publishResult
publishBiddingCreatives
cleanOldAdxUnitData
echo "export adx unit task end at `date  "+%Y-%m-%d %H:%M"`" >> ${LOG_FILE}

