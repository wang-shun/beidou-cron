#!/bin/sh
source /home/work/beidou-cron/conf/snap_include.conf
source /home/work/beidou-cron/lib/beidou_lib.sh

cd /home/work/beidou-cron

CUR_CLASSPATH='.'
for f in `ls lib/*.jar`
do
	CUR_CLASSPATH=${CUR_CLASSPATH}:$f
done

CUR_CLASSPATH='conf:'${CUR_CLASSPATH}

today=`date +%Y%m%d`

if [ $# -ge 1 ]; then
   day=$1
fi

logfile=${BASE_DIR}logs/snap_ad_get.${today}.log

#合并snap文件
java -classpath ${CUR_CLASSPATH} com.baidu.beidou.tool.MergeSnapIndex ${day}>> ${logfile}
