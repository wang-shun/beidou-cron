#!/bin/bash

#@file: send_message.sh
#@author: caichao


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"



program=send_message.sh

curr_date=`date  "+%Y%m%d"`
before_yestday_date=`date "-d 2 day ago" "+%y%m%d"`
yestday_date=`date "-d 1 day ago" "+%y%m%d"`



WORK_PATH=${DATA_PATH}/send_message
LOG_PATH=${LOG_PATH}/send_message
LOG_NAME=send_message
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

#export变量，子进程可以获取
export WORK_PATH
export BIN_PATH

BEFORE_YESTDAY=${WORK_PATH}/before_yestday
YESTDAY=${WORK_PATH}/yestday
ALL_USER=${WORK_PATH}/all_user
JAVA_HANDLE=${WORK_PATH}/message_for_java
OFFSET=${WORK_PATH}/offset
MONITOR_DATA=${WORK_PATH}/monitor_data

#手动重跑传入的参数参考： sh send_message.sh 140613 140614 &
if [ $# -eq 2 ];then
	before_yestday_date=$1
	yestday_date=$2
	echo -e "2014-06-16\t0" > $OFFSET
fi

if [ -e ${BEFORE_YESTDAY} ];then
	rm -f ${BEFORE_YESTDAY}
fi

if [ -e ${YESTDAY} ];then
	rm -f ${YESTDAY} 
fi

msg="read the day of before yestday data fail"
runsql_clk_read "set names 'utf8';select userid,sum(bid) from SF_Click.clk"${before_yestday_date}" group by userid" "${BEFORE_YESTDAY}"
alert $? "${msg}"

msg="read yestday data fail"
runsql_clk_read "set names 'utf8';select userid,sum(bid) from SF_Click.clk"${yestday_date}" group by userid" "${YESTDAY}"
alert $? "${msg}"


msg="read all user fail"
runsql_cap_read "set names 'utf8';select userid,username from beidoucap.useraccount" "${ALL_USER}"
alert $? "${msg}"

awk -F'\t' '
	ARGIND==1{
		before[$1]=$2
	}
	ARGIND==2{
		yestday[$1]=$2
	}
	ARGIND==3{
		if(before[$1]"x"=="x"){
			b=0;
		} else {
			b=before[$1];
		}

		if(yestday[$1]"x"=="x") {
			y=0;
		}else {
			y=yestday[$1];
		}
		printf("%d\t%s\t%.2f\t%.2f\n",$1,$2,b,y)
	}

' ${BEFORE_YESTDAY} ${YESTDAY} ${ALL_USER} |  awk -F'\t' '{if(!($3==0&&$4==0)){print $0}}' > ${JAVA_HANDLE}

#从线下抓取测试数据，一方面可以看到消息触发，另一方面可以用户监控
wget -q -t3 -O${MONITOR_DATA} ftp://cq01-rdqa-pool181.cq01.baidu.com/home/beidou/caichao/send_massage_online/send_message_monitor
cat ${MONITOR_DATA} >> ${JAVA_HANDLE}


#去除空行
msg="去除空行出错"
sed -i '/^$/d' ${JAVA_HANDLE}
alert $? "${msg}"

RETRY_TIME=5
#发送message

for((i=1;i<=${RETRY_TIME};i++));
do
	java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.message.service.SendMessageMain ${JAVA_HANDLE} ${OFFSET} > ${LOG_FILE} 2>&1
	if [ $? -ne 0 ];then
		alert $? "[Error]send_message fail $i times"
		sleep 120s
	else
		exit 0;
	fi
done

INF "finished"
