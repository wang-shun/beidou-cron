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

program=urlchecksendmail.sh
reader_list=genglei01
LOG_DEST=${LOG_PATH}/urlchecksendmail
LOG_FILE=${LOG_DEST}/urlchecksendmail.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_DEST}
mkdir -p ${LOCAL_DEST}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="cd ${BIN_PATH} failed"
cd ${BIN_PATH}
alert $? "${msg}"

CUR_DATE=`date +%Y%m%d`

function patrolurlsendmail()
{
	msg="send mails for patrol valid url failed"
	java -Xms1024m -Xmx2096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.UrlJumpCheckSendMail -p 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

function instanturlsendmail()
{
	msg="send mails for instant url failed"
	java -Xms1024m -Xmx2096m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.UrlJumpCheckSendMail -i 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

#no params: send mails for all url
#1 param:
#	$1="patrol": send mails for patrol valid url
#	$1="instant": send mails for instant url

if [ $# -eq 0 ]; then
	patrolurlsendmail
	instanturlsendmail
elif [ $# -eq 1 ]; then
	if [ $1 = "patrol" ]; then
		patrolurlsendmail
	elif [ $1 = "instant" ]; then
		instanturlsendmail
	fi
fi