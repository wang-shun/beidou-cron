#!/bin/sh

#author:zhangpingan


ROOT_PATH=/home/work/beidou-cron
DATA_PATH=${ROOT_PATH}/data
LOG_PATH=${ROOT_PATH}/log
CONF_PATH=${ROOT_PATH}/conf


CONF_SH="${CONF_PATH}/process_monitor.conf"
[ -f "${CONF_SH}" ] || echo "not exist ${CONF_SH} "

LOG_FILE=${LOG_PATH}/process_monitor.log

function PRINT_LOG()
{
    echo "[`date +"%Y%m%d-%H:%M:%S"`]$1" >> ${LOG_FILE}
}

function CT_MONITOR()
{
#function:To Monitor Some Task OverTime Running.
#parameter: param 1:Proc_name, param 2: OverTime(Seconds)
#param3 is optional,if the there are several ct task using  same script, then param3 is the input param of the task
	PROC_NAME="$1"
	TIMEOUT="$2"
	PROC_INPUT_PARAM="$3"
	if [ ! -z $PROC_INPUT_PARAM ];then
		PROC_NAME="${PROC_NAME} ${PROC_INPUT_PARAM}"
	fi
	


	TIME_COST=$(((i+1)*MONITOR_INTERVAL))
	pid=`ps aux  |grep "${PROC_NAME}" | grep work | grep -v "\-bash" | grep -v $0  |grep -v "grep" | head -1 | awk '{print $2}'`;
	if [ ! -z $pid ];then
		time_elipse=`ps aux | awk -v v=$pid '{if($2==v){print $9}}'`;
		if [ -z ${time_elipse} ];then
			return 0
		fi
		datestr=`date +%Y-%m-%d`
		time_begin=`date -d "${datestr} ${time_elipse}:00" +%s`
		time_curr=`date +%s`
		TIME_COST=$((time_curr-time_begin))

		if [ ${TIME_COST} -lt ${TIMEOUT} ];then
			PRINT_LOG "${PROC_NAME}("$pid") has running ${TIME_COST} Seconds(OutTime: ${TIMEOUT} Seconds)"
		else
			PRINT_LOG "${PROC_NAME}("$pid") has overtime running.killed"
			kill ${pid}
			return 0
		fi
	fi
}

cat ${CONF_SH} | while read line
do
    FIELDS=`echo $line | awk -F'#' '{print NF}'`;
	if [ ! -z $FIELDS ] && [ $FIELDS -ge 2  ];then
		CT_NAME=`echo $line | awk -F'#' '{print $1}'`
		CT_OVERTIME=`echo $line | awk -F'#' '{print $2}'`
		CT_PARAM=`echo $line | awk -F'#' '{print $3}'`
		CT_MONITOR ${CT_NAME} ${CT_OVERTIME} ${CT_PARAM}
	fi
done


