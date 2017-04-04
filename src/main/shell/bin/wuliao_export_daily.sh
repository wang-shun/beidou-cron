#!/bin/sh
#modified by wangchongjie@2012-12-05,for db sharding
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/wuliao_export_daily.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${ADMAKER_EXPORT_FILEPATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

program=wuliao_export_daily.sh
reader_list=liangshimu
msg="${program}:error to query materials from db"

SQL="select s0.adid, s0.srchs, s0.clks from beidoustat.stat_ad_${TIME_YYYYMM} as s0 where s0.wuliaotype>=2 and s0.wuliaotype<=3 and s0.date='${TIME_YYYYMMDD}';"
runsql_stat_read "$SQL" ${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.1.tmp
alert $? "${msg}"

SQL="select s.id, mcId, mcVersionId from beidou.cprounitstate? s join beidou.cprounitmater? m on s.id=m.id where state!=2 and wuliaotype in (2,3);"
runsql_sharding_read "$SQL" ${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.2.tmp $TAB_UNIT_SLICE
alert $? "${msg}"

java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.AdmakerMaterExport \
	${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.1.tmp \
	${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.2.tmp \
	${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME} >> ${LOG_FILE}

cd ${ADMAKER_EXPORT_FILEPATH}
md5sum ${ADMAKER_EXPORT_FILENAME} > ${ADMAKER_EXPORT_FILENAME}.md5
