#!/bin/bash
#@name: crm_userinfo
#@author: caohanzhen
#@date: 2012.08.30
#@version: 2.1
#@detail: a script to access database

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/crm_cdc.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

YESTERDAY=`date -d "1 days ago" +%Y%m%d`

if [ $# -gt 0 ];then
    YESTERDAY=$1
fi

program=crm_userinfo.sh

function PRINT_LOG(){
    if [ ! -d $LOG_PATH ];then
        mkdir $LOG_PATH
    fi
    
    NOW=`date +%Y%m%d-%H:%M:%S`
    echo "[$NOW] $1" >> $LOG_FILE
}

if [ ! -d $DATA_CAT ];then
    mkdir $DATA_CAT
fi

if [ ! -d $DATA_CAT/$YESTERDAY ];then 
    mkdir $DATA_CAT/$YESTERDAY
fi

PRINT_LOG "获取生效客户列表"
runsql_sharding_read "select userid from beidoucap.useraccount ua where ua.ustate=0 and ua.ushifenstatid in (2,3,6) and ua.userid in (select userid from beidou.cproplan cp where cp.planstate=0 and cp.planid in (select planid from beidou.cprogroup cg where cg.groupstate=0 and cg.groupid in (select gid from beidou.cprounitstate? cu where cu.state=0 and  [cu.uid])));" "$DATA_CAT/$YESTERDAY/valid_${loop}" ${TAB_UNIT_SLICE}
alert $? "获取生效客户列表错误"

PRINT_LOG "获取余额不为0客户列表"
runsql_cap_read "select userid,balancestat from beidoucap.useraccount" "$DATA_CAT/$YESTERDAY/balancestat"
alert $? "获取余额不为0客户列表错误"

awk '{print $1}' $DATA_CAT/$YESTERDAY/valid_* | sort -k1n  -u > $DATA_CAT/$YESTERDAY/user_valid

YEAR=`echo $YESTERDAY | cut -c1-4`
MONTH=`echo $YESTERDAY | cut -c5-6`
DAY=`echo $YESTERDAY | cut -c7-8`
FORMAT="$YEAR-$MONTH-$DAY"

awk -F'\t' -v datestr=$FORMAT '
	ARGIND==1{map[$1];} 
	ARGIND==2{
		if($1 in map){
			if($2==1){
				printf("%s\t%s\t%s\n",$1,1,datestr)
			} else {
			    printf("%s\t%s\t%s\n",$1,2,datestr)
			}
		} else {
		    if($2==1){
				printf("%s\t%s\t%s\n",$1,3,datestr)
			} else {
			    printf("%s\t%s\t%s\n",$1,4,datestr)
			}
		}
	}' $DATA_CAT/$YESTERDAY/user_valid  $DATA_CAT/$YESTERDAY/balancestat | sort -k1n > $DATA_CAT/$YESTERDAY/crm_bduser_state_$YESTERDAY
	
cd $DATA_CAT/$YESTERDAY

PRINT_LOG "CRM账户状态信息导出完毕，生成MD5"
md5sum crm_bduser_state_$YESTERDAY > crm_bduser_state_$YESTERDAY.md5

rm -f $DATA_CAT/$YESTERDAY/valid_*
rm -f $DATA_CAT/$YESTERDAY/balancestat
del_date=`date -d "${KEEP_DATA} days ago" +%Y%m%d`
rm -rf $DATA_CAT/${del_date}

#regist DTS
msg="regist DTS for ${CRM_USERINFO_CRM_BDUSER_STATE} failed."
md5=`getMd5FileMd5 $DATA_CAT/$YESTERDAY/crm_bduser_state_$YESTERDAY.md5`
noahdt add ${CRM_USERINFO_CRM_BDUSER_STATE} -m md5=${md5} -i date=${YESTERDAY} bscp://$DATA_CAT/$YESTERDAY/crm_bduser_state_$YESTERDAY
alert $? "${msg}"
