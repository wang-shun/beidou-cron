#!/bin/sh
#@file:exportAtRightInfo.sh
#@author:hujunhai
#@date:2013-12-04
#@version:1.0.0.0
#@brief:导出beidou.plan_atright_info中AT右信息给检索端使用

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/exportAtRightInfo.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=exportAtRightInfo.sh

LOG_FILE=${LOG_PATH}/exportAtRightInfo.log
DATA_FILE=at_right_info.data

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

function main(){
	
	sql="select userid,planid,groupid,business_keywords,refer_url from beidou.group_atright_info where [userid];"
	
	rm -f $DATA_FILE
	for((i=0;i<${SHARDING_SLICE};i++));
	do
	   msg="export data from beidou.plan_atright_info failed,db sharding is "$i
	   runsql_single_read "$sql" ${DATA_FILE}.${i} ${i}
	   alert $? "${msg}"
	   
	   cat ${DATA_FILE}.${i} >> $DATA_FILE
	   rm -f ${DATA_FILE}.${i}
	done
}

main