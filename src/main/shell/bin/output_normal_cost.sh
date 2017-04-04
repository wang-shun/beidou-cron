#!/bin/sh
#@file: output_normal_cost.sh
#@author: zengyunfeng
#@date: 2010-01-29
#@version: 1.0.0.0
#@brief: output normal user total cost in union site and inner site
#@modify: wangchongjie since 2012.12.12 for cpweb525

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=output_normal_cost.sh
reader_list=zengyunfeng

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CNT_FILE="../conf/cntnid.conf"

STAT_DATE=`date -d 'yesterday' +%Y%m%d`
if [ -n "$1" ] ; then
	STAT_DATE=$1
fi

DW_FILE_PATH=${DATA_PATH}/dw/output/
DW_FILE_NAME=beidou_normal_cost.${STAT_DATE}
DW_FILE_HEAVY_NAME=beidou_heavy_cost.${STAT_DATE}

TMP_PATH=${DATA_PATH}/dw/tmp/
OUTPUT_FILE=${TMP_PATH}/beidou_normal_cost
OUTPUT_HEAVY_FILE=${TMP_PATH}/beidou_heavy_cost
USERID_LIST=${TMP_PATH}/userid
TMP_FILE=${TMP_PATH}/tmp

#内部流量的cntnid，数组的key为cntnid,值为名称
#对应于计费名在 SF_Map.cntmate中mateabst=指定计费名的mateid， 
#该mateid也对应beidoufinan中cost_yyyyMMdd中的列cntnid

mkdir -p ${TMP_PATH}
mkdir -p ${DW_FILE_PATH}

CNTNID=`sed '1d' ${CNT_FILE} |awk '{if(NR==1){IDS=$1}else{IDS=IDS","$1}}END{print IDS}' `

if [ -f ${OUTPUT_FILE} ] ; then
	rm ${OUTPUT_FILE}
fi
	msg="导出DW联盟数据失败"
	SQL=" select userid from SF_User.useracct b where b.ulevelid=10101"
	runsql_cap_read "$SQL" ${USERID_LIST}_10101
	
	msg="导出DW联盟数据失败"
	SQL="select userid from SF_User.useracct b where b.ulevelid=10104"
	runsql_cap_read "$SQL" ${USERID_LIST}_10104
	
if [ -z "${CNTNID}" ] ; then

	SQL="select 0,price,price*rrate,userid from beidoufinan.cost_${STAT_DATE} a where a.userid > 30 and (a.userid < 1381000 or a.userid > 1381999) "
	runsql_clk_read "$SQL" ${TMP_FILE}
	
	alert $? "${msg}"
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10101  ${TMP_FILE} | awk -F'\t' '{sum1+=$2;sum2+=$3}END{printf("0\t%s\t%s\n",sum1,sum2)}' > ${OUTPUT_FILE}

	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10104  ${TMP_FILE} | awk -F'\t' '{sum1+=$2;sum2+=$3}END{printf("0\t%s\t%s\n",sum1,sum2)}' > ${OUTPUT_HEAVY_FILE}

else
	SQL="select 0,price,price*rrate,userid from beidoufinan.cost_${STAT_DATE} a where  a.userid > 30 and (a.userid < 1381000 or a.userid > 1381999) and a.cntnid not in (${CNTNID})"
	runsql_clk_read "$SQL" ${TMP_FILE}
	
	alert $? "${msg}"
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10101  ${TMP_FILE} | awk -F'\t' '{sum1+=$2;sum2+=$3}END{printf("0\t%.4f\t%.4f\n",sum1,sum2)}' > ${OUTPUT_FILE}

    awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10104  ${TMP_FILE} | awk -F'\t' '{sum1+=$2;sum2+=$3}END{printf("0\t%.4f\t%.4f\n",sum1,sum2)}' > ${OUTPUT_HEAVY_FILE}
	 

	msg="导出DW内部流量数据失败"
	SQL="select a.cntnid, price, price*rrate,userid from beidoufinan.cost_${STAT_DATE} a where a.userid > 30 and (a.userid < 1381000 or a.userid > 1381999) and a.cntnid in (${CNTNID})" 
	runsql_clk_read "$SQL" ${TMP_FILE}
	
	alert $? "${msg}"
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10101  ${TMP_FILE} | awk -F'\t' '{map1[$1]+=$2;map2[$1]+=$3}END{for(item in map1){printf("%s\t%.4f\t%.4f\n",item,map1[item],map2[item])}}' >> ${OUTPUT_FILE}
	
awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10104  ${TMP_FILE} | awk -F'\t' '{map1[$1]+=$2;map2[$1]+=$3}END{for(item in map1){printf("%s\t%.4f\t%.4f\n",item,map1[item],map2[item])}}' >> ${OUTPUT_HEAVY_FILE}
		
       
fi

msg="cntnid文件排序失败"
sort -k1,1 ${CNT_FILE} > ${TMP_PATH}/cntnid.sort
alert $? "${msg}"
msg="${OUTPUT_FILE}文件排序失败"
sort -k1,1 ${OUTPUT_FILE} > ${OUTPUT_FILE}.sort
alert $? "${msg}"
msg="${OUTPUT_HEAVY_FILE}文件排序失败"
sort -k1,1 ${OUTPUT_HEAVY_FILE} > ${OUTPUT_HEAVY_FILE}.sort
alert $? "${msg}"
msg="${OUTPUT_FILE}.sort文件JOIN失败"
join -a 1 ${TMP_PATH}/cntnid.sort ${OUTPUT_FILE}.sort | awk '{if(NF==2){print $2"\t"0"\t"0}else{printf "%s\t%.2f\t%.2f\n", $2,$3,$4}}' > ${OUTPUT_FILE}
alert $? "${msg}"

msg="${OUTPUT_HEAVY_FILE}.sort文件JOIN失败"
join -a 1 ${TMP_PATH}/cntnid.sort ${OUTPUT_HEAVY_FILE}.sort | awk '{if(NF==2){print $2"\t"0"\t"0}else{printf "%s\t%.2f\t%.2f\n", $2,$3,$4}}' > ${OUTPUT_HEAVY_FILE}
alert $? "${msg}"

msg="${OUTPUT_FILE}文件编码转换失败"
iconv -fgbk -tutf8 -o${DW_FILE_PATH}/${DW_FILE_NAME} ${OUTPUT_FILE}
alert $? "${msg}"
msg="${OUTPUT_HEAVY_FILE}文件编码转换失败"
iconv -fgbk -tutf8 -o${DW_FILE_PATH}/${DW_FILE_HEAVY_NAME} ${OUTPUT_HEAVY_FILE}
alert $? "${msg}"

msg="进入目录${DW_FILE_PATH}失败"
cd ${DW_FILE_PATH}
alert $? "${msg}"

msg="md5${DW_FILE_NAME}失败"
md5sum ${DW_FILE_NAME} > ${DW_FILE_NAME}.md5
alert $? "${msg}"

msg="md5${DW_FILE_HEAVY_NAME}失败"
md5sum ${DW_FILE_HEAVY_NAME} > ${DW_FILE_HEAVY_NAME}.md5
alert $? "${msg}"
