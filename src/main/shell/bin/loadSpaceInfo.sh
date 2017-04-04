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

program=loadSpaceInfo.sh
reader_list=yang_yun

LOG_FILE=${LOG_PATH}/loadSpaceInfo.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}


function call() {
	msg="执行远程调用-载入空间信息失败("$1")"
	
	url=http://$1:8080/rpc/loadSpaceInfoCTTask
	retryCount=0
	MAX_RETRY=5
	sucFlag=0
	while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
	do
	retryCount=$(($retryCount+1))
	java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
	#alert_return $? "${msg}"
	        if [ $? -eq 0 ]
	        then
	                sucFlag=1
	        else
	                echo "fail count $retryCount" >> ${LOG_FILE}
	                sleep 30
	        fi
	done
	if [ $sucFlag -eq 1 ]
	then
	    echo "fail count $retryCount" >> ${LOG_FILE}
	else
	    alert $? "${msg}"
	fi
}

function call_noport() {
	msg="执行远程调用-载入空间信息失败("$1")"
	
	url=http://$1/rpc/loadSpaceInfoCTTask
	retryCount=0
	MAX_RETRY=5
	sucFlag=0
	while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
	do
	retryCount=$(($retryCount+1))
	java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
	#alert_return $? "${msg}"
	        if [ $? -eq 0 ]
	        then
	                sucFlag=1
	        else
	                echo "fail count $retryCount" >> ${LOG_FILE}
	                sleep 30
	        fi
	done
	if [ $sucFlag -eq 1 ]
	then
	    echo "fail count $retryCount" >> ${LOG_FILE}
	else
	    alert $? "${msg}"
	fi
}

for server in `echo ${WEB_SERVER_IP_LIST[@]}`; do
    call $server;
done

#added by genglei for audit
for server in `echo ${AUDIT_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server/audit;
done

#added by genglei for manager
for server in `echo ${MANAGER_SERVER_IP_PORT_LIST[@]}`; do
    call_noport $server/manager;
done

#add by kanghongwei for exp
for server in `echo ${EXP_SERVER_IP_LIST[@]}`; do
    call $server;
done
