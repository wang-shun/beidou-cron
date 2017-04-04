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

# 昨天的日期，格式为YYmmdd，这里暂定今天为昨天，意义上今天部署的东西明天上线，所以今天可以看做“昨天”
YESTERDAY=`date +%Y%m%d`

######################################################################################################
# 文件结构说明
# ${DATA_PATH}/wm123/previewshot/                             <------- 工作文件夹
#                               /online_sync                  <------- 同步线上previewshot目录  <弃用，目前不采用线上到beidou-cron的备份策略>
#                               /temp/tmp                     <------- 临时文件，存放wget标准输出信息
#                               /20110405/                    <------- 以更新文件日期命名的文件夹
#                                        /previewshot         <------- 要更新的文件
#                                        /20110405.bak.tar    <------- 更新日期当日线上previewshot备份压缩包 <弃用，目前不采用线上到beidou-cron的备份策略>
#
#######################################################################################################
CRON_PREVIEWSHOT_BASE_PATH=${DATA_PATH}/wm123/previewshot
CRON_PREVIEWSHOT_ONLINE_SYNC_PATH=$CRON_PREVIEWSHOT_BASE_PATH/online_sync
CRON_PREVIEWSHOT_WORK_PATH=$CRON_PREVIEWSHOT_BASE_PATH/$YESTERDAY
CRON_PREVIEWSHOT_TO_BE_SYNC_PATH=$CRON_PREVIEWSHOT_WORK_PATH/previewshot
CRON_PREVIEWSHOT_TEMP_PATH=$CRON_PREVIEWSHOT_BASE_PATH/temp
CRON_PREVIEWSHOT_TEMP_FILE=$CRON_PREVIEWSHOT_TEMP_PATH/tmp
LOG_FILE=${LOG_PATH}/updateWM123AdLinkSnapshot.log

# 线上机器上WM123 app部署的路径以及截图保存的路径
ONLINE_WM123_WEB_APP_BASE_PATH="/home/work/wm123-web"
ONLINE_PREVIEWSHOT_DEST_PATH=$ONLINE_WM123_WEB_APP_BASE_PATH"/previewshot"
ONLINE_USER="work"
ONLINE_BACKUP_PATH=/home/work/opdir/backup/wm123-web.previewshot.$YESTERDAY

# 线下获取截图压缩包线下机器hostname，路径
OFFLINE_PREVIEWSHOT_FROM_SERVER="tc-et-cpro01.tc.baidu.com"
OFFLINE_PREVIEWSHOT_FROM_FILE_NAME="previewshot_"$YESTERDAY".zip"
OFFLINE_PREVIEWSHOT_FROM_PATH="/home/work/zhangxu/online/previewshot/"$OFFLINE_PREVIEWSHOT_FROM_FILE_NAME

#--------------- function  --------------

##! @TODO: 环境清理
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 0 如果运行成功，返回code=0
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
	
	# 新建下载文件夹
	if [ ! -e "$CRON_PREVIEWSHOT_WORK_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_WORK_PATH
	fi
	
	if [ ! -e "$CRON_PREVIEWSHOT_TO_BE_SYNC_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
	fi
	
	if [ ! -e "$CRON_PREVIEWSHOT_TEMP_PATH" ];then
		mkdir -p $CRON_PREVIEWSHOT_TEMP_PATH
	fi
	
	# 清空临时文件
	echo >  $CRON_PREVIEWSHOT_TEMP_FILE
	
	return 0
}

##! @TODO: 拷贝截图发生错误时，回滚操作
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function rollback()
{
	for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
		msg="回滚截图到线上文件${server}失败"	
		#rsync -auzv --delete -e ssh ${CRON_PREVIEWSHOT_ONLINE_SYNC_PATH}/previewshot ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH >>$LOG_FILE
		ssh ${server} "cd $ONLINE_PREVIEWSHOT_DEST_PATH && rm -rf * && mkdir -p $ONLINE_PREVIEWSHOT_DEST_PATH && cp -r $ONLINE_BACKUP_PATH/* $ONLINE_PREVIEWSHOT_DEST_PATH/ "
		RET_CODE=$?
		if [ $RET_CODE -ne 0 ];then
			ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
		fi
	done
}

##! @TODO: 打印输出提示
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
echo_msg "= 开始${YESTERDAY}任务"
echo_msg "======================================="


# Step1. 环境初始化
echo_msg "Step1. 环境初始化开始"
env_setup
echo_msg "Step1. 环境初始化成功"


# Step2. 获取推广位链接截图压缩包，如果存在就wget，如果不存在表示没有更新请求，脚本退出
echo_msg "Step2. 获取推广位链接截图压缩包开始，wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH}开始"
cd $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
msg="时间：$YESTERDAY。文件${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}获取失败"
wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH} --output-file=$CRON_PREVIEWSHOT_TEMP_FILE
RET_CODE=$?
if [ $RET_CODE -ne 0 ] ;then
    # 如果错误信息不是No such file，表示下载遇到了问题，直接报警退出
    grep "No such file" $CRON_PREVIEWSHOT_TEMP_FILE
    alert $? "${msg}"
	echo_msg "时间：$YESTERDAY。文件${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}不存在，没有任何更新" 
	# 删除文件
	rm -f ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}
	rmdir $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
	rmdir $CRON_PREVIEWSHOT_WORK_PATH
	echo_msg "======================================="
	echo_msg "= 结束${YESTERDAY}任务"
	echo_msg "======================================="
	exit 0
fi
echo_msg "Step2. 获取推广位链接截图压缩包成功，wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH}成功"


# Step3. 获取推广位链接截图压缩包md5并检查
echo_msg "Step3. 获取推广位链接截图压缩包md5并检查开始"
cd $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH
msg="时间：$YESTERDAY。文件${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}.md5获取失败"
wget ftp://${OFFLINE_PREVIEWSHOT_FROM_SERVER}${OFFLINE_PREVIEWSHOT_FROM_PATH}.md5 --output-file=$CRON_PREVIEWSHOT_TEMP_FILE
alert $? "${msg}"
msg="时间：$YESTERDAY。文件${OFFLINE_PREVIEWSHOT_FROM_SERVER}/${OFFLINE_PREVIEWSHOT_FROM_PATH}：md5检查失败"
md5sum -c ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}.md5 > /dev/null
alert $? "${msg}"
rm -f ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}.md5
echo_msg "Step3. 获取推广位链接截图压缩包md5并检查成功"


# Step4. 解压截图压缩包
echo_msg "Step4. 解压${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}开始"
unzip -o ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}
cp ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME} ..
rm -f ${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}
echo_msg "Step4. 解压${OFFLINE_PREVIEWSHOT_FROM_FILE_NAME}成功"


# Step5. 备份线上推广位链接截图文件
# 弃用，目前不采用线上到beidou-cron的备份策略
#echo_msg "Step4. 同步线上${tcmgr00}:${ONLINE_PREVIEWSHOT_DEST_PATH}文件到$CRON_PREVIEWSHOT_ONLINE_SYNC_PATH开始"
#msg="同步线上推广位链接截图文件失败;同步server为work@tc-beidou-mgr00.tc.baidu.com"
#rsync -auzv --delete -e ssh ${tcmgr00}:$ONLINE_PREVIEWSHOT_DEST_PATH $CRON_PREVIEWSHOT_ONLINE_SYNC_PATH >>$LOG_FILE
#alert $? "${msg}"
#echo_msg "Step5. 同步线上${tcmgr00}:${ONLINE_PREVIEWSHOT_DEST_PATH}文件到$CRON_PREVIEWSHOT_ONLINE_SYNC_PATH成功"

# Step5. 备份线上文件
echo_msg "Step5. 开始备份..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	msg="备份线上${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}文件到${ONLINE_BACKUP_PATH}失败"
	ssh ${server} "mkdir -p $ONLINE_BACKUP_PATH && cp -r $ONLINE_PREVIEWSHOT_DEST_PATH/* $ONLINE_BACKUP_PATH/"
	alert $? "${msg}"
	echo_msg "备份线上${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}文件到${ONLINE_BACKUP_PATH}成功"
done
echo_msg "Step5. 所有备份完成"


# Step6. 同步到线上文件
echo_msg "Step6. 开始同步更新..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	echo_msg "同步新截图${CRON_PREVIEWSHOT_TO_BE_SYNC_PATH} 到${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}开始"
	msg="同步新截图${CRON_PREVIEWSHOT_TO_BE_SYNC_PATH} 到线上文件${server}失败，线上已回滚"	
    rsync -azv --delete -e ssh $CRON_PREVIEWSHOT_TO_BE_SYNC_PATH ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH/ >>$LOG_FILE
	RET_CODE=$?
	if [ $RET_CODE -ne 0 ];then
		rollback
		ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
	fi
	echo_msg "同步新截图$CRON_PREVIEWSHOT_TO_BE_SYNC_PATH 到${server}:${ONLINE_PREVIEWSHOT_DEST_PATH}成功"
done
echo_msg "Step6. 所有同步更新完成"

endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

echo_msg "================================================================="
echo_msg "= 结束${YESTERDAY}任务；结束时间： `date +"%Y-%m-%d_%H:%M:%S"`, 共用时：${spendtime}s"
echo_msg "================================================================="

exit 0
