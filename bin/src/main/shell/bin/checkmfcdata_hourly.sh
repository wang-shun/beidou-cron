#!/bin/sh
#@file: checkmfcdata_hourly.sh
#@author: kanghongwei
#@date: 2011-08-29
#@version: 1.0.0.1
#@modify: wangchongjie since 2012.12.10 for cpweb525
#@intention: check out of step accounts between beidou and mfc
#			 and update beidou db to the right status or just 
#			 send mails

COMMON_CONF="../conf/common.conf"
DB_CONF="../lib/db_sharding.sh"
HOURLY_CONF="../conf/checkmfcdata_hourly.conf"
LIB_CONF="../lib/beidou_lib.sh"
ALERT_CONF="../bin/alert.sh"
BASH_CONF="/home/work/.bash_profile"
CONF_SH="../conf/classpath_recommend.conf"

timestamp=""

#
#  检查配置文件的正确性
#params: 待检查的配置文件
#
function checkConf()
{
	if [ $# -gt 0 ]
	then
		for toBeCheckConf in "$@"
		do
			if [ -f ${toBeCheckConf} ]
			then
				source ${toBeCheckConf}
			else 
			   return 1
			fi
		done
	fi
	
	return 0
}

#
#  检查checkmfcdata_hourly.conf中的配置项
#
function checkConfItem()
{
	open_log
	
	if ! [[ ${DEBUG_MOD} ]]
	then
		log "FATAL" "DEBUG_MOD is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${LOG_NAME} ]]
	then
		log "FATAL" "LOG_NAME is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${LOG_LEVEL} ]]
	then
		log "FATAL" "LOG_LEVEL is empty or its value is invalid"
		close_log 1
		return 1
	fi

	if ! [[ ${LOG_SIZE} ]]
	then
		log "FATAL" "LOG_SIZE is empty or its value is invalid"
		close_log 1
		return 1
	fi

	if ! [[ ${PROJ_DATA_PATH} ]]
	then
		log "FATAL" "PROJ_DATA_PATH is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${HOUR_DATA_PATH} ]]
	then
		log "FATAL" "HOUR_DATA_PATH is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${TMP_DATA_PATH} ]]
	then
		log "FATAL" "TMP_DATA_PATH is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${SCAN_NUM} ]]
	then
		log "FATAL" "SCAN_NUM is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if [ ${SCAN_NUM} -le 1 ]
	then
		log "FATAL" "SCAN_NUM should greater than 1"
		close_log 1
		return 1
	fi
	
	if ! [[ ${MAX_RETRY_TIMES} ]]
	then
		log "FATAL" "MAX_RETRY_TIMES is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${SLEEP_SECONDS_FOR_DB} ]]
	then
		log "FATAL" "SLEEP_SECONDS_FOR_DB is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${SLEEP_SECONDS_FOR_PRO} ]]
	then
		log "FATAL" "SLEEP_SECONDS_FOR_PRO is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${MAX_PRESERVE_DAY} ]]
	then
		log "FATAL" "MAX_PRESERVE_DAY is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if [ ${MAX_PRESERVE_DAY} -le 1 ]
	then
		log "FATAL" "MAX_PRESERVE_DAY should greater than 1"
		close_log 1
		return 1
	fi

	if ! [[ ${UPPER_UPDATE_NUM} ]]
	then
		log "FATAL" "UPPER_UPDATE_NUM is empty or its value is invalid"
		close_log 1
		return 1
	fi

	if [ ${UPPER_UPDATE_NUM} -le 1 ]
	then
		log "FATAL" "UPPER_UPDATE_NUM should greater than 1"
		close_log 1
		return 1
	fi
	
	if ! [[ ${MAIL_FROM} ]]
	then
		log "FATAL" "MAIL_FROM is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	
	if ! [[ ${MFC_BDUSERFUND_FILE_PATH} ]]
	then
		log "FATAL" "MFC_BDUSERFUND_FILE_PATH is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${MFC_BDUSERFUND_MD5FILE_PATH} ]]
	then
		log "FATAL" "MFC_BDUSERFUND_MD5FILE_PATH is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	if ! [[ ${API_LOG_NAME} ]]
	then
		log "FATAL" "API_LOG_NAME is empty or its value is invalid"
		close_log 1
		return 1
	fi
	
	close_log 0
	
	return 0
}

#
#  检查文件路径存在与否(不存在则新建)
#params: 待验证的路径
#
function checkPath()
{	
	open_log
	
	if [ $# -gt 0 ]
	then
		for toBeCheckPath in "$@"
		do
			if ! [ -e ${toBeCheckPath} ]
			then 
			    mkdir ${toBeCheckPath}
			    if [ $? -ne 0 ] 
			    then
			        log "FATAL" "create ${toBeCheckPath} error!"
			        close_log 1
			        return 1
			    fi  
			else
			    if ! [ -w ${toBeCheckPath} ]
			    then 
			        chmod g+w ${toBeCheckPath}
			        if [ $? -ne 0 ] 
			        then 
			            log  "FATAL" "${toBeCheckPath} is not writable after chmod!"
			            close_log 1
			            return 1
			        fi  
			    fi  
			fi
		done
	fi
	
	close_log 0
	return 0
}

#
#程序执行前的检测工作
#
function prepare()
{	
	#check conf
	checkConf "${COMMON_CONF}" "${DB_CONF}" "${HOURLY_CONF}" "${LIB_CONF}" "${ALERT_CONF}" "${BASH_CONF}" "${CONF_SH}"
	if [ $? -ne 0 ]
	then
		return 1
	fi

	#check conf items
	checkConfItem
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	#check paths
	checkPath "${LOG_PATH}" "${PROJ_DATA_PATH}" "${HOUR_DATA_PATH}" "${TMP_DATA_PATH}"
	if [ $? -ne 0 ]
	then
		return 1
	fi
	
	return 0
}

#日期处理函数
#将当前日期减去指定小时候，返回格式yyyymmddHH的日期格式
#param1: 待减去的小时数
function getFormatDate()
{
	if [ $# -eq 1 ]
	then
		tmpStep=`expr ${timestamp} - ${1} \* 3600`
		formatDate=`date +"%Y%m%d%H" -d "1970-01-01 UTC ${tmpStep} seconds"`
		echo ${formatDate}
	fi
}

#清除中间文件
#params: 等待清除的中间文件，默认清除所有中间文件
function clearTmpFile()
{
	if [ $# -gt 0 ]
	then
		if [ $# -eq 1 ]
		then
			if	[ $1 = "all" ]
			then
				cd ${TMP_DATA_PATH}
				rm -f *.tmp
			else
				rm -f ${1}
			fi
		else
			for tmpFile in "$@"
			do
				rm -f ${tmpFile}
			done
		fi
	fi
}

#
#  求两个文件的交集
#param1:待求交集文件1
#param2:待求交集文件2
#param3:生成的交集文件
#
function getIntersection()
{	
	tmpFile_sort_1=${TMP_DATA_PATH}/get.intersection.sort.1.tmp
	tmpFile_sort_2=${TMP_DATA_PATH}/get.intersection.sort.2.tmp
	
	#clear tmp file
	clearTmpFile "${tmpFile_sort_1}" "${tmpFile_sort_2}"
	
	sort ${1} > "${tmpFile_sort_1}"
	sort ${2} > "${tmpFile_sort_2}"
	
	join -1 1 -2 1 ${tmpFile_sort_1} ${tmpFile_sort_2} | sort > ${3}
}

#
#  根据北斗和mfc账户信息文件，生成不同步的账户信息文件
#
#param1: 北斗账户中balancestat=1的账户id文件
#param2: mfc中balance=1的账户id文件
#param3: 在北斗账户中balancestat=1,在mfc中balance=0的账户id文件(balancestat will be 0)
#param4: 在北斗账户中balancestat=0,在mfc中balance=1的账户id文件(balancestat will be 1)
#
function genereateOutOfStepFile()
{
	FILE_1=$1
	FILE_2=$2
	
	#这里加入一站式平台的名单过滤
    cp ${FILE_1} ${FILE_1}.all
	cp ${FILE_2} ${FILE_2}.all
	
	EXP_LIST=${EXP_FILE}
	wget ftp://${EXP_SERVER}/${EXP_FILE} -O ${TMP_DATA_PATH}/${EXP_LIST}.tmp
	sleep 5

	cat ${EXP_LIST}.tmp | sort -u |  sed '/^$/d' > ${EXP_LIST}
	
	if [ -f ${EXP_LIST} ];then
		awk 'ARGIND==1{map[$1]}ARGIND==2{if($1 in map){printf("%s\n",$0)}}' ${EXP_LIST} ${FILE_1}.all > ${FILE_1}
		awk 'ARGIND==1{map[$1]}ARGIND==2{if($1 in map){printf("%s\n",$0)}}' ${EXP_LIST} ${FILE_2}.all > ${FILE_2}
	fi
	
	
	FILE_OUTPUT_1=$3
	FILE_OUTPUT_2=$4
	tmpFile_intersection=${TMP_DATA_PATH}/genereate.outOfStep.intersection.tmp
	
	getIntersection  "${FILE_1}" "${FILE_2}" "${tmpFile_intersection}"
	cat ${tmpFile_intersection} ${FILE_1} | 
	  sort | 
	    uniq -c | 
	      awk '$1 == 1 {print $2}' | 
	        sort > ${FILE_OUTPUT_1}
	cat ${tmpFile_intersection} ${FILE_2} | 
	  sort | 
	    uniq -c | 
	      awk '$1 == 1 {print $2}' | 
	        sort > ${FILE_OUTPUT_2}
}

#
#  generate will-be-0.check.yyyymmddHH.dat 
#and will-be-1.check.yyyymmddHH.dat files
#
function generateHourFile()
{
	open_log
	
	returnFlag=0
		
	#define tmp files
	tmpFile_beidou_balancestat_1=${TMP_DATA_PATH}/generate.hour.beidou.balancestat.1.tmp
	tmpFile_mfc_balance_1=${TMP_DATA_PATH}/generate.hour.mfc.balance.1.tmp
	tmpFile_will_be_0=${TMP_DATA_PATH}/generate.hour.will-be.0.tmp
	tmpFile_will_be_1=${TMP_DATA_PATH}/generate.hour.will-be.1.tmp
	tmpFile_will_be_0_confirm=${TMP_DATA_PATH}/generate.hour.will-be.0.confirm.tmp
	tmpFile_will_be_1_confirm=${TMP_DATA_PATH}/generate.hour.will-be.1.confirm.tmp
	
	mfc_bduserfund_file=${TMP_DATA_PATH}/bduserfund.txt
	mfc_bduserfund_md5file=${TMP_DATA_PATH}/bduserfund.txt.md5
	mfc_api_input=${TMP_DATA_PATH}/userIds_Check.out
	mfc_api_output=${TMP_DATA_PATH}/userIds_Balance.out
	
	hourFile_will_be_0=${HOUR_DATA_PATH}"/will-be-0.check."$(getFormatDate "0")".dat"
	hourFile_will_be_1=${HOUR_DATA_PATH}"/will-be-1.check."$(getFormatDate "0")".dat"
	
	#clear tmp file
	clearTmpFile "${tmpFile_beidou_balancestat_1}" "${tmpFile_mfc_balance_1}" "${tmpFile_will_be_0}" "${tmpFile_will_be_1}" "${tmpFile_will_be_0_confirm}" "${tmpFile_will_be_1_confirm}" "${hourFile_will_be_0}" "${hourFile_will_be_1}"
	#clear mfc online and api file
	clearTmpFile "${mfc_bduserfund_file}" "${mfc_bduserfund_md5file}" "${mfc_api_input}" "${mfc_api_output}"
	
	#generate empty hour file
	touch ${hourFile_will_be_0}
	touch ${hourFile_will_be_1}
	
	#select beidou db first time
	runsql_cap_read "select userid from beidoucap.useraccount where balancestat = 1;"  "${tmpFile_beidou_balancestat_1}"
	returnFlag=$?
	if [ ${returnFlag} -gt 0 ]
	then
		close_log ${returnFlag}
		return ${returnFlag}
	fi
	
	#select mfc first time
	wget -c -T 5 -t 3 --limit-rate=500k -P ${TMP_DATA_PATH} ${MFC_BDUSERFUND_FILE_PATH}
	if [ $? -gt 0 ]
	then
		log "FATAL" "wget ${MFC_BDUSERFUND_FILE_PATH} failed."
		close_log 1
		return 1
	fi
	
	wget -c -T 5 -t 3 --limit-rate=500k -P ${TMP_DATA_PATH} ${MFC_BDUSERFUND_MD5FILE_PATH}
	if [ $? -gt 0 ]
	then
		log "FATAL" "wget ${MFC_BDUSERFUND_MD5FILE_PATH} failed."
		close_log 1
		return 1
	fi
	
	#md5sum -c ${mfc_bduserfund_md5file} > /dev/null
	#if [ $? -gt 0 ]
	#then
	#	log "FATAL" "verify ${mfc_bduserfund_md5file} failed."
	#	close_log 1
	#	return 1
	#fi
	
	awk '{if($2 > 0) print $1}' ${mfc_bduserfund_file} > ${tmpFile_mfc_balance_1}
	if [ $? -gt 0 ]
	then
		log "FATAL" "generate ${tmpFile_mfc_balance_1} failed."
		close_log 1
		return 1
	fi
	
	genereateOutOfStepFile ${tmpFile_beidou_balancestat_1} ${tmpFile_mfc_balance_1} ${tmpFile_will_be_0} ${tmpFile_will_be_1}
	
	sleep  ${SLEEP_SECONDS_FOR_PRO}
	
	SELCT_CONFIRM_USERIDS=`cat ${tmpFile_will_be_0} ${tmpFile_will_be_1} | sed '/^$/d' | awk 'BEGIN{str="-1";} {str=str","$1} END{print str}'`
	
	#select beidou db second time
	runsql_cap_read "select userid from beidoucap.useraccount where userid in ( ${SELCT_CONFIRM_USERIDS}) and balancestat = 1;" "${tmpFile_will_be_0_confirm}"
	returnFlag=$?
	if [ ${returnFlag} -gt 0 ] 
	then
		close_log ${returnFlag}
		return ${returnFlag}
	fi
	
	#select mfc second time from api
	cat ${tmpFile_will_be_0} ${tmpFile_will_be_1} > ${mfc_api_input}
	
	java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.RetriveUserBalance >> ${LOG_PATH}/${API_LOG_NAME} 2>&1
	if [ $? -gt 0 ]
	then
		log "FATAL" "call com.baidu.beidou.account.RetriveUserBalance api failed when generate hour file."
		close_log 1
		return 1
	fi
	
	if [ -f ${mfc_api_output} ]
	then
		awk '{if($2 > 0) print $1}' ${mfc_api_output} > ${tmpFile_will_be_1_confirm}
	else
		log "FATAL" "genereate generate.hour.will-be.1.confirm.tmp failed when generate hour file."
		close_log 1
		return 1
	fi
	
	#generate hour files
	genereateOutOfStepFile ${tmpFile_will_be_0_confirm} ${tmpFile_will_be_1_confirm} ${hourFile_will_be_0} ${hourFile_will_be_1}
	
	close_log $returnFlag
	return $returnFlag
}

#
#  scan hour files and generage
#will-be-0.tmp and will-be-1.tmp fiels
#
function scanHourFile()
{	
	open_log
	
	tmpFile_scan_will_be_0=${TMP_DATA_PATH}"/scan_will-be-0.tmp"
	tmpFile_scan_will_be_1=${TMP_DATA_PATH}"/scan_will-be-1.tmp"
	tmpFile_result_will_be_0=${TMP_DATA_PATH}"/will-be-0.tmp"
	tmpFile_result_will_be_1=${TMP_DATA_PATH}"/will-be-1.tmp"
	
	clearTmpFile "${tmpFile_scan_will_be_0}" "${tmpFile_scan_will_be_1}" "${tmpFile_result_will_be_0}" "${tmpFile_result_will_be_1}"
	
	#generate will-be-0.tmp
	hourCount=0;
	while [[ ${hourCount} -lt ${SCAN_NUM} ]]
	do
	    dateName=$(getFormatDate "${hourCount}")
	    fileName=${HOUR_DATA_PATH}"/""will-be-0.check."${dateName}".dat"
	    if ! [ -f ${fileName} ]
	    then
	    	#扫描到的文件必须是连续文件
	    	log "FATAL" "absent file: ${fileName}."
	    	close_log 1
	    	return 1
	    fi
	    cat ${fileName} >> ${tmpFile_scan_will_be_0}
	    hourCount=$((hourCount + 1))
	done
	
	cat ${tmpFile_scan_will_be_0} |
	  sort |
	    uniq -c |
	      sort -r |
	        tr -s ["      "] | 
	          awk '{if($1 >= '${SCAN_NUM}') print $2}' > ${tmpFile_result_will_be_0}
	          
	#generate will-be-1.tmp
	hourCount=0;
	while [[ ${hourCount} -lt ${SCAN_NUM} ]]
	do
	    dateName=$(getFormatDate "${hourCount}")
	    fileName=${HOUR_DATA_PATH}"/""will-be-1.check."${dateName}".dat"
	    if ! [ -f ${fileName} ]
	    then
	    	#扫描到的文件必须是连续文件
	    	log "FATAL" "absent file: ${fileName}."
	    	close_log 1
	    	return 1
	    fi
	    cat ${fileName} >> ${tmpFile_scan_will_be_1}
	    hourCount=$((hourCount + 1))
	done
	
	cat ${tmpFile_scan_will_be_1} |
	  sort |
	    uniq -c |
	      sort -r |
	        tr -s ["      "] | 
	          awk '{if($1 >= '${SCAN_NUM}') print $2}' > ${tmpFile_result_will_be_1}
	close_log 0
	return 0
}

#
#	1)confirm before update;
#	2)update beidou db;
#	3)send email
#
#param1: will-be-0.tmp
#param2: will-be-1.tmp
#
function updateBeidou()
{
	WILL_BE_0_TMP=$1
	WILL_BE_1_TMP=$2

	open_log
	returnFlag=0
	
	#mail params
	MAIL_TIME=`date +"%Y-%m-%d-%H" -d "1970-01-01 UTC ${timestamp} seconds"`
	MAIL_SUBJECT="beidou & mfc data check hourly [${MAIL_TIME}]"
	MAIL_BODY_WILL_BE_0_MSG="The accounts list with balancestat to be 0 is empty."
	MAIL_BODY_WILL_BE_1_MSG="The accounts list with balancestat to be 1 is empty."
	
	#define tmp files
	tmpFile_select_account_max_price_0=${TMP_DATA_PATH}/beidou.max.price.0.tmp
	tmpFile_select_account_max_price_1=${TMP_DATA_PATH}/beidou.max.price.1.tmp
	
	tmpFile_update_confirm_beidou_to_0=${TMP_DATA_PATH}/update.confirm.beidou.0.tmp
	tmpFile_update_confirm_mfc_to_0=${TMP_DATA_PATH}/update.confirm.mfc.0.tmp
	tmpFile_update_confirm_intersection_to_0=${TMP_DATA_PATH}/update.confirm.intersection.0.tmp
	
	tmpFile_update_confirm_beidou_to_1=${TMP_DATA_PATH}/update.confirm.beidou.1.tmp
	tmpFile_update_confirm_mfc_to_1=${TMP_DATA_PATH}/update.confirm.mfc.1.tmp
	tmpFile_update_confirm_intersection_to_1=${TMP_DATA_PATH}/update.confirm.intersection.1.tmp

	#mfc api tmp file
	tmpFile_mfc_api_input=${TMP_DATA_PATH}/userIds_Check.out
	tmpFile_mfc_api_output=${TMP_DATA_PATH}/userIds_Balance.out
	
	#clear tmp file
	clearTmpFile "${tmpFile_select_account_max_price_0}" "${tmpFile_select_account_max_price_1}"
	clearTmpFile "${tmpFile_update_confirm_beidou_to_0}" "${tmpFile_update_confirm_mfc_to_0}" "${tmpFile_update_confirm_intersection_to_0}"
	clearTmpFile "${tmpFile_update_confirm_beidou_to_1}" "${tmpFile_update_confirm_mfc_to_1}" "${tmpFile_update_confirm_intersection_to_1}"
	clearTmpFile "${tmpFile_mfc_api_input}" "${tmpFile_mfc_api_output}"
	
	#deal will-be-0.tmp
	if [ -s ${WILL_BE_0_TMP} ]
	then
		#rowCount=`more ${WILL_BE_0_TMP} | sed '/^$/d' | wc -l`
		rowCount=`cat ${WILL_BE_0_TMP} | wc -l`
		accountUserIDs=`cat ${WILL_BE_0_TMP} | sed '/^$/d' | awk 'BEGIN{str="-1";} {str=str","$1} END{print str}'`
		log "TRACE" "rowCount: ${rowCount}, accountUserIDs: ${accountUserIDs}"
		if [ ${rowCount} -gt ${UPPER_UPDATE_NUM} ]
		then
			log "TRACE" "${WILL_BE_0_TMP}'s rowCount ${rowCount} exceed ${UPPER_UPDATE_NUM}"
			runsql_sharding_read "select distinct a.userid,max(b.price) from beidou.cprogroup a left join beidou.cprogroupinfo b on a.groupid=b.groupid where a.userid in ( ${accountUserIDs} ) and [a.userid] group by a.userid" "${tmpFile_select_account_max_price_0}" 
			returnFlag=$?
			if [ ${returnFlag} -gt 0 ]
			then
				close_log ${returnFlag}
				return ${returnFlag}
			fi
			#查询到了max price的账户
			if [ -s  ${tmpFile_select_account_max_price_0} ]
			then
				MAIL_BODY_WILL_BE_0_MSG=`awk 'BEGIN{OFS="";ORS="<p>";print "The accounts list with balancestat to be 0 and his(her) available max group bid is:<br/><p>userId  max-bid(unitN)</p>"}{print $1"("$2")"}' ${tmpFile_select_account_max_price_0}`
			fi
		else
			#confirm beidou db before update
			runsql_cap_read "select userid from beidoucap.useraccount where userid in (${accountUserIDs}) and balancestat=1;" "${tmpFile_update_confirm_beidou_to_0}"
			returnFlag=$?
			if [ ${returnFlag} -gt 0 ]
			then
				close_log ${returnFlag}
				return ${returnFlag}
			fi
			
			#confirm mfc db before update
			cat ${WILL_BE_0_TMP} | sed '/^$/d' > ${tmpFile_mfc_api_input}
			
			java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.RetriveUserBalance >> ${LOG_PATH}/${API_LOG_NAME} 2>&1
			if [ $? -gt 0 ]
			then
				log "FATAL" "call com.baidu.beidou.account.RetriveUserBalance api failed when confirm mfc db before update."
				close_log 1
				return 1
			fi
			
			if [ -f ${tmpFile_mfc_api_output} ]
			then
				awk '{if($2 == 0) print $1}' ${tmpFile_mfc_api_output} > ${tmpFile_update_confirm_mfc_to_0}
			else
				log "FATAL" "genereate update.confirm.mfc.0.tmp failed when confirm mfc db before update."
				close_log 1
				return 1
			fi
			
			getIntersection  "${tmpFile_update_confirm_beidou_to_0}" "${tmpFile_update_confirm_mfc_to_0}" "${tmpFile_update_confirm_intersection_to_0}"
			
			updatedAccountUserIDs="-1"
			if [ -s ${tmpFile_update_confirm_intersection_to_0} ]
			then
				#update beidou db
				updatedCount=0
				while read updateUserId
				do
				runsql_cap "update beidoucap.useraccount set balancestat = 0 where userid = ${updateUserId} and balancestat = 1 limit 1"
					if [ $? -eq 0 ]
					then
						updatedCount=$((updatedCount+1))
						updatedAccountUserIDs=${updatedAccountUserIDs}","${updateUserId}
					else 
						#查询出的待更新账户，更新不成功，则等待下次更新
						log "FATAL" "update beidou account ${updateUserId} error."
					fi
				done  < ${tmpFile_update_confirm_intersection_to_0}
			fi
			

			runsql_sharding_read "select distinct a.userid,max(b.price) from beidou.cprogroup a left join beidou.cprogroupinfo b on a.groupid=b.groupid where a.userid in ( ${updatedAccountUserIDs} ) and [a.userid] group by a.userid" "${tmpFile_select_account_max_price_0}"
			returnFlag=$?
			if [ ${returnFlag} -gt 0 ]
			then
				close_log ${returnFlag}
				return ${returnFlag}
			fi
			#查询到了max price的账户
			if [ -s  ${tmpFile_select_account_max_price_0} ]
			then
				MAIL_BODY_WILL_BE_0_MSG=`awk 'BEGIN{OFS="";ORS="<p>";print "The accounts list with balancestat have been updated to 0 and his(her) available max group bid is:<br/><p>updated userId  max-bid(unitN)</p>"}{print $1"("$2")"}' ${tmpFile_select_account_max_price_0}`
			fi
		fi
	else
		log "TRACE" "${WILL_BE_0_TMP} is empty."
	fi
	
	
	#deal with will-be-1.tmp
	if [ -s ${WILL_BE_1_TMP} ]
	then
		#rowCount=`more ${WILL_BE_1_TMP} | sed '/^$/d' | wc -l`
		rowCount=`cat ${WILL_BE_1_TMP} | wc -l`
		accountUserIDs=`cat ${WILL_BE_1_TMP} | sed '/^$/d' | awk 'BEGIN{str="-1";} {str=str","$1} END{print str}'`
		log "TRACE" "rowCount: ${rowCount}, accountUserIDs: ${accountUserIDs}"
		if [ ${rowCount} -gt ${UPPER_UPDATE_NUM} ]
		then
			log "TRACE" "${WILL_BE_1_TMP}'s rowCount ${rowCount} exceed ${UPPER_UPDATE_NUM}"
			runsql_sharding_read "select distinct a.userid,max(b.price) from beidou.cprogroup a left join beidou.cprogroupinfo b on a.groupid=b.groupid where a.userid in ( ${accountUserIDs} ) and [a.userid] group by a.userid" "${tmpFile_select_account_max_price_1}"
			
			returnFlag=$?
			if [ ${returnFlag} -gt 0 ]
			then
				close_log ${returnFlag}
				return ${returnFlag}
			fi
			#查询到了max price的账户
			if [ -s  ${tmpFile_select_account_max_price_1} ]
			then
				MAIL_BODY_WILL_BE_1_MSG=`awk 'BEGIN{OFS="";ORS="<p>";print "The accounts list with balancestat to be 1 and his(her) available max group bid is:<br/><p>userId  max-bid(unitN)</p>"}{print $1"("$2")"}' ${tmpFile_select_account_max_price_1}`
			fi
		else
			#confirm beidou db before update
			runsql_cap_read "select userid from beidoucap.useraccount where userid in (${accountUserIDs}) and balancestat=0;"  ${tmpFile_update_confirm_beidou_to_1}
			returnFlag=$?
			if [ ${returnFlag} -gt 0 ]
			then
				close_log ${returnFlag}
				return ${returnFlag}
			fi
			
			#confirm mfc db before update
			clearTmpFile "${tmpFile_mfc_api_input}" "${tmpFile_mfc_api_output}"
			cat ${WILL_BE_1_TMP} | sed '/^$/d' > ${tmpFile_mfc_api_input}
			
			java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.RetriveUserBalance >> ${LOG_PATH}/${API_LOG_NAME} 2>&1
			if [ $? -gt 0 ]
			then
				log "FATAL" "call com.baidu.beidou.account.RetriveUserBalance api failed when confirm mfc db before update."
				close_log 1
				return 1
			fi
			
			if [ -f ${tmpFile_mfc_api_output} ]
			then
				awk '{if($2 > 0) print $1}' ${tmpFile_mfc_api_output} > ${tmpFile_update_confirm_mfc_to_1}
			else
				log "FATAL" "genereate update.confirm.mfc.1.tmp failed when confirm mfc db before update."
				close_log 1
				return 1
			fi
			
			getIntersection  "${tmpFile_update_confirm_beidou_to_1}" "${tmpFile_update_confirm_mfc_to_1}" "${tmpFile_update_confirm_intersection_to_1}"
			
			updatedAccountUserIDs="-1"
			if [ -s ${tmpFile_update_confirm_intersection_to_1} ]
			then
				#update beidou db
				updatedCount=0
				while read updateUserId
				do					
					runsql_cap "update beidoucap.useraccount set balancestat = 1 where userid = ${updateUserId} and balancestat = 0 limit 1"
					if [ $? -eq 0 ]
					then
						updatedCount=$((updatedCount+1))
						updatedAccountUserIDs=${updatedAccountUserIDs}","${updateUserId}
					else 
						#查询出的待更新账户，更新不成功，则等待下次更新
						log "FATAL" "update beidou account ${updateUserId} error."
					fi
				done  < ${tmpFile_update_confirm_intersection_to_1}
			fi
			
			
			runsql_sharding_read "select distinct a.userid,max(b.price) from beidou.cprogroup a left join beidou.cprogroupinfo b on a.groupid=b.groupid where a.userid in ( ${updatedAccountUserIDs} ) and [a.userid] group by a.userid" "${tmpFile_select_account_max_price_1}"
			returnFlag=$?
			if [ ${returnFlag} -gt 0 ]
			then
				close_log ${returnFlag}
				return ${returnFlag}
			fi
			#查询到了max price的账户
			if [ -s  ${tmpFile_select_account_max_price_1} ]
			then
				MAIL_BODY_WILL_BE_1_MSG=`awk 'BEGIN{OFS="";ORS="<p>";print "The accounts list with balancestat have been updated to 1 and his(her) available max group bid is:<br/><p>updated userId  max-bid(unitN)</p>"}{print $1"("$2")"}' ${tmpFile_select_account_max_price_1}`
			fi
		fi
	else
		log "TRACE" "${WILL_BE_1_TMP} is empty."
	fi

	#send mail after update or not
	EMPTY_LIST_WILL_BE_0="The accounts list with balancestat to be 0 is empty."
	EMPTY_LIST_WILL_BE_1="The accounts list with balancestat to be 1 is empty."
	if  [ "${EMPTY_LIST_WILL_BE_0}" != "${MAIL_BODY_WILL_BE_0_MSG}" ] || [ "${EMPTY_LIST_WILL_BE_1}" != "${MAIL_BODY_WILL_BE_1_MSG}" ]
	then
		MAIL_BODY=$(get_MAIL_BODY "${MAILLIST}" "${MAIL_FROM}" "${MAIL_SUBJECT}" "${MAIL_BODY_WILL_BE_0_MSG}" "${MAIL_BODY_WILL_BE_1_MSG}")
		echo -e ${MAIL_BODY} | sed -e 's/[^ ]* //' | /usr/sbin/sendmail -t
	fi
	close_log $returnFlag
	return $returnFlag
}

#
#clear hour files before MAX_PRESERVE_DAY's day
#
function clearHourFiles()
{
	cd ${HOUR_DATA_PATH}
	
	open_log
	
	hourFilePrefix="will\-be\-[01]\.check\."
	excludeFileNames=""
	
	if [ ${MAX_PRESERVE_DAY} -gt 1 ]
	then
		clearDay=0
		while [ ${clearDay} -lt ${MAX_PRESERVE_DAY} ]
		do
			dayName=`date -d "-${clearDay} day" "+%Y%m%d"`
			clearDay=$((clearDay+1))
			if [ -z ${excludeFileNames} ]
			then
				excludeFileNames=${hourFilePrefix}${dayName}
			else
				excludeFileNames=${excludeFileNames}"|"${hourFilePrefix}${dayName}
			fi
		done
		
		if [ -n ${excludeFileNames} ]
		then
			rm -f `ls | grep -iv -E ${excludeFileNames}`
		fi
	else
		log "TRACE" "${MAX_PRESERVE_DAY} should big than 1."
	fi
	
	close_log 0
}

#
#main function
#
function main()
{
	open_log
	
	log "TRACE" "===================================================================="
	log "TRACE" "定时任务$0执行开始，开始时间：`date +"%Y-%m-%d %H:%M:%S"`"
	log "TRACE" "===================================================================="
	
	timestamp=`date +"%s"`
	
	#清除所有临时文件
	log "TRACE" "===================================================================="
	clearTmpFile "all"
	log "TRACE" "定时任务$0清理完临时文件，完成时间：`date +"%Y-%m-%d %H:%M:%S"`"
	log "TRACE" "===================================================================="
	
	#生成每小时不同步文件
	log "TRACE" "===================================================================="
	generateHourFile
	if [ $? -ne 0 ]
	then
		SendMail "$0 generate hour files failed." "${MAILLIST}"
		close_log 0
		exit 1
	fi
	log "TRACE" "定时任务$0生成小时文件，完成时间：`date +"%Y-%m-%d %H:%M:%S"`"
	log "TRACE" "===================================================================="
	
	#扫描小时文件，生成临时文件
	log "TRACE" "===================================================================="
	scanHourFile
	if [ $? -ne 0 ]
	then
		SendMail "$0 scan hour files failed." "${MAILLIST}"
		close_log 0
		exit 1
	fi
	log "TRACE" "定时任务$0扫描小时文件，完成时间：`date +"%Y-%m-%d %H:%M:%S"`"
	log "TRACE" "===================================================================="
	
	#更新北斗库，或者只发送邮件不更新
	log "TRACE" "===================================================================="
	paramTmpFile0=${TMP_DATA_PATH}"/will-be-0.tmp"
	paramTmpFile1=${TMP_DATA_PATH}"/will-be-1.tmp"
	if  ! [ -f ${paramTmpFile0} ] || ! [ -f ${paramTmpFile1} ]
	then
		SendMail "will-be-0.tmp or will-be-1.tmp missing before update beidou." "${MAILLIST}"
		close_log 0
		exit 1
	fi
	updateBeidou ${paramTmpFile0} ${paramTmpFile1}
	log "TRACE" "定时任务$0更新北斗库，完成时间：`date +"%Y-%m-%d %H:%M:%S"`"
	log "TRACE" "===================================================================="
	
	#清除MAX_PRESERVE_DAY天之前的所有小时文件
	clearHourFiles
	
	endTime=`date +"%s"`
	spendTime=$((endTime-timestamp))
	log "TRACE" "===================================================================="
	log "TRACE" "定时任务$0执行完毕，完成时间：`date +"%Y-%m-%d %H:%M:%S"`，共耗时：${spendTime}s"
	log "TRACE" "===================================================================="
	
	close_log 0
	exit $?
}

prepare
if [ $? -ne 0 ]
then
	echo "prepare for $0 error!"
	exit 1
fi
main
