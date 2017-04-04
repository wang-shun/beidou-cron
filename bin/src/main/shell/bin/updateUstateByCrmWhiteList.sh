#!/bin/sh

#@file:updateUstateByCrmWhiteList.sh
#@author:wangxiaokun
#@date:2015-01-20
#@brief:��CRM�Ŷӻ�ȡ��Ч���Żݿ�ܺ�ͬ������,
#���˳���ֹͶ�����˵�����,��ǰһ������ݶԱ�,����useraccount���ustate�ֶ�Ϊ1(��ֹͶ��)����0(����)

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/updateUstateByCrmWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=updateUstateByCrmWhiteList.sh
reader_list=wangxiaokun

current_date=`date +"%Y%m%d"`

LOG_FILE_NAME=${LOG_PATH}/updateUstateByCrmWhiteList/${current_date}.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}/updateUstateByCrmWhiteList
mkdir -p ${DATA_PATH}
mkdir -p ${BIN_PATH}

mkdir -p ${WORK_PATH}
alert $? "����${WORK_PATH}ʧ��"
cd ${WORK_PATH}

#��־��¼����
function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE_NAME
	fi
}

#����CRM���ṩ������
function downloadData()
{
    log "����${FILE_NAME}_${1}���ݿ�ʼ"  
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_SERVER}${REMOTE_FILE_PATH}${FILE_NAME}_${1} -O ${FILE_NAME}_${1}
	
	if [ $? -ne 0 ];then
		log "����${FILE_NAME}_${1}����ʧ��"
		return 1
	fi
	
	log "����${FILE_NAME}_${1}.md5�ļ���ʼ"
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_SERVER}${REMOTE_FILE_PATH}${FILE_NAME}_${1}.md5 -O ${FILE_NAME}_${1}.md5
	alert $? "����${FILE_NAME}_${1}.md5�ļ���ʼ����ʧ��"
	
	md5sum -c ${FILE_NAME}_${1}.md5
	alert $? "��֤�ļ���MD5ʧ��"
   
	log "����${FILE_NAME}_${1}���ݽ���"
}

#CRM�ṩ���ļ���ʽΪ:userid,product_line_group,begin_date,end_date
#�����߼���wangmeng not in product_line_group && file_date in [begin_date,end_date)
function filterDisableUserIds()
{
	log "����CRM�ṩ���ļ�${FILE_NAME}_${1}��ʼ"
	
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	next_date=`date -d "+1 day ${1}" "+%Y%m%d"`
	
	awk -F"\t" -v dt="$next_date" '!/wangmeng/{
			if((dt>=$3) && (dt<$4) ) {
				print $1
			}
		}' ${FILE_NAME}_${1} > ${CRM_DISABLE_USERIDS}_${1}
	
	
	log "����CRM�ṩ���ļ�${FILE_NAME}_${1}����"
	
}


#��ǰһ����ļ��Աȣ���ȡ��Ҫ���µ��б�������useraccount��
function update()
{
	log "��ʼ���±�useraccount"
	
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	#�ж�ǰһ����ļ��Ƿ����,������˵����һ�����У��򴴽�һ�����ļ�
	pre_date=`date -d "-1 day ${1}" "+%Y%m%d"`
	if [ ! -e ${CRM_DISABLE_USERIDS}_${pre_date} ]
		then touch ${CRM_DISABLE_USERIDS}_${pre_date}
	fi
	
	update_to_1_str=`awk -F"\t" 'ARGIND==1{preMap[$1]=$1}ARGIND==2{if(!($1 in preMap)){printf(",%s",$1)}}' ${CRM_DISABLE_USERIDS}_${pre_date} ${CRM_DISABLE_USERIDS}_${1}`
	update_to_0_str=`awk -F"\t" 'ARGIND==1{crmDisableMap[$1]=$1}ARGIND==2{if(!($1 in crmDisableMap)){printf(",%s",$1)}}' ${CRM_DISABLE_USERIDS}_${1} ${CRM_DISABLE_USERIDS}_${pre_date}`
	
	up0_sql="update beidoucap.useraccount set ustate=0 where userid in(-1"${update_to_0_str}") and ustate=1";
	up1_sql="update beidoucap.useraccount set ustate=1 where userid in(-1"${update_to_1_str}") and ustate=0";
	
	log "����ustate=0��SQL��${up0_sql}"
	log "����ustate=1��SQL��${up1_sql}"
	
	runsql_cap "${up0_sql}"
	alert $? "����useraccount����ustate=0ʧ��"

	runsql_cap "${up1_sql}"
	alert $? "����useraccount����ustate=1ʧ��"
	
	log "���±�useracount����"
}

#ɾ��һ����֮ǰ������
function deleteHistoryData()
{
	log "ɾ����ʷ���ݿ�ʼ"
	cd ${WORK_PATH}
	timeDel=`date -d "30 day ago" +%Y%m%d%H`
	if [ -e ${FILE_NAME}_${timeDel} ];then
	    rm -f ${FILE_NAME}_${timeDel}
		rm -f ${CRM_DISABLE_USERIDS}_${timeDel}
	fi
	log "ɾ����ʷ���ݽ���"
}

#main
function main()
{
	#Ĭ�����ص������ݣ��������û�����ɣ�������ǰһ������
    file_date=${current_date}
	downloadData ${file_date}
	if [ $? -ne 0 ];then
	    file_date=`date -d "1 day ago" +%Y%m%d`
		downloadData ${file_date}
		alert $? "����${FILE_NAME}_${file_date}����ʧ��"
	fi
	#��CRM�����й��˳�����Ͷ���û��б�
	filterDisableUserIds ${file_date}
	#�������ݿ�
	update ${file_date}
	#ɾ����ʷ����
	deleteHistoryData
	#��Ǵ������
	touch done.${file_date}
	
}

main
