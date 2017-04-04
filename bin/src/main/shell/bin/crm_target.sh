#!/bin/bash
#@file: crm_target
#@author: caohanzhen
#@date: 2012.09.06
#@version: 2.2
#@brief: download and analyze crm log data

CONF_PATH=/home/work/beidou-cron/conf/
CONF_SH="$CONF_PATH/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="$CONF_PATH/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/crm_cdc.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

case $# in
    0)
    DATE=`date -d "1 days ago" +%Y%m%d`
    ;;
    1)
    DATE=$1
    ;;
esac

program=crm_target.sh

function PRINT_LOG(){
    
    NOW=`date +%Y%m%d-%H:%M:%S`
    echo "[$NOW] $1" >> $LOG_FILE
}


if [ ! -d $DATA_CAT ];then 
    mkdir -p $DATA_CAT
fi

if [ ! -d $DATA_CAT/$DATE ];then 
    mkdir -p $DATA_CAT/$DATE
fi

function getTarget(){
    wget -q -t 3 --limit-rate=50m $PATH_TARGET/$2.$1  -O $DATA_CAT/$1/target.$2.$1
    if [ $? -eq 1 ];then
		PRINT_LOG "*ERROR* copy target.$2.$1 FAILURE"
		return 1
	fi
	
	awk '{print $1}' $DATA_CAT/$1/target.$2.$1 | sort -u > $DATA_CAT/$1/target.$2.$1.tmp

    rm -f $DATA_CAT/$1/$2.$1.merge

	awk  '
	ARGIND==1{
		user_map[$1] 
		srch[$1]=0;
		clk[$1]=0;
		cost[$1]=0;
	} ARGIND==2{
		srch[$1]+=$4;
		clk[$1]+=$5;
		cost[$1]+=$6;
	} END {
		for(user in user_map){
        printf("%s\t%s\t%s\t%s\n", user, srch[user], clk[user], cost[user]);
    }
}' $DATA_CAT/$1/target.$2.$1.tmp $DATA_CAT/$1/target.$2.$1 > $DATA_CAT/$1/$2.$1.merge

    if [ $? -ne 0 ];then 
        PRINT_LOG "*Error* merge error"
        return 1
    fi
}

getTarget $DATE vt
alert $? "Error in getTarget $DATE vt"

getTarget $DATE rt
alert $? "Error in getTarget $DATE rt"

getTarget $DATE pt
alert $? "Error in getTarget $DATE pt"

getTarget $DATE it
alert $? "Error in getTarget $DATE it"

getTarget $DATE ct
alert $? "Error in getTarget $DATE ct"

getTarget $DATE qt
alert $? "Error in getTarget $DATE qt"

getTarget $DATE hct
alert $? "Error in getTarget $DATE hct"


YEAR=`echo $DATE | cut -c1-4`
MONTH=`echo $DATE | cut -c5-6`
DAY=`echo $DATE | cut -c7-8`
FORMAT="$YEAR-$MONTH-$DAY"

rm  -f $DATA_CAT/$DATE/crm_targettype_$DATE.tmp

awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 0, $2, $3, $4/100, date}' $DATA_CAT/$DATE/pt.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp
awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 1, $2, $3, $4/100, date}' $DATA_CAT/$DATE/ct.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp
awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 1, $2, $3, $4/100, date}' $DATA_CAT/$DATE/qt.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp
awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 1, $2, $3, $4/100, date}' $DATA_CAT/$DATE/hct.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp
awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 8, $2, $3, $4/100, date}' $DATA_CAT/$DATE/rt.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp
awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 16, $2, $3, $4/100, date}' $DATA_CAT/$DATE/vt.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp
awk -v date=$FORMAT '{printf "%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, 32, $2, $3, $4/100, date}' $DATA_CAT/$DATE/it.$DATE.merge >> $DATA_CAT/$DATE/crm_targettype_$DATE.tmp


# 此处需要进一步合并ct，qt，hct的值

awk -F'\t' -v v='#' '{
	key=$1v$2
	srch[key]+=$3;
	clk[key]+=$4;
	cost[key]+=$5;
	datestr[key]=$6
} END {
    for(item in srch){
		printf("%s#%s#%s#%.2f#%s\n",item,srch[item],clk[item],cost[item],datestr[item])
	}
}' $DATA_CAT/$DATE/crm_targettype_$DATE.tmp | awk -F'#' '{if(!($3==0 && $4==0 && $5==0)){printf("%s\t%s\t%s\t%s\t%.2f\t%s\n", $1, $2, $3, $4, $5, $6)}}' >  $DATA_CAT/$DATE/crm_targettype_$DATE

cd $DATA_CAT/$DATE
md5sum crm_targettype_$DATE > crm_targettype_$DATE.md5

#regist DTS
msg="regist DTS for ${CRM_TARGET_CRM_TARGETTYPE} failed."
md5=`getMd5FileMd5 $DATA_CAT/$DATE/crm_targettype_$DATE.md5`
noahdt add ${CRM_TARGET_CRM_TARGETTYPE} -m md5=${md5} -i date=${DATE} bscp://$DATA_CAT/$DATE/crm_targettype_$DATE
alert $? "${msg}"

rm -f $DATA_CAT/$DATE/target.*
rm -f $DATA_CAT/$DATE/*.tmp
rm -f $DATA_CAT/$DATE/*.merge
del_date=`date -d "${KEEP_DATA} days ago" +%Y%m%d`
rm -rf $DATA_CAT/${del_date}
    
