#!/bin/bash

#@file: calculateInterest.sh
#@author: wangxiongjie
#@date: 2013-09-06
#@version: 1.0.0.0
#@brief: calculate interest adoption by groups which has consume data in last 7 days;


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/calculateInterestAdopt.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATA_PATH=${DATA_PATH}/calculateInterestAdopt
mkdir -p ${DATA_PATH}
mkdir -p ${LOG_PATH}

LOG_FILE=${LOG_PATH}/calculateInterestAdopt.log

function downloadGroupUserTradeFile()
{
	msg="download ${GROUP_USER_TRADE_FILE} from dts failed at `date +'%Y-%m-%d %M:%S'`"
	noahdt download ${EXPORTGROUPHASCONSUME_GROUPUSERTRADE} ${DATA_PATH}
	isRight=`checkMD5ForDTS ${DATA_PATH}/${GROUP_USER_TRADE_FILE} ${EXPORTGROUPHASCONSUME_GROUPUSERTRADE} "md5"`
	if [[ $? -ne 0 ]]
	then
		echo "${msg}" >> ${LOG_FILE}
		exit 1
	fi
}

function exportGroupInfoWithTrade()
{
	query_sql="select i.groupid,i.userid,i.iid,it.parentid from beidou.cprogroupit i, beidoucode.interest it,beidou.cprogroup g where i.groupid=g.groupid and i.iid=it.interestid and g.targettype&32=32 and i.iid<100000 and [g.userid];"
	runsql_sharding_read "${query_sql}" ${DATA_PATH}/${GROUP_INTEREST_FILE}
}

function calculateAdoption()
{
	awk -F'\t' '{
		gid_uid[$1]=$2
		if($4==0){
			interestid=$3
		}else{
			interestid=$4
		}
		if($1 in gid_it){
			gid_it[$1]=gid_it[$1]"|"interestid
		}else{
			gid_it[$1]=interestid
		}
	}END{
		for(n in gid_it){
			print n"\t"gid_uid[n]"\t"gid_it[n]
		}
	}' ${DATA_PATH}/${GROUP_INTEREST_FILE} > ${DATA_PATH}/${GROUP_INTEREST_FILE}.joined
	
	# count group number for every user trade, usered for calculate adoption 
	awk -F'\t' 'ARGIND==1{utradeMap[$2]=$3;groupidMap[$1]=0}ARGIND==2{
		if($2 in utradeMap && $1 in groupidMap){
			utrade=utradeMap[$2]
			if(gnumber[utrade]==""){
				gnumber[utrade]=1
			}else{
				gnumber[utrade]=gnumber[utrade]+1
			}
			delete interest_arr
			delete interests
			split($3,interest_arr,"|")
			for(j in interest_arr){
				interests[interest_arr[j]]=interest_arr[j]
			}
			for(i in interests){
				if(utrade_interest[utrade,i]==""){
					utrade_interest[utrade,i]=1
				}else{
					utrade_interest[utrade,i]=utrade_interest[utrade,i]+1
				}
			}
		}
	}END{
		for(n in utrade_interest){
			split(n,ut_adt,SUBSEP)
			utrade=""ut_adt[1]
			total=gnumber[utrade]
			adoption=int(utrade_interest[n]*100/total)
			print ut_adt[1]"\t"ut_adt[2]"\t"adoption
		}
	}' ${DATA_PATH}/${GROUP_USER_TRADE_FILE} ${DATA_PATH}/${GROUP_INTEREST_FILE}.joined > ${DATA_PATH}/${UTRADE_INTEREST_ADOPTION_FILE}

}

function importAdoption()
{
	awk -F'\t' '{print "insert into beidouext.it_adopt_ratio(user_tradeid,interestid,ratio) values("$1", "$2", "$3");"}' ${DATA_PATH}/${UTRADE_INTEREST_ADOPTION_FILE} > ${DATA_PATH}/${ADOPTION_SQL}
	clean_sql="delete from beidouext.it_adopt_ratio;"
	import_sql="source ${DATA_PATH}/${ADOPTION_SQL}"
	echo "$clean_sql"
	runsql_xdb "$clean_sql"
	if [[ $? -ne 0 ]]; then
	    echo "$msg" >> ${LOG_FILE}
	fi
	sleep 5
	echo "$import_sql"
	runsql_xdb "$import_sql"
	if [[ $? -ne 0 ]]; then
	    echo "$msg" >> ${LOG_FILE}
	fi
}

downloadGroupUserTradeFile
exportGroupInfoWithTrade
calculateAdoption
importAdoption
