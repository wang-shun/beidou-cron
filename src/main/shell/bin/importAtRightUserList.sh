#!/bin/sh
#@file:importAtRightUserList.sh
#@author:hujunhai
#@date:2013-12-04
#@version:1.0.0.0
#@brief:获取AT右项目白名单用户，将增量数据导入beidouext.atright_userlist

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"
CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
CONF_SH="../conf/importAtRightUserList.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "
program=importAtRightUserList.sh

LOG_FILE=${LOG_PATH}/importAtRightUserList.log
USERLIST_FILE_LAST=${DATA_PATH}/userlist.last
USERLIST_FILE_NOW=${DATA_PATH}/userlist.now
USERLIST_FILE_DB=${DATA_PATH}/userlist.db
USERLIST_FILE_BAK=${DATA_PATH}/userlist.bak

ADD_DATETIME=`date +%F\ %T`

mkdir -p ${ROOT_PATH}
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

TODAY=`date +%Y%m%d`

msg="进入数据目录${DATA_PATH}失败"
cd ${DATA_PATH}
alert $? "${msg}"

#wget下载数据
function download_data(){
	msg="download data failed,file path="$USERLIST_FTP_PATH/$USERLIST_FILE

	wget -t 3 -q $USERLIST_FTP_PATH/$USERLIST_FILE -O $USERLIST_FILE
	if [ $? -ne 0 ] || ! [ -f $USERLIST_FILE ]
	then
		alert 1 "${msg}"
	fi
	
	wget -t 3 -q $USERLIST_FTP_PATH/$USERLIST_FILE.md5 -O $USERLIST_FILE.md5
	if [ $? -ne 0 ] || ! [ -f $USERLIST_FILE.md5 ]
	then
		alert 1 "${msg}"
	fi
	
	md5sum -c $USERLIST_FILE.md5
	alert $? "${msg}"
	mv $USERLIST_FILE $USERLIST_FILE_NOW
	rm $USERLIST_FILE.md5
}

#diff数据,将数据增量更新到DB中
function diff_and_update(){
	last_md5=`md5sum $USERLIST_FILE_LAST | awk '{print $1}'`
	now_md5=`md5sum $USERLIST_FILE_NOW  | awk '{print $1}'`
	
	if [[ $last_md5 != $now_md5 ]]
	then
		awk -F'\t' -v"addtime=${CURR_DATETIME}" '
		{
			if(NR==FNR){
				map[$1]=1;
			}else if(map[$1]!=1){
				print $1"\t"addtime;
			}
		}
		'  $USERLIST_FILE_LAST $USERLIST_FILE_NOW > $USERLIST_FILE_DB
		load_data_into_xdb
	fi
	mv $USERLIST_FILE_LAST $USERLIST_FILE_BAK
	mv $USERLIST_FILE_NOW $USERLIST_FILE_LAST
}

function load_data_into_xdb(){
	sql="load data local infile \"${USERLIST_FILE_DB}\" into table beidouext.atright_user fields terminated by '\t';"
	
	msg="load data into xdb failed,sql="$sql
	runsql_xdb "$sql"
	alert $? "${msg}"
	rm ${USERLIST_FILE_DB}
}

function main(){
	CURR_DATETIME=`date +%F\ %T`
	echo "start at "$CURR_DATETIME >> ${LOG_FILE}
	
	if [[ ! -f "${USERLIST_FILE_LAST}" ]]
	then
		echo "0" > $USERLIST_FILE_LAST
	fi
	
	#1、下载用户信息数据
	download_data
	
	#2、将数据增量更新到DB中
	diff_and_update
	
	CURR_DATETIME=`date +%F\ %T`
	echo "end at "$CURR_DATETIME >> ${LOG_FILE}
}

main
