#!/bin/bash

#filename: unit_tag_clear.sh
#@auther:  dongying
#@fuction: clear data from unit_tag_history
#@date:    2014-05-28
#@version: 1.0.0.0 

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="../bin/alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../conf/unit_tag_clear.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

#common config
LOG_FILE=${LOG_PATH}/unit_tag_clear.log

#default file timestamp
DEFAULT_TIME=`date -d "3 months ago" +%Y%m%d`


function PRINT_LOG()
{
    local logTime=`date +%Y%m%d-%H:%M:%S`
    echo "[${logTime}]$0-$1" >> $LOG_FILE
}

function db_execute()
{
	if [[ -z $1 ]]; then
		PRINT_LOG "[error used] please input db_info like : 'mysql -h hostname -P portnum -u username -p password'"
		return 1
	else
		db_info=$1
	fi

	if [[ -z $2 ]]; then
		PRINT_LOG "[error used] please input operation_sql_file for param 1"
		return 1
	else
		operation_sql_file=$2
	fi
	
	if [[ ! -z $3 ]]; then
		export_file=$3
	fi

	#try to exec clear_sql and operation_sql for times
	cnt=0
	while [[ $cnt -lt $RETRY_TIMES ]]; do
		if [ $# -eq 3 ];then
			$db_info -e "source $operation_sql_file" > $export_file  2>> ${LOG_FILE}
		else
			$db_info -e "source $operation_sql_file"  2>> ${LOG_FILE}
		fi
		if [[ $? -eq 0 ]]; then
			return 0
		fi
		cnt=$(($cnt+1))
		sleep 5
	done
	PRINT_LOG "$0:Execute Query Failed-$2"
	return 1
}


PRINT_LOG "-------------------BENGIN TO CLEAR UNIT_TAG ---------------------"

#param check 
#no param means use 3 month before today for clear date
if [ $# -eq 0 ]; then
	CLEAR_DATE=${DEFAULT_TIME}
#1 param support use input date for clear date
elif [ $# -eq 1 ]; then
	if [[ $1 =~ "^[0-9]{8}$" ]]; then
		CLEAR_DATE=$1;
	else
    	PRINT_LOG "******illegal param for date, please input yyyymmdd!!******"
    	exit 1
  	fi
#other means error
else
	PRINT_LOG "******illegal param num!! exit******"
	exit 1
fi

PRINT_LOG "******************** clear date is ${CLEAR_DATE} *********************"
#get minid and maxid which need to delete
mysql_conn_audit_rd="${MYSQL_CLIENT} -h${BEIDOU_DB_IP_AUDIT_READ} -P${BEIDOU_DB_PORT_AUDIT_READ} -u${BEIDOU_DB_USER_AUDIT_READ} -p${BEIDOU_DB_PASSWORD_AUDIT_READ} -Daudit --default-character-set=utf8 --skip-column-names"
mysql_conn_audit="${MYSQL_CLIENT} -h${BEIDOU_DB_IP_AUDIT} -P${BEIDOU_DB_PORT_AUDIT} -u${BEIDOU_DB_USER_AUDIT} -p${BEIDOU_DB_PASSWORD_AUDIT} -Daudit  --default-character-set=utf8 --skip-column-names"


cd $CLEAR_DATA_PATH 

echo "select id from unit_tag_history order by id limit 1" > min.sql
echo "select id from unit_tag_history where time<$CLEAR_DATE order by id desc limit 1" > max.sql

PRINT_LOG "******************* get minid and maxid from audit db ************************"
minid=`db_execute "${mysql_conn_audit_rd}" "$CLEAR_DATA_PATH/min.sql" || alert $? "$0-Error Read Data From audit db"`
maxid=`db_execute "${mysql_conn_audit_rd}" "$CLEAR_DATA_PATH/max.sql" || alert $? "$0-Error Read Data From audit db"`

#make sure this time has data need to delete
if [ -z $maxid  ] || [ $maxid -le 0 ] || [ -z $minid ] ||  [ $minid -le 0 ]; then
	PRINT_LOG "******************* no data need to delete , so exit!! *****"
	exit 0
fi

deleteNum=$(( $maxid-$minid+1 ))
if [ $deleteNum -gt 0 ]; then
	PRINT_LOG "******************* need delete from $minid to $maxid , total num is $deleteNum ***************"
fi

#loop to delete data
checkNum=$(($deleteNum%$BATCH_NUM_CONF))
if [ $checkNum -eq 0 ]; then
	loopNum=$(($deleteNum/$BATCH_NUM_CONF))
else
	loopNum=$(($deleteNum/$BATCH_NUM_CONF+1))
fi

DELETE_DATA_FILE=$CLEAR_DATA_PATH/delete-$CLEAR_DATE.txt
for((i=0;i<$loopNum;i++))
do
	beginid=$(($minid + i*$BATCH_NUM_CONF))
	endid_temp=$(( $minid + (i+1)*$BATCH_NUM_CONF -1))
	endid=$(( ($endid_temp<$maxid)?$endid_temp:$maxid ))
	
	echo "select * from unit_tag_history where id>=$beginid and id<=$endid" > select.sql
	echo "delete from unit_tag_history where id>=$beginid and id<=$endid" > delete.sql
	export_flie=${DELETE_DATA_FILE}.$i
	db_execute "${mysql_conn_audit_rd}" "$CLEAR_DATA_PATH/select.sql" "$export_flie" || alert $? "$0-Error Read Data From audit db"
	PRINT_LOG "******************* query data from db from $beginid to $endid *****"
	cat $export_flie >> $DELETE_DATA_FILE
	
	db_execute "${mysql_conn_audit}" "$CLEAR_DATA_PATH/delete.sql" || alert $? "$0-Error Delete Data From audit db"
	PRINT_LOG "******************* delete data from db from $beginid to $endid *****"
	
	rm $export_flie
	sleep 2
done


PRINT_LOG "-------------------END TO CLEAR UNIT_TAG --------------------"

