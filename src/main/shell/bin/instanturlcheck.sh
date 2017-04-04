#!/bin/sh

WORK_PATH="/home/work/beidou-cron/bin"
cd $WORK_PATH
if [ $? -ne 0 ]
then
        echo "Cannot cd work path($WORK_PATH)!"
else
        echo "$0 is working under Path[$WORK_PATH]!"
fi

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/classpath_recommend.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


CONF_FILE="../conf/instanturlcheck.conf"
source $CONF_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load conffile[$CONF_FILE]!"
	exit 1
fi

program=instanturlcheck.sh
reader_list=genglei01
LOG_DEST=${LOG_PATH}/instanturlcheck
LOG_FILE=${LOG_DEST}/instanturlcheck.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_DEST}
mkdir -p ${LOCAL_DEST}

msg="cd ${BIN_PATH} failed"
cd ${BIN_PATH}
alert $? "${msg}"

CUR_DATE=`date +%Y%m%d`
CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}.${CUR_DATE}

START_POINT_FILE=$LOCAL_DEST/${START_POINT}

function recvresult()
{
	cp ${START_POINT_FILE} ${START_POINT_FILE}.${CUR_DATE}

	msg="receive instant results from bmq failed"
	java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.UrlJumpCheck -c${START_POINT_FILE} 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

recvresult
