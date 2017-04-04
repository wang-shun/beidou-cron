#!/bin/sh

source statconf.conf
source costconf.conf

CUR_CLASSPATH='lib/beidou-cron.jar:lib/freemarker-2.3.8.jar:lib/log4j-1.2.15.jar:lib/mail-1.3.1.jar:lib/mysql-connector-java-5.1.6.jar:lib/spring-2.5.6.jar:lib/activation-1.0.2.jar:lib/commons-logging-1.0.4.jar'
CUR_CLASSPATH='conf:'${CUR_CLASSPATH}

cd ${HOME_PATH}
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.account.UpdateCachFund >> ${LOG_FILE}

ifError $? "更新大客户缓存数据失败"