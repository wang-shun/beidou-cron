#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=cproplan-state.sh
reader_list=zhangpeng

msg="update beidou.cproplan failed."

runsql_sharding "update beidou.cproplan t set t.planstate = 0 where t.planstate = 3 and t.startdate = DATE_FORMAT(now(),'%Y%m%d') and [t.userid];"
alert $? "${msg}"

runsql_sharding "update beidou.cproplan t set t.planstate = 4 where t.planstate = 0 and t.hasenddate = 1 and t.enddate <= DATE_FORMAT(DATE_SUB(now(),INTERVAL 1 DAY),'%Y%m%d') and [t.userid];"
alert $? "${msg}"
