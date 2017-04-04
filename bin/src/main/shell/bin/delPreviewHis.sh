#!/bin/sh

#@file:delPreviewHis.sh
#@author:yangyun
#@date:2010-05-28
#@version:1.0.0.0
#@brief:an user only can store M(=50) preview url history ,so must delete records that exceed ths number of users

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/delPreviewHis.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=delPreviewHis.sh
reader_list=yang_yun

LOG_FILE=${LOG_PATH}/${LOG_NAME}
DATA_FILE=${DATA_PATH}/${DATA_NAME}
mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

start=`date +"%s"`
runsql_xdb_read "use ${HIS_DB}; select userid,count(userid)-${HIS_MAX} as c from ${HIS_TABLE} group by userid having c>0" "${DATA_FILE}"
alert $? "get users of exceed max number failed"

while read str
do
	userid=`echo $str|awk -F ' ' '{print $1}'`
	count=`echo $str|awk -F ' ' '{print $2}'`
	
	runsql_xdb "use ${HIS_DB}; delete from ${HIS_TABLE} where userid=${userid} order by statistime asc limit ${count};"
	alert $? "delete preview history failed"
done <${DATA_FILE}

end=`date +"%s"`
spendtime=$(($end-$start))
echo "delPreviewHis at `date +"%Y-%m-%d %H:%M:%S"`,spend time:${spendtime}s">>${LOG_FILE}
