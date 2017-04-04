#!/bin/sh
#给有效用户创建全站人群，调用build_people_relation_sub.sh启动8个进程处理

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

CONF_SH=../conf/bulidUserRelation.conf
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

DATE_NOW=`date +"%Y%m%d"`
BULID_USER_RELATION_OUTPUT=${DATA_PATH}${BULID_USER_RELATION_OUTPUT}
LOG_PATH=${LOG_PATH}${BULID_USER_RELATION_LOG_PATH}
LOG_FILE=${LOG_PATH}"/"${BULID_USER_RELATION_LOG_NAME}${DATE_NOW}



#useraccount表中查询出来的userid列表
USERACCOUNT_USERID_FILE=${BULID_USER_RELATION_OUTPUT}"/"${USERACCOUNT_USERID_FILE_NAME}
USERACCOUNT_USERID_FILE_NEW=${BULID_USER_RELATION_OUTPUT}"/"${USERACCOUNT_USERID_NEW_FILE_NAME}
USERACCOUNT_USERID_FILE_BAK=${BULID_USER_RELATION_OUTPUT}"/"${USERACCOUNT_USERID_BAK_FILE_NAME}

mkdir -p "${BULID_USER_RELATION_OUTPUT}"
mkdir -p "${LOG_PATH}"



function PRINT_LOG()
{
	timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}




#从beidoucap.useraccount表中查询需要建立关系的人群userid
function getDataFromUserAccount()
{
	>$USERACCOUNT_USERID_FILE
	
	local sql="select userid from beidoucap.useraccount where ustate=0 and sfstattransfer =0  and ushifenstatid in (2,3,6) order by userid asc"
	
	runsql_cap_read "$sql" "$USERACCOUNT_USERID_FILE"
	
	if [ $? -ne 0 ];then
		PRINT_LOG "[error] query beidoucap.useraccount fail [sql] $sql"
		return 1
	fi
	
	local peoplesql="select userid from beidou.vtpeople where type=5"
	runsql_sharding_read "$peoplesql" "${USERACCOUNT_USERID_FILE_BAK}"
	
	if ! [ -e ${USERACCOUNT_USERID_FILE_BAK} ] ;then
		touch ${USERACCOUNT_USERID_FILE_BAK}
	fi
	awk '
		ARGIND==1{olduser[$1]}
		ARGIND==2{if(!($1 in olduser)){print $0}}
	' ${USERACCOUNT_USERID_FILE_BAK} $USERACCOUNT_USERID_FILE  > ${USERACCOUNT_USERID_FILE_NEW}
	
	
	#cp $USERACCOUNT_USERID_FILE ${USERACCOUNT_USERID_FILE_BAK}

}



start_time=`date +"%F %T"`
echo "[${start_time}]=====================================start=============="

#清空上次运行文件数据
for (( i=0;i<8;i++ ))
do
	> ${BULID_USER_RELATION_OUTPUT}/$i
done

#便于重跑失败数据 脚本传入失败数据文件路径  没有路径参数则按正常流程运行
if [ -z $1 ];then
	getDataFromUserAccount
else
	mv $1 ${USERACCOUNT_USERID_FILE_NEW}
fi

#数据分拨
while read line
do
	userid=`echo $line | awk '{print $1}'`
	slice=`getUserSlice "$userid"`
	echo "$userid" >> ${BULID_USER_RELATION_OUTPUT}"/"$slice
done < ${USERACCOUNT_USERID_FILE_NEW}

#启动8进程处理数据
for(( i=0;i<8;i++ ))
do
	echo ${BULID_USER_RELATION_OUTPUT}"/"$i
	sh build_people_relation_sub.sh $i &
done

wait

end_time=`date +"%F %T"`
echo "[${end_time}]========================================end==============="