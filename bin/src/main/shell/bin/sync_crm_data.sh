#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

YF_SERVER="yf-beidou-cron01.yf01.baidu.com"
CRM_DAT_PATH="/home/work/var/crm"
CRMFILE_NAME=crm_

BUSINESS_FILE_PATH="/home/work/beidou-cron/data/crm"
BUSINESS_FILE_NAME="business"

DAY_SRCH_FILE_NAME="day_srchs"

#sync /home/work/beidou-cron/data/crm/business

function sync_crm_dat(){
	STAT_DATE=`date -d 'yesterday' +%Y%m%d`
	CRMFILE=${CRM_DAT_PATH}/${CRMFILE_NAME}${STAT_DATE}.dat
	CRMFILE_MD5=${CRM_DAT_PATH}/${CRMFILE_NAME}${STAT_DATE}.dat.md5
	if [ -f ${CRMFILE} ] ;then
		rm -f ${CRMFILE}
	fi
	if [ -f ${CRMFILE_MD5} ] ;then
		rm -f ${CRMFILE_MD5}
	fi
	
	msg="sync crm data from yf to tc failed."
	wget ftp://${YF_SERVER}/${CRMFILE} -O ${CRMFILE}
	alert $? "${msg}"
	
	wget ftp://${YF_SERVER}/${CRMFILE_MD5} -O ${CRMFILE_MD5}
	alert $? "${msg}"
}

function sync_business_file(){
	STAT_DATE=`date -d 'yesterday' +%Y%m%d`
	
	BUSINESS_FILE=${BUSINESS_FILE_PATH}/${BUSINESS_FILE_NAME}
	BUSINESS_FILE_MD5=${BUSINESS_FILE_PATH}/${BUSINESS_FILE_NAME}.md5
	
	BUSINESS_DATE_FILE=${BUSINESS_FILE_PATH}/${BUSINESS_FILE_NAME}.${STAT_DATE}
	BUSINESS_DATE_FILE_MD5=${BUSINESS_FILE_PATH}/${BUSINESS_FILE_NAME}.${STAT_DATE}.md5
	
	if [ -f ${BUSINESS_FILE} ] ;then
		rm -f ${BUSINESS_FILE}
	fi
	if [ -f ${BUSINESS_FILE_MD5} ] ;then
		rm -f ${BUSINESS_FILE_MD5}
	fi
	if [ -f ${BUSINESS_DATE_FILE} ] ;then
		rm -f ${BUSINESS_DATE_FILE}
	fi
	if [ -f ${BUSINESS_DATE_FILE_MD5} ] ;then
		rm -f ${BUSINESS_DATE_FILE_MD5}
	fi
	
	msg="sync business file from yf to tc failed."
	wget ftp://${YF_SERVER}/${BUSINESS_FILE} -O ${BUSINESS_FILE}
	alert $? "${msg}"
	
	wget ftp://${YF_SERVER}/${BUSINESS_FILE_MD5} -O ${BUSINESS_FILE_MD5}
	alert $? "${msg}"
	
	msg="sync business date file from yf to tc failed."
	wget ftp://${YF_SERVER}/${BUSINESS_DATE_FILE} -O ${BUSINESS_DATE_FILE}
	alert $? "${msg}"
	
	wget ftp://${YF_SERVER}/${BUSINESS_DATE_FILE_MD5} -O ${BUSINESS_DATE_FILE_MD5}
	alert $? "${msg}"
}

function sync_day_srch_file(){
	STAT_DATE=`date -d 'yesterday' +%Y%m%d`
	SRCH_FILE=${BUSINESS_FILE_PATH}/${DAY_SRCH_FILE_NAME}.${STAT_DATE}
	SRCH_FILE_MD5=${BUSINESS_FILE_PATH}/${DAY_SRCH_FILE_NAME}.${STAT_DATE}.md5
	
	msg="sync day srch file from yf to tc failed."
	wget ftp://${YF_SERVER}${SRCH_FILE} -O ${SRCH_FILE}
	alert $? "${msg}"
	
	wget ftp://${YF_SERVER}${SRCH_FILE_MD5} -O ${SRCH_FILE_MD5}
	alert $? "${msg}"
}

function main(){

	sync_crm_dat
	
	sync_business_file
	
	sync_day_srch_file
}

main
exit $?
