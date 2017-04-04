#!/bin/sh

#@file:importItStat.sh
#@author:wangxiaokun
#@date:2013-01-28
#@brief:将兴趣推荐的pv、uv数据入beidouext.interest_stat，如果上游数据有变化则刷新缓存。

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importItStat.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importItStat.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/importItStat.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BIN_PATH}

mkdir -p ${WORK_PATH}
alert $? "建立${WORK_PATH}失败"
cd ${WORK_PATH}

#日志记录方法
function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE
	fi
}

#下载兴趣的pv、uv数据并验证是否发生变化
function downloadData()
{
    log "下载兴趣的统计数据开始"  
	
	mkdir -p ${TMP_PATH}
    cd ${TMP_PATH}
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${TAR_FILE}
	alert $? "下载兴趣的统计数据失败"
	
	tar -xzvf ${TMP_PATH}${TAR_FILE} 
	alert $? "解压文件失败"
   
	md5sum -c ${FILE_NAME}".md5"
	alert $? "验证文件的MD5失败"
   
	log "下载兴趣的统计数据结束"
}

#对比新旧文件，判断是否变化
#如果没有变化，则直接退出
#如果有变化执行后面的入库刷缓存操作
function diffFile()
{
	log "对比新旧文件判断是否有变化"
	#进入工作目录
    cd ${WORK_PATH}
	log "对比新旧文件判断是否有变化"
	if [ -f ${WORK_PATH}${FILE_NAME} ];then
	    
		diff ${WORK_PATH}${FILE_NAME} ${TMP_PATH}${FILE_NAME}
		if [ $? -eq 0 ];then
			log "文件没有变化，直接退出"
			rm -rf ${TMP_PATH}
			exit 0
		fi
		log "文件发生变化，执行入库操作"
		#删除老文件
		rm -f ${WORK_PATH}${FILE_NAME}
		
	fi
	
	cp ${TMP_PATH}${FILE_NAME} ${WORK_PATH}${FILE_NAME}
	rm -rf ${TMP_PATH}	

}

#数据文件导入DB
function importDB()
{
	log "入库兴趣统计表beidouext.interest_stat开始"
	
	#进入工作目录
    cd ${WORK_PATH}
	
	runsql_xdb "drop table if exists beidouext.interest_stat_tmp" 
	alert $? "drop临时表interest_stat_tmp失败"
	
	runsql_xdb "create table beidouext.interest_stat_tmp like beidouext.interest_stat"
	alert $? "建立临时表interest_stat_tmp失败"
	
	runsql_xdb "load data local infile '${WORK_PATH}${FILE_NAME}' into table beidouext.interest_stat_tmp(iid,regid,uv,pv)"
	alert $? "数据导入临时表失败"
	
	runsql_xdb "drop table if exists beidouext.interest_stat; rename table beidouext.interest_stat_tmp to beidouext.interest_stat"
	alert $? "重命名临时表失败"
	
	log "入库兴趣统计表beidouext.interest_stat结束"
}

#刷新缓存
function refreshCache()
{
	log "刷新兴趣统计缓存开始"
	
	#进入shell程序目录
	cd ${BIN_PATH}
	sh loadInterestStat.sh
	
	log "刷新兴趣统计缓存结束"

}
#main
function main()
{
	downloadData
	diffFile
	importDB
	refreshCache
}

main
