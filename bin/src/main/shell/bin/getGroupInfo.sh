#!/bin/bash

#@file: getGroupInfo.sh
#@author: genglei01
#@date: 2012-11-15
#@version: 1.0.0.1
#@brief: download group_info data from database
#get group info data from current database, output into file for crm-dc

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/getGroupInfo.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=getGroupInfo.sh
reader_list=genglei01

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

TODAY=`date +"%Y%m%d"`
DATA_FILE=${GROUP_INFO_PRE}.${TODAY}

echo -e "stat [${DATA_FILE}] begin at `date +"%Y-%m-%d %H:%M:%S"`" >> ${LOG_FILE}
start=`date +"%s"`

cd ${DATA_PATH} && rm -f ${DATA_FILE}
runsql_sharding_read "select g.userid, g.planid, g.groupid, gi.reglist, gi.isallregion, gi.price, gi.isallsite, gi.sitetradelist, gi.sitelist, gi.genderinfo from cprogroup g join cprogroupinfo gi on g.groupid=gi.groupid where [g.userid]" "${DATA_FILE}"

md5sum ${DATA_FILE} > ${DATA_FILE}.md5

#regist file to dts
msg="regist DTS for ${GET_GROUP_INFO_GROUP_INFO} failed."
md5=`getMd5FileMd5 ${DATA_PATH}/${DATA_FILE}.md5`
noahdt add ${GET_GROUP_INFO_GROUP_INFO} -m md5=${md5} -i date=${TODAY} bscp://${DATA_PATH}/${DATA_FILE}
alert $? "${msg}"

end=`date +"%s"`
spendtime=$(($end-$start))
echo -e "stat [${DATA_FILE}] end at `date +"%Y-%m-%d %H:%M:%S"`, spend time:${spendtime}s" >> ${LOG_FILE}

