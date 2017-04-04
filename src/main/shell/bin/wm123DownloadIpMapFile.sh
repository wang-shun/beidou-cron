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

program=wm123DownloadIpMapFile.sh
reader_list=lvzichan

LOG_FILE=${LOG_PATH}/wm123DownloadIpMapFile.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function call() {

msg="执行远程调用-wm123下载最新的beidou_ip.map文件失败("$1")"

url=http://$1/downloadIpMapFile.rpc

java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
alert_return $? "${msg}"


}


for server in `echo ${WM123_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done

