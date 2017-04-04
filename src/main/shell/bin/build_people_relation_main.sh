#!/bin/sh
#����Ч�û�����ȫվ��Ⱥ������build_people_relation_sub.sh����8�����̴���

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



#useraccount���в�ѯ������userid�б�
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




#��beidoucap.useraccount���в�ѯ��Ҫ������ϵ����Ⱥuserid
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

#����ϴ������ļ�����
for (( i=0;i<8;i++ ))
do
	> ${BULID_USER_RELATION_OUTPUT}/$i
done

#��������ʧ������ �ű�����ʧ�������ļ�·��  û��·��������������������
if [ -z $1 ];then
	getDataFromUserAccount
else
	mv $1 ${USERACCOUNT_USERID_FILE_NEW}
fi

#���ݷֲ�
while read line
do
	userid=`echo $line | awk '{print $1}'`
	slice=`getUserSlice "$userid"`
	echo "$userid" >> ${BULID_USER_RELATION_OUTPUT}"/"$slice
done < ${USERACCOUNT_USERID_FILE_NEW}

#����8���̴�������
for(( i=0;i<8;i++ ))
do
	echo ${BULID_USER_RELATION_OUTPUT}"/"$i
	sh build_people_relation_sub.sh $i &
done

wait

end_time=`date +"%F %T"`
echo "[${end_time}]========================================end==============="