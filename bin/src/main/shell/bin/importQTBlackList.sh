#!/bin/sh

#@file:importQTBlackList.sh
#@author:lingbing
#@date:2011-08-10
#@version:1.0.0.0
#@brief:import qtblack and qtuserblacklist to database
#modified by kanghongwei at 2011-09-23 for project cpweb336 

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/qtblacklist.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=importQTBlackList.sh

mkdir -p ${DATA_PATH}

function archive()
{
	archiveBlackList=${DATA_PATH}/${QT_BLACKLIST_FILE_IMPORT}.`date +"%Y%m%d%H"`
	[ -f ${archiveBlackList} ] && rm ${archiveBlackList}
	archiveUserBlackList=${DATA_PATH}/${QT_USER_BLACKLIST_FILENAME}.`date +"%Y%m%d%H"`
	[ -f ${archiveUserBlackList} ] && rm ${archiveUserBlackList}
	
	#ץȡ�������ʱ�
	for file in `echo ${QT_BLACKLIST_FILENAME[@]}`;do
		msg="wget�ļ�${file}ʧ��"
		wget -t 3 -w 3 --limit-rate=10m -P ${DATA_PATH} ftp://${QT_BLACKLIST_HOST}/${QT_BLACKLIST_PATH}/${file} -O ${DATA_PATH}/${file}
		alert $? "${msg}"
		
		msg="awk�з��ļ�${file}ʧ��"
		awk -F"\t" '{print $2"\t"$3}' ${DATA_PATH}/${file} >> ${archiveBlackList}
		alert $? "${msg}"
	done
	
	#ץȡ�û�����شʱ�
	msg="wget�ļ�${QT_USER_BLACKLIST_FILENAME}ʧ��"
	wget -t 3 -w 3 --limit-rate=10m -P ${DATA_PATH} ftp://${QT_USER_BLACKLIST_HOST}/${QT_USER_BLACKLIST_PATH}/${QT_USER_BLACKLIST_FILENAME} -O ${DATA_PATH}/${QT_USER_BLACKLIST_FILENAME}
	alert $? "${msg}"
	cp ${DATA_PATH}/${QT_USER_BLACKLIST_FILENAME} ${archiveUserBlackList}
	
	if ! [ -s ${archiveBlackList} ]
	then
		alert "1" "get empty file ${archiveBlackList} from ufs"
	fi
	
	if ! [ -s ${archiveUserBlackList} ]
	then
		alert "1" "get empty file ${archiveUserBlackList} from ufs"
	fi
}

function importDB()
{
	##���������ʱ�
	msg="drop��ʱ��qtblacklst_tmpʧ��"
	runsql_xdb "drop table if exists beidoureport.qtblacklist_tmp" 
	alert $? "${msg}"
	
	msg="������ʱ��qtblacklist_tmpʧ��"
	runsql_xdb "create table beidoureport.qtblacklist_tmp like beidoureport.qtblacklist"
	alert $? "${msg}"
	
	msg="���غ������ʱ�ʧ��"
	blackImportFIle="${DATA_PATH}"/"${QT_BLACKLIST_FILE_IMPORT}".`date -d "-${FIX_DELAY} hour" "+%Y%m%d%H"`
	if ! [ -s ${blackImportFIle} ]
	then
		alert "1" "file ${blackImportFIle} is empty"
	fi
	runsql_xdb "load data local infile '${blackImportFIle}' into table beidoureport.qtblacklist_tmp"
	alert $? "${msg}"
	
	msg="��������ʱ��ʧ��"
	runsql_xdb "drop table if exists beidoureport.qtblacklist; rename table beidoureport.qtblacklist_tmp to beidoureport.qtblacklist"
	alert $? "${msg}"
	
	##����û�����شʱ�
	msg="drop��ʱ��qtuserblacklst_tmpʧ��"
	runsql_xdb "drop table if exists beidoureport.qtuserblacklist_tmp"
	alert $? "${msg}"
	
	msg="������ʱ��qtuserblacklist_tmpʧ��"
	runsql_xdb "create table beidoureport.qtuserblacklist_tmp like beidoureport.qtuserblacklist"
	alert $? "${msg}"
	
	msg="�����û�����شʱ�ʧ��"
	blackUserImportFIle="${DATA_PATH}"/"${QT_USER_BLACKLIST_FILENAME}".`date -d "-${FIX_DELAY} hour" "+%Y%m%d%H"`
	if ! [ -s ${blackUserImportFIle} ]
	then
		alert "1" "file ${blackUserImportFIle} is empty"
	fi
	
	last_import_file="${DATA_PATH}"/"${QT_USER_BLACKLIST_FILENAME}"."last_import"
	if [ -s ${last_import_file} ] 
	then
		unlink ${last_import_file}
	fi
	
	ln -s ${blackUserImportFIle} ${last_import_file}
	md5sum -c ${last_import_file}.md5
	# ����������ݲ�ͬ���Ž��е���
	if [ $? != 0 ]
	then
		log "INFO" "different blackUserImportFile: ${blackUserImportFIle}"
		runsql_xdb "load data local infile '${blackUserImportFIle}' into table beidoureport.qtuserblacklist_tmp"
		alert $? "${msg}"
		
		msg="��������ʱ��ʧ��"
		runsql_xdb "drop table if exists beidoureport.qtuserblacklist; rename table beidoureport.qtuserblacklist_tmp to beidoureport.qtuserblacklist"
		alert $? "${msg}"
		
		md5sum ${last_import_file} > ${last_import_file}.md5
	fi
}

#���ָ������ǰ�ġ��������ʡ��͡��û�����شʱ�ġ��鵵�ļ�
function clearArchive()
{
	cd ${DATA_PATH}
	
	open_log
	
	excludeFileNames=""
	
	if [ ${MAX_PRESERVE_DAY} -gt 1 ]
	then
		clearDay=0
		while [ ${clearDay} -lt ${MAX_PRESERVE_DAY} ]
		do
			dayName=`date -d "-${clearDay} day" "+%Y%m%d"`
			clearDay=$((clearDay+1))
			if [ -z ${excludeFileNames} ]
			then
				excludeFileNames=${1}${dayName}
				excludeFileNames=${excludeFileNames}"|"${2}${dayName}
			else
				excludeFileNames=${excludeFileNames}"|"${1}${dayName}	
				excludeFileNames=${excludeFileNames}"|"${2}${dayName}
			fi
		done
		
		#��ǰСʱ��UFS�����ļ���ɾ��
		for excludeFile in `echo ${QT_BLACKLIST_FILENAME[@]}`
		do
			excludeFileNames=${excludeFileNames}"|"${excludeFile}
		done
		excludeFileNames=${excludeFileNames}"|"${QT_USER_BLACKLIST_FILENAME}"$"
		excludeFileNames=${excludeFileNames}"|"${2}"last_import"
		
		if [ -n ${excludeFileNames} ]
		then
			rm -f `ls | grep -iv -E ${excludeFileNames}`
		fi
	else
		log "TRACE" "${MAX_PRESERVE_DAY} should big than 1."
	fi
	
	close_log 0
}

main()
{
	ONLY_FETCH=$1
	
	open_log
	
	log "TRACE" "start at `date +%F\ %T`"
	
	#�鵵UFS�ļ�
	archive
	
	#����beidou��ʷ��
	if [ -z ${ONLY_FETCH} ];then
		importDB
	fi
		
	#������ڹ鵵�ļ�
	clearArchive "qtblacklist\.txt\." "qt_blk_word\.map\."
	
	log "TRACE" "end at `date +%F\ %T`"
	
	close_log 0
}

if [ $# -ne 0 ];then
	if [[ "$1" == "${FETCH_FLAG}" ]];then
		main "${FETCH_FLAG}"
	fi
else
	main
fi
