#!/bin/sh
#@file:exportAtRightInfo.sh
#@author:wangxiongjie
#@date:2014-01-09
#@version:1.0.0.0
#@brief:出发哨兵层级增量

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
program=das_sentinel.sh

#LOG_FILE=${LOG_PATH}/das_sentinel.log

function main(){
	sql="update beidou.data_flow_monitor set monitor_time=unix_timestamp() where [userid];"
	
	msg="trigger data flow failed at "`date +"%Y-%m-%d %H:%M:%S"`
	runsql_sharding "${sql}"
	if [ $? -ne "0" ]
	then
		echo "$msg"
	fi
	#echo "trigger data flow at "`date +"%Y-%m-%d %H:%M:%S"` >> LOG_FILE
}

main
