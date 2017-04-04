#!/bin/sh
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/unionsite.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/stat_sitelink.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

program=stat_sitelink_base.sh
reader_list=liangshimu
CURR_DATETIME=`date +%F\ %T`
echo "${program}:${CURR_DATETIME}" >> ${LOG_PATH}/stat_sitelink.log

msg="${program}:error occurs"
if [ ! -f "${BEIDOU_DATA_PATH}/${BASE_FILENAME}" ]; then
    > ${BEIDOU_DATA_PATH}/${BASE_FILENAME}
fi
for i in `seq 1 30`
do
TIME_YYYYMMDD=`date -d "$i days ago" +%Y%m%d`
awk -F '/' '{if(index($1,"http:")>0 && NF>=3) print $3}' ${CLK_PATH}/click.${TIME_YYYYMMDD} | sort -u > ${BEIDOU_DATA_PATH}/sorturl${TIME_YYYYMMDD}
sort -u ${BEIDOU_DATA_PATH}/sorturl${TIME_YYYYMMDD} ${BEIDOU_DATA_PATH}/${BASE_FILENAME} > ${BEIDOU_DATA_PATH}/tmp.out
rm ${BEIDOU_DATA_PATH}/${BASE_FILENAME}
mv ${BEIDOU_DATA_PATH}/tmp.out ${BEIDOU_DATA_PATH}/${BASE_FILENAME}
done
