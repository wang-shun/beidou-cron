#!/bin/sh

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/interestSync_sh.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=interestSync.sh
reader_list=tianxin

# 如果不存在log目录则创建log目录
echo $logPath
if [ ! -d $logPath ]; then
	mkdir -p $logPath
fi

# 如果不存在历史目录，则创建历史目录
echo $history
if [ ! -d $history ]; then
	echo "$history does not exist! now create it." >> $logFile
	mkdir -p $history
fi

echo $work
# 如果不存在工作目录，则创建工作目录
if [ ! -d $work ]; then
	echo "$work does not exist! now create it." >> $logFile
	mkdir -p $work
fi

# 进入工作目录进行工作
cd $work
echo "cd in $work" >> $logFile
rm -rf interest*

echo $ftpList
# 下载list文件，如果不存在则报错退出
wget $ftpList 
alert $? "下载 $ftpList 失败"
echo "wget $ftpList success" >> $logFile

# 下载MD5文件，如果不存在则报错退出
wget $ftpMd5
alert $? "下载 $ftpMd5 失败"
echo "wget $ftpMd5 success" >> $logFile

# 验证文件是否正确下载
md5sum -c $md5
alert $? "it提供的MD5与兴趣字典文件不匹配"
echo "check downloaded file with downloaded md5 success" >> $logFile

# 如果有历史文件，则验证历史文件是否有变化
if [ -f "$history/$list" ]; then
	echo "$hitory/$list exists , so check it with the new downloaded md5" >> $logFile
    mv $list $list.1
    cp "$history/$list" ./
    md5sum -c $md5

    # 如果没有变化则直接突出
    if [ $? -eq 0 ]; then
        echo "interest file do not change" >> $logFile
        rm -rf *
        exit 0
    fi

    #如果有变化则恢复工作目录为md5与list两个文件
    echo "interest file has changed , load the interest infomation into database" >> $logFile
    rm -f $list
    mv $list.1 $list
fi
  
# 如果文件有差别，则运行python程序merge新的文件与db之间的区别
cd $curDir
echo "execute python script to sync interest" >> $logFile
python interestSync.py $work/$list $pythonConf $logFile >> $logFile

# 如果python执行失败，则报错退出
alert $? "执行python程序入库兴趣字典出错"

#刷新缓存
for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; do
    sh ./interestCacheReload.sh $server >> $logFile
    alert $? "刷新WEB兴趣缓存失败[$server]"
done

for server in `echo ${EXP_WEB_SERVER_IP_PORT_LIST[@]}`; do
     sh ./interestCacheReload.sh $server >> $logFile
     alert $? "刷新小流量WEB兴趣缓存失败[$server]"
done

for server in `echo ${APIV2_SERVER_IP_PORT_LIST[@]}`; do
    sh ./interestCacheReload.sh $server >> $logFile
    alert $? "刷新API兴趣缓存失败[$server]"
done

# 如果python执行成功，则将新的文件转移到历史目录下
mv -f $work/$list $work/$md5 $history/
rm -f $work/*
echo "success to load new interest file" >> $logFile
echo "load interest file end" >> $logFile
exit 0
