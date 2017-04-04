#!/bin/sh

#@file: getAotQtkrWord.sh
#@author: hanxu03
#@date: 2011-12-13
#@version: 1.0.0
#@brief: 1.下载QT的推荐词  2.过滤低于阈值的词，过滤黑名单的词，过滤已购词  3.将剩下的推荐词入库
#modified by wangchongjie for cpweb432 at 2012-03-31
#modified by wangxiaokun at 2013-08-21

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/getAotQtkrWord.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/mongodb.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

#params in alert.sh 
type="ERROR"
module=getAotQtkrWord
program=getAotQtkrWord.sh
reader_list=hanxu03

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BAK_PATH}

LOG_FILE=${LOG_PATH}/getAotQtkrWord.log

cd ${DATA_PATH}

date_download=`date +%Y%m%d`

	
########## download qtkr and md5,and check md5
function fetch_aot_qtkrword()
{
	if [ -f "${QTKRWORD_NAME}" ] ; then
	rm -f ${QTKRWORD_NAME}
	fi

	if [ -f "${QTKRWORD_NAME}.md5" ] ; then
		rm -f ${QTKRWORD_NAME}.md5
	fi

	### 抓取文件的md5
	getfile_command="wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ${QTKRWORD_DOWNLOAD_PATH}/${QTKRWORD_NAME}.md5  -O ${QTKRWORD_NAME}.md5 "

	clear_command="cd $DATA_PATH && if [[ -f ${QTKRWORD_NAME}.md5 ]];then rm ${QTKRWORD_NAME}.md5; fi"

	msg="failed to download aotqtkrword.md5"

	getfile "$getfile_command" "$clear_command" 
	if [[ $? -ne 0 ]]; then
		alert 1 "$msg"
	fi

	### 抓取文件
	getfile_command="wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ${QTKRWORD_DOWNLOAD_PATH}/${QTKRWORD_NAME} -O  ${QTKRWORD_NAME} "

	clear_command="cd $DATA_PATH && if [[ -f ${QTKRWORD_NAME} ]];then rm ${QTKRWORD_NAME}; fi"

	msg="failed to download aotqtkrword"

	getfile "$getfile_command" "$clear_command" 
	if [[ $? -ne 0 ]]; then
		alert 1 "$msg"
	fi

	### 校验md5
	msg="failed to check aotqtkrword md5"
	md5sum -c ${QTKRWORD_NAME}.md5 > /dev/null
	alert $? "${msg}"
	
	########## bak qtkrword and delete bak for 3 days ago

	cp ${QTKRWORD_NAME} ${BAK_PATH}${QTKRWORD_NAME}.${date_download}

	date_delete=`date -d"3 days ago" +%Y%m%d`
	rm -f ${QTKRWORD_RESULT_NAME}.${date_delete}
	rm -f ${BAK_PATH}${QTKRWORD_NAME}.${date_delete}

	########## get the result

	java -Xms2048m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.aot.ImportAotQtkrWord ${DATA_PATH}${QTKRWORD_NAME} ${DATA_PATH}${QTKRWORD_RESULT_NAME}.${date_download} 0 30  >> ${LOG_FILE} 2>&1
	alert $? "计算QT主动推荐失败"
}

# import into the db
function loadDataToMongo(){

	#创建MOGODB临时表索引
	echo -e db.${AOTQTKR_MONGO_COLLECTION}_tmp.drop"();\n"db.${AOTQTKR_MONGO_COLLECTION}_tmp.ensureIndex"({\"groupid\" : 1});" > create.js
	echo -e db.${AOTQTKR_MONGO_COLLECTION}.drop"();\n"db.${AOTQTKR_MONGO_COLLECTION}_tmp.renameCollection"('"${AOTQTKR_MONGO_COLLECTION}"');" > rename.js 
	server="$1"
        host=`echo $server | awk -F":" '{print $1}'`
        port=`echo $server | awk -F":" '{print $2}'`

        msg="MONGO建表失败,host=${host} port=${port} db=${AOTQTKR_MONGO_DB} collection=${AOTQTKR_MONGO_COLLECTION} "
        ${MONGO_CLIENT} ${host}:${port}/${AOTQTKR_MONGO_DB} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js >> ${LOG_FILE} 2>&1
	if [ $? -ne 0 ] ; then
		return 1;
	fi

        #将生成文件导入MONGODB临时表
        msg="MONGO批量插入失败,host=${host} port=${port} db=${AOTQTKR_MONGO_DB} collection=${AOTQTKR_MONGO_COLLECTION} "
        ${MONGOIMPORT_CLIENT} -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${AOTQTKR_MONGO_DB} -c${AOTQTKR_MONGO_COLLECTION}_tmp -f groupid,wordid  --type "tsv" --file ${DATA_PATH}/${QTKRWORD_RESULT_NAME}.${date_download} >> ${LOG_FILE} 2>&1 &
	for pid in `jobs -p`;do
		wait $pid
		[ "x$?" != "x0" ] && return 1
        done

        #替换原表
        msg="MONGO替换原表失败,host=${host} port=${port} db=${AOTQTKR_MONGO_DB} collection=${AOTQTKR_MONGO_COLLECTION} "
        ${MONGO_CLIENT} ${host}:${port}/${AOTQTKR_MONGO_DB} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} rename.js >> ${LOG_FILE} 2>&1
	if [ $? -ne 0 ] ; then
		return 1;
	fi

	return 0
}

#import data to mongo 
function import_mongo()
{
	for server in `echo ${MONGO_DB_IP_PORT_WRITE[@]}`;do

		cnt=0
		retry=3

		while [ $cnt -lt $retry ]
		do
			loadDataToMongo $server
			if [ $? -ne 0 ] ; then
				((cnt++))
			else
				break;
			fi
		done

		if [ $cnt -ge $retry ] ; then
				alert 1 "load aotqtkrword into mongo failed "
		fi

	done
}

#import data to bak mongo 
function import_bak_mongo()
{
	for server in `echo ${MONGO_BAK_DB_IP_PORT_WRITE[@]}`;do

		cnt=0
		retry=3

		while [ $cnt -lt $retry ]
		do
			loadDataToMongo $server
			if [ $? -ne 0 ] ; then
				((cnt++))
			else
				break;
			fi
		done

		if [ $cnt -ge $retry ] ; then
				alert 1 "load aotqtkrword into mongo failed "
		fi

	done
}

#main
if [ $# -ne 0 ];then
	if [[ "$1" == "fetch" ]];then
		fetch_aot_qtkrword 
	fi
	if [[ "$1" == "import" ]];then
		import_mongo 
	fi
	if [[ "$1" == "importbak" ]];then
		import_bak_mongo 
	fi
fi	
