#!/bin/bash

#@file: nova_sentinel.sh
#@date: 2011-10-16
#@version: 1.0.0.1
#@author: zhangpingan
#@brief: generate heartbeat for nova


CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=nova_sentinel.sh
reader_list=zhangpingan

USER_ID=2233117
PLAN_ID=139673
GROUP_ID=310479

USER_ID_LIST="5859860,5859905,5859981,5860080,5860129,5860186,5859745,5859830"
PLAN_ID_LIST="1561976,1507153,1507162,1507155,1524868,1507157,1507158,1507151"

c_year=` date +"%Y"`
c_month=`date +"%m"`
c_date=`date +"%d"`
c_hour=`date +"%H"`
c_minute=`date +"%M"`
c_seconds=`date +"%S"`

timestamp=`date +%d%H%M`;

msg="Nova哨兵增量语句执行失败"
slice=`getUserSlice ${USER_ID}`
#哨兵增量、以及对计划的预算递增1，这个是为了保证夜间每5分钟导给下游的预算数据md5有变化，否则下游会停止更新 by zhangpingan
runsql_single "update beidou.cproplan set budget = (budget%100+11),mondayscheme=0, tuesdayscheme=${c_year},wednesdayscheme=${c_month},thursdayscheme=${c_date},fridayscheme=${c_hour},saturdayscheme=${c_minute},sundayscheme=${c_seconds}  where planid=${PLAN_ID} and userid=${USER_ID};" ${slice}
alert $? "${msg}"

runsql_single "use beidou;update beidou.cprogroupinfo a set a.price=${timestamp} where a.groupid in (select groupid from beidou.cprogroup b where b.groupid=${GROUP_ID} and b.planid=${PLAN_ID} and b.userid=${USER_ID}) limit 1;" ${slice}
alert $? "${msg}"

runsql_sharding "update beidou.cproplan set mondayscheme=0, tuesdayscheme=${c_year},wednesdayscheme=${c_month},thursdayscheme=${c_date},fridayscheme=${c_hour},saturdayscheme=${c_minute},sundayscheme=${c_seconds}  where planid in (${PLAN_ID_LIST});"