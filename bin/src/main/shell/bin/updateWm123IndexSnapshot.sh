#!/bin/bash
#@file: updateWm123IndexSnapshot.sh
#@author: zhangxu04
#@date: 2011-05-03
#@version: 1.0.0.0
#@brief: Copy index snapshot image zip from offline server and put it on online servers and update database
#no argument need

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/updateWm123IndexSnapshot.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=updateWm123IndexSnapshot.sh
reader_list=zhangxu04

#--------------- var --------------

WM123_SERVER_IP_LIST=($tcmgr00 $tcmgr01 $aimgr00 $aimgr01)

# ��������ڣ���ʽΪYYmmdd�������ݶ�����Ϊ���죬�����Ͻ��첿��Ķ����������ߣ����Խ�����Կ��������족
YESTERDAY=`date +%Y%m%d`

######################################################################################################
# �ļ��ṹ˵��
# ${DATA_PATH}/wm123/snapshot/                             <------- �����ļ���
#                            /temp/tmp                     <------- ��ʱ�ļ������wget��׼�����Ϣ
#                            /20110405/                    <------- �Ը����ļ������������ļ���
#                                     /snapshot            <------- Ҫ���µ��ļ�
#                                     /sql                 <------- Ҫִ�е�sql�ű�
#                                     /backup              <------- ���ݿⱸ��sql�洢·��
#
#######################################################################################################
#--------------- function  --------------

##! @TODO: ��������
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 0 ������гɹ�������code=0
function env_setup()
{
	if [ ! -e "$CRON_SNAPSHOT_BASE_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_BASE_PATH
	fi
	
	if [ -e "$CRON_SNAPSHOT_WORK_PATH" ];then
		rm -rf $CRON_SNAPSHOT_WORK_PATH
	fi
	
	# �½������ļ���
	if [ ! -e "$CRON_SNAPSHOT_WORK_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_WORK_PATH
	fi
	
	if [ ! -e "$CRON_SNAPSHOT_TO_BE_SYNC_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_TO_BE_SYNC_PATH
	fi
	
	if [ ! -e "$CRON_SNAPSHOT_TO_BE_EXECSQL_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
	fi
	
	if [ ! -e "$CRON_SNAPSHOT_BACKUP_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_BACKUP_PATH
	fi
	
	if [ ! -e "$CRON_SNAPSHOT_TEMP_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_TEMP_PATH
	fi
	
	# �����ʱ�ļ�
	echo >  $CRON_SNAPSHOT_TEMP_FILE
	
	return 0
}

##! @TODO: ������ͼ��������ʱ���ļ��ع�����
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function rollback_file()
{
	for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
		msg_rb="���½�ͼ�������󣬻ع������������ָ���ͼ�������ļ�${server}ʧ��"	
		#rsync -auzv --delete -e ssh ${CRON_SNAPSHOT_ONLINE_SYNC_PATH}/snapshot ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH >>$LOG_FILE
		ssh ${server} "rm -rf $ONLINE_SNAPSHOT_DEST_PATH && cp -r $ONLINE_BACKUP_PATH/snapshot $ONLINE_SNAPSHOT_DEST_PATH "
		RET_CODE_RB=$?
		if [ $RET_CODE_RB -ne 0 ];then
			ifError "${RET_CODE_RB}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg_rb}" "${reader_list}"
		fi
	done
}

##! @TODO: ������ͼ��������ʱ�����ݿ�ع�����
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function rollback_db()
{
	msg="���½�ͼ�������󣬻ع��������������ǻָ����ݿ��beidou.${UNIONSITEINFOS_TABLE_NAME}ʧ��"
	runsql_xdb "`cat ${CRON_SNAPSHOT_BACKUP_FILE}`"
    alert $? "${msg}"
}


##! @TODO: ��ӡ�����ʾ
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function echo_msg()
{
	echo $1 | tee -a $LOG_FILE
}

#--------------- main --------------

startMills=`date +"%s"`

echo_msg "======================================="
echo_msg "= ��ʼ${YESTERDAY}����"
echo_msg "======================================="


# Step1. ������ʼ��
echo_msg "Step1. ������ʼ����ʼ"
env_setup
echo_msg "Step1. ������ʼ���ɹ�"


# Step2. ��ȡվ���ͼѹ������������ھ�wget����������ڱ�ʾû�и������󣬽ű��˳�
echo_msg "Step2. ��ȡվ���ͼѹ������ʼ��wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH}��ʼ"
cd $CRON_SNAPSHOT_TO_BE_SYNC_PATH
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}��ȡʧ��"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH} --output-file=$CRON_SNAPSHOT_TEMP_FILE
RET_CODE=$?
if [ $RET_CODE -ne 0 ] ;then
    # ���������Ϣ����No such file����ʾ�������������⣬ֱ�ӱ����˳�
    grep "No such file" $CRON_SNAPSHOT_TEMP_FILE
    alert $? "${msg}"
	echo_msg "ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}�����ڣ�û���κθ���" 
	# ɾ���ļ�
	rm -f ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}
	rmdir $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
	rmdir $CRON_SNAPSHOT_BACKUP_PATH
	rmdir $CRON_SNAPSHOT_TO_BE_SYNC_PATH
	rmdir $CRON_SNAPSHOT_WORK_PATH
	echo_msg "======================================="
	echo_msg "= ����${YESTERDAY}����"
	echo_msg "======================================="
	exit 0
fi
echo_msg "Step2. ��ȡվ���ͼѹ�����ɹ���wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH}�ɹ�"


# Step3. ��ȡվ���ͼѹ����md5�����
echo_msg "Step3. ��ȡվ���ͼѹ����md5����鿪ʼ"
cd $CRON_SNAPSHOT_TO_BE_SYNC_PATH
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}.md5��ȡʧ��"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH}.md5 --output-file=$CRON_SNAPSHOT_TEMP_FILE
alert $? "${msg}"
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}��md5���ʧ��"
md5sum -c ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}.md5 > /dev/null
alert $? "${msg}"
rm -f ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}.md5
echo_msg "Step3. ��ȡվ���ͼѹ����md5�����ɹ�"


# Step4. ��ȡվ���ͼִ��SQL
echo_msg "Step4. ��ȡվ���ͼִ��SQL�ű���ʼ��wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH}��ʼ"
cd $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_SQL_FROM_PATH}��ȡʧ��"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH} --output-file=$CRON_SNAPSHOT_TEMP_FILE
alert $? "${msg}"
echo_msg "Step4. ��ȡվ���ͼִ��SQL�ű���ʼ��wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH}�ɹ�"


# Step5. ��ȡվ���ͼִ��SQL��md5�ļ������
echo_msg "Step5. ��ȡվ���ͼִ��SQL��md5�ļ�����鿪ʼ"
cd $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_SQL_FROM_PATH}.md5��ȡʧ��"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH}.md5 --output-file=$CRON_SNAPSHOT_TEMP_FILE
alert $? "${msg}"
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_SQL_FROM_PATH}��md5���ʧ��"
md5sum -c ${OFFLINE_SNAPSHOT_SQL_FROM_FILE_NAME}.md5 > /dev/null
alert $? "${msg}"
rm -f ${OFFLINE_SNAPSHOT_SQL_FROM_FILE_NAME}.md5
echo_msg "Step5. ��ȡվ���ͼִ��SQL��md5�ļ������ɹ�"


# Step6. ��ѹ��ͼѹ����
echo_msg "Step6. ��ѹ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}��ʼ"
cd $CRON_SNAPSHOT_TO_BE_SYNC_PATH
unzip -o ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}
msg="snapshot.zipѹ�����ļ���ʽ������ѹ����Ӧֱ�Ӱ���jpg�ļ�"
ls *.jpg
alert $? "${msg}"
cp ${OFFLINE_SNAPSHOT_FROM_FILE_NAME} ..
rm -f ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}
echo_msg "Step6. ��ѹ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}�ɹ�"


# Step7. ���������ļ�
echo_msg "Step7. ��ʼ����..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	msg="��������${server}:${ONLINE_SNAPSHOT_DEST_PATH}�ļ���${ONLINE_BACKUP_PATH}ʧ��"
	ssh ${server} "mkdir -p $ONLINE_BACKUP_PATH; cp -r $ONLINE_SNAPSHOT_DEST_PATH/ $ONLINE_BACKUP_PATH/"
	alert $? "${msg}"
	echo_msg "��������${server}:${ONLINE_SNAPSHOT_DEST_PATH}�ļ���${ONLINE_BACKUP_PATH}�ɹ�"
done
echo_msg "Step7. ���б������"


# Step8. ͬ���������ļ�
echo_msg "Step8. ��ʼ���½�ͼ�ļ�..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	echo_msg "ͬ���½�ͼ${CRON_SNAPSHOT_TO_BE_SYNC_PATH} ��${server}:${ONLINE_SNAPSHOT_DEST_PATH}��ʼ"
	msg="ͬ���½�ͼ${CRON_SNAPSHOT_TO_BE_SYNC_PATH}�������ļ�${server}ʧ��,�����ѻع�"	
    rsync -azv -e ssh $CRON_SNAPSHOT_TO_BE_SYNC_PATH ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH/ >>$LOG_FILE
	RET_CODE=$?
	if [ $RET_CODE -ne 0 ];then
		rollback_file
		ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
		exit 1
	fi
	echo_msg "ͬ���½�ͼ$CRON_SNAPSHOT_TO_BE_SYNC_PATH ��${server}:${ONLINE_SNAPSHOT_DEST_PATH}�ɹ�"
done
echo_msg "Step8. ���н�ͼ�ļ��������"


# Step9. �������ݿ��
echo_msg "Step9. ��ʼ�������ݱ�beidou.${UNIONSITEINFOS_TABLE_NAME}��ʼ"
QUERY_ALL_SQL="select * from beidouext.unionsiteinfos";
UNIONSITEINFOS_FILE="${CRON_SNAPSHOT_BACKUP_PATH}/unionsiteinfos_file.txt"
if [ -s ${UNIONSITEINFOS_FILE} ]
then
	rm -f ${UNIONSITEINFOS_FILE}
fi
runsql_xdb_read "${QUERY_ALL_SQL}" "${UNIONSITEINFOS_FILE}"
if [ -s ${UNIONSITEINFOS_FILE} ]
then
	awk -v maxNum=${MAX_INSERT_NUM_PER_TIME} -v sqlFile=${CRON_SNAPSHOT_BACKUP_FILE} 'BEGIN{ ORS=""; count=0; firstExeTime=0;}; 
	{	
		if(firstExeTime == 0){
			print "insert into beidouext.unionsiteinfos values " >> sqlFile
		}
		if(count < maxNum) {
	        if(count % maxNum ==(maxNum-1)){
                print "("$1",\""$2"\",""\""$3"\",""\""$4"\","$5","$6","$7","$8","$9","$10",\""$11"\",""\""$12"\" );"  >> sqlFile  
             }else {
             	print "("$1",\""$2"\",""\""$3"\",""\""$4"\","$5","$6","$7","$8","$9","$10",\""$11"\",""\""$12"\" ),"  >> sqlFile
             }
            count += 1;
	    }else{
	    	print "\n""insert into beidouext.unionsiteinfos values " >> sqlFile
	    	print "("$1",\""$2"\",""\""$3"\",""\""$4"\","$5","$6","$7","$8","$9","$10",\""$11"\",""\""$12"\" ),"  >> sqlFile
			count = 1;    
	    }
	    
	    firstExeTime +=1;
	} END{print "0);\n" >> sqlFile }' "${UNIONSITEINFOS_FILE}"
fi

RET_CODE=$?
if [ $RET_CODE -ne 0 ];then
	msg="�������ݿ��beidou.${UNIONSITEINFOS_TABLE_NAME}ʧ�ܣ������ѻָ���ͼ�ļ�"	
	rollback_file
	ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
	exit 1
fi
echo_msg "Step9. ��ʼ�������ݱ�beidou.${UNIONSITEINFOS_TABLE_NAME}�ɹ�"

# Step10. �������ݿ�
echo_msg "Step10. ��ʼ�������ݱ�beidou.${UNIONSITEINFOS_TABLE_NAME}��ʼ"
runsql_xdb "`cat ${CRON_SNAPSHOT_TO_BE_EXECSQL_PATH}/${OFFLINE_SNAPSHOT_SQL_FROM_FILE_NAME}`"

RET_CODE=$?
if [ $RET_CODE -ne 0 ];then
	msg="�������ݿ��beidou.${UNIONSITEINFOS_TABLE_NAME}ʧ�ܣ������ѻָ���ͼ�ļ������ݿ��ѻع�"	
	rollback_file
	rollback_db
	ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
	exit 1
fi
echo_msg "Step10. ��ʼ�������ݱ�beidou.${UNIONSITEINFOS_TABLE_NAME}��ʼ"


endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

echo_msg "================================================================="
echo_msg "= ����${YESTERDAY}���񣻽���ʱ�䣺 `date +"%Y-%m-%d_%H:%M:%S"`, ����ʱ��${spendtime}s"
echo_msg "================================================================="


exit 0
