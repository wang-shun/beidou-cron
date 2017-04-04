#!/bin/bash
#@file: crm_type
#@author: caohanzhen
#@date: 2012.08.29
#@version: 2.1
#@brief: download and analyze crm log data

CONF_PATH=/home/work/beidou-cron/conf/

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="$CONF_PATH/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/crm_cdc.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=crm_type.sh

function PRINT_LOG(){
    
    NOW=`date +%Y%m%d-%H:%M:%S`
    echo "[$NOW] $1" >> $LOG_FILE
}

case $# in
    0) 
    DATE=`date -d "1 days ago" +%Y%m%d`
    ;;
    1)
    DATE=$1
    ;;
esac

if [ ! -d $DATA_CAT ];then 
    mkdir $DATA_CAT
fi

if [ ! -d $DATA_CAT/$DATE ];then 
    mkdir $DATA_CAT/$DATE
fi

wget --limit-rate=50m ${PATH_TYPE}${DATE} -O ${DATA_CAT}/${DATE}/type_${DATE} 
wget --limit-rate=50m ${PATH_TYPE}${DATE}.md5 -O ${DATA_CAT}/${DATE}/type_${DATE}.md5

md5=`cat ${DATA_CAT}/${DATE}/type_${DATE}.md5| awk '{print $1}'` 
md5_check=`md5sum ${DATA_CAT}/${DATE}/type_${DATE}| awk '{print $1}'`
if [ ! "${md5}" = "${md5_check}" ];then
	alert 1 "Download stat_ad_final_${DATE} Error"
fi

runsql_sharding_read "use beidou; select groupid,grouptype from cprogroup where [userid]" "$DATA_CAT/$DATE/grouptype"
alert $? "获取推广组类型错误"

cp ${DATA_CAT}/${DATE}/type_${DATE}  ${DATA_CAT}/${DATE}/type_${DATE}.bak
awk -F'\t' 'ARGIND==1{map[$1]=$2;}ARGIND==2{printf("%s\t%s\n",$0,map[$7])}' $DATA_CAT/${DATE}/grouptype ${DATA_CAT}/${DATE}/type_${DATE}.bak > ${DATA_CAT}/${DATE}/type_${DATE}


awk '{print $5}' $DATA_CAT/$DATE/type_$DATE | sort -un > $DATA_CAT/$DATE/type_$DATE.tmp

function getType(){
case $2 in
    word) 
    awk '{if($8~/1/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{ 
    user_map[$1] 
    srch[$1]=0; 
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
    photo) 
    awk '{if ($8~/2/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{
    user_map[$1] 
    srch[$1]=0; 
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
    flash) 
    awk '{if ($8~/3/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{ 
    user_map[$1] 
    srch[$1]=0; 
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
    woto) 
    awk '{if ($8~/5/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{ 
    user_map[$1] 
    srch[$1]=0; 
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
    fixed)
    awk '{if ($11~/1|3|5|7/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{ 
    user_map[$1] 
    srch[$1]=0; 
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
    float)
    awk '{if ($11~/2|6/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{ 
    user_map[$1] 
    srch[$1]=0;
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
    paste)
    awk '{if ($11~/4/) print $0}' $DATA_CAT/$1/type_$1 > $DATA_CAT/$1/type_$1.$2
awk '
ARGIND==1{ 
    user_map[$1] 
    srch[$1]=0; 
    clk[$1]=0; 
    cost[$1]=0;
} ARGIND==2{ 
    srch[$5]+=$2; 
    clk[$5]+=$3; 
    cost[$5]+=$4;
} END { 
    for(user in user_map){ 
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]); 
    } 
}' $DATA_CAT/$1/type_$1.tmp $DATA_CAT/$1/type_$1.$2 > $DATA_CAT/$1/$2.$1
    ;;
esac

if [ $? -ne 0 ];then 
    PRINT_LOG "*Error* merge error"
    return 1
fi
}

getType $DATE word
alert $? "Error in getType $DATE word"

getType $DATE photo
alert $? "Error in getType $DATE photo"

getType $DATE flash
alert $? "Error in getType $DATE flash"

getType $DATE woto
alert $? "Error in getType $DATE woto"

getType $DATE fixed
alert $? "Error in getType $DATE fixed"

getType $DATE float
alert $? "Error in getType $DATE float"

getType $DATE paste
alert $? "Error in getType $DATE paste"

YEAR=`echo $DATE | cut -c1-4`
MONTH=`echo $DATE | cut -c5-6`
DAY=`echo $DATE | cut -c7-8`
FORMAT="$YEAR-$MONTH-$DAY"

rm -f $DATA_CAT/$DATE/crm_wuliaotype_$DATE

rm -f $DATA_CAT/$DATE/crm_grouptype_$DATE

awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 1, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/word.$DATE >> $DATA_CAT/$DATE/crm_wuliaotype_$DATE
awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 2, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/photo.$DATE >> $DATA_CAT/$DATE/crm_wuliaotype_$DATE
awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 3, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/flash.$DATE >> $DATA_CAT/$DATE/crm_wuliaotype_$DATE
awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 5, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/woto.$DATE >> $DATA_CAT/$DATE/crm_wuliaotype_$DATE


awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 1, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/fixed.$DATE >> $DATA_CAT/$DATE/crm_grouptype_$DATE
awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 2, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/float.$DATE >> $DATA_CAT/$DATE/crm_grouptype_$DATE
awk -v date=$FORMAT '{if(!($2==0 && $3==0 && $4==0)){printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 4, $2, $3, $4/100, date}}' $DATA_CAT/$DATE/paste.$DATE >> $DATA_CAT/$DATE/crm_grouptype_$DATE

cd $DATA_CAT/$DATE
md5sum crm_wuliaotype_$DATE > crm_wuliaotype_$DATE.md5
md5sum crm_grouptype_$DATE > crm_grouptype_$DATE.md5

#regist DTS
msg="regist DTS for ${CRM_TYPE_CRM_GROUPTYPE} failed."
md5=`getMd5FileMd5 $DATA_CAT/$DATE/crm_grouptype_$DATE.md5`
noahdt add ${CRM_TYPE_CRM_GROUPTYPE} -m md5=${md5} -i date=${DATE} bscp://$DATA_CAT/$DATE/crm_grouptype_$DATE
alert $? "${msg}"

msg="regist DTS for ${CRM_TYPE_CRM_WULIAOTYPE} failed."
md5=`getMd5FileMd5 $DATA_CAT/$DATE/crm_wuliaotype_$DATE.md5`
noahdt add ${CRM_TYPE_CRM_WULIAOTYPE} -m md5=${md5} -i date=${DATE} bscp://$DATA_CAT/$DATE/crm_wuliaotype_$DATE
alert $? "${msg}"

rm -f $DATA_CAT/$DATE/type_*
rm -f $DATA_CAT/$DATE/word*
rm -f $DATA_CAT/$DATE/photo*
rm -f $DATA_CAT/$DATE/flash*
rm -f $DATA_CAT/$DATE/woto*
rm -f $DATA_CAT/$DATE/fixed*
rm -f $DATA_CAT/$DATE/float*
rm -f $DATA_CAT/$DATE/paste*
rm -f $DATA_CAT/$DATE/grouptype*
del_date=`date -d "${KEEP_DATA} days ago" +%Y%m%d`
rm -rf $DATA_CAT/${del_date}

