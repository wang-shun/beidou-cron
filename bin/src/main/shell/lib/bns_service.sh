#!/bin/bash
#@file: bns_service.sh
#@author: kanghongwei
#@version: 1.0.0
#@intention: get various kind of servers list from BNS instead of specifying in conf files.

#@usage
#1. get single server:
#serverListArray=(`functionName BNS_SERVER_NAME`)
#echo ${serverListArray[0]}

#2. get serverList in for cycle
#for server in 	`functionName BNS_SERVER_NAME`
#do
#  echo ${server}
#done

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/bns.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


SERVER_INFO_FILE="./serverInfo.dat.$$"

function getServersInfo(){
	if [[ -z $1 ]]; then
		alert 1 "No bns service name specified."
	else    
    	local bns_name=$1
	fi
	
	get_instance_by_service ${bns_name} -pi > ${SERVER_INFO_FILE}
	
	if [[ ! -s ${SERVER_INFO_FILE} ]]; then
		alert 1 "No servers list for specified bns service name : ${bns_name}."
	fi
}

#return ipList,format: [XX.XX.XX.XX XX.XX.XX.XX]
#$1:bns service name 
function getIpList() {
	getServersInfo $1
    while read line
    do
        serverIP=`echo ${line} | awk -F" " '{print $2}'`
        echo -e ${serverIP}
    done < ${SERVER_INFO_FILE}
    rm -rf ${SERVER_INFO_FILE}
}

#return portList,format:[PP PP]
#$1:bns service name 
function getPortList(){
	getServersInfo $1
    while read line
    do
        serverPort=`echo ${line} | awk -F" " '{print $3}'`
        echo -e ${serverPort}
    done < ${SERVER_INFO_FILE}
    rm -rf ${SERVER_INFO_FILE}
}

#return portList,format:[XX.XX.XX.XX:PP XX.XX.XX.XX:PP]
#$1:bns service name 
function getIpPortList(){
	getServersInfo $1
    while read line
    do
        serverIP=`echo ${line} | awk -F" " '{print $2}'`
        serverPort=`echo ${line} | awk -F" " '{print $3}'`
        echo -e ${serverIP}:${serverPort}
    done < ${SERVER_INFO_FILE}
    rm -rf ${SERVER_INFO_FILE}
}

#return portList,format:[XX.XX.XX.XX:PP/suffix XX.XX.XX.XX:PP/suffix]
#$1:bns service name
#$1:bns service suffix
function getIpPortWithSuffixList(){
    getServersInfo $1
    
    if [[ -z $2 ]]; then
	    alert 1 "No suffix specified for bns service name :${bns_name}."
	else    
    	local suffix=$2
	fi
    
    while read line
    do
        serverIP=`echo ${line} | awk -F" " '{print $2}'`
        serverPort=`echo ${line} | awk -F" " '{print $3}'`
        echo -e ${serverIP}:${serverPort}/${suffix}
    done < ${SERVER_INFO_FILE}
    rm -rf ${SERVER_INFO_FILE}
}

#return portList,format:[serverName1=XX.XX.XX.XX serverName2=XX.XX.XX.XX]
#$1:bns service name
function getNameIpList(){
	getServersInfo $1
    while read line
    do
        serverName=`echo ${line} | awk -F" " '{print $1}'`
        serverIP=`echo ${line} | awk -F" " '{print $2}'`
        echo -e ${serverName}=${serverIP}
    done < ${SERVER_INFO_FILE}
    rm -rf ${SERVER_INFO_FILE}
}

#merget two server list
#$1:server list1
#$2:server list2
function mergeServerList(){
	
	while [ $# -gt 0 ]
	do
		echo $1
		shift
	done
}