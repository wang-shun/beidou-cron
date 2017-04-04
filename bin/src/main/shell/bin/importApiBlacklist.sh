#!/bin/sh

#@file:importApiBlacklist.sh
#@author:zhangxu
#@date:2012-01-29
#@version:1.0.0.0
#@brief:import api v2 user blacklist
#@pre-request: must set api server list in common.conf like:
#				APIV2_SERVER_IP_LIST=($jxapi01 $tcapi01)
#				APIV2_SERVER_IP_PORT_LIST=($jxapi01':8230' $tcapi01':8230')


CONF_SH=/home/work/.bash_profile
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/importApiBlacklist.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importApiBlacklist.sh
reader_list=zhangxu

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${LOG_PATH}/importApiBlacklist
mkdir -p ${API_BLACKLIST_DATA_PATH}

# ���������
# $1: �������ļ�
# $2: ���ݿ��
function import_and_load_blacklist()
{
	BLACKLIST_FILE=$1
	BLACKLIST_TABLE=$2
	ZONGKONG_CENTER_DOWNLOAD_URL=${BLACKLIST_FILE_URL}
	
	#ץȡ�������ļ�
	[ -f ${BLACKLIST_FILE} ] && mv ${BLACKLIST_FILE} ${BLACKLIST_FILE}.bak
	[ -f ${BLACKLIST_FILE}.md5 ] && mv ${BLACKLIST_FILE}.md5 ${BLACKLIST_FILE}.md5.bak
	
	wget -q  ${BLACKLIST_FILE_URL}/${BLACKLIST_FILE}.md5
	if [ $? -ne 0 ];then
		wget -q  ${BLACKLIST_FILE_URL_BAK}/${BLACKLIST_FILE}.md5
		if [ $? -eq 0 ];then
			echo "�ܿ�������������${BLACKLIST_FILE_URL}������ͨ���л����ܿ����ı��ݷ�����${BLACKLIST_FILE_URL_BAK}" >> ${LOG_FILE}
			ZONGKONG_CENTER_DOWNLOAD_URL=${BLACKLIST_FILE_URL_BAK}
		fi
	fi
	rm -f ${BLACKLIST_FILE}.md5
	
	msg="wget�������ļ�${ZONGKONG_CENTER_DOWNLOAD_URL}${BLACKLIST_FILE}.md5ʧ��"
	wget -q  ${ZONGKONG_CENTER_DOWNLOAD_URL}/${BLACKLIST_FILE}.md5
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi

	msg="wget�������ļ�${ZONGKONG_CENTER_DOWNLOAD_URL}${BLACKLIST_FILE}ʧ��"
	wget -q  ${ZONGKONG_CENTER_DOWNLOAD_URL}/${BLACKLIST_FILE}
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi

	msg="${ZONGKONG_CENTER_DOWNLOAD_URL}${BLACKLIST_FILE}�ļ���md5У��ʧ��"
	md5sum -c ${BLACKLIST_FILE}.md5
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi
	
	msg="${BLACKLIST_FILE}�ļ�Ϊ��"
	if [ ! -s ${BLACKLIST_FILE} ];then
		alert 1 "${msg}"
		return 1
	fi
	
	# ȥ������
	sed '/^$/d' ${BLACKLIST_FILE} > ${BLACKLIST_FILE}.tmp
	line_num=`cat ${BLACKLIST_FILE} | wc -l`
	line_num_rm_blank=`cat ${BLACKLIST_FILE}.tmp | wc -l`
	blank_line_num=`expr $line_num - $line_num_rm_blank`
	if [ $blank_line_num -ne 0 ];then
		echo "ȥ��${blank_line_num}������" >> ${LOG_FILE}
	fi
	[ -f ${BLACKLIST_FILE}.tmp ] && cp ${BLACKLIST_FILE}.tmp ${BLACKLIST_FILE}
	
	# �Ƚ��ļ��������ͬ��û�б�Ҫ�ٴ���⣬������2
	if [ ! -e ${BLACKLIST_FILE}.bak ];then
		touch ${BLACKLIST_FILE}.bak
	fi
	if [ ! -e ${BLACKLIST_FILE}.md5.bak ];then
		touch ${BLACKLIST_FILE}.md5.bak
	fi
	diff ${BLACKLIST_FILE} ${BLACKLIST_FILE}.bak >> /dev/null
	if [ $? -eq 0 ];then
		echo "${BLACKLIST_FILE} do not change, pass updating database." >> ${LOG_FILE}
		return 2;
	fi
	
	echo "${BLACKLIST_FILE}�ļ�(��${line_num}��)�Ѹ��£������ļ���${BLACKLIST_TABLE}_bak��" >> ${LOG_FILE}
	START_TIME=`date +%s`
	msg="�����ļ�${BLACKLIST_FILE}ʧ��"
	
	runsql_cap "use beidoucap;drop table if exists ${BLACKLIST_TABLE}_bak;
		create table ${BLACKLIST_TABLE}_bak like ${BLACKLIST_TABLE};
		load data local infile '${API_BLACKLIST_DATA_PATH}/${BLACKLIST_FILE}' into table ${BLACKLIST_TABLE}_bak;"
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi
	END_TIME=`date +%s`
	echo "load���ݿ��ļ�${BLACKLIST_FILE}ʱ��(��λΪ��)Ϊ��`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
	
	START_TIME=`date +%s`
	msg="��������${BLACKLIST_TABLE}ʧ��"
	
	runsql_cap "use beidoucap; rename table ${BLACKLIST_TABLE} to ${BLACKLIST_TABLE}_tmp,
	${BLACKLIST_TABLE}_bak to ${BLACKLIST_TABLE},
	${BLACKLIST_TABLE}_tmp to ${BLACKLIST_TABLE}_bak;"
	  
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi
	END_TIME=`date +%s`
	echo "rename���ݿ��${BLACKLIST_TABLE}ʱ��(��λΪ��)Ϊ��`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
	
	# call rpc 
	sleep 5
	msg="��������Ŀ¼${BIN_PATH}ʧ��"
	cd ${BIN_PATH}
	alert $? "${msg}"
	
	msg="ˢ��api�������û�����ʧ��"
	sh loadApiBlacklistCache.sh
	alert $? "${msg}"
	
	msg="��������Ŀ¼${API_BLACKLIST_DATA_PATH}ʧ��"
	cd ${API_BLACKLIST_DATA_PATH}
	alert $? "${msg}"
	
	return 0
}

# ����ʧ��ʱ�ָ��ļ�
function backup()
{
	last_return=$1
	BLACKLIST_FILE=$2
	if [ $last_return -eq 1 ];then
		[ -f ${BLACKLIST_FILE}.bak ] && mv ${BLACKLIST_FILE}.bak ${BLACKLIST_FILE}
		[ -f ${BLACKLIST_FILE}.md5.bak ] && mv ${BLACKLIST_FILE}.md5.bak ${BLACKLIST_FILE}.md5
	elif [ $last_return -eq 0 ];then
		cp ${BLACKLIST_FILE} ${BLACKLIST_FILE}.${TIMESTAMP} && rm ${BLACKLIST_FILE}.bak && rm ${BLACKLIST_FILE}.md5.bak
	elif [ $last_return -eq 2 ];then
		rm ${BLACKLIST_FILE}.bak && rm ${BLACKLIST_FILE}.md5.bak
	fi
}

# main
msg="��������Ŀ¼${API_BLACKLIST_DATA_PATH}ʧ��"
cd ${API_BLACKLIST_DATA_PATH}
alert $? "${msg}"
echo "`date +"%Y-%m-%d %H:%M:%S"`,��ʼ����">>${LOG_FILE}

export BLACKLIST_FILE_URL=${API_BLACKLIST_FILE_URL}
export BLACKLIST_FILE_URL_BAK=${API_BLACKLIST_FILE_URL_BAK}
import_and_load_blacklist ${API_BLACKLIST_FILE} ${API_BLACKLIST_TABLE}
backup $? ${API_BLACKLIST_FILE}

