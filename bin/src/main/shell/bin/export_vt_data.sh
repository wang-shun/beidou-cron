#!/bin/bash

#@file: export_vt_data.sh
#@author: zhangxu
#@date: 2013-01-23
#@version: 1.0.0.0
#@brief: 从DB中导出以下文件rt3people, rt3url供UFS使用，查询广告库vtpeople以及vturl表

cd `dirname $0`

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/export_vt_data.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BAK_PATH}

open_log

cd ${DATA_PATH}
if [ $? -ne 0 ] ; then
	log "FATAL" "${DATA_PATH} do not exists, exit" 
	close_log 1
	exit 1
fi

startMills=`date +"%s"`
startTime=`date +"%Y-%m-%d_%H:%M:%S"`
log "DEBUG" "export begin at ${startTime}" 


#####################################
##### export vt people from DB
#####################################
rm -f ${VT_PEOPLE_FILE}.new
runsql_sharding_read "use beidou; select pid, jsid, userid, type, hpid,alivedays from beidou.vtpeople " "${VT_PEOPLE_FILE}.new" 

rm -f ${VT_PEOPLE_FILE} 
mv ${VT_PEOPLE_FILE}.new ${VT_PEOPLE_FILE}

md5sum ${VT_PEOPLE_FILE} > ${VT_PEOPLE_MD5_FILE} 

log "DEBUG" "finish to export vt people file" 

#####################################
##### export vt url from DB
#####################################
rm -f ${VT_URL_FILE}.new
runsql_sharding_read "use beidou; select pid, url, userid from beidou.vturl" "${VT_URL_FILE}.new" 

rm -f ${VT_URL_FILE} 
mv ${VT_URL_FILE}.new ${VT_URL_FILE}


md5sum ${VT_URL_FILE} > ${VT_URL_MD5_FILE} 

log "DEBUG" "finish to export vt people file" 


#####################################
##### bak data file
#####################################

cp ${VT_PEOPLE_FILE} ${BAK_PATH}/${VT_PEOPLE_FILE}.${startTime}
cp ${VT_PEOPLE_MD5_FILE} ${BAK_PATH}/${VT_PEOPLE_MD5_FILE}.${startTime}

cp ${VT_URL_FILE} ${BAK_PATH}/${VT_URL_FILE}.${startTime}
cp ${VT_URL_MD5_FILE} ${BAK_PATH}/${VT_URL_MD5_FILE}.${startTime}

log "DEBUG" "finish to bak file" 


endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))
log "DEBUG" "export end at `date +"%Y-%m-%d_%H:%M:%S"`, spend time:${spendtime}s" 

close_log 0

