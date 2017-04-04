#!/bin/sh

#@file:importAotStat.sh
#@author:yangyun
#@date:2010-12-06
#@version:1.0.0.0
#@brief:import aot plan and group and unit statinfo to database

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/aotStatInfo.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

program=importAotStat.sh

#add by dongying
LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
if [ $? -ne 0 ] 
then
    alert 1 "Conf error: Fail to load libfile[$LIB_FILE]!"
fi

LOG_FILE=${LOG_PATH}/importAotStat.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

CURR_DATETIME=`date +%F\ %T`
echo "start at "$CURR_DATETIME >> ${LOG_FILE}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? ${msg}

#将数据文件先copy过来
msg="复制AD统计文件失败"
cp ${AD_STAT_FILE_PATH}/${AD_STAT_FILE_PREFIX}${AD_STAT_FILE_DATE} ./ad_stat
alert $? "${msg}"

#load data infile
#adid, srchs, clks, cost, userid, planid, groupid
sql="use aot;CREATE TABLE cprounitstat(\
  adid bigint(20) NOT NULL,\
  srchs bigint(20) NOT NULL,\
  clks int(11) NOT NULL,\
  cost int(11) NOT NULL,\
  userid int(10) NOT NULL,\
  planid int(10) NOT NULL,\
  groupid int(10) NOT NULL,\
  PRIMARY  KEY adid (adid),\
KEY planid (planid),\
KEY groupid (groupid)\
) ENGINE=MyISAM DEFAULT CHARSET=binary"

retryCount=0
MAX_RETRY=3
sucFlag=0
while [[ $retryCount -lt $MAX_RETRY ]] && [[ $sucFlag -eq 0 ]]
do
        retryCount=$(($retryCount+1))
        msg="Drop cprounitstat表失败"
        runsql_xdb "use aot;drop table if exists cprounitstat"
        #alert $? "${msg}"
        if [ $? -eq 0 ]
        then
                sucFlag=1
        else
                echo "fail count $retryCount" >> ${LOG_FILE}
                sleep 30
        fi
done
if [ $sucFlag -eq 1 ]
then
    echo "drop table successfully!" >> ${LOG_FILE}
else
    alert $? "${msg}"
fi

db_sql="${sql}"
msg="创建cprounitstat表失败"
runsql_xdb "$db_sql" >> $LOG_FILE
alert $? "${msg}"

db_sql="use aot;load data local infile '${DATA_PATH}/ad_stat' into table cprounitstat"
msg="导入cprounitstat表失败"
runsql_xdb "$db_sql" >> $LOG_FILE
alert $? "${msg}"

#import CproGroupStat
msg="计算groupstat失败"
awk -F"\t" '{srchs[$7]+=$2;clks[$7]+=$3;cost[$7]+=$4;}END{for(gid in srchs){print gid"\t"srchs[gid]"\t"clks[gid]"\t"cost[gid];}}' ./ad_stat > group_stat
alert $? "${msg}"

msg="生成groupstat的sql失败"
awk -F"\t" '{print "update aot.cprogroupstat_tmp set srchs="$2", clks="$3", cost="$4" where groupid="$1";";}' ./group_stat > group_stat.sql
alert $? "${msg}"

db_sql="source ${DATA_PATH}/group_stat.sql"
msg="加载groupstat的sql失败"
runsql_xdb "$db_sql" >> $LOG_FILE
alert $? "${msg}"

#import CproPlanStat
msg="计算planstat失败"
awk -F"\t" '{srchs[$6]+=$2;clks[$6]+=$3;cost[$6]+=$4;}END{for(pid in srchs){print pid"\t"srchs[pid]"\t"clks[pid]"\t"cost[pid];}}' ./ad_stat > plan_stat
alert $? "${msg}"

msg="生成planstat的sql失败"
awk -F"\t" '{print "update aot.cproplanstat_tmp set srchs="$2", clks="$3", cost="$4" where planid="$1";";}' ./plan_stat > plan_stat.sql
alert $? "${msg}"

db_sql="source ${DATA_PATH}/plan_stat.sql"
msg="加载planstat的sql失败"
runsql_xdb "$db_sql" >> $LOG_FILE
alert $? "${msg}"

db_sql="use aot;drop table if exists cproplanstat; rename table cproplanstat_tmp to cproplanstat;"
msg="重命名cproplanstat表失败"
runsql_xdb "$db_sql" >> $LOG_FILE
alert $? "${msg}"

db_sql="use aot;drop table if exists cprogroupstat; rename table cprogroupstat_tmp to cprogroupstat;"
msg="重命名cprogroupstat表失败"
runsql_xdb "$db_sql" >> $LOG_FILE
alert $? "${msg}"










