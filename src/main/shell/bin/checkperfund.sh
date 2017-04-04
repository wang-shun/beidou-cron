#!/bin/sh

source statconf.conf
source costconf.conf

CUR_CLASSPATH='lib/beidou-cron.jar:lib/freemarker-2.3.8.jar:lib/log4j-1.2.15.jar:lib/mail-1.3.1.jar:lib/mysql-connector-java-5.1.6.jar:lib/spring-2.5.6.jar:lib/activation-1.0.2.jar:lib/commons-logging-1.0.4.jar'
CUR_CLASSPATH='conf:'${CUR_CLASSPATH}

#��ʼ���������ʷ����
cd ${PERFUND_CHECK_LOCAL_PATH}
rm *

#����beidou��ˮ�ļ�

cd ${HOME_PATH}

java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.OutputSuccTransfer >> ${LOG_FILE}

ifError $? "��������ת�˶����ļ�ʧ��"

#��ȡshifenת�˶����ļ�
cd ${PERFUND_CHECK_LOCAL_PATH}

mv ${PERFUND_CHECK_SHIFEN_FILE_NAME} ${PERFUND_CHECK_SHIFEN_FILE_NAME}".beidou"
mv ${PERFUND_CHECK_SHIFEN_FILE_MD5}  ${PERFUND_CHECK_SHIFEN_FILE_MD5}".beidou"

wget -q ftp://${PERFUND_CHECK_SHIFEN_USER}:${PERFUND_CHECK_SHIFEN_PASSWORD}@${PERFUND_CHECK_SHIFEN_SERVER}/${PERFUND_CHECK_SHIFEN_PATH}/${PERFUND_CHECK_SHIFEN_FILE_NAME}
ifError $? "����shifen��ˮ�����ļ�ʧ�ܣ�"

wget -q ftp://${PERFUND_CHECK_SHIFEN_USER}:${PERFUND_CHECK_SHIFEN_PASSWORD}@${PERFUND_CHECK_SHIFEN_SERVER}/${PERFUND_CHECK_SHIFEN_PATH}/${PERFUND_CHECK_SHIFEN_FILE_MD5}
ifError $? "����shifen��ˮ����MD5�ļ�ʧ�ܣ�"

DIFF_RES=`diff ${PERFUND_CHECK_SHIFEN_FILE_MD5}".beidou" ${PERFUND_CHECK_SHIFEN_FILE_MD5}`

if [ "abc"${DIFF_RES} != "abc" ]; then
	hit "ע�⣬shifen��beidouת����ˮ��Ϣ����"
fi