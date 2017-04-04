#!/bin/sh
#@file:importTradeWords.sh
#@author:zhangzhenhua02
#@date:2014-05-22
#@version:1.0.0.0
#@brief:计算30天内推广组点击、展现top20的关键词，并存入mongodb

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/mongodbs.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/importTradeWords.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=importTradeWords.sh

mkdir -p ${WORD_DATA_PATH}
alert $? "建立数据存放目录${WORD_DATA_PATH}失败"

mkdir -p ${LOG_PATH}
alert $? "建立日志存储目录${LOG_PATH}失败"
LOG_FILE=${LOG_PATH}/importTradeWords.log

cd ${WORD_DATA_PATH}
alert $? "进入工作目录${WORD_DATA_PATH}失败"

function fetch_data_from_db()
{
	rm -f ${WORD_FILE_PATH}.*
	#获取数据天数
	days=31
	#数据分片数
	num=30
	#1、从beidoustat.stat_keyword_yyyymmdd中导出前30天的数据
	i=1;
	while [ $i -lt $days ]
	do
		datestr=`date -d "-$i day $TODAY" +%Y%m%d`
		 
		#根据groupid将数据文件分片
		index=0
		while [ $index -lt $num ] 
		do
			msg="从beidoustat.stat_keyword_${datestr}中获取数据失败,today=${TODAY}"
			runsql_stat_read "select userid,groupid,wordid,click,srch from stat_keyword_${datestr} where groupid%$num=$index;" ${WORD_FILE_PATH}.tmp.$i.$index
			cat ${WORD_FILE_PATH}.tmp.$i.$index >> ${WORD_FILE_PATH}.$index
			rm -rf ${WORD_FILE_PATH}.tmp.$i.$index
			index=$((index+1))
		done
		alert $? "${msg}"
		i=$((i+1))
	done
	#2、生成mongodb的数据源文件
	msg="生成${TODAY}的mongodb数据源文件失败"
	index=0
	while [ $index -lt $num ] 
	do
		#计算总的点击、展现数
		awk -F "\t" '
		{
			mapclk[$1","$2","$3]=mapclk[$1","$2","$3]+$4;
			mapsrch[$1","$2","$3]=mapsrch[$1","$2","$3]+$5;
		}END{
			for(key in mapsrch){
				print key","mapclk[key]","mapsrch[key];
			}
		}' ${WORD_FILE_PATH}.$index> ${WORD_FILE_PATH}.$index.total
		rm -rf ${WORD_FILE_PATH}.$index
		#按照点击、展现排序
		sort -k4nr,4 -k5nr,5 -t, ${WORD_FILE_PATH}.$index.total > ${WORD_FILE_PATH}.$index.sort #以逗号分隔，只对第四个域和第五个域进行降序排序
		rm -rf ${WORD_FILE_PATH}.$index.total
		#取top20
		awk -F "," '
		{
			map[$1","$2]=map[$1","$2]+1;
			if(map[$1","$2]<21){
				print $1"\t"$2"\t"$3"\t"$4"\t"$5;
			}
		}' ${WORD_FILE_PATH}.$index.sort >> ${WORD_FILE_PATH}.$TODAY.final
		rm -rf ${WORD_FILE_PATH}.$index.sort
		index=$((index+1))
	done
	alert $? "${msg}"
}

function fetch_data_from_hive()
{
	rm -f ${WORD_FILE_PATH}.*
	#下载hive运行结果数据
	wget -t 3 -q $REMOTE_FILE_PATH/tradeword.$TODAY.sort
	wget -t 3 -q $REMOTE_FILE_PATH/tradeword.$TODAY.sort.md5
	msg="${TODAY}下载hive计算结果数据失败"
	md5sum -c tradeword.$TODAY.sort.md5
	if [ $? -ne 0 ];
	then
		echo $msg >>$LOG_FILE
		return 1
	fi

	#校验hive数据结果为空
	msg="${TODAY}下载hive计算结果数据为空"
	[ -f "${WORD_FILE_PATH}.$TODAY.sort" ] && num=`wc -l "${WORD_FILE_PATH}.$TODAY.sort"|awk '{print $1}'` || return 1
	if [ $num -eq 0 ];
	then
		echo $msg >>$LOG_FILE
		return 1
	fi
	#取top20
	msg="${TODAY}计算最终结果失败"
	awk -F "\t" '
	{
		map[$1","$2]=map[$1","$2]+1;
		if(map[$1","$2]<21){
			print $1"\t"$2"\t"$3"\t"$4"\t"$5;
		}
	}' ${WORD_FILE_PATH}.$TODAY.sort > ${WORD_FILE_PATH}.$TODAY.final
	if [ $? -ne 0 ];
	then
		echo $msg >>$LOG_FILE
		return 1
	fi
	rm -rf ${WORD_FILE_PATH}.$TODAY.sort
}

function import_mongo()
{
	#校验mongodb数据源文件是否为空
	[ -f "${WORD_FILE_PATH}.$TODAY.final" ] && num=`wc -l "${WORD_FILE_PATH}.$TODAY.final"|awk '{print $1}'` || exit 1
	if [ $num -eq 0 ];
	then
		exit 1
	fi
	#创建MOGODB临时表索引
	echo -e db.${MONGO_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.ensureIndex"({\"groupid\":1,\"clks\":-1,\"srchs\":-1});" > create.js
	echo -e db.${MONGO_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_COLLECTION_NAME}"');" > rename.js 
	
	server=${MONGO_sHARDING[${MONGO_SHARDINGS}]}
	msg="${server}建立临时表失败"
	${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js --quiet 
	alert $? ${msg}
		
	host=`echo $server | awk -F":" '{print $1}'`
	port=`echo $server | awk -F":" '{print $2}'`
	${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${MONGO_DB_NAME} -c${MONGO_TMP_COLLECTION_NAME} -f"${MONGO_COLUMN_NAME}" --type "tsv" --file ${WORD_FILE_PATH}.$TODAY.final & > /dev/null &
	for pid in `jobs -p`;do
        wait $pid
        [ "x$?" != "x0" ] && alert 1 "导入失败"
	done
    msg="${server}重命名临时表失败"
    ${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} rename.js  --quiet
    alert $? ${msg}
}

function import_bak_mongo()
{
	#校验mongodb数据源文件是否为空
	[ -f "${WORD_FILE_PATH}.$TODAY.final" ] && num=`wc -l "${WORD_FILE_PATH}.$TODAY.final"|awk '{print $1}'` || exit 1
	if [ $num -eq 0 ];
	then
		exit 1
	fi
	#创建MOGODB临时表索引
	echo -e db.${MONGO_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.ensureIndex"({\"groupid\":1,\"clks\":-1,\"srchs\":-1});" > create.js
	echo -e db.${MONGO_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_COLLECTION_NAME}"');" > rename.js 
	
	server=${MONGO_BAK_sHARDING[${MONGO_SHARDINGS}]}
	msg="${server}建立临时表失败"
	${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js --quiet 
	alert $? ${msg}
		
	host=`echo $server | awk -F":" '{print $1}'`
	port=`echo $server | awk -F":" '{print $2}'`
	${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${MONGO_DB_NAME} -c${MONGO_TMP_COLLECTION_NAME} -f"${MONGO_COLUMN_NAME}" --type "tsv" --file ${WORD_FILE_PATH}.$TODAY.final & > /dev/null &
	for pid in `jobs -p`;do
        wait $pid
        [ "x$?" != "x0" ] && alert 1 "导入失败"
	done
    msg="${server}重命名临时表失败"
    ${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} rename.js  --quiet
    alert $? ${msg}
    rm -rf ${WORD_FILE_PATH}.$TODAY.final
    
}
if [ $# -ne 0 ];then
	if [[ "$1" == "db" ]];then
		fetch_data_from_db
		import_mongo
		import_bak_mongo
	else
		fetch_data_from_hive
		if [ $? -ne 0 ];then
			fetch_data_from_db
		fi
		import_mongo
		import_bak_mongo
	fi
else
	fetch_data_from_hive
	if [ $? -ne 0 ];then
		fetch_data_from_db
	fi
	import_mongo
	import_bak_mongo
fi