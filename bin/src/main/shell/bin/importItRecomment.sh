#!/bin/sh

#@file:importItRecomment.sh
#@author:wangxiaokun
#@date:2013-01-28
#@brief:����Ȥ�Ƽ������������⣬������������б仯��ˢ�»��档
#�������Ƽ��Ľ�ţ��Ȥ�ͺ�����Ȥ��beidouext.interest_recommend����Ȥ��pv��uv������beidouext.interest_stat

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importItRecomment.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importItRecomment.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/importItRecomment.log

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

#�����Ƽ���Ȥ������֤�Ƿ����仯
function downloadData()
{
    log "���ؽ�ţ�ͺ����Ƽ���Ȥ���ݿ�ʼ" 
	#������ʱĿ¼��Ž�ѹ�ļ�
	mkdir -p ${TMP_PATH}
	cd ${TMP_PATH}
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${TAR_FILE}
	alert $? "���ؽ�ţ�ͺ����Ƽ���Ȥ����ʧ��"
	
	tar -xvf ${TMP_PATH}${TAR_FILE}
	alert $? "��ѹ�Ƽ���Ȥ�ļ�ʧ��"
    
	md5sum -c ${FILE_NAME}".md5"
	alert $? "��֤�Ƽ���Ȥ�ļ���MD5ʧ��"
   
	log "���ؽ�ţ�ͺ����Ƽ���Ȥ���ݽ���"
}

#�Ա��¾��ļ����ж��Ƿ�仯
#���û�б仯����ֱ���˳�
#����б仯ִ�к�������ˢ�������
function diffFile()
{
	log "�Ա��¾��ļ��ж��Ƿ��б仯"
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	if [ -f ${WORK_PATH}${FILE_NAME} ];then
	    
		diff ${WORK_PATH}${FILE_NAME} ${TMP_PATH}${FILE_NAME}
		if [ $? -eq 0 ];then
			log "�Ƽ���Ȥ�ļ�û�б仯��ֱ���˳�"
			rm -rf ${TMP_PATH}
			exit 0
		fi
		log "�Ƽ���Ȥ�Ƽ������仯��ִ��������"
		#ɾ�����ļ�
		rm -f ${WORK_PATH}${FILE_NAME}
		
	fi
	
	#�����ļ�����������Ŀ¼
	cp ${TMP_PATH}${FILE_NAME} ${WORK_PATH}${FILE_NAME}
	#ɾ����ʱĿ¼
	rm -rf ${TMP_PATH}	

}


#�����ļ�����DB
function importDB()
{
	log "�����Ȥ�Ƽ���beidouext.interest_recommend��ʼ"
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	
	runsql_xdb "drop table if exists beidouext.interest_recommend_tmp" 
	alert $? "drop��ʱ��interest_recommend_tmpʧ��"
	
	runsql_xdb "create table beidouext.interest_recommend_tmp like beidouext.interest_recommend"
	alert $? "������ʱ��interest_recommend_tmpʧ��"
	
	runsql_xdb "load data local infile '${WORK_PATH}${FILE_NAME}' into table beidouext.interest_recommend_tmp(tradename,tradeid,itname,iid,level)"
	alert $? "���ݵ�����ʱ��ʧ��"
	
	runsql_xdb "drop table if exists beidouext.interest_recommend; rename table beidouext.interest_recommend_tmp to beidouext.interest_recommend"
	alert $? "��������ʱ��ʧ��"
	log "�����Ȥ�Ƽ���beidouext.interest_recommend����"
}

#ˢ�»���
function refreshCache()
{
	log "ˢ����Ȥ�Ƽ����濪ʼ"
	#����shell����Ŀ¼
	cd ${BIN_PATH}
	sh loadInterestRecommend.sh
	
	log "ˢ����Ȥ�Ƽ��������"

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
