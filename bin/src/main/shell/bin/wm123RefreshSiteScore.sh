#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/wm123RefreshSiteScore.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

program=wm123RefreshSiteScore.sh
reader_list=lvzichan

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${WM123_DATA_PATH}

######## Download ${DOMAIN_RESULT_FILE},max retry num:${MAX_DAYS_AGO}
function geDomainResultFile()
{
	cd ${WM123_DATA_PATH}
	
	daysAgo=3
	while [[ ${daysAgo} -le ${MAX_DAYS_AGO} ]]
	do
		DATE=`date +%Y%m%d -d"${daysAgo} days ago"`
		
		if [ -e "${DOMAIN_RESULT_FILE}_${DATE}" ]
		then
			rm ${DOMAIN_RESULT_FILE}_${DATE}
		fi
		wget -q ${HETU_URL}/${DATE}/${DOMAIN_RESULT_FILE}_${DATE}
		if [ $? -ne 0 ]
		then
			daysAgo=$[daysAgo+1]
			continue
		fi
		
		if [ -e "${DOMAIN_RESULT_FILE}_${DATE}.md5" ]
		then
			rm ${DOMAIN_RESULT_FILE}_${DATE}.md5
		fi	
		wget -q ${HETU_URL}/${DATE}/${DOMAIN_RESULT_FILE}_${DATE}.md5
		if [ $? -ne 0 ]
		then
			daysAgo=$[daysAgo+1]
			continue
		fi
		
		md5sum -c ${DOMAIN_RESULT_FILE}_${DATE}.md5
		if [ $? -ne 0 ]
		then
			daysAgo=$[daysAgo+1]
			continue
		fi
		
		mv ${DOMAIN_RESULT_FILE}_${DATE} ${DOMAIN_RESULT_FILE}
		rm ${DOMAIN_RESULT_FILE}_${DATE}.md5
		msg="Download ${DOMAIN_RESULT_FILE} success! Rename ${DOMAIN_RESULT_FILE}_${DATE} to ${DOMAIN_RESULT_FILE}"
		echo "${msg}"
		log "TRACE" "${msg}"
		break
	done
	
	if [ ${daysAgo} -gt ${MAX_DAYS_AGO} ]
	then
		return 1
	else
		return 0
	fi
}

########### Download ${DOMAIN_COOKIEINFO_FILE},max retry num:${MAX_DAYS_AGO}
function geDomainCookieinfoFile()
{
	cd ${WM123_DATA_PATH}
	
	daysAgo=3
	while [[ ${daysAgo} -le ${MAX_DAYS_AGO} ]]
	do
		DATE=`date +%Y%m%d -d"${daysAgo} days ago"`
		
		if [ -e "${DOMAIN_COOKIEINFO_FILE}_${DATE}" ]
		then
			rm ${DOMAIN_COOKIEINFO_FILE}_${DATE}
		fi	
		wget -q ${DOMAIN_COOKIE_URL}/${DATE}/${DOMAIN_COOKIEINFO_FILE}_${DATE}
		if [ $? -ne 0 ]
		then
			daysAgo=$[daysAgo+1]
			continue
		fi
		
		if [ -e "${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5" ]
		then
			rm ${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5
		fi
		wget -q ${DOMAIN_COOKIE_URL}/${DATE}/${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5
		if [ $? -ne 0 ]
		then
			daysAgo=$[daysAgo+1]
			continue
		fi
		
		md5sum -c ${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5
		if [ $? -ne 0 ]
		then
			daysAgo=$[daysAgo+1]
			continue
		fi
		
		mv ${DOMAIN_COOKIEINFO_FILE}_${DATE} ${DOMAIN_COOKIEINFO_FILE}
		rm ${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5
		msg="Download ${DOMAIN_COOKIEINFO_FILE} success! Rename ${DOMAIN_COOKIEINFO_FILE}_${DATE} to ${DOMAIN_COOKIEINFO_FILE}"
		echo "${msg}"
		log "TRACE" "${msg}"
		break
	done
	
	if [ ${daysAgo} -gt ${MAX_DAYS_AGO} ]
	then
		return 1
	else
		return 0
	fi
}

####### merge ${DOMAIN_RESULT_FILE} and ${DOMAIN_COOKIEINFO_FILE} to ${DOMAIN_FINAL_FILE}
function mergeToDomainFinalFile()
{
	cd ${WM123_DATA_PATH}
	if ! [ -e "${DOMAIN_COOKIEINFO_FILE}" -a -e "${DOMAIN_RESULT_FILE}" ]
	then
		return 1
	fi

	awk -v'OFS=\t' 'NR==FNR {map[$1]=$5} NR>FNR{if(map[$1]"x" != "x") {print $1,$4,$5,$11,$7,map[$1]} else {print $1,$4,$5,$11,$7,-1}}' ${DOMAIN_COOKIEINFO_FILE} ${DOMAIN_RESULT_FILE} > ${DOMAIN_FINAL_FILE}
	if [ $? -eq 0 ]
	then
		msg="Merge success! Merge${DOMAIN_RESULT_FILE},${DOMAIN_COOKIEINFO_FILE} to ${DOMAIN_FINAL_FILE}"
		echo "${msg}"
		log "TRACE" "${msg}"
		return 0
	else
		return 1
	fi
}

######## Download ${DOMAIN_TU_RESULT_FILE}1~14,sort by "domain",write some items(value!=-1) to file ${DOMAIN_TU_FINAL_FILE}.1~14
function getDomainTuFile()
{
	cd ${WM123_DATA_PATH}
	
	curFileNum=1
	hasGetFile=0
	while [[ ${curFileNum} -le ${MAX_DOMAIN_TU_FILE_NUM} ]]
	do
		daysAgo=$[curFileNum+2]
		DATE=`date +%Y%m%d -d"${daysAgo} days ago"`
		
		if [ -e "${DOMAIN_TU_RESULT_FILE}_${DATE}" ]
		then
			rm ${DOMAIN_TU_RESULT_FILE}_${DATE}
		fi
		wget -q ${HETU_URL}/${DATE}/${DOMAIN_TU_RESULT_FILE}_${DATE}
		if [ $? -ne 0 ]
		then
			curFileNum=$[curFileNum+1]
			continue
		fi
		
		if [ -e "${DOMAIN_TU_RESULT_FILE}_${DATE}.md5" ]
		then
			rm ${DOMAIN_TU_RESULT_FILE}_${DATE}.md5
		fi
		wget -q ${HETU_URL}/${DATE}/${DOMAIN_TU_RESULT_FILE}_${DATE}.md5
		if [ $? -ne 0 ]
		then
			curFileNum=$[curFileNum+1]
			continue
		fi
		
		md5sum -c ${DOMAIN_TU_RESULT_FILE}_${DATE}.md5
		if [ $? -ne 0 ]
		then
			curFileNum=$[curFileNum+1]
			continue
		fi
		
		awk -v'OFS=\t' '{if($3>=0&&$4>=0&&$5>=0&&$7>=0&&$10>=0&&$11>=0){print $1,$2,$3,$4*$5,$10+$11,$7}}' ${DOMAIN_TU_RESULT_FILE}_${DATE} | sort -t $'\t' -n > ${DOMAIN_TU_FINAL_FILE}.${curFileNum}
		rm ${DOMAIN_TU_RESULT_FILE}_${DATE}*
		msg="Download ${DOMAIN_TU_RESULT_FILE} success! Sort&awk ${DOMAIN_TU_RESULT_FILE}_${DATE} to ${DOMAIN_TU_FINAL_FILE}.${curFileNum}"
		echo "${msg}"
		log "TRACE" "${msg}"
		hasGetFile=1
		curFileNum=$[curFileNum+1]
	done
	
	if [ ${hasGetFile} -eq 0 ]
	then
		return 1
	else
		return 0
	fi
}

####### The main function
log "TRACE" "START-------------`date +%Y%m%d_%H:%M:%S`"

msg="Fail to download file : ${DOMAIN_RESULT_FILE}!"
geDomainResultFile
alert $? "${msg}"

msg="Fail to download file : ${DOMAIN_COOKIEINFO_FILE}!"
geDomainCookieinfoFile
alert $? "${msg}"

msg="Fail to merge file ${DOMAIN_RESULT_FILE},${DOMAIN_COOKIEINFO_FILE} to ${DOMAIN_FINAL_FILE}!"
mergeToDomainFinalFile
alert $? "${msg}"

msg="Fail to download file : ${DOMAIN_TU_RESULT_FILE}!"
getDomainTuFile
alert $? "${msg}"

log "TRACE" "MIDDLE,download file done---------------:`date +%Y%m%d_%H:%M:%S`"

java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WM123SiteScoreRefresher 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "Fail to execute WMSiteIndexImporter!"

log "TRACE" "END,compute score && update to DB done---------------:`date +%Y%m%d_%H:%M:%S`"
exit 0