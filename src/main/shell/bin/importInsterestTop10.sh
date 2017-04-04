#!/bin/bash

#@file: importInsterestTop10.sh
#@author: dongguoshuang
#@date: 2012-06-11
#@version: 1.0.0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="${CONF_PATH}/importInsterestTop10.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

mkdir -p ${DATA_PATH}
mkdir -p ${LOG_PATH}

program=importInsterestTop10.sh
reader_list=dongguoshuang
alias wget="wget -c -T 180 -t 5 --limit-rate=30m"
LOG_FILE=${LOG_PATH}"/"importInsterestTop10`date +%Y%m%d`".log"

STAT_DATE=`date +%Y%m%d`

cd ${DATA_PATH}
rm -rf *

#��ȡ��Ȥ��������TOP10�����ļ�
wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SEARCHWORD_FILE_NAME}
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ��������TOP10�����ļ�${SEARCHWORD_FILE_NAME}ʱ,ִ��wget����������."
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SEARCHWORD_FILE_NAME}|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ��������TOP10�����ļ�${SEARCHWORD_FILE_NAME}��С�д���." 
	exit ${EXE_FAIL}
fi

wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SEARCHWORD_FILE_NAME}.md5
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ��������TOP10����MD5�ļ�${SEARCHWORD_FILE_NAME}.ʱ,ִ��wget����������."
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.md5|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ��������TOP10����MD5�ļ�${SEARCHWORD_FILE_NAME}.md5ʱ�ļ���С�д���."
	exit ${EXE_FAIL}
fi

if [ "`md5sum -c ${SEARCHWORD_FILE_NAME}.md5 |grep OK`" = "" ]; then  
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},��Ȥ��������TOP10�����ļ�${SEARCHWORD_FILE_NAME}��md5��֤����ȷ."
	exit ${EXE_FAIL}
fi

#��ȡ��Ȥ�������ΪTOP10�����ļ�
wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SITEVISIT_FILE_NAME}
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ�������ΪTOP10�����ļ�${SITEVISIT_FILE_NAME}ʱ,ִ��wget����������"
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SITEVISIT_FILE_NAME}|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ�������ΪTOP10�����ļ�${SITEVISIT_FILE_NAME}��С�д���." 
	exit ${EXE_FAIL}
fi

wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SITEVISIT_FILE_NAME}.md5
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ�������ΪTOP10����MD5�ļ�${SITEVISIT_FILE_NAME}.md5ʱ,ִ��wget����������." 
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SITEVISIT_FILE_NAME}.md5|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},������Ȥ�������ΪTOP10����MD5�ļ�${SITEVISIT_FILE_NAME}.md5��С�д���." 
	exit ${EXE_FAIL}
fi

if [ "`md5sum -c ${SITEVISIT_FILE_NAME}.md5 |grep OK`" = "" ]; then  
	alert 1 "[Error]:[importInsterestTop10.sh]��${CURR_DATE},��Ȥ�������ΪTOP10�����ļ�${SITEVISIT_FILE_NAME}.md5��md5��֤����ȷ."
	exit ${EXE_FAIL}
fi

cat ${SEARCHWORD_FILE_NAME} | tr -d " " > ${SEARCHWORD_FILE_NAME}.tmp
#��Ȥ��������TOP10���쵼��sql
echo "/*!40101 SET NAMES utf8 */;" >${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql
awk -v quote="'" '{print "insert into beidoure.insterestsearchword(interestId,word,searchTimes,createTime) values("$1","quote$2quote","$3",now());"}' ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.tmp >> ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql

#��Ȥ��������TOP10ִ�����ݵ���
msg="ִ�е���${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql�ļ����ʧ��,������ȼ��鿴"

clear_sql="delete from beidoure.insterestsearchword"
runsql_re "$clear_sql" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

db_sql="source ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql"
runsql_re "$db_sql" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

cat ${SITEVISIT_FILE_NAME} | tr -d " " > ${SITEVISIT_FILE_NAME}.tmp
#��Ȥ�������ΪTOP10���쵼��sql
echo "/*!40101 SET NAMES utf8 */;" >${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql
awk -v quote="'" '{print "insert into beidoure.insterestsitevisit(interestId,site,viewTimes,createTime) values("$1","quote$2quote","$3",now());"}' ${DATA_PATH}/${SITEVISIT_FILE_NAME}.tmp >> ${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql

#��Ȥ�������ΪTOP10ִ�����ݵ���
msg="ִ�е���${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql�ļ����ʧ��,������ȼ��鿴"

clear_sql="delete from beidoure.insterestsitevisit"
runsql_re "$clear_sql" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

db_sql="source ${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql"
runsql_re "$db_sql" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi


echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` import data to db ok!!" >> $LOG_FILE
echo "===========================================================================" >> $LOG_FILE

exit 0
