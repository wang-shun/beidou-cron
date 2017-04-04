#!/bin/sh
ROOT_PATH=/home/work/beidou-cron
LOG_PATH=${ROOT_PATH}/logs
LOG_FILE=${LOG_PATH}/adjustSiteTrade.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

type="ERROR"
module=beidou-cron
program=AdjustSiteTradeSystem.sh
reader_list=zhuqian

INPUT_MAPPING=${ROOT_PATH}/dat/trademap_v5.dat                     
INPUT_FIRSTTRADE=${ROOT_PATH}/dat/firsttrade_v5.dat                     
INPUT_FIRSTTRADE_OLD=${ROOT_PATH}/dat/firsttrade_v4.dat 

alert() {

	if [ $# -lt 2 ]
	then
		return
	fi
	ifError $1 "[${type}][${module}]$2@${CURR_DATETIME}" "${program}" \
			"$2" "${reader_list}"
	
}

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=classpath.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

function call() {
	msg="调整行业分类体系失败"
	java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprogroup.AdjustSiteTradeSystem ${INPUT_MAPPING} ${INPUT_FIRSTTRADE} ${INPUT_FIRSTTRADE_OLD} >> ${LOG_FILE}
	alert $? ${msg}
	return 0;
}

call


