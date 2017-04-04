#!/bin/sh

#@file:importTradeWordWhiteList.sh
#@author:wangxiaokun
#@date:2013-09-22
#@brief:将行业词推荐功能的白名单用户导入whitelist表中，并刷新缓存，脚本逻辑会判断文件是否更新，如果没有更新则直接退出。

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importTradeWordWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importTradeWordWhiteList.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/importTradeWordWhiteList.log

#初始化目录
function init(){

	mkdir -p ${ROOT_PATH}
	mkdir -p ${LOG_PATH}
	mkdir -p ${DATA_PATH}
	mkdir -p ${BIN_PATH}
	mkdir -p ${WORK_PATH}
	
	alert $? "建立${WORK_PATH}失败"
	cd ${WORK_PATH}
}


#日志记录方法
function log() {
	if [ "$1" ]
	then
		echo "[`date +%F\ %T`]:$1" >> $LOG_FILE
	fi
}

#下载行业词用户列表并验证是否发生变化
function downloadData()
{
    log "下载行业词用户列表数据开始" 
	#建立临时目录存放新数据
	if [ -d ${TMP_PATH} ];then
		rm -rf ${TMP_PATH}
	fi

	mkdir -p ${TMP_PATH}
	cd ${TMP_PATH}
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${FILE_NAME}
	alert $? "下载行业词用户列表数据失败"
	
	wget -q --tries=3 --limit-rate=${LIMIT_RATE} ${REMOTE_FILE_PATH}${FILE_NAME}.md5
	alert $? "下载行业词用户列表数据md5失败"
	
	md5sum -c ${FILE_NAME}".md5"
	alert $? "验证行业词用户列表数据的MD5失败"
   
	log "下载行业词用户列表数据结束"
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
			log "文件没有变化，直接退出"
			rm -rf ${TMP_PATH}
			exit 0
		fi
		log "文件发生变化，执行入库操作"
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
	log "入库白名单表beidoucap.whiltlist开始"
	#进入工作目录
    cd ${WORK_PATH}
	#删除旧的行业词白名单
	runsql_cap "delete from beidoucap.whitelist where type=${WHITE_TYPE_GAME} or type=${WHITE_TYPE_MEDICAL}" >> ${LOG_FILE} 2>&1
	alert $? "删除数据库行业词用户白名单失败"

	runsql_cap "load data local infile '${FILE_NAME}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
	alert $? "导入行业词用户白名单失败"
	
	log "入库白名单表beidoucap.whiltlist结束"
}
#web远程调用方法
function call() {
    msg="执行远程调用-刷新行业词推荐用户缓存失败("$1")"
    url=http://$1/rpc/loadTradeWordWhiteList
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password    
    alert_return $? "${msg}"
}

#刷新缓存
function refreshCache()
{
	log "刷新用户行业词白名单缓存开始"
	#进入BIN目录
	cd ${BIN_PATH}
	for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; do
    	call $server;
	done
	
	log "刷新用户行业词白名单缓存结束"

}
#main
function main()
{
	init
	downloadData
	diffFile
	importDB
	refreshCache
}

main
