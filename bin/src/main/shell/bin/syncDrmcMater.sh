#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/syncDrmcMater.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding_ubmc.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

LOG_NAME=syncDrmcMater_sh
LOG_LEVEL=8
LOG_SIZE=1800000

program=syncDrmcMater.sh
reader_list=genglei01

mkdir -p ${LOG_PATH}
mkdir -p ${DRMC_OUTPUT_PATH}
mkdir -p ${DRMC_INPUT_PATH}

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
	echo "[INFO]start to sync material from ubmc to drmc "$CURR_DATETIME >> ${LOG_FILE}
	
	msg="invoke java failed"
	for slice in `seq $1 $2`; do
		for index in `seq 0 7`; do
			# sync unitmater
			log "INFO" "begin to sync unit material from ubmc to drmc, index="$index", slice="$slice
			db_file=${DB_FILE}"."${slice}"."${index}
			log_file=${LOGINFO_FILE}"."${slice}"."${index}
			error_file=${ERROR_MATER_FILE}"."${slice}"."${index}
			invalid_file=${INVALID_MATER_FILE}"."${slice}"."${index}
			SELECT_SQL="select s.id, s.uid, chaTime, wuliaoType, title, description1, description2, showUrl, targetUrl, wireless_show_url, wireless_target_url, fileSrc, file_src_md5, height, width, ubmcsyncflag, mcId, mcVersionId, drmcsyncflag, s.state from beidou.cprounitstate$index s join beidou.cprounitmater$index m on s.id=m.id where state in (0,1) and drmcsyncflag=0;"
			
			runsql_single_read "${SELECT_SQL}" "${db_file}" ${slice}
			java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.SyncDrmcMater -m${MATER_CNT_PER_SELECT} -i${invalid_file} -e${error_file} -l${log_file} -d${db_file} -t${MAX_THREAD} -a$index -z$slice >> ${LOG_FILE} 2>&1
			# if the result of "java" is wrong then send error message
			alert $? ${msg}
			
			#backup log
			LOG_DATE=`date +%Y%m%d%H%M`
			mv ${db_file} ${db_file}.${LOG_DATE}
			mv ${log_file} ${log_file}.${LOG_DATE}
			mv ${error_file} ${error_file}.${LOG_DATE}
			mv ${invalid_file} ${invalid_file}.${LOG_DATE}
			log "INFO" "end to sync unit material from ubmc to drmc, index="$index", slice="$slice
		done
	done
	
	CURR_DATETIME=`date +%F\ %T`
	echo "[INFO]end to sync material from ubmc to drmc "$CURR_DATETIME >> ${LOG_FILE}
}

if [ $# -eq 2 ]; then
	start_sync $1 $2
else
	start_sync 0 7
fi
