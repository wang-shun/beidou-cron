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

program=refreshTcSiteCache.sh
reader_list=wangchongjie

LOG_FILE=${LOG_PATH}/refreshTcSiteCache.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}



function call() {
    msg="ִ��Զ�̵���-ˢ��App����ʧ��("$1")"
    url=http://$1/rpc/reloadTcSiteInfo
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password    
    alert_return $? "${msg}"
}

for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done

for server in `echo ${APIV2_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done

##########  ��ʼ-С��������     ###########

for server in `echo ${EXP_WEB_SERVER_IP_PORT_LIST[@]}`; do
    call $server;
done

##########  ����-С��������     ###########
