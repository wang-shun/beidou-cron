#!/bin/bash

#@file: calculateTradeAdopt.sh
#@author: wangxiongjie
#@date: 2013-09-06
#@version: 1.0.0.0
#@brief: calculate trade adoption by groups which has consume data in last 7 days;


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/calculateTradeAdopt.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATA_PATH=${DATA_PATH}/calculateTradeAdopt
mkdir -p ${DATA_PATH}
mkdir -p ${LOG_PATH}

LOG_FILE=${LOG_PATH}/exportGroupHasConsume.log

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
	query_sql="select groupid,userid,sitetradelist from cprogroupinfo where isallsite=0 and sitetradelist is not null and sitetradelist!='' and [userid];"
	runsql_sharding_read "${query_sql}" ${DATA_PATH}/${GROUP_ADTRADE_FILE}
	echo "finished export group info from cprogroupinfo at `date +'%Y-%m-%d %M:%S'`" >> ${LOG_FILE}
}

function calculateAdoption()
{
	# count group number for every user trade, usered for calculate adoption 
	awk -F'\t' 'BEGIN{
		baiduflow[260]=260
		baiduflow[270]=270
		baiduflow[281]=281
		baiduflow[282]=282
		baiduflow[283]=283
		baiduflow[284]=284
		baiduflow[285]=285
		baiduflow[286]=286}
	ARGIND==1{utradeMap[$2]=$3;groupidMap[$1]=0}ARGIND==2{
		if($2 in utradeMap && $1 in groupidMap){
			utrade=""utradeMap[$2]
			if(gnumber[utrade]==""){
				gnumber[utrade]=1
			}else{
				gnumber[utrade]=gnumber[utrade]+1
			}
			delete adtrades_arr
			delete adtrades
			split($3,adtrades_arr,"|")
			for(j in adtrades_arr){
				if(adtrades_arr[j] in baiduflow || adtrades_arr[j]==""){
					continue
				}else if(adtrades_arr[j]>=100){
					first_adtrade=int(adtrades_arr[j]/100)
					adtrades[first_adtrade]=first_adtrade
				}else{
					adtrades[adtrades_arr[j]]=adtrades_arr[j]
				}
			}
			for(i in adtrades){
				if(utrade_adtrade[utrade,i]==""){
					utrade_adtrade[utrade,i]=1
				}else{
					utrade_adtrade[utrade,i]=utrade_adtrade[utrade,i]+1
				}
			}
		}
	}END{
		for(n in utrade_adtrade){
			split(n,ut_adt,SUBSEP)
			utrade=""ut_adt[1]
			total=gnumber[utrade]
			adoption=int(utrade_adtrade[n]*100/total)
			print ut_adt[1]"\t"ut_adt[2]"\t"adoption
		}
	}' ${DATA_PATH}/${GROUP_USER_TRADE_FILE} ${DATA_PATH}/${GROUP_ADTRADE_FILE} > ${DATA_PATH}/${UTRADE_ADTRADE_ADOPTION_FILE}
	echo "finished calculate trade adoption at `date +'%Y-%m-%d %M:%S'`" >> ${LOG_FILE}
}

function importAdoption()
{
	awk -F'\t' '{print "insert into beidouext.sitetrade_adopt_ratio(user_tradeid,site_tradeid,ratio) values("$1", "$2", "$3");"}' ${DATA_PATH}/${UTRADE_ADTRADE_ADOPTION_FILE} > ${DATA_PATH}/${ADOPTION_SQL}
	clean_sql="delete from beidouext.sitetrade_adopt_ratio;"
	import_sql="source ${DATA_PATH}/${ADOPTION_SQL}"
	echo "$clean_sql"
	runsql_xdb "$clean_sql"
	if [[ $? -ne 0 ]]; then
	    echo "$msg" >> ${LOG_FILE}
	fi
	sleep 5
	echo "finished clean table beidouext.sitetrade_adopt_ratio" >> ${LOG_FILE}
	echo "$import_sql"
	runsql_xdb "$import_sql"
	if [[ $? -ne 0 ]]; then
	    echo  "$msg" >> ${LOG_FILE}
	fi
	echo "finished import data to beidouext.sitetrade_adopt_ratio" >> ${LOG_FILE}
}

downloadGroupUserTradeFile
exportGroupInfoWithTrade
calculateAdoption
importAdoption
