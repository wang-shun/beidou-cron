#!/bin/sh
#1、 网盟提供给风控文本创意的信息库，只提供普通文本创意，智能创意的不需要提供；
#2、 创意库需要的字段：unitid，创意的标题，创意的描述1，创意的描述2；
#for beidou at 2014-10-13

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/fengkongWangmengUnit.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=fengkongWangmengUnit.sh

mkdir -p ${FENGKONG_STAT_DATA_PATH}
alert $? "建立数据存放目录${FENGKONG_STAT_DATA_PATH}失败"

mkdir -p ${LOG_PATH}
alert $? "建立日志存储目录${LOG_PATH}失败"
LOG_FILE=${LOG_PATH}/fengkongWangmengUnit.log

cd ${FENGKONG_STAT_DATA_PATH}
alert $? "进入数据存放目录${FENGKONG_STAT_DATA_PATH}失败"

#过滤参数，并初始化变量
function init_conf() {
	if [ $1 ];then
		format=`echo $1|grep "[0-9]\{4\}[0,1][0-9][0-3][0-9]"`
		if [ ${#1} -ne 8 ]||[ "${format}" != "$1" ];then
			log "++++++++++++Param Error:$1 should be format YYYYMMDD++++++++++++"
			return 1 #错误时返回
		fi
		datatime=${format}
	else
		datatime="`date +%Y%m%d`"
	fi
}

function statistics_unit_data()
{
	msg="参数错误:输入参数格式应该为'YYYYMMDD'"
	init_conf $1
	if [ $? -eq 1 ]
	then
		alert 1 ${msg}
		exit 1
	fi
	
	#从数据库获取在投放的创意信息
	msg="从数据库中获取创意数据失败"
	runsql_sharding_read "select id, title, description1, description2 from beidou.online_unit where wuliao_type = 1 and is_smart = 0;" ${UNIT_DATA}.$datatime
	alert $? "${msg}"
	
	msg="MD5校验失败"
	md5sum ${UNIT_DATA_NAME}.$datatime > ${UNIT_DATA_NAME}.$datatime.md5
	alert $? "${msg}"
}

#main
if [ $# -eq 0 ];then
	statistics_unit_data
fi

if [ $# -ne 0 ];then
	statistics_unit_data $1
fi
