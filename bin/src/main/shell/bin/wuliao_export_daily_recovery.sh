#!/bin/sh
#modified by wangchongjie@2012-12-05,for db sharding
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/wuliao_export_daily.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

if [ $# -gt 0 ]
then
  TIME_YYYYMM=`date -d "$1" +%Y%m`
  if [ $? -gt 0 ]
  then
    echo "the #1 param should be conform to format YYYYMMDD";
    exit 1;
  fi
  TIME_YYYYMMDD=`date -d "$1" +%Y%m%d`
else 
  echo "the #1 param should be conform to format YYYYMMDD";
  exit 1;
fi
ADMAKER_EXPORT_FILENAME=beidou_material_${TIME_YYYYMMDD}

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${ADMAKER_EXPORT_FILEPATH}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

program=wuliao_export_daily_recovery.sh
reader_list=liangshimu
msg="${program}:error to query materials from db"

SQL="select s0.adid, s0.srchs, s0.clks from beidoustat.stat_ad_${TIME_YYYYMM} as s0 where s0.wuliaotype>=2 and s0.wuliaotype<=3 and s0.date='${TIME_YYYYMMDD}';" 
runsql_stat_read "$SQL" ${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.1.tmp
alert $? "${msg}"

SQL="select c0.id, c0.filesrc from beidou.cprounitmater? as c0 where [c0.userid];"
runsql_sharding_read "$SQL" ${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.2.tmp $TAB_UNIT_SLICE
alert $? "${msg}"

awk 'FILENAME=="${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.2.tmp"{FILESRC[$1]=$2;} \
	FILENAME=="${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.1.tmp"{print $1"\t"FILESRC[$1]"\t"$2"\t"$3}' \
    ${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.2.tmp \
	${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}.1.tmp \
	 > ${ADMAKER_EXPORT_FILEPATH}/${ADMAKER_EXPORT_FILENAME}

cd ${ADMAKER_EXPORT_FILEPATH}
md5sum ${ADMAKER_EXPORT_FILENAME} > ${ADMAKER_EXPORT_FILENAME}.md5
