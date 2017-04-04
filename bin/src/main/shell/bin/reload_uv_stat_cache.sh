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


program=interestCacheReload.sh
reader_list=tianxin

LOG_FILE=${LOG_PATH}/uvStatCacheReload.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function reloadCache() {
    echo "$1"
    url=http://$1/rpc/reloadUvStatCache
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
}


# reload uv stat cache for report
for server in `echo ${REPORT_SERVER_IP_PORT_LIST[@]}`; do
    reloadCache $server/report >> $LOG_FILE
    alert $? "Fail to reload uv stat cache for beidou-report[$server]"
done

# reload uv stat cache for exp report
for server in `echo ${EXP_REPORT_SERVER_IP_PORT_LIST[@]}`; do
    reloadCache $server >> $LOG_FILE
    alert $? "Fail to reload uv stat cache for beidou-report[$server]"
done 

