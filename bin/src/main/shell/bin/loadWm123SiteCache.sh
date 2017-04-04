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

program=loadWm123SiteCache.sh
reader_list=liangshimu

LOG_FILE=${LOG_PATH}/loadWm123SiteCache.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function call() {

msg="执行远程调用-载入站点数据失败("$1")"

url=http://$1/refreshCache.rpc

java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
alert_return $? "${msg}"


}


for server in `echo ${WM123_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done

