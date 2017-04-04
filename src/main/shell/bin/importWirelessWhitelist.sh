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

msg="��������Ŀ¼${DATA_PATH}ʧ��"
cd ${DATA_PATH}
alert $? "${msg}"

CONF_SH=${WHITE_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#ǿ�Ʊ�֤PM���õİ�����ֻ������
touch ${WHITE_FILE} 
cp ${WHITE_FILE} ${WHITE_FILE}.bak

#ץȡ��������������
msg="wget�ļ�${WHITE_FILE}ʧ��"
wget -q  ${WHITE_URL}/${WHITE_FILE} -O ${WHITE_FILE}.uncheck
alert $? "${msg}"

awk -v file1=${WHITE_FILE}.uncheck -v file2=${WHITE_FILE}.bak 'FILENAME==file1 {LINE[$0]=$0}\
	FILENAME==file2 {if(""==LINE[$0])print $0}'\
	$WHITE_FILE.uncheck\
	$WHITE_FILE.bak > pmConfigWrong.out

wrongCnt=`wc -l pmConfigWrong.out|awk '{print $1}'`
if [[ 0 != $wrongCnt ]];then
	echo "whitelist ${WHITE_FILE} is not allowed reduced." >> ${LOG_FILE}
	alert 1 "PM���õ��ƶ��������������٣����������ʧ��"
fi

mv ${WHITE_FILE}.uncheck ${WHITE_FILE}

msg="wget�ļ�${WHITE_FILE}.md5ʧ��"
wget  -q ${WHITE_URL}/${WHITE_FILE}.md5 -O ${WHITE_FILE}.md5
alert $? "${msg}"


msg="${WHITE_FILE}�ļ���md5У��ʧ��"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

CONF_SH=${USERTRADE_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#ץȡusertrade�ļ�
msg="wget�ļ�${USERTRADE_FILE}ʧ��"
wget -q  ${USERTRADE_URL}/${USERTRADE_FILE} -O ${USERTRADE_FILE}
alert $? "${msg}"

msg="wget�ļ�${USERTRADE_FILE}.md5ʧ��"
wget  -q ${USERTRADE_URL}/${USERTRADE_FILE}.md5 -O ${USERTRADE_FILE}.md5
alert $? "${msg}"

msg="${USERTRADE_FILE}�ļ���md5У��ʧ��"
md5sum -c ${USERTRADE_FILE}.md5
alert $? "${msg}"

#userIdֱ�ӱ�������ʱ������ļ�
WHITELIST_WITH_TIME=${WHITE_FILE_NEW}.`date +%Y%m%d`
rm -f ${WHITELIST_WITH_TIME}

awk -v userid_tag=${WHITE_USERID_TAG} -v white_type=${WHITE_TYPE} '{if(userid_tag ==$1){printf("%s\t%s\n",white_type,$2)}}' ${WHITE_FILE} >> ${WHITELIST_WITH_TIME}
alert $? "awk ������${WHITELIST_WITH_TIME}ʧ��"

CONF_SH=${TMP_TRADE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

#tradeId��������ʱ�ļ�
awk  -v tradeid_tag=${WHITE_TRADEID_TAG} '{if(tradeid_tag == $1) print $2}' ${WHITE_FILE} >> ${TMP_TRADE}
alert $? "awk ������${TMP_TRADE}ʧ��"


#����tradeId��ȡ���е�userId
for line in `cat ${TMP_TRADE}` 
do
    awk -F "\t" -v tradeId=${line} -v white_type=${WHITE_TYPE} '{if(tradeId == $2) printf("%s\t%s\n",white_type,$1)}' ${USERTRADE_FILE} >> ${WHITELIST_WITH_TIME}
    alert $? "awk ������${WHITELIST_WITH_TIME} BY ${TMP_TRADE}ʧ��"
done

cp ${WHITELIST_WITH_TIME} ${WHITELIST_WITH_TIME}.tmp
sort -k2n -u ${WHITELIST_WITH_TIME}.tmp > ${WHITELIST_WITH_TIME}

#ǿ�Ʊ�֤������ֻ������
#WHITELIST_YESTODAY=${WHITE_FILE_NEW}.`date -d "1 day ago" +%Y%m%d`
#if [ -f "${WHITELIST_YESTODAY}" ];then
#	diff=`awk 'ARGIND==1{map[$2]}ARGIND==2{if(!($2 in map)){print $0}}' ${WHITELIST_WITH_TIME} ${WHITELIST_YESTODAY} | wc -l`
#    if [ ${diff} -gt 0 ];then
#        echo "whitelist ${WHITELIST_WITH_TIME} is not allowed reduced." >> ${LOG_FILE}
#        alert 1 "�ƶ������������û����٣����������ʧ��"
#    fi
#fi

#runsql_cap "delete from beidoucap.whitelist where type='${WHITE_TYPE}'; load data local infile '${WHITELIST_WITH_TIME}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1

md5sum ${WHITELIST_WITH_TIME} > ${WHITELIST_WITH_TIME}.md5

runsql_cap "load data local infile '${WHITELIST_WITH_TIME}' replace into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
alert $? "�����ƶ�Ӧ������������ʧ��"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}
