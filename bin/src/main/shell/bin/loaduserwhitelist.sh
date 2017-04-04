#!/bin/sh

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

program=loaduserwhitelist.sh
reader_list=zhuqian

LOG_FILE=${LOG_PATH}/loaduserwhitelist.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function call() {

msg="执行远程调用-载入自有流量用户白名单失败("$1")"

url=http://$1:8080/rpc/loadUserWhiteList

java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
alert_return $? "${msg}"


}

function call_noport() {
    msg="执行远程调用-载入自有流量用户白名单失败("$1")"
    url=http://$1/rpc/loadUserWhiteList
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password    
    alert_return $? "${msg}"
}

for server in `echo ${WEB_SERVER_IP_LIST[@]}`; do
    call $server;
done

for server in `echo ${API_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server;
done

for server in `echo ${APIV2_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server;
done

#add by kanghongwei for exp
for server in `echo ${EXP_SERVER_IP_LIST[@]}`; do
    call $server;
 done
