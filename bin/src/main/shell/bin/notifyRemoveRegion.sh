#!/bin/sh
source conf/snap_include.conf
source lib/beidou_lib.sh

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

logfile=logs/notifyRemoveRegion.${today}.log

regionFile=conf/deleteRegion.conf

regions=""

while read LINE
do
    region="$LINE",${region}
done < $regionFile

echo $region

upgradeTime="2009#11@16%"

java -classpath ${CUR_CLASSPATH} com.baidu.beidou.tool.SendDeletedAreaMailToUser $region $upgradeTime >> ${logfile}