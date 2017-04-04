#!/bin/sh

#@file:importItStat.sh
#@author:wangxiaokun
#@date:2013-01-28
#@brief:����Ȥ�Ƽ���pv��uv������beidouext.interest_stat��������������б仯��ˢ�»��档

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importItStat.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importItStat.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/importItStat.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BIN_PATH}

mkdir -p ${WORK_PATH}
alert $? "����${WORK_PATH}ʧ��"
cd ${WORK_PATH}

#��־��¼����
function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE
	fi
}

#������Ȥ��pv��uv���ݲ���֤�Ƿ����仯
function downloadData()
{
    log "������Ȥ��ͳ�����ݿ�ʼ"  
	
	mkdir -p ${TMP_PATH}
    cd ${TMP_PATH}
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${TAR_FILE}
	alert $? "������Ȥ��ͳ������ʧ��"
	
	tar -xzvf ${TMP_PATH}${TAR_FILE} 
	alert $? "��ѹ�ļ�ʧ��"
   
	md5sum -c ${FILE_NAME}".md5"
	alert $? "��֤�ļ���MD5ʧ��"
   
	log "������Ȥ��ͳ�����ݽ���"
}

#�Ա��¾��ļ����ж��Ƿ�仯
#���û�б仯����ֱ���˳�
#����б仯ִ�к�������ˢ�������
function diffFile()
{
	log "�Ա��¾��ļ��ж��Ƿ��б仯"
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	log "�Ա��¾��ļ��ж��Ƿ��б仯"
	if [ -f ${WORK_PATH}${FILE_NAME} ];then
	    
		diff ${WORK_PATH}${FILE_NAME} ${TMP_PATH}${FILE_NAME}
		if [ $? -eq 0 ];then
			log "�ļ�û�б仯��ֱ���˳�"
			rm -rf ${TMP_PATH}
			exit 0
		fi
		log "�ļ������仯��ִ��������"
		#ɾ�����ļ�
		rm -f ${WORK_PATH}${FILE_NAME}
		
	fi
	
	cp ${TMP_PATH}${FILE_NAME} ${WORK_PATH}${FILE_NAME}
	rm -rf ${TMP_PATH}	

}

#�����ļ�����DB
function importDB()
{
	log "�����Ȥͳ�Ʊ�beidouext.interest_stat��ʼ"
	
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	
	runsql_xdb "drop table if exists beidouext.interest_stat_tmp" 
	alert $? "drop��ʱ��interest_stat_tmpʧ��"
	
	runsql_xdb "create table beidouext.interest_stat_tmp like beidouext.interest_stat"
	alert $? "������ʱ��interest_stat_tmpʧ��"
	
	runsql_xdb "load data local infile '${WORK_PATH}${FILE_NAME}' into table beidouext.interest_stat_tmp(iid,regid,uv,pv)"
	alert $? "���ݵ�����ʱ��ʧ��"
	
	runsql_xdb "drop table if exists beidouext.interest_stat; rename table beidouext.interest_stat_tmp to beidouext.interest_stat"
	alert $? "��������ʱ��ʧ��"
	
	log "�����Ȥͳ�Ʊ�beidouext.interest_stat����"
}

#ˢ�»���
function refreshCache()
{
	log "ˢ����Ȥͳ�ƻ��濪ʼ"
	
	#����shell����Ŀ¼
	cd ${BIN_PATH}
	sh loadInterestStat.sh
	
	log "ˢ����Ȥͳ�ƻ������"

}
#main
function main()
{
	downloadData
	diffFile
	importDB
	refreshCache
}

main
