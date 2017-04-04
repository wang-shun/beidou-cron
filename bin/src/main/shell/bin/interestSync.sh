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

# ���������logĿ¼�򴴽�logĿ¼
echo $logPath
if [ ! -d $logPath ]; then
	mkdir -p $logPath
fi

# �����������ʷĿ¼���򴴽���ʷĿ¼
echo $history
if [ ! -d $history ]; then
	echo "$history does not exist! now create it." >> $logFile
	mkdir -p $history
fi

echo $work
# ��������ڹ���Ŀ¼���򴴽�����Ŀ¼
if [ ! -d $work ]; then
	echo "$work does not exist! now create it." >> $logFile
	mkdir -p $work
fi

# ���빤��Ŀ¼���й���
cd $work
echo "cd in $work" >> $logFile
rm -rf interest*

echo $ftpList
# ����list�ļ�������������򱨴��˳�
wget $ftpList 
alert $? "���� $ftpList ʧ��"
echo "wget $ftpList success" >> $logFile

# ����MD5�ļ�������������򱨴��˳�
wget $ftpMd5
alert $? "���� $ftpMd5 ʧ��"
echo "wget $ftpMd5 success" >> $logFile

# ��֤�ļ��Ƿ���ȷ����
md5sum -c $md5
alert $? "it�ṩ��MD5����Ȥ�ֵ��ļ���ƥ��"
echo "check downloaded file with downloaded md5 success" >> $logFile

# �������ʷ�ļ�������֤��ʷ�ļ��Ƿ��б仯
if [ -f "$history/$list" ]; then
	echo "$hitory/$list exists , so check it with the new downloaded md5" >> $logFile
    mv $list $list.1
    cp "$history/$list" ./
    md5sum -c $md5

    # ���û�б仯��ֱ��ͻ��
    if [ $? -eq 0 ]; then
        echo "interest file do not change" >> $logFile
        rm -rf *
        exit 0
    fi

    #����б仯��ָ�����Ŀ¼Ϊmd5��list�����ļ�
    echo "interest file has changed , load the interest infomation into database" >> $logFile
    rm -f $list
    mv $list.1 $list
fi
  
# ����ļ��в��������python����merge�µ��ļ���db֮�������
cd $curDir
echo "execute python script to sync interest" >> $logFile
python interestSync.py $work/$list $pythonConf $logFile >> $logFile

# ���pythonִ��ʧ�ܣ��򱨴��˳�
alert $? "ִ��python���������Ȥ�ֵ����"

#ˢ�»���
for server in `echo ${WEB_SERVER_IP_PORT_LIST[@]}`; do
    sh ./interestCacheReload.sh $server >> $logFile
    alert $? "ˢ��WEB��Ȥ����ʧ��[$server]"
done

for server in `echo ${EXP_WEB_SERVER_IP_PORT_LIST[@]}`; do
     sh ./interestCacheReload.sh $server >> $logFile
     alert $? "ˢ��С����WEB��Ȥ����ʧ��[$server]"
done

for server in `echo ${APIV2_SERVER_IP_PORT_LIST[@]}`; do
    sh ./interestCacheReload.sh $server >> $logFile
    alert $? "ˢ��API��Ȥ����ʧ��[$server]"
done

# ���pythonִ�гɹ������µ��ļ�ת�Ƶ���ʷĿ¼��
mv -f $work/$list $work/$md5 $history/
rm -f $work/*
echo "success to load new interest file" >> $logFile
echo "load interest file end" >> $logFile
exit 0
