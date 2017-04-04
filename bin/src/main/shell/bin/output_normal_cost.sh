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

#�ڲ�������cntnid�������keyΪcntnid,ֵΪ����
#��Ӧ�ڼƷ����� SF_Map.cntmate��mateabst=ָ���Ʒ�����mateid�� 
#��mateidҲ��Ӧbeidoufinan��cost_yyyyMMdd�е���cntnid

mkdir -p ${TMP_PATH}
mkdir -p ${DW_FILE_PATH}

CNTNID=`sed '1d' ${CNT_FILE} |awk '{if(NR==1){IDS=$1}else{IDS=IDS","$1}}END{print IDS}' `

if [ -f ${OUTPUT_FILE} ] ; then
	rm ${OUTPUT_FILE}
fi
	msg="����DW��������ʧ��"
	SQL=" select userid from SF_User.useracct b where b.ulevelid=10101"
	runsql_cap_read "$SQL" ${USERID_LIST}_10101
	
	msg="����DW��������ʧ��"
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
	 

	msg="����DW�ڲ���������ʧ��"
	SQL="select a.cntnid, price, price*rrate,userid from beidoufinan.cost_${STAT_DATE} a where a.userid > 30 and (a.userid < 1381000 or a.userid > 1381999) and a.cntnid in (${CNTNID})" 
	runsql_clk_read "$SQL" ${TMP_FILE}
	
	alert $? "${msg}"
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10101  ${TMP_FILE} | awk -F'\t' '{map1[$1]+=$2;map2[$1]+=$3}END{for(item in map1){printf("%s\t%.4f\t%.4f\n",item,map1[item],map2[item])}}' >> ${OUTPUT_FILE}
	
awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($4 in map){print $0}}' ${USERID_LIST}_10104  ${TMP_FILE} | awk -F'\t' '{map1[$1]+=$2;map2[$1]+=$3}END{for(item in map1){printf("%s\t%.4f\t%.4f\n",item,map1[item],map2[item])}}' >> ${OUTPUT_HEAVY_FILE}
		
       
fi

msg="cntnid�ļ�����ʧ��"
sort -k1,1 ${CNT_FILE} > ${TMP_PATH}/cntnid.sort
alert $? "${msg}"
msg="${OUTPUT_FILE}�ļ�����ʧ��"
sort -k1,1 ${OUTPUT_FILE} > ${OUTPUT_FILE}.sort
alert $? "${msg}"
msg="${OUTPUT_HEAVY_FILE}�ļ�����ʧ��"
sort -k1,1 ${OUTPUT_HEAVY_FILE} > ${OUTPUT_HEAVY_FILE}.sort
alert $? "${msg}"
msg="${OUTPUT_FILE}.sort�ļ�JOINʧ��"
join -a 1 ${TMP_PATH}/cntnid.sort ${OUTPUT_FILE}.sort | awk '{if(NF==2){print $2"\t"0"\t"0}else{printf "%s\t%.2f\t%.2f\n", $2,$3,$4}}' > ${OUTPUT_FILE}
alert $? "${msg}"

msg="${OUTPUT_HEAVY_FILE}.sort�ļ�JOINʧ��"
join -a 1 ${TMP_PATH}/cntnid.sort ${OUTPUT_HEAVY_FILE}.sort | awk '{if(NF==2){print $2"\t"0"\t"0}else{printf "%s\t%.2f\t%.2f\n", $2,$3,$4}}' > ${OUTPUT_HEAVY_FILE}
alert $? "${msg}"

msg="${OUTPUT_FILE}�ļ�����ת��ʧ��"
iconv -fgbk -tutf8 -o${DW_FILE_PATH}/${DW_FILE_NAME} ${OUTPUT_FILE}
alert $? "${msg}"
msg="${OUTPUT_HEAVY_FILE}�ļ�����ת��ʧ��"
iconv -fgbk -tutf8 -o${DW_FILE_PATH}/${DW_FILE_HEAVY_NAME} ${OUTPUT_HEAVY_FILE}
alert $? "${msg}"

msg="����Ŀ¼${DW_FILE_PATH}ʧ��"
cd ${DW_FILE_PATH}
alert $? "${msg}"

msg="md5${DW_FILE_NAME}ʧ��"
md5sum ${DW_FILE_NAME} > ${DW_FILE_NAME}.md5
alert $? "${msg}"

msg="md5${DW_FILE_HEAVY_NAME}ʧ��"
md5sum ${DW_FILE_HEAVY_NAME} > ${DW_FILE_HEAVY_NAME}.md5
alert $? "${msg}"
