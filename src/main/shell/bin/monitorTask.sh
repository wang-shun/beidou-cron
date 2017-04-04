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
program=monitorTask.sh
reader_list=kanghongwei


LOG_FILE=${LOG_PATH}/monitorTask.log
url=http://${WEB_BGW_4_RPC}/rpc/monitorTask

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

function call() {
	msg="执行远程调用-超时巡查任务失败"
	java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password >> ${LOG_FILE} 2>&1
	exitStatus=$?
	alert ${exitStatus} "${msg}"
	return ${exitStatus}
}

call
exit $?
