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


CONF_FILE="../conf/patrolvalid.conf"
source $CONF_FILE
if [ $? -ne 0 ]
then
	echo "Conf error: Fail to load conffile[$CONF_FILE]!"
	exit 1
fi

program=patrolvalid.sh
reader_list=genglei01
LOG_FILE=${LOG_PATH}/patrolvalid.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${LOCAL_DEST}

CURR_DATETIME=`date +%F\ %T`
echo $CURR_DATETIME >> ${LOG_FILE}

msg="���빤��Ŀ¼${BIN_PATH}ʧ��"
cd ${BIN_PATH}
alert $? "${msg}"

CUR_DATE=`date +%Y%m%d`
mkdir -p $LOCAL_DEST/${CUR_DATE}

AKA_AUDIT_RESULT_FILE=$LOCAL_DEST/${CUR_DATE}/${AKA_AUDIT_RESULT}.${CUR_DATE}
AKA_AUDIT_LOG_FILE=$LOCAL_DEST/${CUR_DATE}/${AKA_AUDIT_LOG}.${CUR_DATE}


function patrolvalid()
{
	
	mkdir -p $LOCAL_DEST/${CUR_DATE}

	AKA_AUDIT_RESULT_FILE=$LOCAL_DEST/${CUR_DATE}/${AKA_AUDIT_RESULT}.${CUR_DATE}
	AKA_AUDIT_LOG_FILE=$LOCAL_DEST/${CUR_DATE}/${AKA_AUDIT_LOG}.${CUR_DATE}

	msg="����aka��Ѳ��Ч���ֹ��ȫ���?�����RD����Ų�?"
	java -Xms2048m -Xmx3072m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.PatrolValid -p -t$MAX_THREAD -o$AKA_AUDIT_RESULT_FILE -l$AKA_AUDIT_LOG_FILE 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

function sendmail()
{
	mkdir -p $LOCAL_DEST/${CUR_DATE}
	AKA_AUDIT_RESULT_FILE=$LOCAL_DEST/${CUR_DATE}/${AKA_AUDIT_RESULT}.${CUR_DATE}

	msg="����aka��Ѳ�����ʼ���?�����RD����Ų�?"
	java -Xms2048m -Xmx3072m -classpath ${CUR_CLASSPATH} com.baidu.beidou.auditmanager.PatrolValid -s -i$AKA_AUDIT_RESULT_FILE 1>> ${LOG_FILE}.${CUR_DATE} 2>>${LOG_FILE}.wf.${CUR_DATE}
	alert $? "${msg}"
}

if [ $# -eq 0 ]; then
	patrolvalid
	sendmail
elif [ $# -eq 1 ]; then
	if [ $1 = "patrolvalid" ]; then
		patrolvalid
	elif [ $1 = "sendmail" ]; then
		sendmail
	fi
elif [ $# -eq 2 ]; then
	if [ $1 = "sendmail" ]; then
		CURR_DATETIME=$2
		sendmail
	fi
fi

