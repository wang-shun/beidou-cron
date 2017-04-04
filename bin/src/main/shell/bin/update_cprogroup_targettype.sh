#!/bin/sh
#改变部分groupid定向方式
#参数1是传入groupid文件路径
#参数2：vt-->tartgettype改成16 rt-->targettype改成8

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

CONF_SH=../conf/init_cprogroup.conf
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

rm -rf $CPROGROUP_DATA_PATH_OUTPUT/
mkdir -p "${CPROGROUP_DATA_PATH_OUTPUT}"

while read idLine
do
	userid1=`echo $idLine | awk '{print $2}'`
	groupid1=`echo $idLine | awk '{print $1}'`
	slice=`getUserSlice "$userid1"`

	echo -e "$userid1\t$groupid1" >> $CPROGROUP_DATA_PATH_OUTPUT"/"$slice
done < "$1"


isRT2VT=$2
if [ "$isRT2VT"x = "vt"x ];then
	targettype=16
elif [ "$isRT2VT"x = "rt"x ];then
	targettype=8
else
	echo "参数错误"
	exit
fi

for (( i=0;i<8;i++ ))
do
	count=0
	where='(0'
	while read line
	do
		userid=`echo $line | awk  '{print $1}'`
		groupid=`echo $line | awk '{print $2}'`
		where=$where","$groupid
		let count++
		if [ $count -eq 1000 ];then
			echo "update beidou.cprogroup set targettype = $targettype,modtime=now() where groupid in "$where");" >> $CPROGROUP_DATA_PATH_OUTPUT"/update_sql"$i$i
			where="(0"
			count=0
		fi
	done < $CPROGROUP_DATA_PATH_OUTPUT"/"$i
	echo "update beidou.cprogroup set targettype = $targettype,modtime=now() where groupid in "$where");" >> $CPROGROUP_DATA_PATH_OUTPUT"/update_sql"$i$i
done

function executeSql()
{
	while read sqlLine
	do
		local update_sql=$sqlLine
		runsql_single "$update_sql" "$2"
		
		sleep 0
	done < $CPROGROUP_DATA_PATH_OUTPUT"/"$1
}

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