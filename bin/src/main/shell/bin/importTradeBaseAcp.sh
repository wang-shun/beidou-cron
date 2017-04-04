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

#���ɱ���ץȡ�ļ���ʱ���
if [ -z $1 ]; then
	TIMESTAMP=`date +%Y%m%d -d"1 days ago"`
else
	TIMESTAMP=$1
fi

#����ץȡ�ļ�����
filename=trade_acp_${TIMESTAMP}.txt

cd ${DATA_PATH}


############################################################
### Step1. ׼����ʼ
############################################################

echo "===========================================================================" >> $LOG_FILE
echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` begin to import tradebaseacp data!!" >> $LOG_FILE

#ץȡ�ļ�
getfile_command="wget -q -c ${SOURCE_FILE_PATH}/${TIMESTAMP}/${filename} -O ${DATA_PATH}/${filename}"
clear_command="cd $DATA_PATH && if [[ -f ${filename} ]]; then rm ${filename}; fi"
msg="��${SOURCE_FILE_PATH}ץȡ${filename}���ʧ�ܣ�����ϵ���β鿴"
getfile "$getfile_command" "$clear_command" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
	alert 1 $msg
fi

#ץȡmd5
getfile_command="wget -q -c ${SOURCE_FILE_PATH}/${TIMESTAMP}/${filename}.md5 -O ${DATA_PATH}/${filename}.md5"
clear_command="cd $DATA_PATH && if [[ -f ${filename}.md5 ]]; then rm ${filename}.md5; fi"
msg="��${SOURCE_FILE_PATH}ץȡ${filename}.md5���ʧ�ܣ�����ϵ���β鿴"
getfile "$getfile_command" "$clear_command" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` down load file and md5 ok" >> $LOG_FILE

#У��md5
msg="У��${SOURCE_FILE_PATH}/${TIMESTAMP}/${filename}�ļ�md5ֵʧ�ܣ�����ϵ���β鿴"
md5sum -c ${DATA_PATH}/${filename}.md5
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

#�ļ���ʽУ��
#ÿ��16��
outfilename="tradebaseacp.txt"
awk '{if(NF==16){print $0}}' ${DATA_PATH}/${filename} > ${DATA_PATH}/${outfilename}
beforenum=`wc -l ${DATA_PATH}/${filename}|awk '{print $1}'`
afternum=`wc -l ${DATA_PATH}/${outfilename}|awk '{print $1}'`
msg="��������${DATA_PATH}/${filename}�д���$(($beforenum-$afternum))���쳣���ݣ�����м��"
if [[ $beforenum -ne $afternum ]]; then
    alert_mail 1 $msg
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` check md5 and file col num ok!!" >> $LOG_FILE

#���쵼��sql
awk '{print "insert into aot.tradebaseacp(secondtradeid,groupclassification,firstregionid,targettype,bid20,bid25,bid30,bid40,bid50,bid60,bid70,bid75,bid80,bid90,avgbid,bidcount) values("$1","$2","$3","$4","$5","$6","$7","$8","$9","$10","$11","$12","$13","$14","$15","$16");"}' ${DATA_PATH}/${outfilename} > ${DATA_PATH}/${outfilename}.sql

#ִ�����ݵ���
db_sql="source ${DATA_PATH}/${outfilename}.sql"
clear_sql="delete from aot.tradebaseacp"
msg="ִ�е���${DATA_PATH}/${outfilename}.sql�ļ����ʧ��,������ȼ��鿴"
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
