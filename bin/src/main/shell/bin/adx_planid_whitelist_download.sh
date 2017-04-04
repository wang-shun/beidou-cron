#!/bin/bash

#@file: adx_planid_whitelist_download.sh
#@author: lixukun
#@intention: common adx planid whitelist download script

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/adx_common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

mkdir -p ${WORK_PATH}
mkdir -p ${DSP_PATH}
mkdir -p ${DSP_TEMP_PATH}
mkdir -p ${LOG_PATH}

program=adx_planid_whitelist_download.sh
LOG_NAME=adx_planid_whitelist_download

# 获取当前最新版本的planid_white_list的路径
function getCurrentPlanIdWhiteListFile() {
	ls -1rt ${DSP_PATH} | grep "${PLANID_WHITELIST_FILE}" | tail -1
}

# 下载最新版本“有效计划白名单”
function downloadPlanIdWhiteList(){
	current_file=${DSP_PATH}/`getCurrentPlanIdWhiteListFile`
	current_version=0
	if [ -f "${current_file}" ] && [ -s "${current_file}" ]
	then
		current_version=${current_file##*.}
	fi
	
	# 获取dts上的最新版本
	dts_lastest_version=`getDTSVersion ${PLANIDS_WHITELIST_DTS_ITEM}|sed 's/^ //'`
	echo "dts_version=${dts_lastest_version},current_version=${current_version}"
	
	if [ -n "${dts_latest_version}" ] && [ ${dts_latest_version} -gt 0 ]
	then
		log "ERROR" "adx_planid_whitelist_download|no new version|${dts_lastest_version}"
		exit 0
	fi
	
	if [ ${current_version} -eq ${dts_lastest_version} ] 
	then
		log "INFO" "adx_planid_whitelist_download|no new version|${dts_lastest_version}"
		exit 0
	fi
	
	# 新文件名，名字+"."+版本号，先写入临时目录
	tmp_file=${DSP_TEMP_PATH}/${PLANID_WHITELIST_FILE}"."${dts_lastest_version}
	
	msg="download planids whitelist failed."
	noahdt download ${PLANIDS_WHITELIST_DTS_ITEM} ${tmp_file}
	alert $? "${msg}"
	
	msg="check planids whitelist md5 for adx failed."
	checkMD5ForDTS ${tmp_file} ${PLANIDS_WHITELIST_DTS_ITEM} "md5"
	alert $? "${msg}"
	
	# 判断planid白名单文件变化
	if [  -s  "${current_file}" ]
	then
		same_count=`awk 'ARGIND==1{map[$1]}ARGIND==2{if( $1 in map){print $0}}' ${tmp_file} ${current_file} | wc  -l`
		new_count=`cat ${tmp_file} | wc -l`
		old_count=`cat ${current_file} | wc -l`
		diff_count=$((new_count + old_count -2*same_count))
		log "INFO" "adx_planid_whitelist_download|planid diff=${diff_count}|max=${PLANID_CHANGE_NUMBER}"
		#if [ ${diff_count} -gt  ${PLANID_CHANGE_NUMBER} ]
		#then
		#	msg="adx_planid_whitelist_download|error|planid diff=${diff_count}|max=${PLANID_CHANGE_NUMBER}"
		#	alert "1" "${msg}"
		#else
			mv ${tmp_file} ${DSP_PATH}/
		#fi
	else
		mv ${tmp_file} ${DSP_PATH}/
	fi
}

# 清理旧版本文件，考虑仅保留最近5个文件
function clearOldVersionFile() {
	remain=10
	total=`ls -rt1 ${DSP_PATH}|grep "${PLANID_WHITELIST_FILE}"|wc -l`
	if [ ${total} -gt ${remain} ]
	then
		ls -rt1 ${DSP_PATH}|head -$((total - remain))|sed "s;^;${DSP_PATH}/;"|xargs rm -f
	fi
	log "INFO" "clearOldVersionFile"
}


function main() {
	open_log
	
	startTime=`date +%s` 
	
	downloadPlanIdWhiteList
	clearOldVersionFile
	
	endTime=`date +%s`
	log "INFO" "adx_planid_whitelist_download|end at `date +%F\ %T`|used $((endTime-startTime))s"
	close_log 0
}

main
exit $?
