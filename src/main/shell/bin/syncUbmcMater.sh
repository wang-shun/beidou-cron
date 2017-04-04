#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/syncUbmcMater.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding_ubmc.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

LOG_NAME=syncUbmcMater_sh
LOG_LEVEL=8
LOG_SIZE=1800000

program=syncUbmcMater.sh
reader_list=genglei01

mkdir -p ${LOG_PATH}
mkdir -p ${UBMC_OUTPUT_PATH}
mkdir -p ${UBMC_INPUT_PATH}

if [ ! -f ${ERROR_MATER_FILE} ]; then
    echo "" >> ${ERROR_MATER_FILE}
fi

if [ ! -f ${LOGINFO_FILE} ]; then
    echo "" >> ${LOGINFO_FILE}
fi

CURR_DATETIME=`date +%F\ %T`
echo "[INFO]running at "$CURR_DATETIME >> ${LOG_FILE}

function start_sync()
{
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to sync material from drmc to ubmc "$CURR_DATETIME >> ${LOG_FILE}
	
	#set stopfile
	#echo "stop=0" > ${STOP_FILE}
	
	msg="invoke java failed"
	for slice in `seq $1 $2`; do
		for index in `seq 0 7`; do
			# sync unitmater
			log "INFO" "begin to sync unit material from drmc to ubmc, index="$index", slice="$slice
			db_file=${DB_FILE}".unit."${slice}"."${index}
			log_file=${LOGINFO_FILE}".unit."${slice}"."${index}
			error_file=${ERROR_MATER_FILE}".unit."${slice}"."${index}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and ubmcsyncflag=0;"
			
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncUbmcMater  -sunit -m${MATER_CNT_PER_SELECT} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the result of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			log "INFO" "end to sync unit material from drmc to ubmc, index="$index", slice="$slice
			
			
			# sync preunitmater
			log "INFO" "begin to sync preunit material from drmc to ubmc, index="$index", slice="$slice
			db_file=${DB_FILE}".preunit."${slice}"."${index}
			log_file=${LOGINFO_FILE}".preunit."${slice}"."${index}
			error_file=${ERROR_MATER_FILE}".preunit."${slice}"."${index}
			SELECT_SQL="select id, userid, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.precprounitmater$index where ubmcsyncflag=0;"
			
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncUbmcMater -spreunit -m${MATER_CNT_PER_SELECT} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the result of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			log "INFO" "end to sync preunit material from drmc to ubmc, index="$index", slice="$slice
		done
		
		# sync tmpunit
		log "INFO" "begin to sync tmpunit material from drmc to ubmc, slice="$slice
		db_file=${DB_FILE}".tmpunit."${slice}
		log_file=${LOGINFO_FILE}".tmpunit."${slice}
		error_file=${ERROR_MATER_FILE}".tmpunit."${slice}
		SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.tmpcprounitstate s join beidou.tmpcprounitmater m on s.id=m.id where ubmcsyncflag=0;"
		
		runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
		java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncUbmcMater -stmpunit -m${MATER_CNT_PER_SELECT} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -z$slice >> ${LOG_FILE} 2>&1
		# if the result of "java" is wrong then send error message
		alert $? ${msg}
		
		#backup log
		LOG_DATE=`date +%Y%m%d%H%M`
		mv ${db_file} ${db_file}.${LOG_DATE}
		mv ${log_file} ${log_file}.${LOG_DATE}
		mv ${error_file} ${error_file}.${LOG_DATE}
		log "INFO" "end to sync tmpunit material from drmc to ubmc, slice="$slice
		
		
		# sync history
		log "INFO" "begin to sync history material from drmc to ubmc, slice="$slice
		db_file=${DB_FILE}".history."${slice}
		log_file=${LOGINFO_FILE}".history."${slice}
		error_file=${ERROR_MATER_FILE}".history."${slice}
		SELECT_SQL="select id, userid, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.auditcprounithistory where ubmcsyncflag=0;"
		
		runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
		java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncUbmcMater -shistory -m${MATER_CNT_PER_SELECT} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -z$slice >> ${LOG_FILE} 2>&1
		# if the result of "java" is wrong then send error message
		alert $? ${msg}
		
		#backup log
		LOG_DATE=`date +%Y%m%d%H%M`
		mv ${db_file} ${db_file}.${LOG_DATE}
		mv ${log_file} ${log_file}.${LOG_DATE}
		mv ${error_file} ${error_file}.${LOG_DATE}
		log "INFO" "end to sync history material from drmc to ubmc, slice="$slice
	done
	
	# sync sysicon
	log "INFO" "begin to sync sysicon material from drmc to ubmc"
	db_file=${DB_FILE}".sysicon"
	log_file=${LOGINFO_FILE}".sysicon"
	error_file=${ERROR_MATER_FILE}".sysicon"
	SELECT_SQL="select id, fileSrc, hight, width, ubmcsyncflag, mcId from beidouext.systemicons where ubmcsyncflag=0;"
	
	runsql_xdb_read "${SELECT_SQL}" "${db_file}"
	java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncUbmcMater -ssysicon -m${MATER_CNT_PER_SELECT} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} >> ${LOG_FILE} 2>&1
	# if the result of "java" is wrong then send error message
	alert $? ${msg}
	
	#backup log
	LOG_DATE=`date +%Y%m%d%H%M`
	mv ${db_file} ${db_file}.${LOG_DATE}
	mv ${log_file} ${log_file}.${LOG_DATE}
	mv ${error_file} ${error_file}.${LOG_DATE}
	log "INFO" "end to sync sysicon material from drmc to ubmc"
	
	# sync usericon
	log "INFO" "begin to sync usericon material from drmc to ubmc"
	db_file=${DB_FILE}".usericon"
	log_file=${LOGINFO_FILE}".usericon"
	error_file=${ERROR_MATER_FILE}".usericon"
	SELECT_SQL="select id, userId, fileSrc, hight, width, ubmcsyncflag, mcId from beidouext.useruploadicons where ubmcsyncflag=0;"
	
	runsql_xdb_read "${SELECT_SQL}" "${db_file}"
	java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncUbmcMater -susericon -m${MATER_CNT_PER_SELECT} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} >> ${LOG_FILE} 2>&1
	# if the result of "java" is wrong then send error message
	alert $? ${msg}
	
	#backup log
	LOG_DATE=`date +%Y%m%d%H%M`
	mv ${db_file} ${db_file}.${LOG_DATE}
	mv ${log_file} ${log_file}.${LOG_DATE}
	mv ${error_file} ${error_file}.${LOG_DATE}
	log "INFO" "end to sync usericon material from drmc to ubmc"
	
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to sync material from drmc to ubmc "$CURR_DATETIME >> ${LOG_FILE}
}

function end_sync()
{
	#set stopfile
	echo "stop=1" > ${STOP_FILE}
}

if [ $1 = "end" ]; then
	end_sync
else
	if [ $# -eq 3 ]; then
		start_sync $2 $3
	else
		start_sync 0 7
	fi
fi
