#!/bin/sh
#@file:updateClusterId.sh
#@author:hujunhai
#@date:2013-12-04
#@version:1.0.0.0
#@brief:更新推广组beidou.cprogroup_atright的clusterid

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/db.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/updateClusterId.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=updateClusterId.sh

LOG_FILE=${LOG_PATH}/updateClusterId.log

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

#clusterids.data file format:<userid><groupid><clusterid><crowd_weight>
function download_clusterids_data(){
	dateStr=$1
	#替换日期
	CLUSERID_FILE=${CLUSERID_FILE/YYMMDDHH/$dateStr}
	msg="download data failed,file path="$CLUSERID_FILE_PATH/$CLUSERID_FILE
	
	wget -t 3 -q $CLUSERID_FILE_PATH/$CLUSERID_FILE -O $CLUSERID_FILE
	if [ $? -ne 0 ] || ! [ -f $CLUSERID_FILE ]
	then
		alert 1 "${msg}"
	fi
	
	wget -t 3 -q $CLUSERID_FILE_PATH/$CLUSERID_FILE.md5 -O $CLUSERID_FILE.md5
	if [ $? -ne 0 ] || ! [ -f $CLUSERID_FILE.md5 ]
	then
		alert 1 "${msg}"
	fi
	
	md5sum -c $CLUSERID_FILE.md5
	alert $? "${msg}"
}

function update_clusterid_into_db(){
	cat $CLUSERID_FILE | while read line
	do
		array=(${line[@]})
		userid=${array[0]}
		groupid=${array[1]}
		clusterid=${array[2]}
		weight=${array[3]}
		if [[ -z $userid ]]
		then
			continue
		fi
		
		#计算分库shardingls
		let "dbsharding=$userid>>6&7"
		
		sql="update beidou.cprogroup_atright set cluster_ids='$clusterid' "
		if [[ $weight -ne -1 ]]
		then
			sql=$sql",crowd_weight=$weight "
		fi
		sql=$sql"where userid=$userid and groupid=$groupid;"
		
		msg="update clusterid into db failed,sql="$sql",dbsharding="$dbsharding
		runsql_single "$sql" "$dbsharding"
		alert $? "${msg}"
		sleep 0.05
	done
}

if [[ $# -ne 0 ]]
then
	CURR_DATETIME=`date +%F\ %T`
	echo "start update $1 cluserid at "$CURR_DATETIME >> ${LOG_FILE}
	
	download_clusterids_data $1
	update_clusterid_into_db
	
	CURR_DATETIME=`date +%F\ %T`
	echo "end update $1 cluserid at "$CURR_DATETIME >> ${LOG_FILE}
else
	#yesterday=`date -d '1 day ago' "+%y%m%d"`
	yesterday=`date "+%y%m%d%H"`
	CURR_DATETIME=`date +%F\ %T`
	echo "start update $yesterday cluserid at "$CURR_DATETIME >> ${LOG_FILE}
	
	download_clusterids_data $yesterday
	update_clusterid_into_db
	
	CURR_DATETIME=`date +%F\ %T`
	echo "end update $yesterday cluserid at "$CURR_DATETIME >> ${LOG_FILE}
fi