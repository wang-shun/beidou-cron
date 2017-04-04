#!/bin/sh
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/classpath_rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=updateAuditerCache.sh
reader_list=genglei01

LOG_FILE=${LOG_PATH}/updateAuditerCache.log
url=http://${tcmgr00}:8080/audit/rpc/updateAuditerCacheTask

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

function call() {
msg="ִ��Զ�̵���-�������Ա����ʧ��"
java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password >>${LOG_FILE} 2>&1
alert $? "${msg}"
return 0;
}

call

