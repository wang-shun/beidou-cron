#!/bin/sh
#Ǩ���ƹ���rt����ʽ��vt������֮ǰ����ʹ��С�����ű�

#CONF_SH="/home/work/.bash_profile"
#[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/common.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=./alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/db.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

CONF_SH=../conf/init_it_rt_targettype.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "



DATE_NOW=`date +"%Y%m%d"`
CPROGROUP_DATA_PATH_OUTPUT=${DATA_PATH}${INIT_CPROGROUP}
LOG_PATH=${LOG_PATH}${INIT_CPROGROUP_LOG_PATH} 
LOG_FILE=${LOG_PATH}"/"${INIT_CPROGROUP_LOG_NAME}${DATE_NOW}

GROUP_DATA=$CPROGROUP_DATA_PATH_OUTPUT"/"$GROUPID_FILE_NAME
ROLLBACK_SQL_DATA=$CPROGROUP_DATA_PATH_OUTPUT"/"$ROLLBACK_SQL_FILE_NAME
UPDATE_SQL_DATA=$CPROGROUP_DATA_PATH_OUTPUT"/"$UPDATE_SQL_FILE_NAME

mkdir -p "${LOG_PATH}"


function PRINT_LOG()
{
	timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}



start_time=`date +"%F %T"`
echo "[${start_time}] =====================================begin============================"
rm -rf ${CPROGROUP_DATA_PATH_OUTPUT}
mkdir -p "${CPROGROUP_DATA_PATH_OUTPUT}"

#8�����ݷֱ����8�������ļ�
sql="select userid,groupid from beidou.cprogroup where targettype=40 and [userid]" 
for (( i=0;i<8;i++))
do
	runsql_single_read "$sql" "$CPROGROUP_DATA_PATH_OUTPUT/$i" "$i"
	if [ $? -ne 0 ];then
		PRINT_LOG "[error] get all groupid data fail"
	fi
done
rm -rf $ROLLBACK_SQL_DATA
rm -rf $UPDATE_SQL_DATA


#�����ɻع�sql�ļ���Ȼ��ÿ���ļ��е�groupidƴ��update��䣬ÿ������1000��groupid
for (( i=0;i<8;i++ ))
do
	count=0
	where='(0'
	while read line
	do
		userid=`echo $line | awk '{print $1}'`
		groupid=`echo $line | awk '{print $2}'`
		echo "update beidou.cprogroup set targettype =40 where groupid = "$groupid";" >> $ROLLBACK_SQL_DATA$i$i
		where=$where","$groupid
		let count++
		if [ $count -eq 500 ];then
			echo "update beidou.cprogroup set targettype = 48,modtime=now() where groupid in "$where");" >> $CPROGROUP_DATA_PATH_OUTPUT"/update_sql"$i$i
			where="(0"
			count=0
		fi
	done < $CPROGROUP_DATA_PATH_OUTPUT"/"$i
	echo "update beidou.cprogroup set targettype = 48,modtime=now() where groupid in "$where");" >> $CPROGROUP_DATA_PATH_OUTPUT"/update_sql"$i$i
done

function executeSql()
{
	while read sqlLine
	do
		local update_sql=$sqlLine
		runsql_single "$update_sql" "$2"
		
		sleep 1
	done < $CPROGROUP_DATA_PATH_OUTPUT"/"$1
}

#ͬʱ8����ͬʱִ��
for (( i=0;i<8;i++))
do
	executeSql "update_sql$i$i" "$i" &
	
done
wait

if [ $? -ne 0 ];then
   PRINT_LOG "[error] update fail"
fi


end_time=`date +"%F %T"`
echo "[${end_time}] =========================================finish================================="
