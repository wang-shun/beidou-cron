#!/bin/sh

#@file:importAotDB.sh
#@author:yangyun
#@date:2010-12-06
#@version:1.0.0.0
#@brief:

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAotDB.sh

LOG_PATH=${LOG_PATH}/aot
LOG_FILE=${LOG_PATH}/importAotDB.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

#���ȴ���tmp��
msg="drop planstat����ʱ��ʧ��"
runsql_xdb "use aot;drop table if exists cproplanstat_tmp;"
alert $? "${msg}"

msg="drop groupstat����ʱ��ʧ��"
runsql_xdb "use aot;drop table if exists cprogroupstat_tmp;"
alert $? "${msg}"

msg="����planstat����ʱ��ʧ��"
runsql_xdb "use aot; create table cproplanstat_tmp like cproplanstat;"
alert $? "${msg}"

msg="����groupstat����ʱ��ʧ��"
runsql_xdb "use aot; create table cprogroupstat_tmp like cprogroupstat;"
alert $? "${msg}"

msg="�˻��Ż��������ݿ�����ʧ��"
java -Xms6144m -Xmx6144m -classpath ${CUR_CLASSPATH} com.baidu.beidou.aot.ImportAotDB >> ${LOG_FILE} 2>&1
alert $? ${msg}

