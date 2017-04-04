#!/bin/sh
CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=AccountMove.sh
reader_list=wangxiongjie
datestr=`date +%Y%m%d`
LOG_FILE=${LOG_PATH}/AccountMove_${datestr}.log
BASE_DIR=/home/work/beidou-cron

#param1 oldUserid
#parma2 newUserid
function moveAccount()
{
	java -Xms1024m -Xmx4096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.accountmove.main.Main $1 $2 >> ${LOG_FILE} 2>&1
}

# read userid file,do account move for every account;
if [ -e "${BASE_DIR}/data/account.txt" ]; then
    while read line
    do
    	echo "START MOVE USERID ${arr[0]} INOT USERID ${arr[1]}." >> ${LOG_FILE}
    	arr=(${line})
    	moveAccount ${arr[0]} ${arr[1]}
    	echo "FINISH MOVE USERID ${arr[0]} INOT USERID ${arr[1]}." >> ${LOG_FILE}
    done < ${BASE_DIR}/data/account.txt
fi