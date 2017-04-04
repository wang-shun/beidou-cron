#!/bin/sh
# This program is used to import saler's customer info.

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="${CONF_PATH}/importSalerInfo.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importSalerInfo.sh
reader_list=zhangpingan
STAT_DATE=`date +%Y%m%d`
if [ -n "$1" ] ;then
	STAT_DATE=`date -d"$1" +%Y%m%d`
fi
cd ${BALANCE_PATH}
alert $? "进入文件目录${BALANCE_PATH}失败"
md5sum -c ${BALANCE_FILE}.${STAT_DATE}.md5
alert $? "文件${BALANCE_FILE}.${STAT_DATE}.md5校验失败"

cd ${BIN_PATH}
alert $? "进入工作目录${BIN_PATH}失败"

# Generate the html bulletin from cpmis.notice_info 
# after the date which stored in beidou.sysnvtab and whose name is MSG_LAST_TIME.
# The html bulletin is in ./message/ directory.
java -Xms512m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.salemanager.ImportSalerInfo -b ${BALANCE_PATH}/${BALANCE_FILE}.${STAT_DATE} -o${BALANCE_PATH}/${SALERINFO_FILE} >> ${LOG_FILE} 2>&1
alert $? "更新销售管理员的综述信息发生异常"

runsql_xdb "use beidouext;drop table if exists salercustomerinfo_tmp;create table salercustomerinfo_tmp like salercustomerinfo;
					load data local infile '${BALANCE_PATH}/${SALERINFO_FILE}' into table salercustomerinfo_tmp;
					drop table salercustomerinfo; rename table salercustomerinfo_tmp to salercustomerinfo;
					"
alert $? "更新数据库salercustomerinfo发生异常"
cd ${BALANCE_PATH}
mv ${SALERINFO_FILE} ${SALERINFO_FILE}.${STAT_DATE}
alert $? "mv SALERINFO_FILE发生异常"
md5sum ${SALERINFO_FILE}.${STAT_DATE} > ${SALERINFO_FILE}.${STAT_DATE}.md5
alert $? "生成MD5文件发生异常"
