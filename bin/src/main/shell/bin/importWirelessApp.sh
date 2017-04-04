#!/bin/sh

#@file:importWirelessApp.sh
#@author:zhuxiaoling
#@date:2013-04-17
#@version:1.0.0.0
#@brief:import qiushi wireless app info to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importWirelessApp.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importWirelessApp.sh

LOG_FILE=${LOG_PATH}/importWirelessApp.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

YESTERDAY=`date -d "-1 day" "+%Y%m%d"`
TODAY=`date +%Y%m%d`

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

[ -f "${TRADE_MAP_FILE}" ] && mv -f ${TRADE_MAP_FILE} ${TRADE_MAP_FILE}.${YESTERDAY}
[ -f "${TRADE_MAP_FILE}.md5" ] && rm -f ${TRADE_MAP_FILE}.md5

[ -f "${WIRELESS_UNION_APP_FILE}" ] && mv -f ${WIRELESS_UNION_APP_FILE} ${WIRELESS_UNION_APP_FILE}.${YESTERDAY}
[ -f "${WIRELESS_UNION_APP_FILE}.md5" ] && rm -f ${WIRELESS_UNION_APP_FILE}.md5


#抓取秋实/北斗行业ID映射文件
msg="wget文件${TRADE_MAP_FILE}失败"
wget -qc -t 3 -T 10  ${TRADE_MAP_URL}/${TRADE_MAP_FILE} -O ${TRADE_MAP_FILE}
alert $? "${msg}"

msg="wget文件${TRADE_MAP_FILE}.md5失败"
wget -qc -t 3 -T 10 ${TRADE_MAP_URL}/${TRADE_MAP_FILE}.md5 -O ${TRADE_MAP_FILE}.md5
alert $? "${msg}"

msg="${TRADE_MAP_FILE}文件的md5校验失败"
md5sum -c ${TRADE_MAP_FILE}.md5
alert $? "${msg}"

#处理usertrade文件
awk 'BEGIN{OFS="\t"} {print $2,$3,$4,$5}' ${TRADE_MAP_FILE} > ${TRADE_MAP_FILE}.tmp2

# 处理union的app
msg="wget文件${WIRELESS_UNION_APP_FILE}失败"
wget -qc -t 3 -T 10 ${WIRELESS_UNION_APP_URL}/${WIRELESS_UNION_APP_FILE} -O ${WIRELESS_UNION_APP_FILE}
if [ $? -ne 0 ]
then
	alert $? "${msg}"
fi

msg="wget文件${WIRELESS_UNION_APP_FILE}.md5失败"
wget  -qc -t 3 -T 10 ${WIRELESS_UNION_APP_URL}/${WIRELESS_UNION_APP_FILE}.md5 -O ${WIRELESS_UNION_APP_FILE}.md5
if [ $? -ne 0 ]
then
	alert $? "${msg}"
fi

msg="${WIRELESS_UNION_APP_FILE}文件的md5校验失败"
md5sum -c ${WIRELESS_UNION_APP_FILE}.md5
alert $? "${msg}"


#将秋实TRADEID映射成北斗TRADEID
awk -F "\t" -v v='\t' 'BEGIN{OFS="\t"}ARGIND==1{key1=$3v$4;map[key1]=$1v$2;next}ARGIND==2{key2=$2v$3; if(key2 in map){print $1,map[key2],$4,strtonum("0x" $5),$6} else {alert 1 "导入联盟app信息出错，原因是没有找到对应的行业映射关系"}}' ${TRADE_MAP_FILE}.tmp2 ${WIRELESS_UNION_APP_FILE} > ${WIRELESS_UNION_APP_FILE}.new

awk -F'\t' '{print $0"\t0"}' ${WIRELESS_UNION_APP_FILE}.new > tmp 
mv tmp ${WIRELESS_UNION_APP_FILE}.new

# 将app信息导入DB
runsql_cap  "load data local infile '${WIRELESS_UNION_APP_FILE}.new' replace into table beidoucode.app CHARACTER SET utf8 fields terminated by '\t' (description,first_trade,second_trade,name,sid,status,type);"   >> ${LOG_FILE} 2>&1

alert $? "导入union app行业信息失败"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}

