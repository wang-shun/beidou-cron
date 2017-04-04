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

# 昨天的日期，格式为YYmmdd，这里暂定今天为昨天，意义上今天部署的东西明天上线，所以今天可以看做“昨天”
YESTERDAY=`date +%Y%m%d`

######################################################################################################
# 文件结构说明
# ${DATA_PATH}/wm123/snapshot/                             <------- 工作文件夹
#                            /temp/tmp                     <------- 临时文件，存放wget标准输出信息
#                            /20110405/                    <------- 以更新文件日期命名的文件夹
#                                     /snapshot            <------- 要更新的文件
#                                     /sql                 <------- 要执行的sql脚本
#                                     /backup              <------- 数据库备份sql存储路径
#
#######################################################################################################
#--------------- function  --------------

##! @TODO: 环境清理
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 0 如果运行成功，返回code=0
function env_setup()
{
	if [ ! -e "$CRON_SNAPSHOT_BASE_PATH" ];then
		mkdir -p $CRON_SNAPSHOT_BASE_PATH
	fi
	
	if [ -e "$CRON_SNAPSHOT_WORK_PATH" ];then
		rm -rf $CRON_SNAPSHOT_WORK_PATH
	fi
	
	# 新建下载文件夹
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
	
	# 清空临时文件
	echo >  $CRON_SNAPSHOT_TEMP_FILE
	
	return 0
}

##! @TODO: 拷贝截图发生错误时，文件回滚操作
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function rollback_file()
{
	for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
		msg_rb="更新截图发生错误，回滚操作启动，恢复截图到线上文件${server}失败"	
		#rsync -auzv --delete -e ssh ${CRON_SNAPSHOT_ONLINE_SYNC_PATH}/snapshot ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH >>$LOG_FILE
		ssh ${server} "rm -rf $ONLINE_SNAPSHOT_DEST_PATH && cp -r $ONLINE_BACKUP_PATH/snapshot $ONLINE_SNAPSHOT_DEST_PATH "
		RET_CODE_RB=$?
		if [ $RET_CODE_RB -ne 0 ];then
			ifError "${RET_CODE_RB}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg_rb}" "${reader_list}"
		fi
	done
}

##! @TODO: 拷贝截图发生错误时，数据库回滚操作
##! @AUTHOR: zhangxu
##! @VERSION: 1.0
##! @IN: 
##! @OUT: 
function rollback_db()
{
	msg="更新截图发生错误，回滚操作启动，但是恢复数据库表beidou.${UNIONSITEINFOS_TABLE_NAME}失败"
	runsql_xdb "`cat ${CRON_SNAPSHOT_BACKUP_FILE}`"
    alert $? "${msg}"
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


# Step2. 获取站点截图压缩包，如果存在就wget，如果不存在表示没有更新请求，脚本退出
echo_msg "Step2. 获取站点截图压缩包开始，wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH}开始"
cd $CRON_SNAPSHOT_TO_BE_SYNC_PATH
msg="时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}获取失败"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH} --output-file=$CRON_SNAPSHOT_TEMP_FILE
RET_CODE=$?
if [ $RET_CODE -ne 0 ] ;then
    # 如果错误信息不是No such file，表示下载遇到了问题，直接报警退出
    grep "No such file" $CRON_SNAPSHOT_TEMP_FILE
    alert $? "${msg}"
	echo_msg "时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}不存在，没有任何更新" 
	# 删除文件
	rm -f ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}
	rmdir $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
	rmdir $CRON_SNAPSHOT_BACKUP_PATH
	rmdir $CRON_SNAPSHOT_TO_BE_SYNC_PATH
	rmdir $CRON_SNAPSHOT_WORK_PATH
	echo_msg "======================================="
	echo_msg "= 结束${YESTERDAY}任务"
	echo_msg "======================================="
	exit 0
fi
echo_msg "Step2. 获取站点截图压缩包成功，wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH}成功"


# Step3. 获取站点截图压缩包md5并检查
echo_msg "Step3. 获取站点截图压缩包md5并检查开始"
cd $CRON_SNAPSHOT_TO_BE_SYNC_PATH
msg="时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}.md5获取失败"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_FROM_PATH}.md5 --output-file=$CRON_SNAPSHOT_TEMP_FILE
alert $? "${msg}"
msg="时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_FROM_PATH}：md5检查失败"
md5sum -c ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}.md5 > /dev/null
alert $? "${msg}"
rm -f ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}.md5
echo_msg "Step3. 获取站点截图压缩包md5并检查成功"


# Step4. 获取站点截图执行SQL
echo_msg "Step4. 获取站点截图执行SQL脚本开始，wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH}开始"
cd $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
msg="时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_SQL_FROM_PATH}获取失败"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH} --output-file=$CRON_SNAPSHOT_TEMP_FILE
alert $? "${msg}"
echo_msg "Step4. 获取站点截图执行SQL脚本开始，wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH}成功"


# Step5. 获取站点截图执行SQL的md5文件并检查
echo_msg "Step5. 获取站点截图执行SQL的md5文件并检查开始"
cd $CRON_SNAPSHOT_TO_BE_EXECSQL_PATH
msg="时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_SQL_FROM_PATH}.md5获取失败"
wget ftp://${OFFLINE_SNAPSHOT_FROM_SERVER}${OFFLINE_SNAPSHOT_SQL_FROM_PATH}.md5 --output-file=$CRON_SNAPSHOT_TEMP_FILE
alert $? "${msg}"
msg="时间：$YESTERDAY。文件${OFFLINE_SNAPSHOT_FROM_SERVER}/${OFFLINE_SNAPSHOT_SQL_FROM_PATH}：md5检查失败"
md5sum -c ${OFFLINE_SNAPSHOT_SQL_FROM_FILE_NAME}.md5 > /dev/null
alert $? "${msg}"
rm -f ${OFFLINE_SNAPSHOT_SQL_FROM_FILE_NAME}.md5
echo_msg "Step5. 获取站点截图执行SQL的md5文件并检查成功"


# Step6. 解压截图压缩包
echo_msg "Step6. 解压${OFFLINE_SNAPSHOT_FROM_FILE_NAME}开始"
cd $CRON_SNAPSHOT_TO_BE_SYNC_PATH
unzip -o ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}
msg="snapshot.zip压缩包文件格式不符，压缩包应直接包含jpg文件"
ls *.jpg
alert $? "${msg}"
cp ${OFFLINE_SNAPSHOT_FROM_FILE_NAME} ..
rm -f ${OFFLINE_SNAPSHOT_FROM_FILE_NAME}
echo_msg "Step6. 解压${OFFLINE_SNAPSHOT_FROM_FILE_NAME}成功"


# Step7. 备份线上文件
echo_msg "Step7. 开始备份..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	msg="备份线上${server}:${ONLINE_SNAPSHOT_DEST_PATH}文件到${ONLINE_BACKUP_PATH}失败"
	ssh ${server} "mkdir -p $ONLINE_BACKUP_PATH; cp -r $ONLINE_SNAPSHOT_DEST_PATH/ $ONLINE_BACKUP_PATH/"
	alert $? "${msg}"
	echo_msg "备份线上${server}:${ONLINE_SNAPSHOT_DEST_PATH}文件到${ONLINE_BACKUP_PATH}成功"
done
echo_msg "Step7. 所有备份完成"


# Step8. 同步到线上文件
echo_msg "Step8. 开始更新截图文件..."
for server in `echo ${WM123_SERVER_IP_LIST[@]}`; do
	echo_msg "同步新截图${CRON_SNAPSHOT_TO_BE_SYNC_PATH} 到${server}:${ONLINE_SNAPSHOT_DEST_PATH}开始"
	msg="同步新截图${CRON_SNAPSHOT_TO_BE_SYNC_PATH}到线上文件${server}失败,线上已回滚"	
    rsync -azv -e ssh $CRON_SNAPSHOT_TO_BE_SYNC_PATH ${server}:$ONLINE_WM123_WEB_APP_BASE_PATH/ >>$LOG_FILE
	RET_CODE=$?
	if [ $RET_CODE -ne 0 ];then
		rollback_file
		ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
		exit 1
	fi
	echo_msg "同步新截图$CRON_SNAPSHOT_TO_BE_SYNC_PATH 到${server}:${ONLINE_SNAPSHOT_DEST_PATH}成功"
done
echo_msg "Step8. 所有截图文件更新完成"


# Step9. 备份数据库表
echo_msg "Step9. 开始备份数据表beidou.${UNIONSITEINFOS_TABLE_NAME}开始"
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
	msg="备份数据库表beidou.${UNIONSITEINFOS_TABLE_NAME}失败，线上已恢复截图文件"	
	rollback_file
	ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
	exit 1
fi
echo_msg "Step9. 开始备份数据表beidou.${UNIONSITEINFOS_TABLE_NAME}成功"

# Step10. 更新数据库
echo_msg "Step10. 开始更新数据表beidou.${UNIONSITEINFOS_TABLE_NAME}开始"
runsql_xdb "`cat ${CRON_SNAPSHOT_TO_BE_EXECSQL_PATH}/${OFFLINE_SNAPSHOT_SQL_FROM_FILE_NAME}`"

RET_CODE=$?
if [ $RET_CODE -ne 0 ];then
	msg="更新数据库表beidou.${UNIONSITEINFOS_TABLE_NAME}失败，线上已恢复截图文件，数据库已回滚"	
	rollback_file
	rollback_db
	ifError "${RET_CODE}" "[${type}][${module}]${msg}@`date +%F\ %T`" "${program}" "${msg}" "${reader_list}"
	exit 1
fi
echo_msg "Step10. 开始更新数据表beidou.${UNIONSITEINFOS_TABLE_NAME}开始"


endMills=`date +"%s"`
spendtime=$(($endMills-$startMills))

echo_msg "================================================================="
echo_msg "= 结束${YESTERDAY}任务；结束时间： `date +"%Y-%m-%d_%H:%M:%S"`, 共用时：${spendtime}s"
echo_msg "================================================================="


exit 0
