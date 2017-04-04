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

program=sendRemind.sh
reader_list=zhangpingan

LOG_FILE=${LOG_PATH}/sendRemind.log
url=http://${WEB_BGW_4_RPC}/rpc/sendRemind

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

function call() {
	msg="执行远程调用-发送余额不足提醒失败"
	java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password 1>>${LOG_FILE} 2>> ${LOG_FILE}.wf
	exitStatus=$?
	alert ${exitStatus} "${msg}"
	return ${exitStatus}
}

call
exit $?
