#!/bin/bash

#@file: import_atleft_trade.sh
#@author: caichao


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/import_atleft_trade.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


program=import_atleft_trade.sh
curr_date=`date  "+%Y%m%d"`

WORK_PATH=${DATA_PATH}/atleft_trade
LOG_PATH=${LOG_PATH}/import_atleft_trade
LOG_NAME=import_atleft_trade_shell
LOG_FILE=${LOG_PATH}/${LOG_NAME}.${curr_date}.log 


mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${WORK_PATH}

function INF()
{
 echo $1
 echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}
function ERR()
{
 echo $1
 echo "[ERROR] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}

INF "begin task..."

#export�������ӽ��̿��Ի�ȡ
export WORK_PATH
export BIN_PATH

ATLEFT_TRADE=${WORK_PATH}/atleft_trade
ALL_TRADE=${WORK_PATH}/all_beidou_trade
JAVA_HANDLE_TRADE=${WORK_PATH}/trade_for_java
COUNT=${WORK_PATH}/count

if [ -e ${ATLEFT_TRADE} ];then
	rm -f ${ATLEFT_TRADE}
fi

if [ -e ${ALL_TRADE} ];then
	rm -f ${ALL_TRADE} 
fi

msg="read trade data from ubi fail"
runsql_atlefttrade_read "set names 'utf8';select userid,tradeid,ifnull(tradename_1,''),ifnull(tradename_2,''),ifnull(ka_trade1,''),ifnull(ka_trade2,''),floor(tradeid / 100) from beidou_conf.dim_user where userid > 0" "${ATLEFT_TRADE}"
alert $? "${msg}"

#sleep һ��ʱ�䣬Ŀ���Ƿ�ֹץȡ�������������ڸ��£��������ܼ�⵽count��wc -l����ȣ�������ֱ���˳�
sleep 30s

msg="count all ubi data"
runsql_atlefttrade_read "select count(*) from beidou_conf.dim_user" "${COUNT}"
alert $? "${msg}"

count_sharding=`cat ${COUNT} | awk '{print $1}'`
file_count=`wc -l ${ATLEFT_TRADE} | awk '{print $1}'`

msg="count not match,ubi data updating..."
if [ ${count_sharding}"x" != ${file_count}"x" ];then
	alert $? "${msg}"
fi

msg="read all trade from beidou db fail"
runsql_sharding_read "set names 'utf8';select userid,tradeid,ifnull(tradename1,''),ifnull(tradename2,''),ifnull(ka_trade1,''),ifnull(ka_trade2,''),first_tradeid from beidou.usertrade" "${ALL_TRADE}"
alert $? "${msg}"

#diff������java�бȶԵģ������������Ҫɾ�����������¼����ļ�����java����
msg="add need to delete trade to file fail"
awk -F'\t' 'ARGIND==1{userid[$1]=$1}ARGIND==2{if(userid[$1]==""){print $1"\t-1\t-1\t-1\t-1\t-1\t-1"}}' ${ATLEFT_TRADE} ${ALL_TRADE} >> ${ATLEFT_TRADE}
alert $? "${msg}"

#diff����java���㣬�������cpuʹ����
msg="���db��ץȡubi����diff�г���"
grep -Fxvf ${ALL_TRADE} ${ATLEFT_TRADE} > ${JAVA_HANDLE_TRADE}
#alert $? "${msg}"

#ȥ������
msg="ȥ�����г���"
sed -i '/^$/d' ${JAVA_HANDLE_TRADE}
alert $? "${msg}"

#����ļ�Ϊ�գ�ֱ�������˳�
file_count=`wc -l ${JAVA_HANDLE_TRADE} | awk '{print $1}'`
if [ ${file_count}"x" = 0"x" ];then
	INF "no diff,exit now"
	exit 0
fi

## ����ļ����������ȷ���
uniq_num=`awk -F'\t' '{print NF}' ${JAVA_HANDLE_TRADE} | uniq | wc -l`
if [ ${uniq_num} -ne 1 ]
then
 	msg="�ļ�${JAVA_HANDLE_TRADE}�������ͳһ,�ļ����ܵ���"
 	ERR "${msg}"
 	alert 1 "${msg}"
fi


#����trade
java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.atleft.service.AtLeftTradeMain ${JAVA_HANDLE_TRADE} > ${LOG_FILE} 2>&1 || alert $? "[Error]import_atleft_trade" 

INF "finished"
