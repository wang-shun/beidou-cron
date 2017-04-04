#!/bin/bash
#@file: getValidPlans.sh
#@author: xiehao
#@date: 2011-05-12
#@version: 1.0.0.0
#@brief: get valid plans based on the four tables below : beidoucap.useraccount,cproplan,cprogroup,cprounitstate

scriptName=$0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/getFinalBudget.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=getValidPlans.sh
reader_list=xiehao

if ! [ -d ${TMP_FILE_PATH} ];then
	mkdir ${TMP_FILE_PATH}
	alert $? "$scriptName error : fail to mkdir ${TMP_FILE_PATH}"
fi

if [ -e ${TMP_FILE_PATH}/plans.out ];then
	mv ${TMP_FILE_PATH}/plans.out ${TMP_FILE_PATH}/plans.out.bak
fi

touch ${TMP_FILE_PATH}/plans.out
runsql_sharding_read "SELECT t.userid,sum(t.budget) FROM (SELECT distinct p.userid,p.planid,p.budget FROM beidoucap.useraccount a,beidou.cproplan p,beidou.cprogroup g,beidou.cprounitstate? u WHERE a.userid=p.userid and p.planid=g.planid and g.groupid=u.gid and a.ustate=0 and a.ushifenstatid in (2,3,6) and a.balancestat<>0 and p.planstate=0 and g.groupstate=0 and u.state=0 and [u.uid]) t GROUP BY t.userid;" "${TMP_FILE_PATH}/plans.out" ${TAB_UNIT_SLICE}

exit 0
