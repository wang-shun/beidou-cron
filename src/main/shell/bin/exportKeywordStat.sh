#!/bin/bash

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/exportKeywordStat.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


program=exportKeywordStat.sh
reader_list=dongying01

LOG_FILE=${LOG_PATH}/.log
exportDir="${DATA_PATH}/exportKeywordStat"

if [ ! -d ${exportDir} ];then
    mkdir ${exportDir}
fi

#set timestamp
if [ -z $1 ];then
    result_time=`date -d "yesterday" +%Y%m%d`
else
    result_time=`date -d "$1" +%Y%m%d`
fi

result_file="xtword${result_time}.txt"

#get beidoukeywordstat.yyyyMMdd.merged file replace query from beidoustat db(by kanghongwei since cpweb535)
msg="wget ${KEYWORD_STAT_FILE} failed."
wget -t3  --limit-rate=10M  ftp://${STAT_KEYWORD_DATA_SERVER}/${DATA_OUTPUT_PATH}/${KEYWORD_STAT_FILE} -O ${exportDir}/${KEYWORD_STAT_FILE}
alert $? "${msg}"

msg="get keyword stat data failed."
cat ${exportDir}/${KEYWORD_STAT_FILE} | awk 'BEGIN{OFS="\t"}{print $3,$5,$6,$7}' > ${exportDir}/${result_file}
alert $? "${msg}"

cd ${exportDir}
md5sum $result_file > $result_file.md5
