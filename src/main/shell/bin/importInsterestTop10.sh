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

#获取兴趣点搜索词TOP10数据文件
wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SEARCHWORD_FILE_NAME}
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点搜索词TOP10数据文件${SEARCHWORD_FILE_NAME}时,执行wget命令有问题."
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SEARCHWORD_FILE_NAME}|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点搜索词TOP10数据文件${SEARCHWORD_FILE_NAME}大小有错误." 
	exit ${EXE_FAIL}
fi

wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SEARCHWORD_FILE_NAME}.md5
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点搜索词TOP10数据MD5文件${SEARCHWORD_FILE_NAME}.时,执行wget命令有问题."
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.md5|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点搜索词TOP10数据MD5文件${SEARCHWORD_FILE_NAME}.md5时文件大小有错误."
	exit ${EXE_FAIL}
fi

if [ "`md5sum -c ${SEARCHWORD_FILE_NAME}.md5 |grep OK`" = "" ]; then  
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},兴趣点搜索词TOP10数据文件${SEARCHWORD_FILE_NAME}的md5验证不正确."
	exit ${EXE_FAIL}
fi

#获取兴趣点浏览行为TOP10数据文件
wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SITEVISIT_FILE_NAME}
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点浏览行为TOP10数据文件${SITEVISIT_FILE_NAME}时,执行wget命令有问题"
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SITEVISIT_FILE_NAME}|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点浏览行为TOP10数据文件${SITEVISIT_FILE_NAME}大小有错误." 
	exit ${EXE_FAIL}
fi

wget  ftp://${SOURCE_SERVER}${SOURCE_PATH}${SITEVISIT_FILE_NAME}.md5
if [ "$?" -ne "0" ]; then	
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点浏览行为TOP10数据MD5文件${SITEVISIT_FILE_NAME}.md5时,执行wget命令有问题." 
	exit ${EXE_FAIL}
fi

if [ "`ls -l ${DATA_PATH}/${SITEVISIT_FILE_NAME}.md5|awk '{print $5}'`" -lt "1" ]; then
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},下载兴趣点浏览行为TOP10数据MD5文件${SITEVISIT_FILE_NAME}.md5大小有错误." 
	exit ${EXE_FAIL}
fi

if [ "`md5sum -c ${SITEVISIT_FILE_NAME}.md5 |grep OK`" = "" ]; then  
	alert 1 "[Error]:[importInsterestTop10.sh]在${CURR_DATE},兴趣点浏览行为TOP10数据文件${SITEVISIT_FILE_NAME}.md5的md5验证不正确."
	exit ${EXE_FAIL}
fi

cat ${SEARCHWORD_FILE_NAME} | tr -d " " > ${SEARCHWORD_FILE_NAME}.tmp
#兴趣点搜索词TOP10构造导入sql
echo "/*!40101 SET NAMES utf8 */;" >${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql
awk -v quote="'" '{print "insert into beidoure.insterestsearchword(interestId,word,searchTimes,createTime) values("$1","quote$2quote","$3",now());"}' ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.tmp >> ${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql

#兴趣点搜索词TOP10执行数据导入
msg="执行导入${DATA_PATH}/${SEARCHWORD_FILE_NAME}.sql文件多次失败,请高优先级查看"

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
#兴趣点浏览行为TOP10构造导入sql
echo "/*!40101 SET NAMES utf8 */;" >${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql
awk -v quote="'" '{print "insert into beidoure.insterestsitevisit(interestId,site,viewTimes,createTime) values("$1","quote$2quote","$3",now());"}' ${DATA_PATH}/${SITEVISIT_FILE_NAME}.tmp >> ${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql

#兴趣点浏览行为TOP10执行数据导入
msg="执行导入${DATA_PATH}/${SITEVISIT_FILE_NAME}.sql文件多次失败,请高优先级查看"

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
