#!/bin/sh
source /home/work/beidou-cron/conf/snap_include.conf

cd /home/work/beidou-cron

CUR_CLASSPATH='.'
for f in `ls lib/*.jar`
do
	CUR_CLASSPATH=${CUR_CLASSPATH}:$f
done

CUR_CLASSPATH='conf:'${CUR_CLASSPATH}

today=`date +%Y%m%d`
logfile=${BASE_DIR}logs/output.${today}.log

java -classpath ${CUR_CLASSPATH} com.baidu.beidou.tool.OrderOutput >> ${logfile}
