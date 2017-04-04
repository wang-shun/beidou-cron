#!/bin/bash

#@file: besc_unit_export.sh
#@author: lixukun
#@date: 2014-03-11
#@version: 1.0.0.0
#@brief: export unit for besc;


CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/besc_unit_export.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=besc_unit_export.sh

LOG_NAME=adx_unit_export

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${WORK_PATH}
mkdir -p ${WORK_PATH}/output

export WORK_PATH


# 获取消费top80w物料id
function get_last_seven_day_unit() {
	for ((i=1;i<8;i++))
	do
		i_date=`date +"%Y-%m-%d" -d"${i} days ago"`
		i_table=`date +"%Y%m" -d"${i} days ago"`
		query_sql="select adid, userid, sum(cost) as total from stat_ad_${i_table} where date='${i_date}' and wuliaotype in (2,3) group by adid order by total desc limit 1500000;"
		runsql_stat_read "${query_sql}" ${WORK_PATH}/${STAT_AD_FILE}_tmp
		if [ -f "${WORK_PATH}/${STAT_AD_FILE}_tmp" ]
		then
			cat ${WORK_PATH}/${STAT_AD_FILE}_tmp >> ${WORK_PATH}/${STAT_AD_FILE_PROCESS}
		fi
	done
	
	cat ${WORK_PATH}/${STAT_AD_FILE_PROCESS} | sort -k1n,1  > ${WORK_PATH}/${STAT_AD_FILE_PROCESS}_tmp
	awk -vstat_ad="${WORK_PATH}/${STAT_AD_FILE}_rank" 'BEGIN{
		adid=0
		uid=0
		cost=0
	}{
		if (adid"X"!=$1"X") {
			if (adid""!="0") {
				printf "%s\t%s\t%s\n",adid,uid,cost >> stat_ad
			}
			adid=$1
			uid=$2
			cost=$3
		} else {
			cost+=$3
		}
	}' ${WORK_PATH}/${STAT_AD_FILE_PROCESS}_tmp
	
	cat ${WORK_PATH}/${STAT_AD_FILE}_rank | sort -k3nr,3 > ${WORK_PATH}/${STAT_AD_FILE}
	
	rm ${WORK_PATH}/${STAT_AD_FILE}_rank
	rm ${WORK_PATH}/${STAT_AD_FILE}_tmp
	rm ${WORK_PATH}/${STAT_AD_FILE_PROCESS}
	rm ${WORK_PATH}/${STAT_AD_FILE_PROCESS}_tmp
}

# 获取指定日期的点击消费数据
function get_ad_stat_day() {
    i=$1
	i_date=`date +"%Y-%m-%d" -d"${i} days ago"`
	i_table=`date +"%Y%m" -d"${i} days ago"`
	log "INFO" "get_ad_stat_day ${i_date}"
	query_sql="select adid, adid, 1, sum(srchs) as totalsrchs, sum(clks) as totalclks, sum(cost) as totalcost, date from stat_ad_${i_table} where date='${i_date}' group by adid;"
	runsql_stat_read "${query_sql}" ${WORK_PATH}/${STAT_AD_DAY_FILE}
}

open_log	
log "INFO" "besc_unit_export start at `date +%F\ %T`"

day=1
if [ $# -ge 1 ];then
day=$1
fi

curr_date=`date +"%Y-%m-%d"`
src_file=${WORK_PATH}/${STAT_AD_FILE}
get_last_seven_day_unit
get_ad_stat_day $day

wget -q ${DOMAIN_URL}/${ADSIZE_FILE} -O ${WORK_PATH}/${ADSIZE_FILE}
alert $? "Fail to download ${ADSIZE_FILE}"
wget -q ${DOMAIN_URL}/${ADSIZE_FILE}.md5 -O ${WORK_PATH}/${ADSIZE_FILE}.md5
alert $? "Fail to download ${ADSIZE_FILE}.md5"
md5sum -c ${WORK_PATH}/${ADSIZE_FILE}.md5
mv ${WORK_PATH}/${ADSIZE_FILE} ${WORK_PATH}/adsize

java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.bes.besc.BescCreativeExport ${src_file} > ${LOG_PATH}/besc_export.${curr_date}.log 2>&1 || alert $? "[Error]BescCreativeExport"

if [ -f "${WORK_PATH}/output/${EXPORT_FILE}" ]
then
	head -800000 ${OUTPUT_PATH}/${EXPORT_FILE} > ${OUTPUT_PATH}/${FINAL_FILE}
else
	touch ${OUTPUT_PATH}/${FINAL_FILE}
fi

cp ${WORK_PATH}/${STAT_AD_DAY_FILE} ${OUTPUT_PATH}/${STAT_AD_DAY_FILE}

cd ${OUTPUT_PATH}
md5sum ${FINAL_FILE} > ${FINAL_FILE}.md5
md5sum ${STAT_AD_DAY_FILE} > ${STAT_AD_DAY_FILE}.md5

log "INFO" "adx_unit_export end at `date +%F\ %T`"
close_log 0
	
	