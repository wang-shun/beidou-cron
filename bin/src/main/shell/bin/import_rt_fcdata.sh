#!/bin/bash

#@file: import_rt_fcdata.sh
#@author: hanxu
#@date: 2010-10-26
#@version: 1.0.0.1
#@brief: 从凤巢抓取数据，格式为fcplanid-fcunitid

cd `dirname $0`

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/import_rt_fcdata.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
if [ $? -ne 0 ] 
then
    echo "Conf error: Fail to load libfile[$LIB_FILE]!"
    exit 1  
fi

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BAK_PATH}

open_log

program=import_rt_fcdata.sh
reader_list=hanxu03
LOG_FILE="${LOG_PATH}/${LOG_NAME}.log.wf"

cd ${DATA_PATH}
if [ $? -ne 0 ] ; then
	log "FATAL" "${DATA_PATH} do not exists, exit" 
	close_log 1
	exit 1
fi

startMills=`date +"%s"`
startTime=`date +"%Y-%m-%d_%H:%M:%S"`
log "DEBUG" "import begin at ${startTime}" 



#####################################
##### get fcplanid-fcunitid from fc
#####################################

### get fcplanid-fcunitid md5
rm -f ${FCPID_FCUID_MD5_FILE}.new
wget -q -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USERNAME}:${PASSWORD}@${FC_SERVER_PATH}/${FCPID_FCUID_MD5_FILE} -O ${FCPID_FCUID_MD5_FILE}.new

if [ $? -ne 0 ] ; then
	log "FATAL" "wget ${FC_SERVER_PATH}/${FCPID_FCUID_MD5_FILE} encountered an error, exit" 
	rm -f ${FCPID_FCUID_MD5_FILE}.new
	close_log 1
	alert_return 1 "wget ${FC_SERVER_PATH}/${FCPID_FCUID_MD5_FILE} encountered an error, exit"
	exit 1
fi

log "DEBUG" "finish to get fcplanid-fcunitid md5" 


### bak md5 and compare old md5 ,new md5
cp ${FCPID_FCUID_MD5_FILE}.new ${BAK_PATH}/${FCPID_FCUID_MD5_FILE}.${startTime}

diff ${FCPID_FCUID_MD5_FILE}.new ${FCPID_FCUID_MD5_FILE} > /dev/null
if [ $? -eq 0 ] ; then
	log "WARNING" "${FCPID_FCUID_MD5_FILE} is same with old md5, exit" 
	rm -f ${FCPID_FCUID_MD5_FILE}.new
	close_log 0
	exit 0;
fi 


if [ -f ${FCPID_FCUID_MD5_FILE} ] ; then
	mv ${FCPID_FCUID_MD5_FILE} ${FCPID_FCUID_MD5_FILE}.old
fi
mv ${FCPID_FCUID_MD5_FILE}.new ${FCPID_FCUID_MD5_FILE}

log "DEBUG" "finish to compare old md5 and new md5" 


### get fcplanid-fcunitid
rm -f ${FCPID_FCUID_FILE}.new
wget -q -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USERNAME}:${PASSWORD}@${FC_SERVER_PATH}/${FCPID_FCUID_FILE} -O ${FCPID_FCUID_FILE}.new

if [ $? -ne 0 ] ; then
	log "FATAL" "wget ${FC_SERVER_PATH}/${FCPID_FCUID_FILE} encountered an error, exit" 
	rm -f ${FCPID_FCUID_FILE}.new

	# revert ${FCPID_FCUID_MD5_FILE}
	rm -f ${FCPID_FCUID_MD5_FILE}
	if [ -f ${FCPID_FCUID_MD5_FILE}.old ] ; then
		mv ${FCPID_FCUID_MD5_FILE}.old ${FCPID_FCUID_MD5_FILE}
	fi 

	close_log 1
	alert_return 1 "wget ${FC_SERVER_PATH}/${FCPID_FCUID_FILE} encountered an error, exit"
	exit 1
fi

log "DEBUG" "finish to get fcplanid-fcunitid" 


### bak FCPID_FCUID_FILE  and check md5
cp ${FCPID_FCUID_FILE}.new ${BAK_PATH}/${FCPID_FCUID_FILE}.${startTime}

if [ -f ${FCPID_FCUID_FILE} ] ; then
	mv ${FCPID_FCUID_FILE} ${FCPID_FCUID_FILE}.old
fi
mv ${FCPID_FCUID_FILE}.new ${FCPID_FCUID_FILE}

md5sum -c ${FCPID_FCUID_MD5_FILE}
if [ $? -ne 0 ] ; then
	log "FATAL" "check ${FCPID_FCUID_FILE} md5 failed, exit" 
	
	# revert ${FCPID_FCUID_FILE}
	rm -f ${FCPID_FCUID_FILE}
	if [ -f ${FCPID_FCUID_FILE}.old ] ; then
		mv ${FCPID_FCUID_FILE}.old ${FCPID_FCUID_FILE}
	fi

	# revert ${FCPID_FCUID_MD5_FILE}
	rm -f ${FCPID_FCUID_MD5_FILE}
	if [ -f ${FCPID_FCUID_MD5_FILE}.old ] ; then
		mv ${FCPID_FCUID_MD5_FILE}.old ${FCPID_FCUID_MD5_FILE}
	fi 

	close_log 1
	alert_return 1 "check ${FCPID_FCUID_FILE} md5 failed, exit" 
	exit 1;
fi

log "DEBUG" "finish to check md5" 




endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))
log "DEBUG" "import end at `date +"%Y-%m-%d_%H:%M:%S"`, spend time:${spendtime}s" 

close_log 0

