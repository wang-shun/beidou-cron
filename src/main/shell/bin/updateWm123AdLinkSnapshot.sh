#!/bin/bash
#@file: updateWm123AdLinkSnapshot.sh
#@author: zhangxu04
#@date: 2011-04-26
#@version: 1.0.0.0
#@brief: Copy ad link snapshot image zip from offline server and put it on online servers. 
#no argument need

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=updateWm123AdLinkSnapshot.sh
reader_list=zhangxu04

#--------------- var --------------

WM123_SERVER_IP_LIST=($tcmgr00 $tcmgr01 $aimgr00 $aimgr01)

# ��������ڣ���ʽΪYYmmdd�������ݶ�����Ϊ���죬�����Ͻ��첿��Ķ����������ߣ����Խ�����Կ��������족
YESTERDAY=`date +%Y%m%d`

######################################################################################################
# �ļ��ṹ˵��
# ${DATA_PATH}/wm123/previewshot/                             <------- �����ļ���
#                               /online_sync                  <------- ͬ������previewshotĿ¼  <���ã�Ŀǰ���������ϵ�beidou-cron�ı��ݲ���>
#                               /temp/tmp                     <------- ��ʱ�ļ������wget��׼�����Ϣ
#                               /20110405/                    <------- �Ը����ļ������������ļ���
#                                        /previewshot         <------- Ҫ���µ��ļ�
#                                        /20110405.bak.tar    <------- �������ڵ�������previewshot����ѹ���� <���ã�Ŀǰ���������ϵ�beidou-cron�ı��ݲ���>
#
#######################################################################################################
CRON_PREVIEWSHOT_BASE_PATH=${DATA_PATH}/wm123/previewshot
CRON_PREVIEWSHOT_ONLINE_SYNC_PATH=$CRON_PREVIEWSHOT_BASE_PATH/online_sync
CRON_PREVIEWSHOT_WORK_PATH=$CRON_PREVIEWSHOT_BASE_PATH/$YESTERDAY
CRON_PREVIEWSHOT_TO_BE_SYNC_PATH=$CRON_PREVIEWSHOT_WORK_PATH/previewshot
CRON_PREVIEWSHOT_TEMP_PATH=$CRON_PREVIEWSHOT_BASE_PATH/temp
CRON_PREVIEWSHOT_TEMP_FILE=$CRON_PREVIEWSHOT_TEMP_PATH/tmp
LOG_FILE=${LOG_PATH}/updateWM123AdLinkSnapshot.log

# ���ϻ�����WM123 app�����·���Լ���ͼ�����·��
ONLINE_WM123_WEB_APP_BASE_PATH="/home/work/wm123-web"
ONLINE_PREVIEWSHOT_DEST_PATH=$ONLINE_WM123_WEB_APP_BASE_PATH"/previewshot"
ONLINE_USER="work"
ONLINE_BACKUP_PATH=/home/work/opdir/backup/wm123-web.previewshot.$YESTERDAY

# ���»�ȡ��ͼѹ�������»���hostname��·��
OFFLINE_PREVIEWSHOT_FROM_SERVER="tc-et-cpro01.tc.baidu.com"
OFFLINE_PREVIEWSHOT_FROM_FILE_NAME="previewshot_"$YESTERDAY".zip"
OFFLINE_PREVIEWSHOT_FROM_PATH="/home/work/zhangxu/online/previewshot/"$OFFLINE_PREVIEWSHOT_FROM_FILE_NAME

#--------------- function  --------------

##! @TODO: ��������
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 0 ������гɹ�������code=0
function env_setup()
{
	if [ ! -e "$CRON_PREVIEWSHOT_BASE_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_BASE_PATH
	fi
	
	#if [ ! -e "$CRON_PREVIEWSHOT_ONLINE_SYNC_PATH" ];then
	#	mkdir -p $CRON_PREVIEWSHOT_ONLINE_SYNC_PATH
	#fi
	
	if [ -e "$CRON_PREVIEWSHOT_WORK_PATH" ];then
		rm -rf $CRON_PREVIEWSHOT_WORK_PATH
	fi
	
	# �½������ļ���
	if [ ! -e "$CRON_PREVIEWSHOT_WORK_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_WORK_PATH
	fi
	
	if [ ! -e "$CRON_PREVIEWSHOT_TO_BE_SYNC_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
	fi
	
	if [ ! -e "$CRON_PREVIEWSHOT_TEMP_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_TEMP_PATH
	fi
	
	# �����ʱ�ļ�
	echo >  $CRON_PREVIEWSHOT_TEMP_FILE
	
	return 0
}

##! @TODO: ������ͼ��������ʱ���ع�����
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function rollback()
{
	for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
		msg="�ع���ͼ�������ļ�${server}ʧ��"	
		#rsync -auzv --delete -e ssh ${CRON_PREVIEWSHOT_ONLINE_SYNC_PATH}/previewshot ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH >>$LOG_FILE
		ssh ${server} "cd $ONLINE_PREVIEWSHOT_DEST_PATH && rm -rf * && mkdir -p $ONLINE_PREVIEWSHOT_DEST_PATH && cp -r $ONLINE_BACKUP_PATH/* $ONLINE_PREVIEWSHOT_DEST_PATH/ "
		RET_CODE=$?
		if [ $RET_CODE -ne 0 ];then
			ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
		fi
	done
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


# Step2. ��ȡ�ƹ�λ���ӽ�ͼѹ������������ھ�wget����������ڱ�ʾû�и������󣬽ű��˳�
echo_msg "Step2. ��ȡ�ƹ�λ���ӽ�ͼѹ������ʼ��wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH}��ʼ"
cd $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}��ȡʧ��"
wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH} --output-file=$CRON_PREVIEWSHOT_TEMP_FILE
RET_CODE=$?
if [ $RET_CODE -ne 0 ] ;then
    # ���������Ϣ����No such file����ʾ�������������⣬ֱ�ӱ����˳�
    grep "No such file" $CRON_PREVIEWSHOT_TEMP_FILE
    alert $? "${msg}"
	echo_msg "ʱ�䣺$YESTERDAY���ļ�${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}�����ڣ�û���κθ���" 
	# ɾ���ļ�
	rm -f ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}
	rmdir $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
	rmdir $CRON_PREVIEWSHOT_WORK_PATH
	echo_msg "======================================="
	echo_msg "= ����${YESTERDAY}����"
	echo_msg "======================================="
	exit 0
fi
echo_msg "Step2. ��ȡ�ƹ�λ���ӽ�ͼѹ�����ɹ���wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH}�ɹ�"


# Step3. ��ȡ�ƹ�λ���ӽ�ͼѹ����md5�����
echo_msg "Step3. ��ȡ�ƹ�λ���ӽ�ͼѹ����md5����鿪ʼ"
cd $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}.md5��ȡʧ��"
wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH}.md5 --output-file=$CRON_PREVIEWSHOT_TEMP_FILE
alert $? "${msg}"
msg="ʱ�䣺$YESTERDAY���ļ�${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}��md5���ʧ��"
md5sum -c ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}.md5 > /dev/null
alert $? "${msg}"
rm -f ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}.md5
echo_msg "Step3. ��ȡ�ƹ�λ���ӽ�ͼѹ����md5�����ɹ�"


# Step4. ��ѹ��ͼѹ����
echo_msg "Step4. ��ѹ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}��ʼ"
unzip -o ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}
cp ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME} ..
rm -f ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}
echo_msg "Step4. ��ѹ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}�ɹ�"


# Step5. ���������ƹ�λ���ӽ�ͼ�ļ�
# ���ã�Ŀǰ���������ϵ�beidou-cron�ı��ݲ���
#echo_msg "Step4. ͬ������${tcmgr00}:${ONLINE_PREVIEWSHOT_DEST_PATH}�ļ���$CRON_PREVIEWSHOT_ONLINE_SYNC_PATH��ʼ"
#msg="ͬ�������ƹ�λ���ӽ�ͼ�ļ�ʧ��;ͬ��serverΪwork@tc-beidou-mgr00.tc.baidu.com"
#rsync -auzv --delete -e ssh ${tcmgr00}:$ONLINE_PREVIEWSHOT_DEST_PATH $CRON_PREVIEWSHOT_ONLINE_SYNC_PATH >>$LOG_FILE
#alert $? "${msg}"
#echo_msg "Step5. ͬ������${tcmgr00}:${ONLINE_PREVIEWSHOT_DEST_PATH}�ļ���$CRON_PREVIEWSHOT_ONLINE_SYNC_PATH�ɹ�"

# Step5. ���������ļ�
echo_msg "Step5. ��ʼ����..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	msg="��������${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}�ļ���${ONLINE_BACKUP_PATH}ʧ��"
	ssh ${server} "mkdir -p $ONLINE_BACKUP_PATH && cp -r $ONLINE_PREVIEWSHOT_DEST_PATH/* $ONLINE_BACKUP_PATH/"
	alert $? "${msg}"
	echo_msg "��������${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}�ļ���${ONLINE_BACKUP_PATH}�ɹ�"
done
echo_msg "Step5. ���б������"


# Step6. ͬ���������ļ�
echo_msg "Step6. ��ʼͬ������..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	echo_msg "ͬ���½�ͼ${CRON_PREVIEWSHOT_TO_BE_SYNC_PATH} ��${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}��ʼ"
	msg="ͬ���½�ͼ${CRON_PREVIEWSHOT_TO_BE_SYNC_PATH} �������ļ�${server}ʧ�ܣ������ѻع�"	
    rsync -azv --delete -e ssh $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH/ >>$LOG_FILE
	RET_CODE=$?
	if [ $RET_CODE -ne 0 ];then
		rollback
		ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
	fi
	echo_msg "ͬ���½�ͼ$CRON_PREVIEWSHOT_TO_BE_SYNC_PATH ��${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}�ɹ�"
done
echo_msg "Step6. ����ͬ���������"

endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

echo_msg "================================================================="
echo_msg "= ����${YESTERDAY}���񣻽���ʱ�䣺 `date +"%Y-%m-%d_%H:%M:%S"`, ����ʱ��${spendtime}s"
echo_msg "================================================================="

exit 0
