#!/bin/sh

#@file:offline-all-exp.sh
#@author:zhangxu
#@date:2011-09-23
#@version:1.0.0
#@brief: 下线全部小流量

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/rpc.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/onlineexp.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=offline-all-exp.sh
reader_list=zhangxu04

#----------- function  -------------

function env_setup()
{
		#backup 
		if [ ! -e "$DATA_PATH_HISTORY_EXP" ];then
                mkdir -p $DATA_PATH_HISTORY_EXP
        fi
        
		[  -e "$DATA_PATH_ONLINE_EXP" ] && cp $DATA_PATH_ONLINE_EXP/* $DATA_PATH_HISTORY_EXP

        if [ ! -e "$DATA_PATH" ];then
                mkdir -p $DATA_PATH
        fi

        if [ -e "$DATA_PATH_ONLINE_EXP" ];then
                rm -rf $DATA_PATH_ONLINE_EXP/*
        fi
		
		if [ ! -e "$DATA_PATH_ONLINE_EXP" ];then
                mkdir -p $DATA_PATH_ONLINE_EXP
        fi

        return 0
}

function INF()
{
	echo $1 
	echo "[INFO] `date +"%Y-%m-%d %H:%M:%S"` "$1 >> $LOG_FILE
}

function ERR()
{
	if [ $1 -ne 0 ]; then
		echo "[ERROR]"$2
		echo "[ERROR] Please check and recover manually."
		echo "[ERROR] `date +"%Y-%m-%d %H:%M:%S"` "$2 >> $LOG_FILE
		#alert $1 $2
		exit 2
	fi
}

function ECHO_ARRAY()
{
	for elem in `echo $1`; 
	do
		INF $elem
	done
}

function call() {
	msg="执行远程调用-载入空间信息失败("$1")"
	url=$1
	java -cp ${CUR_CLASSPATH} com.baidu.ctclient.HessianRpcClientUsingErrorCode $url $username $password
	ERR $? "${msg}"
	return 0;
}

helper()
{
	echo "offline-all-exp (auto offline all experiment versions)"
	echo "Version : 1.0.0 (build 20110914)"
	echo "write by zhangxu04@baidu.com "
	echo "Copyright (C) 2011 Baidu.com "
	echo "USAGE: online-exp "        
}

#-------------- main ---------------

if [ $# -gt 0 ];then
	helper
	exit 1
fi

env_setup

INF "----------------------------------------" 
INF "Start to offline all experiment version" 
INF "----------------------------------------" 
INF ""

cd ${DATA_PATH_ONLINE_EXP}

INF "========================================================="
INF "Step1. Create empty conf files" 
INF "========================================================="
echo > ${EXP_CONF_FILE}
md5sum ${EXP_CONF_FILE} > ${EXP_CONF_FILE}.md5
INF "Create empty ${EXP_CONF_FILE} and its md5 done" 

INF "Are you going to offline all experiment versions?"
INF "Please enter (y/n): "
while read an
    do
        case $an in
            y)
				verify="yes"
                break
				;;
            n)
                INF "user exit"
                exit 1
                ;;
        esac
        INF "Are you going to offline all experiment versions?"
		INF "Please enter (y/n): "
done	
INF ""

INF "========================================================="
INF "Step2. Start to upload exp conf files to all online server"
INF "========================================================="
INF "Files will be distributed to the following servers:"
for s in `echo ${WEB_SERVER_NAME_LIST[@]}`; 
do
	INF $s
done

for s in `echo ${EXP_SERVER_NAME_LIST[@]}`; 
do
	INF $s
done
INF ""
for server in `echo ${WEB_SERVER_IP_LIST[@]}`; 
do
	INF "------ Begin processing $server -----"
	
	msg="新建/清理${DEST_EXP_DIR_PATH}/*目录发生错误|$server"
	INF "exec cmd: mkdir -p ${DEST_EXP_DIR_PATH} && cd ${DEST_EXP_DIR_PATH} && rm -rf ${DEST_EXP_DIR_PATH}/*"
	ssh ${server} "mkdir -p ${DEST_EXP_DIR_PATH} && cd ${DEST_EXP_DIR_PATH} && rm -rf ${DEST_EXP_DIR_PATH}/*" >> $LOG_FILE
	ERR $? "${msg}"
	INF "Check and clear dest dir done"
	
	msg="rsync远程拷贝小流量配置文件发生错误|$server"
	INF "rsync files to $server"
	rsync -azv -e ssh ${DATA_PATH_ONLINE_EXP}/* ${server}:${DEST_EXP_DIR_PATH} >> $LOG_FILE
	ERR $? "${msg}"
	INF "rsync files done"
	
	msg="检查拷贝到线上的小流量配置文件md5发生错误|$server"
	for i in ${versions[@]}; 
	do
		#INF "Execute the following command on $server: cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5"
		ssh ${server} "cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5" >> $LOG_FILE
		ERR $? "${msg}"
	done
	for i in ${apaches[@]}; 
	do
		#INF "Execute the following command on $server: cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5"
		ssh ${server} "cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5" >> $LOG_FILE
		ERR $? "${msg}"
	done
	INF "Check files md5 done"
	ssh ${server} "cd ${DEST_EXP_DIR_PATH} && rm *.md5" >> $LOG_FILE
	INF "Remove md5 files done"
	
	INF "----- End processing $server -----"
	INF ""
done

for server in `echo ${EXP_SERVER_IP_LIST[@]}`; 
do
	INF "------ Begin processing $server -----"
	
	msg="新建/清理${DEST_EXP_DIR_PATH}/*目录发生错误|$server"
	INF "exec cmd: mkdir -p ${DEST_EXP_DIR_PATH} && cd ${DEST_EXP_DIR_PATH} && rm -rf ${DEST_EXP_DIR_PATH}/*"
	ssh ${server} "mkdir -p ${DEST_EXP_DIR_PATH} && cd ${DEST_EXP_DIR_PATH} && rm -rf ${DEST_EXP_DIR_PATH}/*" >> $LOG_FILE
	ERR $? "${msg}"
	INF "Check and clear dest dir done"
	
	msg="rsync远程拷贝小流量配置文件发生错误|$server"
	INF "rsync files to $server"
	rsync -azv -e ssh ${DATA_PATH_ONLINE_EXP}/* ${server}:${DEST_EXP_DIR_PATH} >> $LOG_FILE
	ERR $? "${msg}"
	INF "rsync files done"
	
	msg="检查拷贝到线上的小流量配置文件md5发生错误|$server"
	for i in ${versions[@]}; 
	do
		#INF "Execute the following command on $server: cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5"
		ssh ${server} "cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5" >> $LOG_FILE
		ERR $? "${msg}"
	done
	for i in ${apaches[@]}; 
	do
		#INF "Execute the following command on $server: cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5"
		ssh ${server} "cd ${DEST_EXP_DIR_PATH} && md5sum -c ${i}.md5" >> $LOG_FILE
		ERR $? "${msg}"
	done
	INF "Check files md5 done"
	ssh ${server} "cd ${DEST_EXP_DIR_PATH} && rm *.md5" >> $LOG_FILE
	INF "Remove md5 files done"
	
	INF "----- End processing $server -----"
	INF ""
done
INF ""
INF ""

INF "========================================================="
INF "Step3. Start to reload all online app's experiment cache"
INF "========================================================="
INF "rpc will be called in the following servers, continue?"
for s in `echo ${WEB_SERVER_NAME_LIST[@]}`; 
do
	INF $s
done

for s in `echo ${EXP_SERVER_NAME_LIST[@]}`; 
do
	INF $s
done
INF "Please enter (y/n): "
while read an
    do
        case $an in
            y)
                break
				;;
            n)
                INF "user exit"
                exit 1
                ;;
        esac
        INF "rpc will be called in the following servers, continue?"
		ECHO_ARRAY "${WEB_SERVER_NAME_LIST[@]}"
		ECHO_ARRAY "${EXP_SERVER_NAME_LIST[@]}"
		INF "Please enter (y/n): "
done	
INF ""

cd $BIN_PATH
for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; 
do
	rpcurl="http://$server/rpc/loadExpConf"
	INF "Call "$rpcurl
	call "http://$server/rpc/loadExpConf" >> $LOG_FILE
	INF "Call rpc on $server done"
	INF ""
	sleep $SLEEP_TIME_BETWEEN_RPC_CALL
done	

INF ""
INF ""				

INF "All finished successfully!"

