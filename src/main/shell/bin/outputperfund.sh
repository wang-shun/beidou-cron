#!/bin/sh

source statconf.conf
source costconf.conf

if [ ! -f ${PERFUND_PATH} ]; then
	mkdir -p ${PERFUND_PATH}
fi

CUR_CLASSPATH='lib/beidou-cron.jar:lib/freemarker-2.3.8.jar:lib/log4j-1.2.15.jar:lib/mail-1.3.1.jar:lib/mysql-connector-java-5.1.6.jar:lib/spring-2.5.6.jar:lib/activation-1.0.2.jar:lib/commons-logging-1.0.4.jar'
CUR_CLASSPATH='conf:'${CUR_CLASSPATH}

cd ${PERFUND_PATH}

#���ݵ�ǰĿ¼�µĽӿ��ļ�

if [ -f ${PERFUND_FILE_NAME} ]; then
	mv ${PERFUND_FILE_NAME} ${PERFUND_BACKUP_PATH}"/"${PERFUND_BACKUP_NAME}
fi

if [ -f ${PERFUND_FILE_MD5} ]; then
	mv ${PERFUND_FILE_MD5} ${PERFUND_BACKUP_PATH}"/"${PERFUND_BACKUP_MD5}
fi

#����java�������ɶ�Ӧ�Ľӿ��ļ�
cd ${HOME_PATH}
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.OutputTransfer >> ${LOG_FILE}

ifError $? "����ת�˽ӿ��ļ�ʧ��"

# ���ɶ�Ӧ��md5�ļ�
cd ${PERFUND_PATH}
if [ -f ${PERFUND_FILE_NAME} ]; then
	md5sum ${PERFUND_FILE_NAME} > ${PERFUND_FILE_MD5}
fi