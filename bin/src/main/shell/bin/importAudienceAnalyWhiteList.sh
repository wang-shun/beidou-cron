#!/bin/sh

#@file:importAudienceAnalyWhiteList.sh
#@author:liuhao05
#@version:1.0.0.0
#@brief:import AudienceAnaly whitelist to database

#modify by wangchongjie since 2012/1/29

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importAudienceAnalyWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAudienceAnalyWhiteList.sh


LOG_FILE=${LOG_PATH}/importAudienceAnalyWhiteList.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录{DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

OUT_FILE=${WHITE_FILE}
[ -f "${OUT_FILE}" ] && rm ${OUT_FILE}
OUT_FILE=${WHITE_FILE_NEW}
[ -f "${OUT_FILE}" ] && rm ${OUT_FILE}

function download_user_type_file()
{
	time=`date -d "1 day ago" +%Y%m%d`
	#抓取文件并验证MD5
	msg="wget文件${USER_TYPE_FILE}失败"
	wget  ${USER_TYPE_URL}/${REMOTE_USER_TYPE_FILE}$time.dat -O ${USER_TYPE_FILE}
	alert $? "${msg}"

	msg="wget文件${USER_TYPE_FILE}.md5失败"
	wget ${USER_TYPE_URL}/${REMOTE_USER_TYPE_FILE}$time.dat.md5 -O ${USER_TYPE_FILE}.md5
	alert $? "${msg}"

	msg="${USER_TYPE_FILE}文件的md5校验失败"
	if [[ `md5sum ${USER_TYPE_FILE}|awk '{print $1}'` != `awk '{print $1}' ${USER_TYPE_FILE}.md5` ]]
	then
		alert 1 "${msg}"
	fi
	
	#username，ulevelid，userid
	awk -F "\t" '{if($2==10104) print $3}' ${USER_TYPE_FILE} | sort -u > ${KA_USER_FILE}
}

function download_user_cost_file()
{
	curFullTime=$1
	
	local fileType=${FILE_TYPE}
	local localStatTempFile=${USER_COST_FILE}
	
	# step0: set data prefix and md5 prefix for different hadoop cluster
	dataPrefix=${DATA_PREFIX_KUN}
	md5Prefix=${MD5_PREFIX_KUN}
	maniftPrefix=${MANIFEST_PREFIX_KUN}
	maniftMd5Prefix=${MANIFEST_MD5_PREFIX_KUN}
	
	
	# step1: download data file from log platform
	wget -t $MAX_RETRY -q "${dataPrefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile ]
	then
		alert 1 "下载用户消费数据文件失败"
	fi
	
	# step2: download manifest file from log platform
	wget -t $MAX_RETRY -T 1800 -q "${maniftPrefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".manifest"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".manifest" ]
	then
		alert 1 "下载用户消费manifest文件失败"
	fi
	
	# step3: download manifest.md5 file from log platform
	wget -t $MAX_RETRY -T 1800 -q "${maniftMd5Prefix}&date=${curFullTime}&item=${fileType}" -O $localStatTempFile".manifest.md5"
	if [ $? -ne 0 ] || ! [ -f $localStatTempFile".manifest.md5" ]
	then
		alert 1 "下载用户消费manifest.md5文件失败"
	fi
	
	# step4: check manifest.md5 file
	offline_manifest_md5=`md5sum $localStatTempFile".manifest" | awk '{print $1}'` #manifest md5 offline
	online_manifest_md5=`awk '{print $1}' $localStatTempFile".manifest.md5"`   #manifest md5 online
	if [ "${offline_manifest_md5}" != "${online_manifest_md5}" ]
	then
		alert 1 "校验用户消费manifest文件失败"
	fi
	
	# step5: user manifest to check date file
	offline_file_line=`ls -al $localStatTempFile | awk '{print $5}'` #line count offline
	online_file_line=`awk '{print $3}' $localStatTempFile".manifest"`   #line count online
	if [ "${offline_file_line}" != "${online_file_line}" ]
	then
		alert 1 "校验用户消费文件失败"
	fi
	
	rm $localStatTempFile".manifest"
	rm $localStatTempFile".manifest.md5"
}


function generate_user_white_list()
{
	runsql_cap_read "select id from beidoucap.whitelist where type=${WHITE_TYPE}" ${WHITE_FILE_OLD} >> ${LOG_FILE} 2>&1
	alert $? "查询受众分析白名单失败"
	
	#KA客户近30天有消费 ，SME客户近一个月日均消费大于等于300	 
	awk -F "\t" -v f1="${WHITE_FILE_OLD}" -v f2="${KA_USER_FILE}" -v f3="${USER_COST_FILE}" '
	FILENAME==f1 {OLDUSER[$1]=$1} 
	FILENAME==f2 {KAUSER[$1]=$1} 
	FILENAME==f3 { if($1 in OLDUSER){} else if($1 in KAUSER){print $1} else if($2 > 900000 ){print $1}}' \
    ${WHITE_FILE_OLD}	\
    ${KA_USER_FILE}	\
	${USER_COST_FILE} > ${WHITE_FILE_TMP}
	 
	awk -F'	' -v white_type=${WHITE_TYPE} 'BEGIN{OFS="	"} $0 ~ /^[0-9]+$/ {print white_type,$1}' ${WHITE_FILE_TMP} | sort -u > ${WHITE_FILE_NEW}
	alert $? "awk 白名单失败"
}

function load_data_into_db()
{

	runsql_cap "load data local infile '${WHITE_FILE_NEW}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
	alert $? "导入受众分析白名单失败"

	CURR_DATETIME=`date +%F\ %T`
	echo "end at "$CURR_DATETIME >> ${LOG_FILE}
}

function main()
{
	run_date=`date -d "$DELAY_DATE day ago" +%Y%m%d`
	
	download_user_type_file
	download_user_cost_file $run_date
	generate_user_white_list
	load_data_into_db
}

#执行main函数
main

