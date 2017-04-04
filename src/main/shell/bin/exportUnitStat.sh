#!/bin/bash

#@file:exportUnitStat.sh
#@author:zhangxichuan
#@date:2015-07-30
#@version:1.0.0.0
#@brief:export unit stat data from olap engine 

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/exportUnitStat.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=exportUnitStat.sh

mkdir -p ${LOG_PATH}
mkdir -p ${OUTPUT_PATH_TMP}
mkdir -p ${OUTPUT_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "${CURR_DATETIME} >> ${LOG_FILE}

cd ${OUTPUT_PATH_TMP}

msg="ExportUnitStat java error."
java -Xms1024m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.cprounit.ExportUnitStat ${OUTPUT_PATH_TMP}/${OUTPUT_FILE_NAME}".tmp" 1>> ${LOG_FILE} 2>>${LOG_FILE}.wf
alert $? ${msg}

msg="output converting error."
cat ${OUTPUT_FILE_NAME}.tmp | awk -F ' ' '{printf"%s\t%s\t%s\t%s\t%s\t%s\t%s\n", $7,$1,$2,$3,$4,$5,$6}' > ${OUTPUT_FILE_NAME}
alert $? ${msg}
rm -f ${OUTPUT_FILE_NAME}.tmp

msg="mv ${OUTPUT_FILE_NAME} error."
mv ${OUTPUT_FILE_NAME} ${OUTPUT_PATH}/
alert $? ${msg}

cd ${OUTPUT_PATH}
msg="md5sum error."
md5sum ${OUTPUT_FILE_NAME} > ${OUTPUT_FILE_NAME}.md5
alert $? ${msg}

rm -f ${OUTPUT_PATH}/${OUTPUT_FILE_NAME_OLD}
rm -f ${OUTPUT_PATH}/${OUTPUT_FILE_NAME_OLD}.md5

CURR_DATETIME=`date +%F\ %T`
echo "end at "${CURR_DATETIME} >> ${LOG_FILE}