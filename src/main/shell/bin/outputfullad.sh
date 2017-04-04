#!/bin/sh

CONF_SH=/home/work/.bash_profile
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/outputfullad.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"


#added by zhangpingan at 20111218
program=outputfullad.sh
reader_list=zhangpingan

function PRINT_LOG()
{
    timeNow=`date +%Y%m%d-%H:%M:%S`
	echo "[${timeNow}]${1}" >> ${LOG_FILE}
}

function findAllGroupIdofEffPlan()
{
    runsql_sharding_read "select t.groupid from beidou.cprogroup t, beidou.cproplan s where t.planid = s.planid and s.planstate = 0 and [t.userid]" "${LOCAL_PATH}/groupIdEffect.txt"
    if [ $? -ne 0 ];then
        PRINT_LOG "query groupid from beidou failed."
        exit 1
    fi
}

function findAllGroupInfo()
{
    runsql_sharding_read "select groupid,isallsite from beidou.cprogroupinfo where [userid]" "${LOCAL_PATH}/groupIdAllSiteList.txt.tmp"
    if [ $? -ne 0 ];then
        PRINT_LOG "query groupid,isallsite from cprogroupinfo failed."
        exit 1
    fi
	awk -F'\t' '{if($2==1){print $1}}' ${LOCAL_PATH}/groupIdAllSiteList.txt.tmp > ${LOCAL_PATH}/groupIdAllSiteList.txt
	rm -f ${LOCAL_PATH}/groupIdAllSiteList.txt.tmp
}

function findAllUnitIdsByGroupId()
{
   runsql_sharding_read	"select id, gid from beidou.cprounitstate? where [uid]" "${LOCAL_PATH}/unitIdAllList.txt" ${TAB_UNIT_SLICE}
   if [ $? -ne 0 ];then
        PRINT_LOG "query id, gid from cprounitstate[0-7] failed."
        exit 1
   fi
}

#main
findAllGroupIdofEffPlan

findAllGroupInfo

rm -f ${LOCAL_PATH}/unitIdAllList.txt
findAllUnitIdsByGroupId

awk -F'\t' '
ARGIND==1{
    EFF_GROUP[$1]
}
ARGIND==2{
    if($1 in EFF_GROUP)
	{
	    ALLSITE_EFFECT_GROUP[$1]
	}
}
ARGIND==3{
    if($2 in ALLSITE_EFFECT_GROUP)
	{
	   printf("%s\n",$1)
	}
}'  ${LOCAL_PATH}/groupIdEffect.txt  ${LOCAL_PATH}/groupIdAllSiteList.txt  ${LOCAL_PATH}/unitIdAllList.txt > ${ADFILE_PATH}/${ADFILE_NAME}

if [ $? -eq 0 ]
then
	cd ${ADFILE_PATH}
	md5sum ${ADFILE_NAME} > ${ADFILE_MD5}
	
	if [ "`date +%H`" = "00" ]; then
		cp fullad.`date +%Y%m%d`00 fullad.txt
		md5sum fullad.txt > fullad.txt.md5
	fi
else
    alert 1 "$0: Export Error [$ADFILE_NAME]."
fi
