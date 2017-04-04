#!/bin/sh

#@file:importWirelessWhitelist.sh
#@author:zhuxiaoling
#@date:2013-04-16
#@version:1.0.0.0
#@brief:import wireless whitelist to database, whitelist is not allowed to decrease

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importWirelessWhitelist.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importWirelessWhitelist.sh

LOG_FILE=${LOG_PATH}/importWirelessWhitelist.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

CONF_SH=${WHITE_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#强制保证PM配置的白名单只增不减
touch ${WHITE_FILE} 
cp ${WHITE_FILE} ${WHITE_FILE}.bak

#抓取无线流量白名单
msg="wget文件${WHITE_FILE}失败"
wget -q  ${WHITE_URL}/${WHITE_FILE} -O ${WHITE_FILE}.uncheck
alert $? "${msg}"

awk -v file1=${WHITE_FILE}.uncheck -v file2=${WHITE_FILE}.bak 'FILENAME==file1 {LINE[$0]=$0}\
	FILENAME==file2 {if(""==LINE[$0])print $0}'\
	$WHITE_FILE.uncheck\
	$WHITE_FILE.bak > pmConfigWrong.out

wrongCnt=`wc -l pmConfigWrong.out|awk '{print $1}'`
if [[ 0 != $wrongCnt ]];then
	echo "whitelist ${WHITE_FILE} is not allowed reduced." >> ${LOG_FILE}
	alert 1 "PM配置的移动流量白名单减少，导入白名单失败"
fi

mv ${WHITE_FILE}.uncheck ${WHITE_FILE}

msg="wget文件${WHITE_FILE}.md5失败"
wget  -q ${WHITE_URL}/${WHITE_FILE}.md5 -O ${WHITE_FILE}.md5
alert $? "${msg}"


msg="${WHITE_FILE}文件的md5校验失败"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

CONF_SH=${USERTRADE_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#抓取usertrade文件
msg="wget文件${USERTRADE_FILE}失败"
wget -q  ${USERTRADE_URL}/${USERTRADE_FILE} -O ${USERTRADE_FILE}
alert $? "${msg}"

msg="wget文件${USERTRADE_FILE}.md5失败"
wget  -q ${USERTRADE_URL}/${USERTRADE_FILE}.md5 -O ${USERTRADE_FILE}.md5
alert $? "${msg}"

msg="${USERTRADE_FILE}文件的md5校验失败"
md5sum -c ${USERTRADE_FILE}.md5
alert $? "${msg}"

#userId直接保存至带时间戳的文件
WHITELIST_WITH_TIME=${WHITE_FILE_NEW}.`date +%Y%m%d`
rm -f ${WHITELIST_WITH_TIME}

awk -v userid_tag=${WHITE_USERID_TAG} -v white_type=${WHITE_TYPE} '{if(userid_tag ==$1){printf("%s\t%s\n",white_type,$2)}}' ${WHITE_FILE} >> ${WHITELIST_WITH_TIME}
alert $? "awk 白名单${WHITELIST_WITH_TIME}失败"

CONF_SH=${TMP_TRADE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#tradeId保存至临时文件
awk  -v tradeid_tag=${WHITE_TRADEID_TAG} '{if(tradeid_tag == $1) print $2}' ${WHITE_FILE} >> ${TMP_TRADE}
alert $? "awk 白名单${TMP_TRADE}失败"


#根据tradeId获取所有的userId
for line in `cat ${TMP_TRADE}` 
do
    awk -F "\t" -v tradeId=${line} -v white_type=${WHITE_TYPE} '{if(tradeId == $2) printf("%s\t%s\n",white_type,$1)}' ${USERTRADE_FILE} >> ${WHITELIST_WITH_TIME}
    alert $? "awk 白名单${WHITELIST_WITH_TIME} BY ${TMP_TRADE}失败"
done

cp ${WHITELIST_WITH_TIME} ${WHITELIST_WITH_TIME}.tmp
sort -k2n -u ${WHITELIST_WITH_TIME}.tmp > ${WHITELIST_WITH_TIME}

#强制保证白名单只增不减
#WHITELIST_YESTODAY=${WHITE_FILE_NEW}.`date -d "1 day ago" +%Y%m%d`
#if [ -f "${WHITELIST_YESTODAY}" ];then
#	diff=`awk 'ARGIND==1{map[$2]}ARGIND==2{if(!($2 in map)){print $0}}' ${WHITELIST_WITH_TIME} ${WHITELIST_YESTODAY} | wc -l`
#    if [ ${diff} -gt 0 ];then
#        echo "whitelist ${WHITELIST_WITH_TIME} is not allowed reduced." >> ${LOG_FILE}
#        alert 1 "移动流量白名单用户减少，导入白名单失败"
#    fi
#fi

#runsql_cap "delete from beidoucap.whitelist where type='${WHITE_TYPE}'; load data local infile '${WHITELIST_WITH_TIME}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1

md5sum ${WHITELIST_WITH_TIME} > ${WHITELIST_WITH_TIME}.md5

runsql_cap "load data local infile '${WHITELIST_WITH_TIME}' replace into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
alert $? "导入移动应用流量白名单失败"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}
