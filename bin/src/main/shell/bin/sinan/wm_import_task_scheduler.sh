#!/bin/bash
#@file: wm_import_task_scheduler.sh
#@author: zhangxu04
#@date: 2011-05-28
#@version: 1.0.0.0
#@brief: ��ѯ˾��DB�������ǰû�����������е�����ͻ�ȡsiteurl list��������˾������

#--------------- var  --------------
CONF_SH=common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=wm_import_task_scheduler.sh
reader_list=zhangxu

TIMESTAMP=`date +%Y%m%d`

TEMP_PATH=$BASE_PATH/"temp"

DATA_PATH=$BASE_PATH/"siteurl"

LOG_PATH=$BASE_PATH/"log"

SYSOUT_LOG=$LOG_PATH/"wm_import_task_scheduler.log"

ERROR_LOG=$LOG_PATH/"wm_import_task_scheduler.log.wf"

#--------------- function  --------------

# ��������
function env_setup()
{
	mkdir -p $TEMP_PATH
	mkdir -p $LOG_PATH
	mkdir -p $DATA_PATH
	#mkdir -p $DATA_PATH/$TIMESTAMP/
	
	cd $BASE_PATH
}

echo_msg(){
   echo "[INFO] "$TIMESTAMP" "$1 | tee -a $SYSOUT_LOG
}

echo_error_msg(){
	echo "[ERROR] "$TIMESTAMP" "$1 >> $SYSOUT_LOG
    echo "[ERROR] "$TIMESTAMP" "$1 | tee -a $ERROR_LOG
}

#--------------- main  --------------

startMills=`date +"%s"`

echo_msg "Start scheduler at `date +"%Y-%m-%d_%H:%M:%S"`"

env_setup

# ���������Ѿ���������tid��siteurlӳ���ϵ�ļ����Ѿ���������statusΪ16
GET_RUNNING_TASK_SQL="SELECT count(*) FROM sn_task WHERE tstatus not in (12,32,10);"
msg="����˾��û�д�������״̬����������ʧ��"
$MYSQL_BIN -h$SINAN_DB_IP -P$SINAN_DB_PORT -u$SINAN_DB_USER -p$SINAN_DB_PASSWORD sinan_beidou --skip-column-name -e "$GET_RUNNING_TASK_SQL" > $TEMP_PATH/running_task_num 
alert $? ${msg}
echo_msg "export sinan running sinan task successfully"
	
msg="sqlִ�з��ز�Ϊһ��"
line_num=`cat $TEMP_PATH/running_task_num | wc -l`
if [ $line_num -ne 1 ];then
	echo_error_msg "sql exec failed, result is one line."
	alert 1 ${msg}
fi

RUNNING_TASK_NUM=`cat $TEMP_PATH/running_task_num`
msg="�������ɽ���"
test $RUNNING_TASK_NUM -ge 0
alert $? ${msg}
echo_msg "Running task number is ${RUNNING_TASK_NUM}"

if [ $RUNNING_TASK_NUM -eq 0 ];then
	msg="��������Ŀ¼${DATA_PATH}ʧ��"
	cd $DATA_PATH
	alert $? ${msg}
	cat $SITEURL_FROM_FILENAME | head -100 > ${SITEURL_FROM_FILENAME}.sub
	cat $SITEURL_FROM_FILENAME | sed '1,100d' >  ${SITEURL_FROM_FILENAME}.new
	cat ${SITEURL_FROM_FILENAME}.new > $SITEURL_FROM_FILENAME
	echo_msg "Get 100 site out of from $SITEURL_FROM_FILENAME"
	line_num=`cat ${SITEURL_FROM_FILENAME}.sub | wc -l`
	if [ $line_num -lt 100 ];then
		mkdir -p $DATA_PATH/$TIMESTAMP/
		msg="��������Ŀ¼${DATA_PATH}/${TIMESTAMP}ʧ��"
		cd $DATA_PATH/$TIMESTAMP
		alert $? ${msg}
		echo_msg "there are less than 100 siteurl from $SITEURL_FROM_FILENAME, so download from beidou side"
		#ץȡ�ļ�
		msg="wget�ļ�${SITEURL_FROM_SERVER}/${SITEURL_FROM_PATH}ʧ��"
		wget ftp://$SITEURL_FROM_SERVER/$SITEURL_FROM_PATH/$SITEURL_FROM_FILENAME
		alert $? ${msg}
		msg="�ÿ����������ļ�Ϊ��"
		if [ ! -s ${SITEURL_FROM_FILENAME} ];then
			alert 1 ${msg}
		fi
		cp $SITEURL_FROM_FILENAME $DATA_PATH
	fi
	cd $DATA_PATH
	echo_msg "Copy new 100 siteurl to $SINAN_TASK_MGR_BIN_PATH"
	msg="�����ļ���${SITEURL_FROM_FILENAME}.sub��${SINAN_TASK_MGR_BIN_PATH}ʧ��"
	cp ${SITEURL_FROM_FILENAME}.sub $SINAN_TASK_MGR_BIN_PATH
	alert $? ${msg}
	echo_msg "Insert task into sinan"
	msg="����˾�ϲ���taskmgrʧ��"
	$PHP_BIN $SINAN_TASK_MGR_BIN_EXEC $SINAN_TASK_MGR_BIN_PATH/${SITEURL_FROM_FILENAME}.sub  >> $SYSOUT_LOG 
	alert $? ${msg}
else
	echo_msg "Running task existing, quit this round of inserting tasks into sinan"
fi

echo_msg "Scheduler finished work."
endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

echo_msg "End scheduler at `date +"%Y-%m-%d_%H:%M:%S"`, cost ${spendtime}s"

exit 0

