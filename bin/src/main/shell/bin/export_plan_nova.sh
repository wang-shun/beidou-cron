#!/bin/bash

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/budgetserver.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=export_plan_nova.sh
reader_list=lingbing

msg="query beidou.cproplan budget info failed."

rm -f ${TMPDATA_PATH}/${RESULT}.tmp
for((i=0;i<${SHARDING_SLICE};i++));
do
	runsql_single_read "select planid, userid, budget*100 from beidou.cproplan"  "${TMPDATA_PATH}/${RESULT}.tmp.${i}" ${i}
	alert $? "${msg}"
	cat "${TMPDATA_PATH}/${RESULT}.tmp.${i}" >> "${TMPDATA_PATH}/${RESULT}.tmp"
	rm -f "${TMPDATA_PATH}/${RESULT}.tmp.${i}"
done

minute=`date +%M`
minute_mod=`expr $minute % 2`
cp ${TMPDATA_PATH}/${RESULT}.tmp ${TMPDATA_PATH}/${RESULT}.tmp.bak
if [ $minute_mod -eq 0 ];then
sort -k1n ${TMPDATA_PATH}/${RESULT}.tmp.bak > ${TMPDATA_PATH}/${RESULT}.tmp
else
sort -k2n ${TMPDATA_PATH}/${RESULT}.tmp.bak > ${TMPDATA_PATH}/${RESULT}.tmp
fi

md5sum ${TMPDATA_PATH}/${RESULT}.tmp > ${TMPDATA_PATH}/${RESULT}.md5.tmp


msg="mv ${TMPDATA_PATH}/${RESULT}.tmp to ${DATA_PATH}/${RESULT} failed."
mv ${TMPDATA_PATH}/${RESULT}.tmp ${DATA_PATH}/${RESULT}
alert $? "${msg}"

cd ${DATA_PATH}
msg="failed to produce md5 checkfile"
md5sum ${RESULT} > ${RESULT}.md5
alert $? "${msg}"
