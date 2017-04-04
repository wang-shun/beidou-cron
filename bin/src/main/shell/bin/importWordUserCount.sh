#!/bin/sh
#��ȡǰһ��ÿ��wordid�Ĺ����û�����������mongodb
#for beidou 3.0 at 2012-05-16

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/mongodb.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/importWordUserCount.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=importWordUserCount.sh

mkdir -p ${WORD_DATA_PATH}
alert $? "�������ݴ��Ŀ¼${WORD_DATA_PATH}ʧ��"

mkdir -p ${LOG_PATH}
alert $? "������־�洢Ŀ¼${LOG_PATH}ʧ��"
LOG_FILE=${LOG_PATH}/importWordUserCount.log

cd ${WORD_DATA_PATH}
alert $? "���빤��Ŀ¼${WORD_DATA_PATH}ʧ��"

function fetch_usercount()
{
	#�����ݿ��ȡwordid�Ĺ�����
	i=0;
	while [ $i -lt $KT_TABLE_NUM ]
	do
		msg="�����ݿ��л�ȡ����ʧ��,host=${BEIDOU_IP_READ} port=${BEIDOU_DB_PORT_READ}"
		runsql_sharding_read "select count(distinct userid),wordid from beidou.cprokeyword$i where [userid] group by wordid" ${WORDUSERCOUNT}.tmp.$i
		alert $? "${msg}"
		sort -nk2 ${WORDUSERCOUNT}.tmp.$i > ${WORDUSERCOUNT}.$i
		INPUT=${INPUT}" "${WORDUSERCOUNT}.$i
		i=$((i+1))
	done

	#�ϲ�64�ű������
	msg="�ϲ���$WORDUSERCOUNT ����ʧ��"
	sort -nk2 -m -T ${WORD_DATA_PATH} ${INPUT} |
	awk -F "\t" 'BEGIN{
		pre=-1;
			sum=0;
	}{
			if($2!=pre){
				if(pre!=-1)print pre"\t"sum;
				sum=$1;
				pre=$2;
		}else if($2==pre)
				sum+=$1
	}END{
			if(pre!=-1)print pre"\t"sum
	}' > ${WORDUSERCOUNT}
	alert $? "${msg}"

	rm -f ${WORDUSERCOUNT}.*
	
	#�з��ļ�
	split_file ${WORDUSERCOUNT}
	#�����ļ�
	backup_file
	#����ɾ�������ļ�
	del_file
}

#$1:Ҫ��Ƭ���ļ�
function split_file()
{
	cd ${WORD_DATA_PATH}
	local file=$1
	#���ļ��г�${MONGO_SHARDINGS}���ļ�
	msg="����split_fileʱ��$file������"
	if [ -f $file ]; then
		let file_sum_line=`wc -l $file|awk '{print $1}'`
		let file_per_shard=${file_sum_line}/${MONGO_SHARDINGS}
		let file_reminder=${file_sum_line}%${MONGO_SHARDINGS}
		if [ $file_reminder -ne 0 ]
		then
			file_per_shard=$((file_per_shard+1))
		fi
	
		split -l $file_per_shard $file -d -a 1 ${MONGO_FILE_PREFIX}.
	else
		alert 1 "$msg"
	fi
}	

#�������������
function backup_file()
{
	cd ${WORD_DATA_PATH}
	mkdir -p ${TODAY}
	
	local index=0
	while [ $index -lt ${MONGO_SHARDINGS} ]
	do
		local file=${MONGO_FILE_PREFIX}.${index}
		#���ݴ����������ļ�
		msg="����${file}ʧ��"
		cp $file ${TODAY}
		alert $? "$msg"
		index=$((index+1))
	done
}

#ɾ��4��ǰ������
function del_file()
{
	cd ${WORD_DATA_PATH}
	fourDaysAgo=`date +%Y%m%d -d"4 days ago"`
	fiveDaysAgo=`date +%Y%m%d -d"5 days ago"`
	rm -rf $fourDaysAgo
	rm -rf $fiveDaysAgo
}


#$1:��Ƭindex
function import_mongo()
{
	index=$1
	cd "${WORD_DATA_PATH}"
	
	#����MOGODB��ʱ������
	echo -e db.${MONGO_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.ensureIndex"({\"wordid\" : 1});" > create.js
	echo -e db.${MONGO_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_COLLECTION_NAME}"');" > rename.js 
	
	for server in "${MONGO_sHARDING[${index}]}";do
		msg="${server}������ʱ��ʧ��"
		${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js --quiet 
		alert $? ${msg}
		
		host=`echo $server | awk -F":" '{print $1}'`
		port=`echo $server | awk -F":" '{print $2}'`
		${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${MONGO_DB_NAME} -c${MONGO_TMP_COLLECTION_NAME} -f"${MONGO_COLUMN_NAME}" --type "tsv" --file ${MONGO_FILE_PREFIX}.${index} & > /dev/null &
		for pid in `jobs -p`;do
        		wait $pid
        		[ "x$?" != "x0" ] && alert 1 "����ʧ��"
		done

                msg="${server}��������ʱ��ʧ��"
                ${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} rename.js  --quiet
                alert $? ${msg}
	done
}

#$1:��Ƭindex
function import_bak_mongo()
{
	index=$1
	cd "${WORD_DATA_PATH}"
	
	#����MOGODB��ʱ������
	echo -e db.${MONGO_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.ensureIndex"({\"wordid\" : 1});" > create.js
	echo -e db.${MONGO_COLLECTION_NAME}.drop"();\n"db.${MONGO_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_COLLECTION_NAME}"');" > rename.js 
	
	for server in "${MONGO_BAK_sHARDING[${index}]}";do
		msg="${server}������ʱ��ʧ��"
		${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} create.js --quiet 
		alert $? ${msg}
		
		host=`echo $server | awk -F":" '{print $1}'`
		port=`echo $server | awk -F":" '{print $2}'`
		${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d${MONGO_DB_NAME} -c${MONGO_TMP_COLLECTION_NAME} -f"${MONGO_COLUMN_NAME}" --type "tsv" --file ${MONGO_FILE_PREFIX}.${index} & > /dev/null &
		for pid in `jobs -p`;do
        		wait $pid
        		[ "x$?" != "x0" ] && alert 1 "����ʧ��"
		done

                msg="${server}��������ʱ��ʧ��"
                ${MONGO_CLIENT} ${server}/${MONGO_DB_NAME} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} rename.js --quiet
                alert $? ${msg}
	done
}

#main
if [ $# -ne 0 ];then
	if [[ "$1" == "fetch" ]];then
		fetch_usercount
	fi
	if [[ "$1" == "import" ]];then
		import_mongo $2
	fi
	if [[ "$1" == "importbak" ]];then
		import_bak_mongo $2
	fi
fi	
