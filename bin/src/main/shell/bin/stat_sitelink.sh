#!/bin/sh
function extractClickUrl() {
CONF_SH="../conf/stat_sitelink.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

echo "stat_sitelink.sh:${CURR_DATETIME}" >> ${LOG_PATH}/stat_sitelink.log

if [ ! -f "${BEIDOU_DATA_PATH}/${BASE_FILENAME}" ]; then
    > ${BEIDOU_DATA_PATH}/${BASE_FILENAME}
fi
awk -F '/' '{if(index($1,"http:")>0 && NF>=3) print $3}' ${CLK_PATH}/click.${TIME_YYYYMMDD} | sort -u > ${BEIDOU_DATA_PATH}/sorturl${TIME_YYYYMMDD}
sort -u ${BEIDOU_DATA_PATH}/sorturl${TIME_YYYYMMDD} ${BEIDOU_DATA_PATH}/${BASE_FILENAME} > ${BEIDOU_DATA_PATH}/tmp.out
rm ${BEIDOU_DATA_PATH}/${BASE_FILENAME}
mv ${BEIDOU_DATA_PATH}/tmp.out ${BEIDOU_DATA_PATH}/${BASE_FILENAME}
}
