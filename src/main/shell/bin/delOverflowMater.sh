#!/bin/sh

#CT任务，用于删除export_problem_delmater.sh脚本导出的问题创意

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

function alert()
{
	if [ $1 -ne 0 ];then
		echo $2
	fi
}

# 创建存放delmater数据、日志目录
DELMATER_DATA_PATH=${DATA_PATH}/delmater/data
DELMATER_LOG_PATH=${DATA_PATH}/delmater/log

mkdir -p ${DELMATER_DATA_PATH}
mkdir -p ${DELMATER_LOG_PATH}

DATE=`date +%Y%m%d%H%M`
LOG_PATH=${DATE}.log
touch ${DELMATER_LOG_PATH}/${LOG_PATH}

#wget file
#mcId_mcVersionId_userid为问题创意数据
wget ftp://cp01-ocean-1551.epc.baidu.com/home/work/beidou-cron/data/delmater/data/delmater_before_2015_mcId_mcVersionId_userid_deltime -O ${DELMATER_DATA_PATH}/delmater_before_2015_mcId_mcVersionId_userid_deltime

msg="invoke java failed"
java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.DeleteOverFlowMater ${DELMATER_DATA_PATH}/delmater_before_2015_mcId_mcVersionId_userid_deltime >> ${DELMATER_LOG_PATH}/${LOG_PATH} 2>&1
# if the result of "java" is wrong then send error message
alert $? ${msg}