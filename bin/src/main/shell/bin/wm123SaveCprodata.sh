#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../conf/wm123SaveCprodata.conf"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source ${CONF_SH} || echo "not exist ${CONF_SH}"

program=wm123SaveCprodata.sh
reader_list=lvzichan

mkdir -p ${WM123_TMPDATA_PATH}
mkdir -p ${WM123_DATA_PATH}


log "TRACE" "START-------------`date +%Y%m%d_%H:%M:%S`"
cd ${WM123_TMPDATA_PATH}
rm *

DATE=`date +%Y%m%d -d"2 days ago"`
wget -q ${DOMAIN_URL}/${DATE}/${DOMAIN_COOKIEINFO_FILE}_${DATE}
alert $? "Fail to download ${DOMAIN_COOKIEINFO_FILE}_${DATE}"
wget -q ${DOMAIN_URL}/${DATE}/${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5
alert $? "Fail to download ${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5"
md5sum -c ${DOMAIN_COOKIEINFO_FILE}_${DATE}.md5
alert $? "Fail to md5sum -c ${DOMAIN_COOKIEINFO_FILE}_${DATE}"

wget -q ${DOMAIN_URL}/${DATE}/${DOMAIN_HOURLY_FILE}_${DATE}
alert $? "Fail to download ${DOMAIN_HOURLY_FILE}_${DATE}"
wget -q ${DOMAIN_URL}/${DATE}/${DOMAIN_HOURLY_FILE}_${DATE}.md5
alert $? "Fail to download ${DOMAIN_HOURLY_FILE}_${DATE}.md5"
md5sum -c ${DOMAIN_HOURLY_FILE}_${DATE}.md5 
alert $? "Fail to md5sum -c ${DOMAIN_HOURLY_FILE}_${DATE}"

awk 'NR==FNR {map[$1]=$1"\t"$3"\t"$6"\t"$9*100"\t"$10;}
	NR>FNR {if(map[$1]"x" != "x") {map[$1]=map[$1]"\t"$2":"$4}}
	END{for(i in map) {print map[i]}}' ${DOMAIN_COOKIEINFO_FILE}_${DATE} ${DOMAIN_HOURLY_FILE}_${DATE} > ${WM123_DATA_PATH}/${DOMAIN_FINAL_FILE}_${DATE}

log "TRACE" "MIDDLE,download file from hetu done---------------:`date +%Y%m%d_%H:%M:%S`"

java -Xms1024m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.unionsite.WM123SiteCprodataImporter  -i${WM123_DATA_PATH}/${DOMAIN_FINAL_FILE}_${DATE} -i ${WM123_DATA_PATH}/${DOMAIN_SAVE_TO_DB}_${DATE} 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? "Fail to execute WM123SiteCprodataImporter!"

log "TRACE" "END---------------:`date +%Y%m%d_%H:%M:%S`"
exit 0