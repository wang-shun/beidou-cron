#!/bin/sh

#@file:importItRecomment.sh
#@author:wangxiaokun
#@date:2013-01-28
#@brief:将兴趣推荐的相关数据入库，如果上游数据有变化则刷新缓存。
#入库包括推荐的金牛兴趣和黑马兴趣入beidouext.interest_recommend，兴趣的pv、uv数据入beidouext.interest_stat

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importItRecomment.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importItRecomment.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/importItRecomment.log

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

#下载推荐兴趣并并验证是否发生变化
function downloadData()
{
    log "下载金牛和黑马推荐兴趣数据开始" 
	#建立临时目录存放解压文件
	mkdir -p ${TMP_PATH}
	cd ${TMP_PATH}
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${TAR_FILE}
	alert $? "下载金牛和黑马推荐兴趣数据失败"
	
	tar -xvf ${TMP_PATH}${TAR_FILE}
	alert $? "解压推荐兴趣文件失败"
    
	md5sum -c ${FILE_NAME}".md5"
	alert $? "验证推荐兴趣文件的MD5失败"
   
	log "下载金牛和黑马推荐兴趣数据结束"
}

#对比新旧文件，判断是否变化
#如果没有变化，则直接退出
#如果有变化执行后面的入库刷缓存操作
function diffFile()
{
	log "对比新旧文件判断是否有变化"
	#进入工作目录
    cd ${WORK_PATH}
	if [ -f ${WORK_PATH}${FILE_NAME} ];then
	    
		diff ${WORK_PATH}${FILE_NAME} ${TMP_PATH}${FILE_NAME}
		if [ $? -eq 0 ];then
			log "推荐兴趣文件没有变化，直接退出"
			rm -rf ${TMP_PATH}
			exit 0
		fi
		log "推荐兴趣推荐发生变化，执行入库操作"
		#删除老文件
		rm -f ${WORK_PATH}${FILE_NAME}
		
	fi
	
	#将新文件拷贝到工作目录
	cp ${TMP_PATH}${FILE_NAME} ${WORK_PATH}${FILE_NAME}
	#删除临时目录
	rm -rf ${TMP_PATH}	

}


#数据文件导入DB
function importDB()
{
	log "入库兴趣推荐表beidouext.interest_recommend开始"
	#进入工作目录
    cd ${WORK_PATH}
	
	runsql_xdb "drop table if exists beidouext.interest_recommend_tmp" 
	alert $? "drop临时表interest_recommend_tmp失败"
	
	runsql_xdb "create table beidouext.interest_recommend_tmp like beidouext.interest_recommend"
	alert $? "建立临时表interest_recommend_tmp失败"
	
	runsql_xdb "load data local infile '${WORK_PATH}${FILE_NAME}' into table beidouext.interest_recommend_tmp(tradename,tradeid,itname,iid,level)"
	alert $? "数据导入临时表失败"
	
	runsql_xdb "drop table if exists beidouext.interest_recommend; rename table beidouext.interest_recommend_tmp to beidouext.interest_recommend"
	alert $? "重命名临时表失败"
	log "入库兴趣推荐表beidouext.interest_recommend结束"
}

#刷新缓存
function refreshCache()
{
	log "刷新兴趣推荐缓存开始"
	#进入shell程序目录
	cd ${BIN_PATH}
	sh loadInterestRecommend.sh
	
	log "刷新兴趣推荐缓存结束"

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
