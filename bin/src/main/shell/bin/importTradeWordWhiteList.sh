#!/bin/sh

#@file:importTradeWordWhiteList.sh
#@author:wangxiaokun
#@date:2013-09-22
#@brief:����ҵ���Ƽ����ܵİ������û�����whitelist���У���ˢ�»��棬�ű��߼����ж��ļ��Ƿ���£����û�и�����ֱ���˳���

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importTradeWordWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importTradeWordWhiteList.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/importTradeWordWhiteList.log

#��ʼ��Ŀ¼
function init(){

	mkdir -p ${ROOT_PATH}
	mkdir -p ${LOG_PATH}
	mkdir -p ${DATA_PATH}
	mkdir -p ${BIN_PATH}
	mkdir -p ${WORK_PATH}
	
	alert $? "����${WORK_PATH}ʧ��"
	cd ${WORK_PATH}
}


#��־��¼����
function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE
	fi
}

#������ҵ���û��б���֤�Ƿ����仯
function downloadData()
{
    log "������ҵ���û��б����ݿ�ʼ" 
	#������ʱĿ¼���������
	if [ -d ${TMP_PATH} ];then
		rm -rf ${TMP_PATH}
	fi

	mkdir -p ${TMP_PATH}
	cd ${TMP_PATH}
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${FILE_NAME}
	alert $? "������ҵ���û��б�����ʧ��"
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${FILE_NAME}.md5
	alert $? "������ҵ���û��б�����md5ʧ��"
	
	md5sum -c ${FILE_NAME}".md5"
	alert $? "��֤��ҵ���û��б����ݵ�MD5ʧ��"
   
	log "������ҵ���û��б����ݽ���"
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
			log "�ļ�û�б仯��ֱ���˳�"
			rm -rf ${TMP_PATH}
			exit 0
		fi
		log "�ļ������仯��ִ��������"
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
	log "����������beidoucap.whiltlist��ʼ"
	#���빤��Ŀ¼
    cd ${WORK_PATH}
	#ɾ���ɵ���ҵ�ʰ�����
	runsql_cap "delete from beidoucap.whitelist where type=${WHITE_TYPE_GAME} or type=${WHITE_TYPE_MEDICAL}" >> ${LOG_FILE} 2>&1
	alert $? "ɾ�����ݿ���ҵ���û�������ʧ��"

	runsql_cap "load data local infile '${FILE_NAME}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
	alert $? "������ҵ���û�������ʧ��"
	
	log "����������beidoucap.whiltlist����"
}
#webԶ�̵��÷���
function call() {
    msg="ִ��Զ�̵���-ˢ����ҵ���Ƽ��û�����ʧ��("$1")"
    url=http://$1/rpc/loadTradeWordWhiteList
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password    
    alert_return $? "${msg}"
}

#ˢ�»���
function refreshCache()
{
	log "ˢ���û���ҵ�ʰ��������濪ʼ"
	#����BINĿ¼
	cd ${BIN_PATH}
	for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; do
    	call $server;
	done
	
	log "ˢ���û���ҵ�ʰ������������"

}
#main
function main()
{
	init
	downloadData
	diffFile
	importDB
	refreshCache
}

main
