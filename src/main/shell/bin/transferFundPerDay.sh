#!/bin/sh
# 
# code migration
# @author yanjie
# @version 1.0.32

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/transferFundPerDay.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=transferFundPerDay.sh
reader_list=zhangpingan
mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

TIME_YYYYMMDD=`date -d 'yesterday' +%Y%m%d`
LOG_FILE=${LOG_PATH}/transferFundPerDay.${TIME_YYYYMMDD}.log

msg="ÿ�ն�ʱת������ʧ�ܣ���׷��"
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.TransferFundPerDay  &> ${LOG_FILE}
alert $? "${msg}"


sleep 10
msg="ÿ�ն�ʱת�����ݲ����ڣ���׷��"
if [ ! -f ${DATA_FILE} ]; then 
	 alert 1 "${msg}"
fi;

msg="ÿ�ն�ʱת�����ݴ���ʧ�ܣ���׷��"
awk -F'\t' '{printf("%s\t%s\t%s\t0000-00-00 00:00:00\n",$2,$3*100,$5)}'  ${DATA_FILE} > ${LOAD_FILE}
alert $? "${msg}"


msg="����ÿ�ն�ʱת����־ʧ�ܣ���׷��"
runsql_xdb "truncate table beidouext.autotransfer;load data local infile '${LOAD_FILE}' into table beidouext.autotransfer;"
alert $? "${msg}"

