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

program=loadInterestStat.sh
reader_list=wangxiaokun

LOG_FILE=${LOG_PATH}/loadInterestStat.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}



function call() {
    msg="执行远程调用-刷新兴趣统计数据缓存失败("$1")"
    url=http://$1/rpc/reloadInterestStat
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password    
    alert_return $? "${msg}"
}

for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done

for server in `echo ${APIV2_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done


