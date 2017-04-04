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

program=loadsiteinfo.sh
reader_list=zengyunfeng,zhuqian

LOG_FILE=${LOG_PATH}/loadsiteinfo.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function call() {

msg="执行远程调用-载入站点数据失败("$1")"

url=http://$1:8080/rpc/loadSite

java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
alert_return $? "${msg}"


}

#由于report模块的context是/report，无法使用web和api的url，因此重写一个方法
#没有把/report写到IP中的原因是为了保证common.conf的一致性

function call_noport() {
    msg="执行远程调用-载入站点数据失败("$1")"
    url=http://$1/rpc/loadSite
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password    
    alert_return $? "${msg}"
}

for server in `echo ${WEB_SERVER_IP_LIST[@]}`; do
    call $server;
done

#for server in `echo ${API_SERVER_IP_LIST[@]}`; do
#    call $server;
#done

for server in `echo ${API_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server;
done

for server in `echo ${APIV2_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server;
done

