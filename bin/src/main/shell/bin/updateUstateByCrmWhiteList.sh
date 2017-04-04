#!/bin/sh

#@file:updateUstateByCrmWhiteList.sh
#@author:wangxiaokun
#@date:2015-01-20
#@brief:从CRM团队获取生效的优惠框架合同白名单,
#过滤出禁止投放网盟的名单,与前一天的数据对比,更新useraccount表的ustate字段为1(禁止投放)或者0(正常)

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/updateUstateByCrmWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=updateUstateByCrmWhiteList.sh
reader_list=wangxiaokun

current_date=`date +"%Y%m%d"`

LOG_FILE_NAME=${LOG_PATH}/updateUstateByCrmWhiteList/${current_date}.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}/updateUstateByCrmWhiteList
mkdir -p ${DATA_PATH}
mkdir -p ${BIN_PATH}

mkdir -p ${WORK_PATH}
alert $? "建立${WORK_PATH}失败"
cd ${WORK_PATH}

#日志记录方法
function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE_NAME
	fi
}

#下载CRM的提供的数据
function downloadData()
{
    log "下载${FILE_NAME}_${1}数据开始"  
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_SERVER}${REMOTE_FILE_PATH}${FILE_NAME}_${1} -O ${FILE_NAME}_${1}
	
	if [ $? -ne 0 ];then
		log "下载${FILE_NAME}_${1}数据失败"
		return 1
	fi
	
	log "下载${FILE_NAME}_${1}.md5文件开始"
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_SERVER}${REMOTE_FILE_PATH}${FILE_NAME}_${1}.md5 -O ${FILE_NAME}_${1}.md5
	alert $? "下载${FILE_NAME}_${1}.md5文件开始数据失败"
	
	md5sum -c ${FILE_NAME}_${1}.md5
	alert $? "验证文件的MD5失败"
   
	log "下载${FILE_NAME}_${1}数据结束"
}

#CRM提供的文件格式为:userid,product_line_group,begin_date,end_date
#过滤逻辑是wangmeng not in product_line_group && file_date in [begin_date,end_date)
function filterDisableUserIds()
{
	log "过滤CRM提供的文件${FILE_NAME}_${1}开始"
	
	#进入工作目录
    cd ${WORK_PATH}
	next_date=`date -d "+1 day ${1}" "+%Y%m%d"`
	
	awk -F"\t" -v dt="$next_date" '!/wangmeng/{
			if((dt>=$3) && (dt<$4) ) {
				print $1
			}
		}' ${FILE_NAME}_${1} > ${CRM_DISABLE_USERIDS}_${1}
	
	
	log "过滤CRM提供的文件${FILE_NAME}_${1}结束"
	
}


#与前一天的文件对比，获取需要更新的列表，并更新useraccount表
function update()
{
	log "开始更新表useraccount"
	
	#进入工作目录
    cd ${WORK_PATH}
	#判断前一天的文件是否存在,不存在说明第一次运行，则创建一个空文件
	pre_date=`date -d "-1 day ${1}" "+%Y%m%d"`
	if [ ! -e ${CRM_DISABLE_USERIDS}_${pre_date} ]
		then touch ${CRM_DISABLE_USERIDS}_${pre_date}
	fi
	
	update_to_1_str=`awk -F"\t" 'ARGIND==1{preMap[$1]=$1}ARGIND==2{if(!($1 in preMap)){printf(",%s",$1)}}' ${CRM_DISABLE_USERIDS}_${pre_date} ${CRM_DISABLE_USERIDS}_${1}`
	update_to_0_str=`awk -F"\t" 'ARGIND==1{crmDisableMap[$1]=$1}ARGIND==2{if(!($1 in crmDisableMap)){printf(",%s",$1)}}' ${CRM_DISABLE_USERIDS}_${1} ${CRM_DISABLE_USERIDS}_${pre_date}`
	
	up0_sql="update beidoucap.useraccount set ustate=0 where userid in(-1"${update_to_0_str}") and ustate=1";
	up1_sql="update beidoucap.useraccount set ustate=1 where userid in(-1"${update_to_1_str}") and ustate=0";
	
	log "设置ustate=0的SQL：${up0_sql}"
	log "设置ustate=1的SQL：${up1_sql}"
	
	runsql_cap "${up0_sql}"
	alert $? "更新useraccount设置ustate=0失败"

	runsql_cap "${up1_sql}"
	alert $? "更新useraccount设置ustate=1失败"
	
	log "更新表useracount结束"
}

#删除一个月之前的数据
function deleteHistoryData()
{
	log "删除历史数据开始"
	cd ${WORK_PATH}
	timeDel=`date -d "30 day ago" +%Y%m%d%H`
	if [ -e ${FILE_NAME}_${timeDel} ];then
	    rm -f ${FILE_NAME}_${timeDel}
		rm -f ${CRM_DISABLE_USERIDS}_${timeDel}
	fi
	log "删除历史数据结束"
}

#main
function main()
{
	#默认下载当天数据，如果当天没有生成，则下载前一天数据
    file_date=${current_date}
	downloadData ${file_date}
	if [ $? -ne 0 ];then
	    file_date=`date -d "1 day ago" +%Y%m%d`
		downloadData ${file_date}
		alert $? "下载${FILE_NAME}_${file_date}数据失败"
	fi
	#从CRM数据中过滤出不可投放用户列表
	filterDisableUserIds ${file_date}
	#更新数据库
	update ${file_date}
	#删除历史数据
	deleteHistoryData
	#标记处理完成
	touch done.${file_date}
	
}

main
