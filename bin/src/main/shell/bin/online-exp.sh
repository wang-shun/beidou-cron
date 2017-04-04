#!/bin/sh

#@file:online-exp.sh
#@author:zhangxu
#@date:2011-09-23
#@version:1.0.0
#@brief: 小流量上线

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

program=online-exp.sh
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
	echo "online-exp (auto online experiment versions)"
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

INF "----------------------------------" 
INF "Start to online experiment version" 
INF "----------------------------------" 
INF ""

cd ${DATA_PATH_ONLINE_EXP}

INF "========================================================="
INF "Step1. Download exp index conf file" 
INF "========================================================="

INF "Begin download ${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}" 
msg="${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}获取小流量配置索引文件失败"
wget -q ${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE} -O ${EXP_CONF_FILE} --limit-rate=${LIMIT_RATE} 
ERR $? "${msg}"
INF "End download ${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}" 

INF "Begin download ${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}.md5" 
msg="${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}.md5获取小流量配置索引文件失败"
wget -q ${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}.md5 -O ${EXP_CONF_FILE}.md5 --limit-rate=${LIMIT_RATE} 
ERR $? "${msg}"
INF "End download ${EXP_CONF_DOWNLOAD_URL}/${EXP_CONF_FILE}.md5" 

msg="获取小流量配置索引文件md5校验失败"
md5sum -c ${EXP_CONF_FILE}.md5 >> $LOG_FILE
ERR $? "${msg}"
INF "Check ${EXP_CONF_FILE} md5 done" 
INF ""
INF ""

INF "========================================================="
INF "Step2. Split exp group file name out of index conf file" 
INF "========================================================="
msg="小流量配置索引文件${EXP_CONF_FILE}不能为空"
cat ${EXP_CONF_FILE} | sed '/^[[:space:]]*$/d' | sed '/^$/d' > ${TMP_EXP_CONF}
mv ${EXP_CONF_FILE} ${EXP_CONF_FILE}.bak
mv ${TMP_EXP_CONF} ${EXP_CONF_FILE}
if [ ! -s $EXP_CONF_FILE ] ;then
	ERR 1 "${msg}"
fi

while read line
do
	msg="小流量配置索引文件${EXP_CONF_FILE}格式错误"
	echo $line | grep "="  >> /dev/null 2>&1
	if [ $? -ne 0 ] ;then
		ERR 1 "${msg}"
	fi
	echo $line | grep "=$"  >> /dev/null 2>&1
	if [ $? -eq 0 ] ;then
		ERR 1 "${msg}"
	fi
	echo $line | grep "^="  >> /dev/null 2>&1
	if [ $? -eq 0 ] ;then
		ERR 1 "${msg}"
	fi
done < ${EXP_CONF_FILE}

rm -f ${EXP_CONF_FILE}.bak
INF "小流量配置索引文件${EXP_CONF_FILE}格式检查通过" 

versions=`awk -F"=" '{v[$1]=$2} END{for(i in v)print v[i]}' ${EXP_CONF_FILE}`
apaches=`awk -F"=" '{v[$1]=$2} END{for(i in v)print i".cookie"}' ${EXP_CONF_FILE}`
for i in ${versions[@]}; 
do    
	INF "Begin download ${i}" 
	msg="获取小流量用户列表文件${EXP_CONF_DOWNLOAD_URL}/${i}失败"
	wget -q ${EXP_CONF_DOWNLOAD_URL}/${i} -O ${i} --limit-rate=${LIMIT_RATE}
	ERR $? "${msg}"
	INF "End download ${i}" 
	
	INF "Begin download ${i}.md5" 
	msg="获取小流量用户列表文件${EXP_CONF_DOWNLOAD_URL}/${i}.md5失败"
	wget -q ${EXP_CONF_DOWNLOAD_URL}/${i}.md5 -O ${i}.md5 --limit-rate=${LIMIT_RATE}
	ERR $? "${msg}"
	INF "End download ${i}.md5" 
	
	msg="小流量用户列表文件md5校验失败"
	md5sum -c ${i}.md5 >> $LOG_FILE
	ERR $? "${msg}"
	INF "Check ${i} md5 done" 
	INF "" 
done
INF "Begin generate transmit used conf files, like apache0.cookie" 
awk -F"=" '{v[$1]=$2} END{for(i in v)print "echo "v[i]" > "i".cookie && md5sum "i".cookie > "i".cookie.md5"}' ${EXP_CONF_FILE} > ${TMP_EXEC_SCRIPT}
. ${TMP_EXEC_SCRIPT}
rm ${TMP_EXEC_SCRIPT}
INF "End generate transmit used conf files" 
INF ""
INF ""

INF "Are you going to deploy the following experiment versions online?"
ECHO_ARRAY "${versions[@]}"
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
        INF "Are you going to deploy the following experiment versions online?"
		ECHO_ARRAY "${versions[@]}"
		INF "Please enter (y/n): "
done	
INF ""

INF "========================================================="
INF "Step3. Start to upload exp conf files to all online server"
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
INF "Step4. Start to reload all online app's experiment cache"
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



