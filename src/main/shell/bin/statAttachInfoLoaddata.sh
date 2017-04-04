#!/bin/sh
#下载log平台的attachInfo数据，以提供附加创意-电话功能日报表
#for beidou at 2014-06-27

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/statAttachInfoLoaddata.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=statAttachInfoLoaddata.sh

mkdir -p ${ATTACHINFO_STAT_DATA_PATH}
alert $? "建立数据存放目录${ATTACHINFO_STAT_DATA_PATH}失败"

mkdir -p ${LOG_PATH}
alert $? "建立日志存储目录${LOG_PATH}失败"
LOG_FILE=${LOG_PATH}/statAttachInfoLoaddata.log

cd ${ATTACHINFO_STAT_DATA_PATH}
alert $? "进入数据存放目录${ATTACHINFO_STAT_DATA_PATH}失败"

function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE
	fi
}

#过滤参数，并初始化变量
function init_conf() {
	if [ $1 ];then
		format=`echo $1|grep "[0-9]\{4\}[0,1][0-9][0-3][0-9]"`
		if [ ${#1} -ne 8 ]||[ "${format}" != "$1" ];then
			log "++++++++++++Param Error:$1 should be format YYYYMMDD++++++++++++"
			#错误时返回
			return 1
		fi
		datatime=${format}
	else
	#要处理的数据的产生日期，这个变量用于产生临时文件夹名以及数据表名
		datatime="`date -d yesterday +%Y%m%d`"
	fi
	#临时文件夹:临时文件存放目录,每次生成一个文件夹
	TMP_FILE_PATH="${ATTACHINFO_STAT_DATA_PATH}/${datatime}/"
	
	#每天要导入stat库的attachInfo数据文件
	attachInfo_data_file="beidou_attach_stat_daily_${datatime}.data"
	#下载的配置
	attachInfo_merged_file="beidou_attach_stat_daily"

	#每次运行生成的数据表名：后缀为产生源数据的日期
	AITable="stat_attachInfo_${datatime}" #AI��ı���
	mkdir -p $TMP_FILE_PATH
}

#下载attachInfo数据
function download(){
	log "下载attachInfo数据开始"
	msg="下载attachInfo数据文件失败"
	wget -q --tries=5 --limit-rate=$LIMIT_RATE -O ${TMP_FILE_PATH}$attachInfo_data_file ${HTTP_PATH}\&date=${datatime}\&item=$attachInfo_merged_file
	alert $? ${msg}
	
}

function loaddata(){
	i=0
	while [ $i -lt $STAT_DB_COUNT ]
	do
		log "load入$i库开始>>"
		msg="loaddata():创建表${AITable}失败"
		runsql_stat "DROP TABLE IF EXISTS ${AITable};
				CREATE TABLE ${AITable} (
				unixTime int(11) DEFAULT NULL,
				userId int(11) DEFAULT NULL,planId int(11) DEFAULT NULL,
				groupId int(11) DEFAULT NULL,
				attachId bigint(20) DEFAULT NULL,attachType int(2) DEFAULT NULL,
				srch int(11) DEFAULT NULL,click int(11) DEFAULT NULL,cost int(11) DEFAULT NULL
			) DEFAULT CHARSET=utf8;" $i >>${LOG_FILE} 2>&1
		alert $? ${msg}
		
		#load data to table stat_attachInfo_YYYYMMDD
		msg="loaddata():load数据入表${AITable}失败"
		runsql_stat "LOAD DATA LOCAL INFILE '${TMP_FILE_PATH}${attachInfo_data_file}' INTO TABLE ${AITable}
			 #CHARACTER SET utf8 FIELDS TERMINATED BY '\t'" $i >>${LOG_FILE} 2>&1
		alert $? ${msg}
		
		log "load入$i库结束<<"
		i=$((i+1))
	done
}

function run() {
	
	msg="参数错误:输入参数格式应该为'YYYYMMDD'"
	init_conf $1
	if [ $? -eq 1 ]
	then
		alert 1 ${msg}
		exit 1
	fi
	
	log "-----------------入库${datatime}数据开始------------------"
	download
	loaddata
	log "-----------------入库${datatime}数据结束------------------"
}

#处理一个时间段的数据进入beidou-stat库
function batchrun() {
	if [ $# -ne 2 ];then
		log "Param Error: count of params of batch() should be 2，like '20140101 20140115'"
		exit 1
	fi

	if [ $1 ];then
		format=`echo $1|grep "[0-9]\{4\}[0,1][0-9][0-3][0-9]"`
		if [ ${#1} -ne 8 ]||[ "${format}" != "$1" ];then
		  log "Param Error:$1 should be format 'YYYYMMDD'"
		  exit 1
		fi
	fi
	
	if [ $2 ];then
		format=`echo $2|grep "[0-9]\{4\}[0,1][0-9][0-3][0-9]"`
		if [ ${#2} -ne 8 ]||[ "${format}" != "$2" ];then
		  log "Param Error:$2 should be format 'YYYYMMDD'"
		  exit 1
		fi
	fi

	if [ $2 -lt $1 ]
	then
		log "Param Error:\$2 must >= \$1"
		exit 1
	fi
	
	log "批量导入历史数据>>开始时间:"$1" 结束时间:"$2
	for((loop=0;;loop++))
	do
		tmp=`date -d "$1 $loop days" "+%Y%m%d"`
		if [ $tmp -le $2 ];then
			run $tmp
		else
			break
		fi
	done 
}

function excute() {

    if [ $# -gt 2 ]; then
		log "there must be 1 or 2 params"
		exit 1
	fi

	if [ $# -eq 0 ]; then
		#有0个参数，即sh stat_attachInfo_loaddata.sh 处理昨天的数据并导入数据库
		run
	elif [ $# -eq 1 ]; then
		#有2个参数，即sh stat_attachInfo_loaddata.sh  20140104 处理20140104的数据并导入数据库
		run $1
	elif [ $# -eq 2 ]; then
		#有3个参数，即sh stat_attachInfo_loaddata.sh  20140101 20120114处理20140101到20140114的数据并导入数据库
		batchrun $1 $2
	fi

}


#$1、$2指要处理的时间
excute $1 $2