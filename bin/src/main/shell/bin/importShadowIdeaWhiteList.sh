#!/bin/sh

#@file:importShadowIdeaWhiteList.sh
#@author:genglei01
#@version:1.0.0.0
#@brief:import ShadowIdea whitelist to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/importShadowIdeaWhiteList.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importShadowIdeaWhiteList.sh

LOG_FILE=${LOG_PATH}/importShadowIdeaWhiteList.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="switch into the workspace ${DATA_PATH}"
cd ${DATA_PATH}
alert $? "${msg}"

CONF_SH=${WHITE_FILE}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}
CONF_SH=${WHITE_FILE_NEW}
[ -f "${CONF_SH}" ] && rm ${CONF_SH}

# wget file
msg="wget the file  ${WHITE_FILE} failed"
wget -q  ${WHITE_URL}/${WHITE_FILE} -O ${WHITE_FILE}
alert $? "${msg}"

msg="wget the file ${WHITE_FILE}.md5 failed"
wget  -q ${WHITE_URL}/${WHITE_FILE}.md5 -O ${WHITE_FILE}.md5
alert $? "${msg}"

msg="check md5 for ${WHITE_FILE} failed"
md5sum -c ${WHITE_FILE}.md5
alert $? "${msg}"

awk -F'	' -v white_type=${WHITE_TYPE} 'BEGIN{OFS="	"} $0 ~ /^[0-9]+$/ {print white_type,$1}' ${WHITE_FILE} > ${WHITE_FILE_NEW}
alert $? "use awk to generate import file failed"

runsql_cap "delete from beidoucap.whitelist where type=${WHITE_TYPE}" >> ${LOG_FILE} 2>&1
alert $? "delete data from beidoucap.whitelist failed"

runsql_cap "load data local infile '${WHITE_FILE_NEW}' into table beidoucap.whitelist" >> ${LOG_FILE} 2>&1
alert $? "load data into beidoucap.whitelist failed failed"

CURR_DATETIME=`date +%F\ %T`
echo "end at "$CURR_DATETIME >> ${LOG_FILE}
