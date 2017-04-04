#!/bin/sh
#@file:importTradeWords.sh
#@author:zhangzhenhua02
#@date:2014-05-22
#@version:1.0.0.0
#@brief:����30�����ƹ�������չ��top20�Ĺؼ��ʣ�������mongodb

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
alert $? "�������ݴ��Ŀ¼${WORD_DATA_PATH}ʧ��"

mkdir -p ${LOG_PATH}
alert $? "������־�洢Ŀ¼${LOG_PATH}ʧ��"
LOG_FILE=${LOG_PATH}/importTradeWords.log

cd ${WORD_DATA_PATH}
alert $? "���빤��Ŀ¼${WORD_DATA_PATH}ʧ��"

function fetch_data_from_db()
{
	rm -f ${WORD_FILE_PATH}.*
	#��ȡ��������
	days=31
	#���ݷ�Ƭ��
	num=30
	#1����beidoustat.stat_keyword_yyyymmdd�е���ǰ30�������
	i=1;
	while [ $i -lt $days ]
	do
		datestr=`date -d "-$i day $TODAY" +%Y%m%d`
		 
		#����groupid�������ļ���Ƭ
		index=0
		while [ $index -lt $num ] 
		do
			msg="��beidoustat.stat_keyword_${datestr}�л�ȡ����ʧ��,today=${TODAY}"
			runsql_stat_read "select userid,groupid,wordid,click,srch from stat_keyword_${datestr} where groupid%$num=$index;" ${WORD_FILE_PATH}.tmp.$i.$index
			cat ${WORD_FILE_PATH}.tmp.$i.$index >> ${WORD_FILE_PATH}.$index
			rm -rf ${WORD_FILE_PATH}.tmp.$i.$index
			index=$((index+1))
		done
		alert $? "${msg}"
		i=$((i+1))
	done
	#2������mongodb������Դ�ļ�
	msg="����${TODAY}��mongodb����Դ�ļ�ʧ��"
	index=0
	while [ $index -lt $num ] 
	do
		#�����ܵĵ����չ����
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
		#���յ����չ������
		sort -k4nr,4 -k5nr,5 -t, ${WORD_FILE_PATH}.$index.total > ${WORD_FILE_PATH}.$index.sort #�Զ��ŷָ���ֻ�Ե��ĸ���͵��������н�������
		rm -rf ${WORD_FILE_PATH}.$index.total
		#ȡtop20
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
	#����hive���н������
	wget -t 3 -q $REMOTE_FILE_PATH/tradeword.$TODAY.sort
	wget -t 3 -q $REMOTE_FILE_PATH/tradeword.$TODAY.sort.md5
	msg="${TODAY}����hive����������ʧ��"
	md5sum -c tradeword.$TODAY.sort.md5
	if [ $? -ne 0 ];
	then
		echo $msg >>$LOG_FILE
		return 1
	fi

	#У��hive���ݽ��Ϊ��
	msg="${TODAY}����hive����������Ϊ��"
	[ -f "${WORD_FILE_PATH}.$TODAY.sort" ] && num=`wc -l "${WORD_FILE_PATH}.$TODAY.sort"|awk '{print $1}'` || return 1
	if [ $num -eq 0 ];
	then
		echo $msg >>$LOG_FILE
		return 1
	fi
	#ȡtop20
	msg="${TODAY}�������ս��ʧ��"
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
	#У��mongodb����Դ�ļ��Ƿ�Ϊ��
	[ -f "${WORD_FILE_PATH}.$TODAY.final" ] && num=`wc -l "${WORD_FILE_PATH}.$TODAY.final"|awk '{print $1}'` || exit 1
	if [ $num -eq 0 ];
	then
		exit 1
	fi
	#����MOGODB��ʱ������
	echo -e db.${MONGO_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.ensureIndex"({\"groupid\":1,\"clks\":-1,\"srchs\":-1});" > create.js
	echo -e db.${MONGO_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_COLLECTION_NAME}"');" > rename.js 
	
	server=${MONGO_sHARDING[${MONGO_SHARDINGS}]}
	msg="${server}������ʱ��ʧ��"
	${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js --quiet 
	alert $? ${msg}
		
	host=`echo $server | awk -F":" '{print $1}'`
	port=`echo $server | awk -F":" '{print $2}'`
	${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${MONGO_DB_NAME} -c${MONGO_TMP_COLLECTION_NAME} -f"${MONGO_COLUMN_NAME}" --type "tsv" --file ${WORD_FILE_PATH}.$TODAY.final & > /dev/null &
	for pid in `jobs -p`;do
        wait $pid
        [ "x$?" != "x0" ] && alert 1 "����ʧ��"
	done
    msg="${server}��������ʱ��ʧ��"
    ${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} rename.js  --quiet
    alert $? ${msg}
}

function import_bak_mongo()
{
	#У��mongodb����Դ�ļ��Ƿ�Ϊ��
	[ -f "${WORD_FILE_PATH}.$TODAY.final" ] && num=`wc -l "${WORD_FILE_PATH}.$TODAY.final"|awk '{print $1}'` || exit 1
	if [ $num -eq 0 ];
	then
		exit 1
	fi
	#����MOGODB��ʱ������
	echo -e db.${MONGO_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.ensureIndex"({\"groupid\":1,\"clks\":-1,\"srchs\":-1});" > create.js
	echo -e db.${MONGO_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_COLLECTION_NAME}"');" > rename.js 
	
	server=${MONGO_BAK_sHARDING[${MONGO_SHARDINGS}]}
	msg="${server}������ʱ��ʧ��"
	${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js --quiet 
	alert $? ${msg}
		
	host=`echo $server | awk -F":" '{print $1}'`
	port=`echo $server | awk -F":" '{print $2}'`
	${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${MONGO_DB_NAME} -c${MONGO_TMP_COLLECTION_NAME} -f"${MONGO_COLUMN_NAME}" --type "tsv" --file ${WORD_FILE_PATH}.$TODAY.final & > /dev/null &
	for pid in `jobs -p`;do
        wait $pid
        [ "x$?" != "x0" ] && alert 1 "����ʧ��"
	done
    msg="${server}��������ʱ��ʧ��"
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