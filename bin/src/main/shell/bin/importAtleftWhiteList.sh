#!/bin/sh

#@file:importAtleftWhiteList.sh
#@author:caichao
#@version:1.0.0.0
#@brief:import atleft whitelist to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importAtleftWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAtleftWhiteList.sh

TODAY=`date +%Y%m%d`
LOG_FILE=${LOG_PATH}/importAtleftWhiteList.log.${TODAY}

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="��������Ŀ¼${DATA_PATH}ʧ��"
cd ${DATA_PATH}
alert $? "${msg}"

CONF_SH=${WHITE_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${WHITE_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#ץȡ�ļ�����֤MD5
msg="wget�ļ�${WHITE_FILE}ʧ��"
wget -q  ${WHITE_URL}/${WHITE_FILE} -O ${WHITE_FILE}
alert $? "${msg}"

msg="wget�ļ�${WHITE_FILE}.md5ʧ��"
wget  -q ${WHITE_URL}/${WHITE_FILE}.md5 -O ${WHITE_FILE}.md5
alert $? "${msg}"

msg="${WHITE_FILE}�ļ���md5У��ʧ��"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

#ȥ�����к��ж��Ƿ�������
sed -i '/^$/d' ${WHITE_FILE}
num=`wc -l ${WHITE_FILE} | awk '{print $1}'`
if [ "${num}" = "0" ];then
	exit 0
fi

awk -F'	' -v white_type=${WHITE_TYPE} 'BEGIN{OFS="	"} $0 ~ /^[0-9]+$/ {print white_type,$1}' ${WHITE_FILE} > ${WHITE_FILE_NEW}
alert $? "awk ������ʧ��"

runsql_cap "delete from beidoucap.whitelist where type=${WHITE_TYPE}" >> ${LOG_FILE} 2>&1
alert $? "ɾ�����ݿ�atleft������ʧ��"

runsql_cap "load data local infile '${WHITE_FILE_NEW}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
alert $? "����atleft������ʧ��"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}

