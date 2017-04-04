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

program=loadUserList.sh
reader_list=yang_yun

LOG_FILE=${LOG_PATH}/loadUserList.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}

function call_noport() {
	msg="执行远程调用-载入用户列表失败("$1")"
	
	url=http://$1/rpc/loadUserListCTTask
	
	java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
	alert_return $? "${msg}"
}

#added by genglei for manager
for server in `echo ${MANAGER_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server/manager;
done
