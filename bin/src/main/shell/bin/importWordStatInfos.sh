#!/bin/sh

#@file:importWordStatInfos.sh
#@author:zhangzhenhua02
#@date:2014-05-22
#@version:1.0.0.0
#@brief:��logƽ̨ͳ�Ƶ�KT���:�վ�չ���������߼�������mongodb

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/mongodbs.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/importWordStatInfos.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=importWordStatInfos.sh

LOG_FILE=${LOG_PATH}/importWordStatInfos.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

msg="����${DATA_PATH}ʧ��"
cd ${DATA_PATH}
alert $? "$msg"

#��logƽ̨ץȡƽ��չ�����ݣ������ļ���Ƭ
function download_and_split_file()
{
	cd "${DATA_PATH}" 
	
	if [[ $QT_SWITCH -eq 1 ]]
	then
		#��logƽ̨ץȡQT 7��ƽ��չ�֣����߼�������
		localStatTempFile=${MONGO_QT_FILE_PREFIX}.${FILETYPE_QT}
		download_file $localStatTempFile ${FILETYPE_QT}
		split_file $localStatTempFile ${MONGO_QT_FILE_PREFIX}
	fi
	
	if [[ $CT_SWITCH -eq 1 ]]
	then
		#��logƽ̨ץȡCT 7��ƽ��չ�֣����߼�������
		localStatTempFile=${MONGO_CT_FILE_PREFIX}.${FILETYPE_CT}
		download_file $localStatTempFile ${FILETYPE_CT}
		split_file $localStatTempFile ${MONGO_CT_FILE_PREFIX}
	fi
	
	if [[ $HCT_SWITCH -eq 1 ]]
	then
		#��logƽ̨ץȡHCT 7��ƽ��չ�֣����߼�������
		localStatTempFile=${MONGO_HCT_FILE_PREFIX}.${FILETYPE_HCT}
		download_file $localStatTempFile ${FILETYPE_HCT}
		split_file $localStatTempFile ${MONGO_HCT_FILE_PREFIX}
	fi
	
	#����3���Ѵ��������
	backup_all_file
	del_file
	
	#����������js�ļ�
	generate_js
}

#$1:������ʱ�ļ���;$2�ļ�����
function download_file()
{
	local file=$1
	local filetype=$2
	
	# step1: download data file
	wget -t 3 -q "${DATA_PREFIX}&date=${YESTERDAY}&item=${filetype}" -O $file --limit-rate=30M
	alert $? "${0}-wget-${file}-failed"
	
	# step2: download manifest file
	wget -t 3 -q "${MANIFEST_PREFIX}&date=${YESTERDAY}&item=${filetype}" -O $file".manifest" --limit-rate=30M
	alert $? "${0}-wget-${file}-manifest-failed"
	
	# step3: download manifest.md5 file
	wget -t 3 -q "${MANIFEST_MD5_PREFIX}&date=${YESTERDAY}&item=${filetype}" -O $file".manifest.md5" --limit-rate=30M
	alert $? "${0}-wget-${file}-manifest-md5-failed"
	
	# step4: check manifest.md5 file
	offline_manifest_md5=`md5sum $file".manifest" | awk '{print $1}'` #manifest md5 offline
	online_manifest_md5=`awk '{print $1}' $file".manifest.md5"`   #manifest md5 online
	if [ "${offline_manifest_md5}" != "${online_manifest_md5}" ]
	then
		alert 1 "${file}-manifest�ļ�md5У��ʧ��"
	fi
	
	# step5: user manifest to check date file
	offline_file_size=`du -b $file|awk '{print $1}'` #line count offline
	online_file_size=`awk '{print $3}' $file".manifest"`   #line count online
	if [ "${offline_file_size}" != "${online_file_size}" ]
	then
		alert 1 "${file}-�ļ���СУ��ʧ��"
	fi
}

#$1:Ҫ��Ƭ��file $2:��Ƭ�ļ�ǰ׺
function split_file()
{
	local file=$1
	local mongoFilePrefix=$2
	cd "${DATA_PATH}"
	
	#��avgadview file�г�${MONGO_SHARDINGS}���ļ�
	msg="����split_fileʱ��${file}������"
	if [ -f "${file}" ]; then
		let file_sum_line=`wc -l ${file}|awk '{print $1}'`
		let file_per_shard=${file_sum_line}/${MONGO_SHARDINGS}
		let file_reminder=${file_sum_line}%${MONGO_SHARDINGS}
		if [ $file_reminder -ne 0 ]
		then
			file_per_shard=$((file_per_shard+1))
		fi
		split -l $file_per_shard ${file} -d -a 1 ${mongoFilePrefix}.
	else
		alert 1 "$msg"
	fi
}

function backup_all_file()
{
	if [[ $CT_SWITCH -eq 1 ]]
	then
		backup_file ${MONGO_CT_FILE_PREFIX}
	fi
	if [[ $QT_SWITCH -eq 1 ]]
	then
		backup_file	${MONGO_QT_FILE_PREFIX}
	fi
	if [[ $HCT_SWITCH -eq 1 ]]
	then
		backup_file	${MONGO_HCT_FILE_PREFIX}
	fi
}

#�������������
#$1:Ҫ���ݵ���mongodb���ļ�ǰ׺
function backup_file()
{
	local mongoFilePrefix=$1
	cd "${DATA_PATH}"
	mkdir -p ${YESTERDAY}
	
	local index=0
	while [ $index -lt ${MONGO_SHARDINGS} ]
	do
		local file=${mongoFilePrefix}.${index}
		#���ݴ����������ļ�
		msg="����${file}ʧ��"
		cp $file ${YESTERDAY}
		alert $? "$msg"
		index=$((index+1))
	done
}

#ɾ��4��ǰ������
function del_file()
{
	cd "${DATA_PATH}"
	fourDaysAgo=`date +%Y%m%d -d"4 days ago"`
	fiveDaysAgo=`date +%Y%m%d -d"5 days ago"`
	rm -rf $fourDaysAgo
	rm -rf $fiveDaysAgo
}

#����js�ļ�
function generate_js()
{
	#CT mongodb �������
	echo -e db.${MONGO_CTSTAT_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_CTSTAT_TMP_COLLECTION_NAME}.ensureIndex"({\"wordid\":1,\"regid\":1});" > ct_create.js
	echo -e db.${MONGO_CTSTAT_COLLECTION_NAME}.drop"();\n"db.${MONGO_CTSTAT_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_CTSTAT_COLLECTION_NAME}"');" > ct_rename.js 
	#QT mongodb �������
	echo -e db.${MONGO_QTSTAT_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_QTSTAT_TMP_COLLECTION_NAME}.ensureIndex"({\"wordid\":1,\"regid\":1});" > qt_create.js
	echo -e db.${MONGO_QTSTAT_COLLECTION_NAME}.drop"();\n"db.${MONGO_QTSTAT_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_QTSTAT_COLLECTION_NAME}"');" > qt_rename.js 
	#HCT mongodb �������
	echo -e db.${MONGO_HCTSTAT_TMP_COLLECTION_NAME}.drop"();\n"db.${MONGO_HCTSTAT_TMP_COLLECTION_NAME}.ensureIndex"({\"wordid\":1,\"regid\":1});" > hct_create.js
	echo -e db.${MONGO_HCTSTAT_COLLECTION_NAME}.drop"();\n"db.${MONGO_HCTSTAT_TMP_COLLECTION_NAME}.renameCollection"('"${MONGO_HCTSTAT_COLLECTION_NAME}"');" > hct_rename.js 

} 

#$1:����ʽ��ct��qt��hct��
function config_mongo_var()
{
	targettype=$1
	if [[ "$targettype" == "ct" ]];then
		mongoStatDbName=${MONGO_CTSTAT_DB_NAME}
		mongoStatTmpCollectionName=${MONGO_CTSTAT_TMP_COLLECTION_NAME}
		mongoFilePrefix=${MONGO_CT_FILE_PREFIX}
		create_js=ct_create.js
		rename_js=ct_rename.js
	fi	
	if [[ "$targettype" == "qt" ]];then
		mongoStatDbName=${MONGO_QTSTAT_DB_NAME}
		mongoStatTmpCollectionName=${MONGO_QTSTAT_TMP_COLLECTION_NAME}
		mongoFilePrefix=${MONGO_QT_FILE_PREFIX}
		create_js=qt_create.js
		rename_js=qt_rename.js
	fi
	if [[ "$targettype" == "hct" ]];then
		mongoStatDbName=${MONGO_HCTSTAT_DB_NAME}
		mongoStatTmpCollectionName=${MONGO_HCTSTAT_TMP_COLLECTION_NAME}
		mongoFilePrefix=${MONGO_HCT_FILE_PREFIX}
		create_js=hct_create.js
		rename_js=hct_rename.js
	fi
	
#	generate_js
}


#$1:����ʽ $2:��Ƭindex
function import_mongo()
{
	targettype=$1
	index=$2
	cd "${DATA_PATH}"
	
	#����mongodb������ر���
	config_mongo_var $targettype
	
	for server in "${MONGO_sHARDING[${index}]}";do
		msg="${server}������ʱ��ʧ��"
		${MONGO_CLIENT} ${server}/$mongoStatDbName -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} ${create_js} --quiet 
		alert $? ${msg}
		
		host=`echo $server | awk -F":" '{print $1}'`
		port=`echo $server | awk -F":" '{print $2}'`
		${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d$mongoStatDbName -c$mongoStatTmpCollectionName -f"${MONGO_WORD_STAT_COLUMN_NAME}" --type "tsv" --file $mongoFilePrefix.${index} & > /dev/null &
		for pid in `jobs -p`;do
        		wait $pid
        		[ "x$?" != "x0" ] && alert 1 "����ʧ��"
		done

                msg="${server}��������ʱ��ʧ��"
                ${MONGO_CLIENT} ${server}/$mongoStatDbName -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} ${rename_js} --quiet
                alert $? ${msg}
	done
}

#$1:����ʽ $2:��Ƭindex
function import_bak_mongo()
{
	targettype=$1
	index=$2
	cd "${DATA_PATH}"
	
	#����mongodb������ر���
	config_mongo_var $targettype
	
	for server in "${MONGO_BAK_sHARDING[${index}]}";do
		msg="${server}������ʱ��ʧ��"
		${MONGO_CLIENT} ${server}/$mongoStatDbName -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} ${create_js} --quiet 
		alert $? ${msg}
		
		host=`echo $server | awk -F":" '{print $1}'`
		port=`echo $server | awk -F":" '{print $2}'`
		${MONGOIMPORT_CLIENT} --ignoreBlanks -h${host} --port ${port} -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} -d$mongoStatDbName -c$mongoStatTmpCollectionName -f"${MONGO_WORD_STAT_COLUMN_NAME}" --type "tsv" --file $mongoFilePrefix.${index} & > /dev/null &
		for pid in `jobs -p`;do
        		wait $pid
        		[ "x$?" != "x0" ] && alert 1 "����ʧ��"
		done

                msg="${server}��������ʱ��ʧ��"
                ${MONGO_CLIENT} ${server}/$mongoStatDbName -u${MONGO_DB_USER} -p${MONGO_DB_PASSWORD} ${rename_js} --quiet
                alert $? ${msg}
	done
}

#main
#$1:�������� $2:����ʽ $3:��Ƭindex
if [ $# -ne 0 ];then
	if [[ "$1" == "fetch" ]];then
		download_and_split_file
	fi
	if [[ "$1" == "import" ]];then
		import_mongo $2 $3
	fi
	if [[ "$1" == "importbak" ]];then
		import_bak_mongo $2 $3
	fi
fi	
