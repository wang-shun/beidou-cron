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


CONF_FILE="../conf/patrolurlcheck.conf"
source $CONF_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load conffile[$CONF_FILE]!"
	exit 1
fi

program=patrolurlcheck.sh
reader_list=genglei01
LOG_DEST=${LOG_PATH}/patrolurlcheck
LOG_FILE=${LOG_DEST}/patrolurlcheck.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_DEST}
mkdir -p ${LOCAL_DEST}

msg="cd ${BIN_PATH} failed"
cd ${BIN_PATH}
alert $? "${msg}"

CUR_DATE=`date +%Y%m%d`
YEST_DATE=`date -d "1 days ago" +%Y%m%d`
mkdir -p $LOCAL_DEST/${CUR_DATE}
CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

URL_CHECK_MAP_CUR_FILE=$LOCAL_DEST/${CUR_DATE}/${URL_CHECK_MAP_PRE}.${CUR_DATE}
URL_CHECK_MAP_YEST_FILE=$LOCAL_DEST/${YEST_DATE}/${URL_CHECK_MAP_PRE}.${YEST_DATE}
URL_CHECK_MAP_TWO_DAYS_FILE=$LOCAL_DEST/${CUR_DATE}/${URL_CHECK_MAP_TWO_DAYS_PRE}.${CUR_DATE}
START_POINT_FILE=$LOCAL_DEST/${START_POINT}

function patrolurl()
{
	msg="patrol valid url and send messages to bmq failed"
	java -Xms2048m -Xmx3072m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.UrlJumpCheckPatrol -p -m${URL_CHECK_MAP_CUR_FILE} -d${CUR_DATE} 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

function recvresult()
{
	cp ${START_POINT_FILE} ${START_POINT_FILE}.${CUR_DATE}
	
	# if there are many errors after online, then use next shell statement
	#cat ${URL_CHECK_MAP_CUR_FILE} ${URL_CHECK_MAP_YEST_FILE} > ${URL_CHECK_MAP_TWO_DAYS_FILE}
	cat ${URL_CHECK_MAP_CUR_FILE} > ${URL_CHECK_MAP_TWO_DAYS_FILE}
	
	msg="receive patrol results from bmq failed"
	java -Xms2048m -Xmx3072m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.UrlJumpCheckPatrol -r -m${URL_CHECK_MAP_TWO_DAYS_FILE} -c${START_POINT_FILE} 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

if [ $# -eq 0 ]; then
	patrolurl
	recvresult
elif [ $# -eq 1 ]; then
	if [ $1 = "patrolurl" ]; then
		patrolurl
	elif [ $1 = "recvresult" ]; then
		recvresult
	fi
fi
