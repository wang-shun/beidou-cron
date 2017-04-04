#!/bin/sh

#@file:importPriceRecommendInfo.sh
#@author:dongying
#@date:2012-05-22
#@version:1.0.0.0
#@brief:import priceRecommendInfo into beidou db from beidou-cron

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importPriceRecommendInfo.conf
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

program=importPriceRecommendInfo.sh
reader_list=dongying01


LOG_FILE=${LOG_PATH}/importPriceRecommendInfo.log

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

#���ɱ���ץȡ�ļ���ʱ���
if [ -z $1 ]; then
	TIMESTAMP=`date +%Y%m%d -d"1 days ago"`
else
	TIMESTAMP=$1
fi

#����ץȡ�ļ�����
filename=price_recommend_${TIMESTAMP}.txt

cd ${DATA_PATH}


############################################################
### Step1. ׼����ʼ
############################################################

echo "===========================================================================" >> $LOG_FILE
echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` begin to import priceRecommendInfo data!!" >> $LOG_FILE

#ץȡ�ļ�
getfile_command="wget -q -c ${PRICE_RECOMMEND_DATA_PREFIX}&date=${TIMESTAMP} -O ${DATA_PATH}/${filename}"
clear_command="cd $DATA_PATH && if [[ -f ${filename} ]]; then rm ${filename}; fi"
msg="��${PRICE_RECOMMEND_DATA_PREFIX}ץȡ${filename}���ʧ�ܣ�����ϵ���β鿴"
getfile "$getfile_command" "$clear_command" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
	alert 1 $msg
fi

#ץȡmd5
getfile_command="wget -q -c ${PRICE_RECOMMEND_MD5_PREFIX}&date=${TIMESTAMP} -O ${DATA_PATH}/${filename}.md5.tmp"
clear_command="cd $DATA_PATH && if [[ -f ${filename}.md5.tmp ]]; then rm ${filename}.md5.tmp; fi"
msg="��${PRICE_RECOMMEND_MD5_PREFIX}ץȡ${filename}.md5���ʧ�ܣ�����ϵ���β鿴"
getfile "$getfile_command" "$clear_command" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` down load file and md5 ok" >> $LOG_FILE

awk -vfname="$filename" '{print $2"  "fname}' ${DATA_PATH}/${filename}".md5.tmp" > ${DATA_PATH}/${filename}".md5"
rm ${DATA_PATH}/${filename}".md5.tmp"
msg="Fail to check md5 for $filename"
md5sum -c ${DATA_PATH}/${filename}".md5" > /dev/null
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi

#�ļ�����ֻȡǰ28������
cut -f1-28  ${DATA_PATH}/${filename} > ${DATA_PATH}/${filename}.tmp

#ÿ��28��
outfilename=${filename}".check"
awk '{if(NF==28){print $0}}' ${DATA_PATH}/${filename}.tmp > ${DATA_PATH}/${outfilename}
beforenum=`wc -l ${DATA_PATH}/${filename}.tmp|awk '{print $1}'`
afternum=`wc -l ${DATA_PATH}/${outfilename}|awk '{print $1}'`
msg="��������${DATA_PATH}/${filename}�д���$(($beforenum-$afternum))���쳣���ݣ�����м��"
if [[ $beforenum -ne $afternum ]]; then
    echo "[WARN] `date +"%Y-%m-%d %H:%M:%S"` "$msg >> $LOG_FILE
fi

echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` check md5 and file col num ok!!" >> $LOG_FILE

#���쵼��sql
awk '{print "insert into aot.pricerecommendinfo(secondtradeid,grouptype,firstregionid,targettype,totalpv,totalbid,bid10,bid20,bid30,bid40,bid50,bid60,bid70,bid80,bid90,bid100,avgbid,cpm10,cpm20,cpm30,cpm40,cpm50,cpm60,cpm70,cpm80,cpm90,cpm100,avgcpm) values("$1","$2","$3","$4","$5","$6","$7","$8","$9","$10","$11","$12","$13","$14","$15","$16","$17","$18","$19","$20","$21","$22","$23","$24","$25","$26","$27","$28");"}' ${DATA_PATH}/${outfilename} > ${DATA_PATH}/${outfilename}.sql


#ִ�����ݵ���
db_sql="source ${DATA_PATH}/${outfilename}.sql"
clear_sql="delete from aot.pricerecommendinfo"
msg="ִ�е���${DATA_PATH}/${outfilename}.sql�ļ����ʧ��,������ȼ��鿴"
runsql_xdb "$clear_sql" >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi
runsql_xdb "$db_sql"  >> $LOG_FILE
if [[ $? -ne 0 ]]; then
    alert 1 $msg
fi


echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` import data to db ok!!" >> $LOG_FILE
echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` end to import priceRecommendInfo data!! " >> $LOG_FILE
echo "===========================================================================" >> $LOG_FILE
