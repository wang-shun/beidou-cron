#!/bin/sh

#@file:importTradeBaseAcp.sh
#@author:dongying
#@date:2011-12-21
#@version:1.0.0.0
#@brief:import tradebaseacp into aot db from beidou-cron

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importTradeBaseAcp.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
if [ $? -ne 0 ]
then
    alert 1 "Conf error: Fail to load libfile[$LIB_FILE]!"
fi

program=importTradeBaseAcp.sh
reader_list=dongying01


LOG_FILE=${LOG_PATH}/importTradeBaseAcp.log

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

#生成本次抓取文件的时间戳
if [ -z $1 ]; then
	TIMESTAMP=`date +%Y%m%d -d"1 days ago"`
else
	TIMESTAMP=$1
fi

#生成抓取文件名称
filename=trade_acp_${TIMESTAMP}.txt

cd ${DATA_PATH}


############################################################
### Step1. 准备开始
############################################################

echo "===========================================================================" >> $LOG_FILE
echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` begin to import tradebaseacp data!!" >> $LOG_FILE

#抓取文件
getfile_command="wget -q -c ${SOURCE_FILE_PATH}/${TIMESTAMP}/${filename} -O ${DATA_PATH}/${filename}"
clear_command="cd $DATA_PATH && if [[ -f ${filename} ]]; then rm ${filename}; fi"
msg="从${SOURCE_FILE_PATH}抓取${filename}多次失败，请联系上游查看"
getfile "$getfile_command" "$clear_command" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
	alert 1 $msg
fi

#抓取md5
getfile_command="wget -q -c ${SOURCE_FILE_PATH}/${TIMESTAMP}/${filename}.md5 -O ${DATA_PATH}/${filename}.md5"
clear_command="cd $DATA_PATH && if [[ -f ${filename}.md5 ]]; then rm ${filename}.md5; fi"
msg="从${SOURCE_FILE_PATH}抓取${filename}.md5多次失败，请联系上游查看"
getfile "$getfile_command" "$clear_command" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` down load file and md5 ok" >> $LOG_FILE

#校验md5
msg="校验${SOURCE_FILE_PATH}/${TIMESTAMP}/${filename}文件md5值失败，请联系上游查看"
md5sum -c ${DATA_PATH}/${filename}.md5
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

#文件格式校验
#每行16列
outfilename="tradebaseacp.txt"
awk '{if(NF==16){print $0}}' ${DATA_PATH}/${filename} > ${DATA_PATH}/${outfilename}
beforenum=`wc -l ${DATA_PATH}/${filename}|awk '{print $1}'`
afternum=`wc -l ${DATA_PATH}/${outfilename}|awk '{print $1}'`
msg="上游数据${DATA_PATH}/${filename}中存在$(($beforenum-$afternum))条异常数据，请进行检查"
if [[ $beforenum -ne $afternum ]]; then
    alert_mail 1 $msg
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` check md5 and file col num ok!!" >> $LOG_FILE

#构造导入sql
awk '{print "insert into aot.tradebaseacp(secondtradeid,groupclassification,firstregionid,targettype,bid20,bid25,bid30,bid40,bid50,bid60,bid70,bid75,bid80,bid90,avgbid,bidcount) values("$1","$2","$3","$4","$5","$6","$7","$8","$9","$10","$11","$12","$13","$14","$15","$16");"}' ${DATA_PATH}/${outfilename} > ${DATA_PATH}/${outfilename}.sql

#执行数据导入
db_sql="source ${DATA_PATH}/${outfilename}.sql"
clear_sql="delete from aot.tradebaseacp"
msg="执行导入${DATA_PATH}/${outfilename}.sql文件多次失败,请高优先级查看"
runsql_xdb "$clear_sql" 
if [[ $? -ne 0 ]]; then
    alert 1 "$msg"
fi
runsql_xdb "$db_sql"
if [[ $? -ne 0 ]]; then
    alert 1 "$msg"
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` import data to db ok!!" >> $LOG_FILE
echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` end to import tradebaseacp data!! " >> $LOG_FILE
echo "===========================================================================" >> $LOG_FILE
