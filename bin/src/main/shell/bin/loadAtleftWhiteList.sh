#!/bin/sh
#@file:loadAtleftWhiteList.sh
#@author:caichao
#@version:1.0.0.0
#@brief:load atleft user whitelist to memery
#

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=loadAtleftWhiteList.sh
reader_list=caichao


CURR_DATE=`date +"%Y%m%d"`
LOG_PATH=${LOG_PATH}/loadAtleftWhite
LOG_FILE=${LOG_PATH}/loadAtleftWhiteList.log.${CURR_DATE}

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function call() {

msg="执行远程调用-载入at左白名单失败("$1")"

url=http://$1:8080/rpc/loadAtLeftWhiteList

echo "[load cache machine] : "${url} >> ${LOG_FILE}

java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
alert_return $? "${msg}"


}

CURR_DATETIME=`date +%F\ %T`
echo "start load whitelist for atleft at "${CURR_DATETIME} >> ${LOG_FILE}
for server in `echo ${WEB_SERVER_IP_LIST[@]}`; do
    call $server;
done

#add by kanghongwei for exp
for server in `echo ${EXP_SERVER_IP_LIST[@]}`; do
    call $server;
done
CURR_DATETIME=`date +%F\ %T`
echo "end load whitelist for atleft at "${CURR_DATETIME} >> ${LOG_FILE}