#!/bin/sh
#@modify: wangchongjie since 2012.12.10 for cpweb525

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=cproplan_budgetover.sh
reader_list=zhangpingan


##########################
##
#config
#
#########################

#log
LOG_NAME=cproplan_budgetover

########################

mkdir -p ${DATA_PATH}/cproplan_budgetover
cd ${DATA_PATH}/cproplan_budgetover

schema=(sundayscheme mondayscheme tuesdayscheme wednesdayscheme thursdayscheme fridayscheme saturdayscheme)
weekday=`date +%w`
dayschema=${schema[$weekday]}

#获取凌晨区间投放且已撞线的计划
runsql_sharding_read "select planid from beidou.cproplan where budgetover=1 and ${dayschema}>0 and ${dayschema}<128 and [userid]"  planid_pre.data
if [ $? -ne 0 ]
then
	log "FATAL" "获取凌晨区间投放且已撞线的计划失败"
	exit 1
fi 

awk 'BEGIN{printf("UPDATE beidou.cproplan t set t.budgetover = 0 WHERE [userid] and planid in(-1")}{printf(",%s",$1)}END{printf(");")}' planid_pre.data> planid_pre.sql
if [ $? -ne 0 ]
then
	log "FATAL" "cproplan_budgetover-pre: generage sql fail"
	exit 1
fi 

sqlstr=`cat planid_pre.sql`

lines=`cat planid_pre.data | wc -l`
if [ ${lines} -lt 50 ];then
	log "NOTICE" "cproplan_budgetover-pre: ${lines} plans will be online"
	runsql_sharding "${sqlstr}"
	if [ $? -ne 0 ]
	then
		log "FATAL" "cproplan_budgetover-pre: execute sql fail"
		exit 1
	fi
else
	log "NOTICE" "cproplan_budgetover-pre: ${lines} plans are too much"
fi

DATE=`date +%Y%m%d`
cp planid_pre.data preonline_userlist.${DATE}
md5sum preonline_userlist.${DATE} > preonline_userlist.${DATE}.md5
cp planid_pre.sql preonline_userlist.${DATE}.sql

dateold=`date -d"10 day ago" +%Y%m%d`
rm -f planid_pre_${dateold}.data
rm -f planid_pre_${dateold}.sql

#regist file to dts

msg="regist DTS for ${BEIDOU_DORADO_PLANID} failed."
md5=`getMd5FileMd5 ${DATA_PATH}/cproplan_budgetover/preonline_userlist.${DATE}.md5`
noahdt add ${BEIDOU_DORADO_PLANID} -m md5=${md5} -i date=${DATE} bscp://${DATA_PATH}/cproplan_budgetover/preonline_userlist.${DATE}
alert $? "${msg}"