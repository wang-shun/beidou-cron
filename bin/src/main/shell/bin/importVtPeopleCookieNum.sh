#!/bin/sh

#@file:importVtPeopleCookieNum.sh
#@author:zhangxu
#@date:2011-10-23
#@version:1.0.0.0
#@brief:import vt people cookie num into db from cm-ufs

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importVtPeopleCookieNum.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importVtPeopleCookieNum.sh
reader_list=zhangxu

LOG_PATH=${LOG_PATH}/${DIR_NAME}
LOG_FILE=${LOG_PATH}/importVtPeopleCookieNum.log
DATA_PATH=${DATA_PATH}/${DIR_NAME}
DATA_SPLIT_NONZERO_PATH=${DATA_PATH}/split_update_nonzero
DATA_SPLIT_ZERO_PATH=${DATA_PATH}/split_update_zero
SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH=${DATA_PATH}/similar_people_split_update_zero
SIMILAR_PEOPLE_DATA_SPLIT_NONZERO_PATH=${DATA_PATH}/similar_people_split_update_nonzero

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${DATA_SPLIT_NONZERO_PATH}
mkdir -p ${DATA_SPLIT_ZERO_PATH}
mkdir -p ${SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH}
mkdir -p ${SIMILAR_PEOPLE_DATA_SPLIT_NONZERO_PATH}
[ ! -f ${LOG_FILE} ] && touch ${LOG_FILE}


CURR_DATETIME=`date +%Y%m%d%H%M`
if [ ! -z $1 ];then
	CURR_DATETIME=$1
fi



msg="��������Ŀ¼${DATA_SPLIT_NONZERO_PATH}ʧ��"
cd ${DATA_SPLIT_NONZERO_PATH}
alert $? "${msg}"

msg="��������Ŀ¼${DATA_SPLIT_ZERO_PATH}ʧ��"
cd ${DATA_SPLIT_ZERO_PATH}
alert $? "${msg}"

msg="��������Ŀ¼${SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH}ʧ��"
cd ${SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH}
alert $? "${msg}"

# ���һЩ��ʱ�ļ�
[ -f "${VT_PEOPLE_ALL_PID_FILE}" ] && rm -f ${VT_PEOPLE_ALL_PID_FILE}
[ -f "${VT_PEOPLE_ALL_ZERO_COOKIENUM_PID_FILE}" ] && rm -f ${VT_PEOPLE_ALL_ZERO_COOKIENUM_PID_FILE}
[ -f "${VT_PEOPLE_UFS_PID_FILE}" ] && rm -f ${VT_PEOPLE_UFS_PID_FILE}
[ -f "${VT_PEOPLE_SHOULD_UPDATE_TO_ZERO_PID_FILE}" ] && rm -f ${VT_PEOPLE_SHOULD_UPDATE_TO_ZERO_PID_FILE}
[ -f "${VT_PEOPLE_TO_BE_SET_ZERO_VTPID_FILE}" ] && rm -f ${VT_PEOPLE_TO_BE_SET_ZERO_VTPID_FILE}
[ -f "${VT_PEOPLE_NOT_EXIST_IN_BEIDOU_PID_FILE}" ] && rm -f ${VT_PEOPLE_NOT_EXIST_IN_BEIDOU_PID_FILE}

[ -f "${SIMILAR_PEOPLE_ALL_PID_FILE}" ] && rm -f ${SIMILAR_PEOPLE_ALL_PID_FILE}
[ -f "${SIMILAR_PEOPLE_ALL_ZERO_COOKIENUM_PID_FILE}" ] && rm -f ${SIMILAR_PEOPLE_ALL_ZERO_COOKIENUM_PID_FILE}
[ -f "${SIMILAR_PEOPLE_SHOULD_UPDATE_TO_ZERO_PID_FILE}" ] && rm -f ${SIMILAR_PEOPLE_SHOULD_UPDATE_TO_ZERO_PID_FILE}
[ -f "${SIMILAR_PEOPLE_TO_BE_SET_ZERO_VTPID_FILE}" ] && rm -f ${SIMILAR_PEOPLE_TO_BE_SET_ZERO_VTPID_FILE}


msg="��������Ŀ¼${DATA_PATH}ʧ��"
cd ${DATA_PATH}
alert $? "${msg}"

if [ $# -gt 1 ];then
	usage
	exit 1
fi

############################################################
### Step1. ׼����ʼ
############################################################

INF "--------------------------------------" 
INF "Start to import vt people cookie num" 
INF "--------------------------------------" 
INF ""

if [ $# -gt 1 ];then
	usage
	exit 1
fi



############################################################
### ��ufs���ػ�ͷ��-���ö�����Ⱥcookie��ͳ���ļ�
############################################################

## ɾ������md5�ļ�
rm -f *.md5
INF "Clear md5 file done"

SUCCESS_FLAG=0

while [ $SUCCESS_FLAG -eq 0 ];
do
	## ץȡufsԭʼ�����ļ�
	INF "Begin download ${VT_PEOPLE_COOKIENUM_FILE}"
	wget -q -t ${MAX_RETRY} --limit-rate=${LIMIT_RATE} ftp://${UFS_SERVER_PATH}/${VT_PEOPLE_COOKIENUM_FILE} -O ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}

	## ץȡufsԭʼ�����ļ�md5
	INF "Begin download ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}.md5"
	msg="ץȡufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}.md5ʧ��"
	wget -q -t ${MAX_RETRY} --limit-rate=${LIMIT_RATE} ftp://${UFS_SERVER_PATH}/${VT_PEOPLE_COOKIENUM_FILE}.md5 -O ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}.md5


	## ��ufsԭʼ�����ļ�����md5У��
	INF "Begin check ${VT_PEOPLE_COOKIENUM_FILE}.md5"
	msg="ufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}��md5У��ʧ��"
	md5_cal=`md5sum ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} | awk '{print $1}'`
	md5_file=`cat ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}.md5| awk '{print $1}'`
	if [ "${md5_cal}x" == "${md5_file}x" ]
	then
		SUCCESS_FLAG=1;
	fi
done


## ��ufsԭʼ�����ļ�����һ���Լ��
## ����ļ�����Ϊ��
if [ ! -s "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}" ]
then
	ERR "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} is empty"
	msg="ufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}Ϊ��"
	alert 1 "${msg}"
fi
INF "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} not empty pass"

## ����ļ��������ܹ���
line_num=`cat ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}| wc -l`
if [ $line_num -le $MAX_LINE_NUMBER ]
then
	ERR "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} line number to less"
	msg="ufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}��������,�ļ����ܴ��ڴ���"
	alert 1 "${msg}"
fi
INF "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} line number enough pass"

## ����ļ����������ȷ���
uniq_num=`sed '/^$/d' ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} | awk -F"\t" '{print NF}' | uniq | wc -l`
if [ $uniq_num -ne 1 ]
then
	ERR "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} got multi field number"
	msg="ufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}�������ͳһ,�ļ����ܵ���"
	alert 1 "${msg}"
fi
field_num=`sed '/^$/d' ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} | awk -F"\t" '{print NF}' | uniq | head -1`
if [ $field_num -ne 2 ]
then
	ERR "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} field number not right"
	msg="ufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}���ڲ�Ϊ�������ֶε���,�ļ����ܵ���"
	alert 1 "${msg}"
fi

## ����ļ����ܰ�������֮����κ��ַ�
grep -v "[0-9]" ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} >> /dev/null
if [ $? -eq 0 ]
then
	ERR "${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} have non-numric chars"
	msg="ufsԭʼ�����ļ�${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME}���ڲ�Ϊ���ֵ���,�����ļ�"
	alert 1 "${msg}"
fi


msg="��ѯ������Ⱥidʧ��"
INF "Begin query all pid" 
QUERY_ALL_PID="select pid, cookienum from beidou.vtpeople where hpid=0 and [userid]"
runsql_sharding_read "$QUERY_ALL_PID" ${VT_PEOPLE_ALL_PID_FILE}.NUM
awk -F'\t' '{print $1}' ${VT_PEOPLE_ALL_PID_FILE}.NUM > ${VT_PEOPLE_ALL_PID_FILE}
alert $? "${msg}"

msg="��ѯ����������Ⱥidʧ��"
INF "Begin query all pid" 
QUERY_ALL_PID="select pid, cookienum from beidou.similar_people where [userid]"
runsql_sharding_read "$QUERY_ALL_PID" ${SIMILAR_PEOPLE_ALL_PID_FILE}.NUM
awk -F'\t' '{print $1}' ${SIMILAR_PEOPLE_ALL_PID_FILE}.NUM > ${SIMILAR_PEOPLE_ALL_PID_FILE}
alert $? "${msg}"


msg="����������ufs���ݹ�������Ⱥidʧ��"
INF "Begin get all pid from ufs file" 
sed '/^$/d' ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} | awk -F"\t" '{print $1}' > ${VT_PEOPLE_UFS_PID_FILE}
alert $? "${msg}"


## �����������ļ�ȥ��ʱ�����׺
mv ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} ${VT_PEOPLE_COOKIENUM_FILE}
INF "Move ${VT_PEOPLE_COOKIENUM_FILE}.${CURR_DATETIME} to ${VT_PEOPLE_COOKIENUM_FILE}"


############################################################
### Step3. ���ɸ����ļ�
############################################################

awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{if(($1 in map) && $2!= map[$1]){print "update beidou.vtpeople set cookienum="map[$1]" where pid="$1";"}}' ${VT_PEOPLE_COOKIENUM_FILE} ${VT_PEOPLE_ALL_PID_FILE}.NUM> vt.sql.${CURR_DATETIME} 

awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{if(!($1 in map) && $2!=0){print "update beidou.vtpeople set cookienum=0 where pid="$1";"}}'   ${VT_PEOPLE_COOKIENUM_FILE} ${VT_PEOPLE_ALL_PID_FILE}.NUM > vt.sql.zero.${CURR_DATETIME}


awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{if(($1 in map) && $2!= map[$1]){print "update beidou.similar_people set cookienum="map[$1]" where pid="$1";"}}' ${VT_PEOPLE_COOKIENUM_FILE} ${SIMILAR_PEOPLE_ALL_PID_FILE}.NUM  > similar.sql.${CURR_DATETIME} 

awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{if(!($1 in map) && $2!=-1){print "update beidou.similar_people set cookienum=-1 where pid="$1";"}}' ${VT_PEOPLE_COOKIENUM_FILE} ${SIMILAR_PEOPLE_ALL_PID_FILE}.NUM  > similar.sql.zero.${CURR_DATETIME} 



############################################################
### Step4. �����ļ���Ƭ
############################################################

rm -rf *.`date -d "20 hour ago" +%Y%m%d%H`*

rm -f ${DATA_SPLIT_NONZERO_PATH}/vt.sql.split*
split -l $SPLIT_FILE_LINE_NUMBER vt.sql.${CURR_DATETIME} ${DATA_SPLIT_NONZERO_PATH}/vt.sql.split 
alert $? "${msg}"


rm -f ${DATA_SPLIT_ZERO_PATH}/vt.sql.zero.split*
split -l $SPLIT_FILE_LINE_NUMBER vt.sql.zero.${CURR_DATETIME} ${DATA_SPLIT_ZERO_PATH}/vt.sql.zero.split 
alert $? "${msg}"


rm -f ${SIMILAR_PEOPLE_DATA_SPLIT_NONZERO_PATH}/similar.sql.split*
split -l $SPLIT_FILE_LINE_NUMBER similar.sql.${CURR_DATETIME} ${SIMILAR_PEOPLE_DATA_SPLIT_NONZERO_PATH}/similar.sql.split 
alert $? "${msg}"


rm -f ${SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH}/similar.sql.zero.split*
split -l $SPLIT_FILE_LINE_NUMBER similar.sql.zero.${CURR_DATETIME} ${SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH}/similar.sql.zero.split 
alert $? "${msg}"



############################################################
### Step5. ���ɷ�Ƭ��sql����ufs���ݹ��������ݸ��������ݿ�
############################################################
## ִ�и���sql

## ִ�и���VT��Ⱥ sql
INF "Begin exec update vt sql" 
for i in `ls ${DATA_SPLIT_NONZERO_PATH}/vt.sql.split*`
do
	INF "Begin exec $j" 
	msg="����vt_people���${i}.sqlִ��ʧ��"
	runsql_sharding "source ${i}"
	alert $? "${msg}"
	INF "Sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL for the next run" 
	sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL
	rm  ${i}
done

INF "Begin exec update vt sql.zero" 
for i in `ls ${DATA_SPLIT_ZERO_PATH}/vt.sql.zero.split*`
do
	INF "Begin exec $j" 
	msg="����vt_people���${i}.sqlִ��ʧ��"
	runsql_sharding "source ${i}"
	alert $? "${msg}"
	INF "Sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL for the next run" 
	sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL
	rm  ${i}
done



## ִ�и���������Ⱥ sql
INF "Begin exec update similar sql" 
for j in `ls ${SIMILAR_PEOPLE_DATA_SPLIT_NONZERO_PATH}/similar.sql.split*`
do
	INF "Begin exec $j" 
	msg="����similar_people���${j}.sqlִ��ʧ��"
	runsql_sharding "source ${j}"
	alert $? "${msg}"
	INF "Sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL for the next run" 
	sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL
	rm  ${j}
done

INF "Begin exec update similar sql" 
for j in `ls ${SIMILAR_PEOPLE_DATA_SPLIT_ZERO_PATH}/similar.sql.zero.split*`
do
	INF "Begin exec $j" 
	msg="����similar_people���${j}.zero.sqlִ��ʧ��"
	runsql_sharding "source ${j}"
	alert $? "${msg}"
	INF "Sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL for the next run" 
	sleep $INTERVAL_TIME_BETWEEN_UPDATE_SQL
	rm  ${j}
done


#STEP 6 ����DMP����Ⱥ
	## ץȡDMPԭʼ�����ļ�
	wget -q -t ${MAX_RETRY} --limit-rate=${LIMIT_RATE} ${DMP_AUDIENCE_COUNT_PATH}/${DMP_AUDIENCE_FILE} -O dmp_account

	## ץȡDMPԭʼ�����ļ�md5
	wget -q -t ${MAX_RETRY} --limit-rate=${LIMIT_RATE} ${DMP_AUDIENCE_COUNT_PATH}/${DMP_AUDIENCE_FILE_MD5} -O dmp_account.md5
	
	
	msg="��ȡDMP��Ⱥ����ʧ��"
	md5_file=`cat dmp_account.md5 | awk '{print $1}'`;
	md5_str=`md5sum dmp_account | awk '{print $1}'`;
	if [ ${md5_file}"x" != "${md5_str}x" ];then
		alert $? "${msg}"
		exit 1;
	fi
	
	msg="��ѯ����DMP��Ⱥidʧ��"
	INF "Begin query all pid" 
	QUERY_ALL_PID="select pid, cookienum from beidou.vtpeople where hpid>0 and [userid]"
	runsql_sharding_read "$QUERY_ALL_PID" dmp_vtid.NUM
	
	awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{if(($1 in map) && $2!=0 && $2!= map[$1]){print "update beidou.vtpeople set cookienum="$2" where pid="$1";"}}' dmp_vtid.NUM dmp_account  > dmp.sql.${CURR_DATETIME}
	
	if [ -s dmp.sql.${CURR_DATETIME} ];then
		runsql_sharding "source ${DATA_PATH}/dmp.sql.${CURR_DATETIME}"
	else
	    INF "NO DMP COOKIE NUM TO BE UPDATED" 
	fi
	rm -rf *.`date -d "20 hour ago" +%Y%m%d%H`*

INF "All finished"
