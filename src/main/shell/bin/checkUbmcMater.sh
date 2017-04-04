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

function check_text()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_text
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/text
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkUbmcText.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check ubmc text "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check ubmc text, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and ubmcsyncflag=1 and wuliaotype=1;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -ctext -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check ubmc text, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check ubmc text "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_image()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_image
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/image
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkUbmcImage.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	ONE_SELECT_PER_ROUND=100
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check ubmc image "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check ubmc image, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and ubmcsyncflag=1 and wuliaotype=3;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cimage -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check ubmc image, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check ubmc image "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_and_update()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_update
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/check_and_update
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkUbmcUpdate.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	ONE_SELECT_PER_ROUND=100
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check and update refMcId/descInfo "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check ubmc image, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and ubmcsyncflag=1 and wuliaotype in (2,3);"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cupdate -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check and update refMcId/descInfo, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check and update refMcId/descInfo "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_all()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_all
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/all
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkUbmcAll.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	ONE_SELECT_PER_ROUND=100
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check ubmc all "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check ubmc all, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and mcId>0;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -call -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check ubmc all, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check ubmc all "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_admaker()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_admaker
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/admaker
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkUbmcAdmaker.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	ONE_SELECT_PER_ROUND=100
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check admaker material "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check admaker material, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and wuliaoType=3 and chaTime>='2013-08-01';"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cadmaker -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check admaker material, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check ubmc admaker material "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_admaker_update()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_admakerUpdate
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/admakerUpdate
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkAdmakerFixUpdate.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check mater image for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check mater image for admaker, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and wuliaotype=3 and chaTime>='2013-08-03';"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cadmakerupdate -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check mater image for admaker, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check mater image for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_md5()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_md5
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/md5
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkMaterMd5.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check mater image for md5 "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check mater image for md5, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and wuliaotype>1;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cmd5 -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check mater image for md5, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check mater image for md5 "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function filter_special_char()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_filter
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/filter
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/filterMaterChar.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to filter material special char "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to filter material special char, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and chaTime>='2013-08-02';"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cfilter -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to filter material special char, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to filter material special char "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function recompile_mater()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_recompileMater
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/recompileMater
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/recompileMater.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to recompile material for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq $3 $4`; do
			log "INFO" "start to recompile material for admaker, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and wuliaotype=3;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -crecompile -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to recompile material for admaker, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to recompile material for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_version()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_version
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/checkVersion
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkVersionMater.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check version for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to check version for admaker, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and wuliaotype=3;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -ccheck_version -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check version for admaker, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check version for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function fix_wirelessurl()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_md5
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/fix_wirelessurl
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/fixWirelessUrl.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to fix all mater for wirelessurl "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq 0 7`; do
			log "INFO" "start to fix all mater for wirelessurl, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and chaTime>='2013-11-28';"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cfix_wirelessurl -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to fix all mater for wirelessurl, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to fix all mater for wirelessurl "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_material()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_checkmater
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/check_material
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/checkMaterial.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check all material "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq $3 $4`; do
			log "INFO" "start to check all material, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -ccheck_material -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check all material, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check all material "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function fix_material()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_fixmater
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/fix_material
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/fixMaterial.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to fix all material "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq $3 $4`; do
			log "INFO" "start to fix all material, index="$index", slice="$slice
			
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2;"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -cfix_material -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to fix all material, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to fix all material "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function recompile_target_mater()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_recompileTargettedMater
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/recompileTargettedMater
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/recompileTargettedMater.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to recompile targetted material for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	index=0
	slice=0
	log "INFO" "start to recompile targetted material for admaker, index="$index", slice="$slice
	
	db_file=${DB_FILE}"."${index}"."${slice}
	log_file=${LOGINFO_FILE}"."${index}"."${slice}
	error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
	invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
	
	# wget file
	wget ftp://cq01-rdqa-pool178.cq01.baidu.com/home/beidou/genglei/check/ubmc-drmc/recompile/data/errormater.log -O ${db_file}
	
	msg="invoke java failed for index="$index", slice="$slice
	java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -crecompile_target_mater -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
	# if the relt of "java" is wrong then send error message
	alert $? ${msg}
	
	#backup log
	LOG_DATE=`date +%Y%m%d%H%M`
	mv ${db_file} ${db_file}.${LOG_DATE}
	mv ${log_file} ${log_file}.${LOG_DATE}
	mv ${error_file} ${error_file}.${LOG_DATE}
	mv ${invalid_file} ${invalid_file}.${LOG_DATE}
	
	log "INFO" "end to recompile targetted material for admaker, index="$index", slice="$slice
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to recompile targetted material for admaker "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}

function check_admaker_material()
{
	open_log
	
	#config
	LOG_NAME=checkUbmcMater_checkadmaker
	
	UBMC_OUTPUT_PATH=${DATA_PATH}/checkUbmcMater/check_admaker_material
	UBMC_INPUT_PATH=${UBMC_OUTPUT_PATH}/input
	LOG_FILE=${LOG_PATH}/check_admaker_material.log
	LOGINFO_FILE=${UBMC_OUTPUT_PATH}/loginfo.log
	ERROR_MATER_FILE=${UBMC_OUTPUT_PATH}/errormater.log
	INVALID_MATER_FILE=${UBMC_OUTPUT_PATH}/invalidmater.log
	#add db_tmp_file to /tmp to avoid network disk delay
	DB_TMP_FILE=/tmp/dbfile.log
	DB_FILE=${UBMC_INPUT_PATH}/dbfile.log
	
	
	mkdir -p ${UBMC_OUTPUT_PATH}
	mkdir -p ${UBMC_INPUT_PATH}
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]start to check all admaker material "$CURR_DATETIME >> ${LOG_FILE}
	
	for index in `seq $1 $2`; do
		for slice in `seq $3 $4`; do
			log "INFO" "start to check all admaker material, index="$index", slice="$slice
			db_tmp_file=${DB_TMP_FILE}"."${index}"."${slice}
			db_file=${DB_FILE}"."${index}"."${slice}
			log_file=${LOGINFO_FILE}"."${index}"."${slice}
			error_file=${ERROR_MATER_FILE}"."${index}"."${slice}
			invalid_file=${INVALID_MATER_FILE}"."${index}"."${slice}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, height, width, mcId, mcVersionId, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state!=2 and (wuliaotype=2 or wuliaotype=3);"
			
			#execute sql
			runsql_single_read "${SELECT_SQL}" "${db_tmp_file}" ${slice}
			
			msg="invoke java failed for index="$index", slice="$slice
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.CheckUbmcMater -ccheck_admaker_material -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_tmp_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the relt of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_tmp_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			
			log "INFO" "end to check all material, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to check all material "$CURR_DATETIME >> ${LOG_FILE}
	
	close_log 0
}


if [ $1 = "check_text" ]; then
	if [ $# -eq 3 ]; then
		check_text $2 $3
	else
		check_text 0 7
	fi
elif [ $1 = "check_image" ]; then
	if [ $# -eq 3 ]; then
		check_image $2 $3
	else
		check_image 0 7
	fi
elif [ $1 = "check_and_update" ]; then
	if [ $# -eq 3 ]; then
		check_and_update $2 $3
	else
		check_and_update 0 7
	fi
elif [ $1 = "all" ]; then
	if [ $# -eq 3 ]; then
		check_all $2 $3
	else
		check_all 0 7
	fi
elif [ $1 = "admaker" ]; then
	if [ $# -eq 3 ]; then
		check_admaker $2 $3
	else
		check_admaker 0 7
	fi
elif [ $1 = "admakerupdate" ]; then
	if [ $# -eq 3 ]; then
		check_admaker_update $2 $3
	else
		check_admaker_update 0 7
	fi
elif [ $1 = "md5" ]; then
	if [ $# -eq 3 ]; then
		check_md5 $2 $3
	else
		check_md5 0 7
	fi
elif [ $1 = "filter" ]; then
	if [ $# -eq 3 ]; then
		filter_special_char $2 $3
	else
		filter_special_char 0 7
	fi
elif [ $1 = "recompile" ]; then
	if [ $# -eq 5 ]; then
		recompile_mater $2 $3 $4 $5
	else
		recompile_mater 0 7 0 7
	fi
elif [ $1 = "check_version" ]; then
	if [ $# -eq 3 ]; then
		check_version $2 $3
	else
		check_version 0 7
	fi
elif [ $1 = "fix_wirelessurl" ]; then
	if [ $# -eq 3 ]; then
		fix_wirelessurl $2 $3
	else
		fix_wirelessurl 0 7
	fi
elif [ $1 = "check_material" ]; then
	if [ $# -eq 5 ]; then
		check_material $2 $3 $4 $5
	else
		check_material 0 7 0 7
	fi
elif [ $1 = "fix_material" ]; then
	if [ $# -eq 5 ]; then
		fix_material $2 $3 $4 $5
	else
		fix_material 0 7 0 7
	fi
elif [ $1 = "recompile_target" ]; then
	if [ $# -eq 5 ]; then
		recompile_target_mater
	else
		recompile_target_mater
	fi
elif [ $1 = "check_admaker_material" ]; then
	if [ $# -eq 5 ]; then
		check_admaker_material $2 $3 $4 $5
	else
		check_admaker_material 0 7 0 7
	fi
fi
