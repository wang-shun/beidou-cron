#!/bin/sh

#@file:importApiBlacklist.sh
#@author:zhangxu
#@date:2012-01-29
#@version:1.0.0.0
#@brief:import api v2 user blacklist
#@pre-request: must set api server list in common.conf like:
#				APIV2_SERVER_IP_LIST=($jxapi01 $tcapi01)
#				APIV2_SERVER_IP_PORT_LIST=($jxapi01':8230' $tcapi01':8230')


CONF_SH=/home/work/.bash_profile
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/importApiBlacklist.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importApiBlacklist.sh
reader_list=zhangxu

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${LOG_PATH}/importApiBlacklist
mkdir -p ${API_BLACKLIST_DATA_PATH}

# 导入黑名单
# $1: 黑名单文件
# $2: 数据库表
function import_and_load_blacklist()
{
	BLACKLIST_FILE=$1
	BLACKLIST_TABLE=$2
	ZONGKONG_CENTER_DOWNLOAD_URL=${BLACKLIST_FILE_URL}
	
	#抓取黑名单文件
	[ -f ${BLACKLIST_FILE} ] && mv ${BLACKLIST_FILE} ${BLACKLIST_FILE}.bak
	[ -f ${BLACKLIST_FILE}.md5 ] && mv ${BLACKLIST_FILE}.md5 ${BLACKLIST_FILE}.md5.bak
	
	wget -q  ${BLACKLIST_FILE_URL}/${BLACKLIST_FILE}.md5
	if [ $? -ne 0 ];then
		wget -q  ${BLACKLIST_FILE_URL_BAK}/${BLACKLIST_FILE}.md5
		if [ $? -eq 0 ];then
			echo "总控中心主服务器${BLACKLIST_FILE_URL}不可连通，切换到总控中心备份服务器${BLACKLIST_FILE_URL_BAK}" >> ${LOG_FILE}
			ZONGKONG_CENTER_DOWNLOAD_URL=${BLACKLIST_FILE_URL_BAK}
		fi
	fi
	rm -f ${BLACKLIST_FILE}.md5
	
	msg="wget黑名单文件${ZONGKONG_CENTER_DOWNLOAD_URL}${BLACKLIST_FILE}.md5失败"
	wget -q  ${ZONGKONG_CENTER_DOWNLOAD_URL}/${BLACKLIST_FILE}.md5
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi

	msg="wget黑名单文件${ZONGKONG_CENTER_DOWNLOAD_URL}${BLACKLIST_FILE}失败"
	wget -q  ${ZONGKONG_CENTER_DOWNLOAD_URL}/${BLACKLIST_FILE}
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi

	msg="${ZONGKONG_CENTER_DOWNLOAD_URL}${BLACKLIST_FILE}文件的md5校验失败"
	md5sum -c ${BLACKLIST_FILE}.md5
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi
	
	msg="${BLACKLIST_FILE}文件为空"
	if [ ! -s ${BLACKLIST_FILE} ];then
		alert 1 "${msg}"
		return 1
	fi
	
	# 去除空行
	sed '/^$/d' ${BLACKLIST_FILE} > ${BLACKLIST_FILE}.tmp
	line_num=`cat ${BLACKLIST_FILE} | wc -l`
	line_num_rm_blank=`cat ${BLACKLIST_FILE}.tmp | wc -l`
	blank_line_num=`expr $line_num - $line_num_rm_blank`
	if [ $blank_line_num -ne 0 ];then
		echo "去除${blank_line_num}个空行" >> ${LOG_FILE}
	fi
	[ -f ${BLACKLIST_FILE}.tmp ] && cp ${BLACKLIST_FILE}.tmp ${BLACKLIST_FILE}
	
	# 比较文件，如果相同则没有必要再次入库，返回码2
	if [ ! -e ${BLACKLIST_FILE}.bak ];then
		touch ${BLACKLIST_FILE}.bak
	fi
	if [ ! -e ${BLACKLIST_FILE}.md5.bak ];then
		touch ${BLACKLIST_FILE}.md5.bak
	fi
	diff ${BLACKLIST_FILE} ${BLACKLIST_FILE}.bak >> /dev/null
	if [ $? -eq 0 ];then
		echo "${BLACKLIST_FILE} do not change, pass updating database." >> ${LOG_FILE}
		return 2;
	fi
	
	echo "${BLACKLIST_FILE}文件(共${line_num}行)已更新，导入文件到${BLACKLIST_TABLE}_bak表" >> ${LOG_FILE}
	START_TIME=`date +%s`
	msg="载入文件${BLACKLIST_FILE}失败"
	
	runsql_cap "use beidoucap;drop table if exists ${BLACKLIST_TABLE}_bak;
		create table ${BLACKLIST_TABLE}_bak like ${BLACKLIST_TABLE};
		load data local infile '${API_BLACKLIST_DATA_PATH}/${BLACKLIST_FILE}' into table ${BLACKLIST_TABLE}_bak;"
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi
	END_TIME=`date +%s`
	echo "load数据库文件${BLACKLIST_FILE}时间(单位为秒)为：`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
	
	START_TIME=`date +%s`
	msg="重命名表${BLACKLIST_TABLE}失败"
	
	runsql_cap "use beidoucap; rename table ${BLACKLIST_TABLE} to ${BLACKLIST_TABLE}_tmp,
	${BLACKLIST_TABLE}_bak to ${BLACKLIST_TABLE},
	${BLACKLIST_TABLE}_tmp to ${BLACKLIST_TABLE}_bak;"
	  
	ret_code=$?
	alert ${ret_code} "${msg}"
	if [ $ret_code -ne 0 ];then
		return 1
	fi
	END_TIME=`date +%s`
	echo "rename数据库表${BLACKLIST_TABLE}时间(单位为秒)为：`expr ${END_TIME} - ${START_TIME}`" >>${LOG_FILE}
	
	# call rpc 
	sleep 5
	msg="进入数据目录${BIN_PATH}失败"
	cd ${BIN_PATH}
	alert $? "${msg}"
	
	msg="刷新api黑名单用户缓存失败"
	sh loadApiBlacklistCache.sh
	alert $? "${msg}"
	
	msg="进入数据目录${API_BLACKLIST_DATA_PATH}失败"
	cd ${API_BLACKLIST_DATA_PATH}
	alert $? "${msg}"
	
	return 0
}

# 导入失败时恢复文件
function backup()
{
	last_return=$1
	BLACKLIST_FILE=$2
	if [ $last_return -eq 1 ];then
		[ -f ${BLACKLIST_FILE}.bak ] && mv ${BLACKLIST_FILE}.bak ${BLACKLIST_FILE}
		[ -f ${BLACKLIST_FILE}.md5.bak ] && mv ${BLACKLIST_FILE}.md5.bak ${BLACKLIST_FILE}.md5
	elif [ $last_return -eq 0 ];then
		cp ${BLACKLIST_FILE} ${BLACKLIST_FILE}.${TIMESTAMP} && rm ${BLACKLIST_FILE}.bak && rm ${BLACKLIST_FILE}.md5.bak
	elif [ $last_return -eq 2 ];then
		rm ${BLACKLIST_FILE}.bak && rm ${BLACKLIST_FILE}.md5.bak
	fi
}

# main
msg="进入数据目录${API_BLACKLIST_DATA_PATH}失败"
cd ${API_BLACKLIST_DATA_PATH}
alert $? "${msg}"
echo "`date +"%Y-%m-%d %H:%M:%S"`,开始任务">>${LOG_FILE}

export BLACKLIST_FILE_URL=${API_BLACKLIST_FILE_URL}
export BLACKLIST_FILE_URL_BAK=${API_BLACKLIST_FILE_URL_BAK}
import_and_load_blacklist ${API_BLACKLIST_FILE} ${API_BLACKLIST_TABLE}
backup $? ${API_BLACKLIST_FILE}

