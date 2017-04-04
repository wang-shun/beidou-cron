#!/bin/sh

#@file: getWeekAvgCost.sh
#@author: xiehao
#@date: 2011-05-21
#@version: 1.0.0.0
#@brief: get average user cost in the last seven days and insert the result into database beidou.week_avg_cost

scriptName=$0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/getWeekAvgCost.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${BIN_PATH}/beidou_lib.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=getWeekAvgCost.sh
reader_list=xiehao

if ! [ -d ${TMP_FILE_PATH} ];then
        mkdir ${TMP_FILE_PATH}
        alert $? "$scriptName error : fail to mkdir ${TMP_FILE_PATH}"
fi

idx=1;
while((idx<=7))
do
  DATE=`date -d"-$idx days" +%Y%m%d`;
  echo selecting cost_$DATE
  touch ${TMP_FILE_PATH}/usercost${idx}.out
  runsql_clk_read "Select userid,sum(price) from beidoufinan.cost_${DATE} group by userid order by null;" "${TMP_FILE_PATH}/usercost${idx}.out"
  let idx=idx+1;
done

awk '{
  if($1 in usersum) usersum[$1] += $2;
  else usersum[$1]=$2;
}
END{
  for(u in usersum) print u,int((usersum[u]/7*1000+5)/10)/100;
}' ${TMP_FILE_PATH}/usercost1.out ${TMP_FILE_PATH}/usercost2.out ${TMP_FILE_PATH}/usercost3.out ${TMP_FILE_PATH}/usercost4.out ${TMP_FILE_PATH}/usercost5.out ${TMP_FILE_PATH}/usercost6.out ${TMP_FILE_PATH}/usercost7.out > ${TMP_FILE_PATH}/weekavgcost.out

# 为商桥准备文件weekavgcost.out
cd ${TMP_FILE_PATH}
md5sum weekavgcost.out > weekavgcost.out.md5

if ! [ -d $FILE_FOR_BRIDGE ];then
  mkdir $FILE_FOR_BRIDGE
  alert $? "$scriptName error : fail to mkdir ${FILE_FOR_BRIDGE}"
fi
if [ -f ${FILE_FOR_BRIDGE}/weekavgcost.out ];then
  mv ${FILE_FOR_BRIDGE}/weekavgcost.out ${FILE_FOR_BRIDGE}/weekavgcost.out.bak
  mv ${FILE_FOR_BRIDGE}/weekavgcost.out.md5 ${FILE_FOR_BRIDGE}/weekavgcost.out.md5.bak
fi
cp ${TMP_FILE_PATH}/weekavgcost.out ${FILE_FOR_BRIDGE}/weekavgcost.out
cp ${TMP_FILE_PATH}/weekavgcost.out.md5 ${FILE_FOR_BRIDGE}/weekavgcost.out.md5


runsql_xdb "use beidouext;drop table if exists week_avg_cost_tmp;
CREATE TABLE week_avg_cost_tmp (  userid int(10) NOT NULL default '0', avgcost decimal(10,2) default NULL, PRIMARY KEY (userid)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;"
alert $? "$scriptName error : failed to create table week_avg_cost"

LOAD_FILE="${TMP_FILE_PATH}/weekavgcost.out"
runsql_xdb "load data local infile '${LOAD_FILE}' into table beidouext.week_avg_cost_tmp fields terminated by ' ';"
alert $? "$scriptName error : failed to insert data into table week_avg_cost"

runsql_xdb "use beidouext;drop table if exists week_avg_cost;alter table week_avg_cost_tmp rename to week_avg_cost;"
alert $? "$scriptName error : failed to rename week_avg_cost_tmp to week_avg_cost;"

#for ODS/EDW
TODAY=`date -d"-0 days" +%Y%m%d`;
SEVENDAYAGO=`date -d"-7 days" +%Y%m%d`;
mkdir -p ${DATA_FILE_PATH}
cp ${TMP_FILE_PATH}/weekavgcost.out ${TMP_FILE_PATH}/weekavgcost.out.${TODAY}
cd ${TMP_FILE_PATH}
md5sum weekavgcost.out.${TODAY} > weekavgcost.out.${TODAY}.md5
rm -rf weekavgcost.out.${SEVENDAYAGO}*

exit 0
