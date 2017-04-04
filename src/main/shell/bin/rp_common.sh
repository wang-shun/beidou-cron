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

CONF_SH=./beidou_lib.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/rp_common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


# Function: call the handshake rpc to check health status
# Parameters:
#   $1:  "server_ip:port"
function handShake() {
    local ipPortString=${1}
    local url="http://${ipPortString}${RP_RPC_URL_HANDSHAKE}"
    log "DEBUG" "trying to detect health status of ${ipPortString}"
    doRpc ${url}
    return $?
}



# Function: all the remote rpc specified by $1
# Parameters:
#   $1:  the whole rpc url
function doRpc(){
    local url=${1}
    java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
    local statusCode=$?
    log "DEBUG" "Call ${url} return ${statusCode}"
    return ${statusCode}
}



# Function: call the actural rpc to do business
# Parameters:
#   $1:  ip and port in form of "server_ip:port"
function executeFunc() {
    local ipPortString=${1}
    local url="http://${1}${RP_RPC_FUNC_URL}";
    log "DEBUG" "call business rpc ${url}"
    doRpc ${url}
    return $?
}


# Function: main function
# No parameters
function main() {
    for server in `echo ${RP_SERVER_IP_PORT_LIST[@]}`; do
        handShake ${server}
        local statusCode=$?
        if [ "${statusCode}" -eq "0" ]; then
            executeFunc ${server}
            local statusCode=$?
            local msg="${RP_RPC_FUNC_DESC}失败(${server})"
            alert_return ${statusCode} "${msg}"
            [ ${statusCode} -ne "0" ] && log "FATAL" "${msg}" 
            return ${statusCode}
        fi
    done
    local statusCode=1;
    local msg="${RP_RPC_FUNC_DESC}失败(所有机器均不可用)"
    alert_return ${statusCode} "${msg}"
    [ ${statusCode} -ne "0" ] && log "FATAL" "${msg}" 
    return ${statusCode}    
}


#The shell script which source this script should:
#
#1. first initialize the following varibales, like:
#   program=rp_cycleReportJob.sh
#   reader_list=hejinggen
#   RP_RPC_FUNC_DESC="远程调用-老report生成周期报表"
#   RP_RPC_FUNC_URL="${RP_RPC_URL_cycleReportJob}"
#   LOG_NAME="rp_cycleReportJob"
#   LOG_FILE="${LOG_PATH}/${LOG_NAME}.log"
#
#2. then call the following functions:
#
#   open_log
#   main
#   statusCode=$?
#   close_log ${statusCode}
#   exit ${statusCode}
