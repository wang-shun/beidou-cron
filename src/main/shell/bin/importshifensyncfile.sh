#!/bin/sh

source statconf.conf
source costconf.conf
source classpath_recommend.conf

if [ ! -f ${PERFUND_FB_LOCAL_PATH} ]; then
	mkdir -p ${PERFUND_FB_LOCAL_PATH}
fi

if [ ! -f ${PERFUND_FB_BACKUP_PATH} ]; then 
    mkdir -p ${PERFUND_FB_BACKUP_PATH}
fi

#备份本地的文件

cd ${PERFUND_FB_LOCAL_PATH}
mv * ${PERFUND_FB_BACKUP_PATH}

#抓取日期为昨天和今天的两个反馈接口文件，如果某个文件或者md5不存在，则直接给出报警，不继续执行，如果md5校验不通过，也停止
wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_MD5}

ifError $? "下载今日转账反馈文件的md5文件失败！"
wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_YESTERDAY_MD5}

ifError $? "下载前日转账反馈文件的md5文件失败！"

wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_NAME}
ifError $? "下载今日转账反馈文件失败！"

wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_YESTERDAY_NAME}
ifError $? "下载前日转账反馈文件失败！"

md5sum -c ${PERFUND_FB_FILE_MD5}
ifError $? "今日转账反馈文件md5校验失败"
md5sum -c ${PERFUND_FB_FILE_YESTERDAY_MD5}
ifError $? "前日转账反馈文件md5校验失败"

#导入结果文件并更新数据库
cd ${HOME_PATH}

java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.ImportShifenSync >> ${LOG_FILE}

ifError $? "导入转账反馈文件失败"
