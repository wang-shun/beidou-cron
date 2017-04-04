#!/bin/sh

#@file: getAotQtWordNum.sh
#@author: hanxu03
#@date: 2011-12-13
#@version: 1.0.0
#@brief: 


CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/getAotQtWordNum.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE

#params in alert.sh 
type="ERROR"
module=getAotQtWordNum
program=getAotQtWordNum.sh
reader_list=hanxu03

mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}
mkdir -p ${BAK_PATH}

LOG_FILE=${LOG_PATH}/getAotQtWordNum.log

########## download usertrade and md5,and check md5

date_download=`date -d"1 days ago" +%Y%m%d`

cd ${DATA_PATH}


### 抓取文件的md5
getfile_command="wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME}.md5 -O ${USER_TRADEID_FILENAME}.md5"

clear_command="cd $DATA_PATH && if [[ -f ${USER_TRADEID_FILENAME}.md5 ]];then rm ${USER_TRADEID_FILENAME}.md5; fi"

msg="Failed to get user_trade md5 for getAotQtWordNum"

getfile "$getfile_command" "$clear_command" 
if [[ $? -ne 0 ]]; then
	alert 1 "$msg"
fi


### 抓取文件
getfile_command="wget -q -c -t ${MAX_RETRY} -T ${TIME_OUT} --limit-rate=${LIMIT_RATE} ftp://${USER_TRADEID_HOST}/${USER_TRADEID_PATH}/${USER_TRADEID_FILENAME} -O ${USER_TRADEID_FILENAME} "

clear_command="cd $DATA_PATH && if [[ -f ${USER_TRADEID_FILENAME} ]];then rm ${USER_TRADEID_FILENAME}; fi"

msg="Failed to get user_trade for getAotQtWordNum"

getfile "$getfile_command" "$clear_command" 
if [[ $? -ne 0 ]]; then
	alert 1 "$msg"
fi



### 校验md5
md5sum -c ${USER_TRADEID_FILENAME}.md5 > /dev/null
alert $? "Failed to check user_trade md5"



########## get the qt word num

java -Xms1024m -Xmx2048m -classpath ${CUR_CLASSPATH} com.baidu.beidou.aot.ImportAotQtWordNum ${DATA_PATH}/${USER_TRADEID_FILENAME} ${DATA_PATH}/${QTWORDNUM_RESULT_NAME}.${date_download} 10 > ${LOG_FILE} 2>&1
alert $? "计算QT推广组有效词数量失败"



########## import into the db

#执行数据导入
db_sql="load data local infile '${DATA_PATH}/${QTWORDNUM_RESULT_NAME}.${date_download}' into table aot.qtwordnum FIELDS TERMINATED BY '\t' ENCLOSED BY '' LINES TERMINATED BY '\n'"
clear_sql="delete from aot.qtwordnum;"
msg="导入账户优化相关QT推广组有效提词量失败"

runsql_xdb "$clear_sql" 
if [[ $? -ne 0 ]]; then
    alert 1 "$msg"
fi
runsql_xdb "$db_sql"
if [[ $? -ne 0 ]]; then
    alert 1 "$msg"
fi
