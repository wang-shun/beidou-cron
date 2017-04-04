#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DCAPP_SERVER=`get_instance_by_service dcapp.NOVA.all|awk -v r=${RANDOM} '{a[NR]=$0; len=NR}END{print a[r%len+1]}'|sed 's/[ \t]*$//g'`
DCAPP_ROOT_PATH="/home/work/var/CI_DATA/beidou/beidou-cron/data/"
DCAPP_DATA_PATH="adtrade/export/output/"

TC_PATH="/home/work/beidou-cron/data/adtrade/export/output/"
TC_BAK_PATH="/home/work/beidou-cron/data/adtrade/export/backup/"

SYNC_FILE="beidouad.txt"

function sync_adtrade_data(){
	
    cd ${TC_BAK_PATH}
    
    DCAPP_ADTRADE_FILE=${DCAPP_ROOT_PATH}${DCAPP_DATA_PATH}${SYNC_FILE}
	DCAPP_ADTRADE_FILE_MD5=${DCAPP_ROOT_PATH}${DCAPP_DATA_PATH}${SYNC_FILE}.md5
	
	TC_ADTRADE_BAK_FILE=${TC_BAK_PATH}${SYNC_FILE}
	TC_ADTRADE_BAK_FILE_MD5=${TC_BAK_PATH}${SYNC_FILE}.md5
	
	TC_ADTRADE_FILE=${TC_PATH}${SYNC_FILE}
	TC_ADTRADE_FILE_MD5=${TC_PATH}${SYNC_FILE}.md5
	
	if [ -f ${TC_ADTRADE_BAK_FILE} ] ;then
		rm -f ${TC_ADTRADE_BAK_FILE}
	fi
	if [ -f ${TC_ADTRADE_BAK_FILE_MD5} ] ;then
		rm -f ${TC_ADTRADE_BAK_FILE_MD5}
	fi
	
	msg="sync beidouad.txt from dcapp to tc failed."
	wget ftp://${DCAPP_SERVER}/${DCAPP_ADTRADE_FILE} -O ${TC_ADTRADE_BAK_FILE}
	alert $? "${msg}"
	
	wget ftp://${DCAPP_SERVER}/${DCAPP_ADTRADE_FILE_MD5} -O ${TC_ADTRADE_BAK_FILE_MD5}
	alert $? "${msg}"
	
	msg="check beidouad.txt.md5 from dcapp to tc failed."
	md5sum -c ${TC_ADTRADE_BAK_FILE_MD5} > /dev/null
	alert $? "${msg}"
	
	if [ -f ${TC_ADTRADE_FILE} ] ;then
		rm -f ${TC_ADTRADE_FILE}
	fi
	
	if [ -f ${TC_ADTRADE_FILE_MD5} ] ;then
		rm -f ${TC_ADTRADE_FILE_MD5}
	fi
	
	mv ${TC_ADTRADE_BAK_FILE} ${TC_ADTRADE_FILE}
	mv ${TC_ADTRADE_BAK_FILE_MD5} ${TC_ADTRADE_FILE_MD5}
}

function main(){
	sync_adtrade_data
}

main
exit $?
