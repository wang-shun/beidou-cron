#!/bin/bash

#@file: get_day_srch.sh
#@author: yang_yun
#@date: 2010-06-09
#@version: 1.0.0.1
#@brief: download user_day_srchs data from cprostat server and sort
#统计用户的日展现数据，如果没有输入参数，则统计昨日的消费数据.
#输入参数的格式为:yyyyMMdd

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/get_day_srch.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=get_day_srch.sh
reader_list=yang_yun


cd ${ROOT_PATH}
if [ $? -ne 0 ] ; then
	exit 1
fi
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

start=`date +"%s"`
#STAT_DATE=`date -d 'yesterday' +%Y%m%d`
if [ -n "$1" ] ;then
	STAT_DATE=`date -d"$1" +%Y%m%d`
fi
echo "stat begin at `date +"%Y-%m-%d %H:%M:%S"`" >> ${LOG_FILE}
cd ${DATA_PATH}&&rm -f ${FILE_PRE}.${STAT_DATE}*

#download user_day_srchs.yyyyMMdd
#wget -q -t $MAX_RETRY ftp://${USERNAME}:${PASSWORD}@${SOURCE_SERVER}/${SOURCE_PATH}/${FILE_PRE}.${STAT_DATE} -P ${DATA_PATH}

function get_day_srch()
{
################ add by liangshimu,20110324:migration to log platform
wget -q -t $MAX_RETRY  ${DATA_PREFIX}${FILETYPE} -O ${DATA_PATH}${FILE_PRE}.${STAT_DATE}
alert "$?" "Error:${SOURCE_SERVER} download ${FILE_PRE}.${STAT_DATE},文件不存在"

#download user_day_srchs.yyyyMMdd.md5
#wget -q -t $MAX_RETRY ftp://${USERNAME}:${PASSWORD}@${SOURCE_SERVER}/${SOURCE_PATH}/${FILE_PRE}.${STAT_DATE}.md5 -P ${DATA_PATH}
wget -q -t $MAX_RETRY  ${MD5_PREFIX}${FILETYPE} -O ${DATA_PATH}${FILE_PRE}.${STAT_DATE}.md5.tmp
alert "$?" "Error:${SOURCE_SERVER} download ${FILE_PRE}.${STAT_DATE}.md5,文件不存在"

awk -vfname="${FILE_PRE}.${STAT_DATE}" '{print $2 "  " fname}' ${FILE_PRE}.${STAT_DATE}.md5.tmp > ${FILE_PRE}.${STAT_DATE}.md5

#md5 check
md5sum -c ${FILE_PRE}.${STAT_DATE}".md5" > /dev/null
if [ $? -ne 0 ];then
alert "$?" "Error:${FILE_PRE}.${STAT_DATE} md5验证失败"
return 1
fi

#sort
msg="failed to sort file ${FILE_PRE}.${STAT_DATE}"
sort -u -k1,1 ${FILE_PRE}.${STAT_DATE}|awk 'BEGIN{OFS=" "} {print $1,$2}' > ${SRCHS_FILE}
alert $? "${msg}"
rm -f ${FILE_PRE}.${STAT_DATE}&&rm -f ${FILE_PRE}.${STAT_DATE}.md5

#create md5
msg="failed to create day_srchs md5 file"
cd ${DATA_PATH}
md5sum ${SRCHS_FILE_NAME}>${SRCHS_FILE_NAME}.md5
alert $? "${msg}"

# the module below is added by xiehao on 20110629
YESTERDAY=`date -d'yesterday' +%Y%m%d`
msg="failed to create day_srchs.${YESTERDAY} file"
cp ${SRCHS_FILE_NAME} ${SRCHS_FILE_NAME}.${YESTERDAY}
md5sum ${SRCHS_FILE_NAME}.${YESTERDAY} > ${SRCHS_FILE_NAME}.${YESTERDAY}.md5
alert $? "${msg}"

#regist file to dts
msg="regist DTS for ${GET_DAY_SRCH_DAY_SRCHS} failed."
md5=`getMd5FileMd5 ${DATA_PATH}/${SRCHS_FILE_NAME}.${YESTERDAY}.md5`
noahdt add ${GET_DAY_SRCH_DAY_SRCHS} -m md5=${md5} -i date=${YESTERDAY} bscp://${DATA_PATH}/${SRCHS_FILE_NAME}.${YESTERDAY}
alert $? "${msg}"

end=`date +"%s"`
spendtime=$(($end-$start))
echo "stat end at `date +"%Y-%m-%d %H:%M:%S"`, spend time:${spendtime}s" >> ${LOG_FILE}
}

success_flag=1
fail_count=-1
while [ $success_flag -eq 1 ]
do
fail_count=$((fail_count+1))
if [ $fail_count -gt 180 ];then
  exit 1
fi
get_day_srch
success_flag=$?
sleep 300
done
