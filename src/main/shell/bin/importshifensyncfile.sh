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

#���ݱ��ص��ļ�

cd ${PERFUND_FB_LOCAL_PATH}
mv * ${PERFUND_FB_BACKUP_PATH}

#ץȡ����Ϊ����ͽ�������������ӿ��ļ������ĳ���ļ�����md5�����ڣ���ֱ�Ӹ���������������ִ�У����md5У�鲻ͨ����Ҳֹͣ
wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_MD5}

ifError $? "���ؽ���ת�˷����ļ���md5�ļ�ʧ�ܣ�"
wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_YESTERDAY_MD5}

ifError $? "����ǰ��ת�˷����ļ���md5�ļ�ʧ�ܣ�"

wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_NAME}
ifError $? "���ؽ���ת�˷����ļ�ʧ�ܣ�"

wget -q ftp://${PERFUND_FB_USER}:${PERFUND_FB_PASSWORD}@${PERFUND_FB_SERVER}/${PERFUND_FB_REMOTE_PATH}/${PERFUND_FB_FILE_YESTERDAY_NAME}
ifError $? "����ǰ��ת�˷����ļ�ʧ�ܣ�"

md5sum -c ${PERFUND_FB_FILE_MD5}
ifError $? "����ת�˷����ļ�md5У��ʧ��"
md5sum -c ${PERFUND_FB_FILE_YESTERDAY_MD5}
ifError $? "ǰ��ת�˷����ļ�md5У��ʧ��"

#�������ļ����������ݿ�
cd ${HOME_PATH}

java -Xms512m -Xmx1024m -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.ImportShifenSync >> ${LOG_FILE}

ifError $? "����ת�˷����ļ�ʧ��"
