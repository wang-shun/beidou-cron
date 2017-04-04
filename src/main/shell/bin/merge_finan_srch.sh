#!/bin/bash
#合并业务数据和展现数据

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/outputcrmstat.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=merge_finan_srch.sh
reader_list=zengyunfeng

cd ${ROOT_PATH}
if [ $? -ne 0 ] ; then
	exit 1
fi
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

STAT_DATE=`date -d 'yesterday' +%Y%m%d`
if [ -n "$1" ] ;then
	STAT_DATE=`date -d"$1" +%Y%m%d`
fi
echo "$0 stat date:$STAT_DATE" >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

#必须对business文件进行按字符进行排序，因为join只处理字符排序的文件
sort -k1,1 ${BUSINESS_FILE} > ${BUSINESS_FILE}.tmp
CRMFILE=${CRMFILE_PATH}/${CRMFILE_NAME}${STAT_DATE}.dat 
msg="合并业务数据和展现数据失败"

#现在business文件已经是32列了，由于有date存在，里面有空格，因此必须用\t进行分隔
join -t'    ' -a 1 ${BUSINESS_FILE}.tmp ${SRCHS_FILE} | awk -F"\t" 'BEGIN{OFS="\t"} {
	if(NF==32) {
		print $1,0,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23,$24,$25,$26,$27,$28,$29,$30,$31,$32} 
	else {
		print $1,$33,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23,$24,$25,$26,$27,$28,$29,$30,$31,$32
	}}'  > ${CRMFILE_NAME}${STAT_DATE}.dat.tmp
alert $? "${msg}"

msg="下载shifen的用户文件失败"
wget -q ftp://${ULEVEL_FILE_FTP_NAME}:${ULEVEL_FILE_FTP_PWD}@${ULEVEL_FILE_SERVER}/${ULEVEL_FILE_PATH}/${ULEVEL_FILE_NAME_PRE}${STAT_DATE}.dat*
alert $? "${msg}"
msg="校验shifen的用户文件MD5失败"
ULEVEL_MD5=`md5sum ${ULEVEL_FILE_NAME_PRE}${STAT_DATE}.dat | cut -d' ' -f1`
alert $? "${msg}"
if [ "${ULEVEL_MD5}" != `cat ${ULEVEL_FILE_NAME_PRE}${STAT_DATE}.dat.md5` ] ;then
	alert 1 "${msg}"
fi

msg="生成ulevel文件失败"
awk -F"\t" 'BEGIN{OFS="\t"} {if($2==10101){print $3,0} else if($2==10104) {print $3,1}}' ${ULEVEL_FILE_NAME_PRE}${STAT_DATE}.dat | sort -k1,1 >${ULEVEL_FILE_NAME_PRE}.tmp
alert $? "${msg}"

#前一个文件已经是33列了
join -t'	' -o'1.1 1.2 1.3 1.4 1.5 1.6 1.7 1.8 1.9 1.10 1.11 1.12 1.13 1.14 1.15 1.16 1.17 1.18 1.19 1.20 1.21 1.22 2.2 1.24 1.25 1.26 1.27 1.28 1.29 1.30 1.31 1.32 1.33' ${CRMFILE_NAME}${STAT_DATE}.dat.tmp  ${ULEVEL_FILE_NAME_PRE}.tmp > ${CRMFILE_NAME}${STAT_DATE}.dat 

msg="生成CRM接口文件MD5发出异常"
md5sum ${CRMFILE_NAME}${STAT_DATE}.dat > ${CRMFILE_NAME}${STAT_DATE}.dat.md5
alert $? "${msg}"

msg="mv结果文件至${CRMFILE_PATH}失败"
mv ${CRMFILE_NAME}${STAT_DATE}.dat.md5 ${CRMFILE_NAME}${STAT_DATE}.dat ${CRMFILE_PATH}
alert $? "${msg}"

#regist file to dts
msg="regist DTS for ${MERGE_FINAN_SRCH_CRM} failed."
md5=`getMd5FileMd5 ${CRMFILE_PATH}${CRMFILE_NAME}${STAT_DATE}.dat.md5`
noahdt add ${MERGE_FINAN_SRCH_CRM} -m md5=${md5} -i date=${STAT_DATE} bscp://${CRMFILE_PATH}${CRMFILE_NAME}${STAT_DATE}.dat
alert $? "${msg}"
