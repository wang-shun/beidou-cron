#!/bin/bash

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/stat_budget.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "

mkdir -p ${INPUT_PATH}
mkdir -p ${OUTPUT_PATH}
mkdir -p ${TMP_PATH}

MSG="获取${USER_BUDGET}.${YESTERDAY}.txt文件失败"
downloadDTSFile "${STAT_USER_BUDGET_USER_BUDGET_TXT}" "${USER_BUDGET}.${YESTERDAY}.txt" "${YESTERDAY}"
alert $? "$MSG"

MSG="获取${BUDGET_TMP_YESTERDAY}文件失败"
downloadDTSFile "${STAT_USER_BUDGET_BUDGET_TXT}" "${BUDGET_TMP_YESTERDAY}" "${YESTERDAY}"
alert $? "$MSG"

MSG="获取用户预算数据失败-${BUDGET_TMP_YESTERDAY} not created"
if [ ! -s ${BUDGET_TMP_YESTERDAY} ]
then
    alert 1 "${MSG}" 
    exit 1
fi


## cost for user
rm -f ${COST_TMP}
runsql_clk_read "select p.userid,p.cost from (select userid, sum(price) as cost from beidoufinan.cost_${YESTERDAY} group by userid) p where p.cost>0 order by p.userid" "${COST_TMP}" 
MSG="query user's cost from beidoufinan.cost_${YESTERDAY}."
if [ ! -s ${COST_TMP} ]
then
    alert 1 "${MSG}"
    exit 1
fi

## offline for user
# user offline
wget ${USER_OFFLINE_FTP} -P ${TMP_PATH} --limit-rate=${USER_OFFLINE_FTP_RATE}
cat ${TMP_PATH}/bdzero.${YESTERDAY}-*.log | awk '{print $1;}' | sort -u > ${USER_OFFLINE_TMP}


MSG="获取用户下线数据失败-${USER_OFFLINE_TMP} not created"
if [ ! -s ${USER_OFFLINE_TMP} ]
then
    alert 1 "${MSG}"
    exit 1
fi

# all valid plans offline for user
runsql_cap_read "select planid ,userid, offtime from beidoucap.cproplan_offline where offtime>='${SQL_DATE}' and offtime<='${SQL_DATE1}' order by planid;" "${PLAN_OFFLINE_TMP}.all"
MSG="query distinct planid from cproplan_offline failed."
if [ ! -f ${PLAN_OFFLINE_TMP} ]
then
    alert 1 "${MSG}"
    exit 1
fi

cp 
awk -F'\t' '{print $1}' "${PLAN_OFFLINE_TMP}.all" | sort -u > "${PLAN_OFFLINE_TMP}"

rm -f ${OFFLINE_TMP}
awk '{print $1;}' ${BUDGET_TMP_YESTERDAY} | sort -u > ${USER_TMP}
awk 'ARGIND==1{map[$1];}ARGIND==2{if(!($2 in map)){print $1;}}' ${PLAN_OFFLINE_TMP} ${BUDGET_TMP_YESTERDAY} | sort -u > ${NOT_OFFLINE_TMP}
awk 'ARGIND==1{map[$1];}ARGIND==2{if(!($1 in map)){print $1;}}' ${NOT_OFFLINE_TMP} ${USER_TMP} > ${USER_ALL_PLAN_OFFLINE_TMP}

cat ${USER_ALL_PLAN_OFFLINE_TMP} ${USER_OFFLINE_TMP} | sort -u > ${OFFLINE_TMP}

#print result
awk 'ARGIND==1{print $1;}ARGIND==2{print $1;}ARGIND==3{print $1;}' ${COST_TMP} ${OFFLINE_TMP} ${USER_TMP} | sort -u > ${ALL_USER_TMP}
awk '
ARGIND==1{map[$1];map1[$1]=$2;}
ARGIND==2{map[$1];map2[$1]=$2;}
ARGIND==3{map[$1];map3[$1];}
ARGIND==4{
cost=0;
offline=0;
budget=0;
if($1 in map1){cost=map1[$1];}
if($1 in map2){budget=map2[$1];}
if(cost>1.3*budget){budget=cost;}
if($1 in map3){offline=1;}
if(($1 > 30) && ($1 < 1381000 || $1 > 1381999) && !(cost==0 && offline==0 && budget==0)){printf("%s\t%0.2f\t%0.2f\t%d\n", $1, cost, budget, offline);}
}' ${COST_TMP} ${USER_BUDGET}.${YESTERDAY}.txt ${OFFLINE_TMP} ${ALL_USER_TMP} | sort -k1n > ${USER_INFO}.${YESTERDAY}.txt


MSG="网盟预算统计数据生成失败-${USER_INFO}.${YESTERDAY}.txt is not created or empty"
if [ ! -s ${USER_INFO}.${YESTERDAY}.txt ]
then
    alert 1 "${MSG}"
    exit 1
fi

#regist DTS for ${STAT_USER_COST_OFFLINE_USER_INFO}
msg="regist DTS for ${STAT_USER_COST_OFFLINE_USER_INFO} failed."
noahdt add ${STAT_USER_COST_OFFLINE_USER_INFO} -i date=${YESTERDAY} bscp://${USER_INFO}.${YESTERDAY}.txt
alert $? "${msg}"

rm -f ${TMP_PATH}/balance.*.log*
#rm ${TMP_PATH}/*.txt

#此处输出的文件是额外提供给Nova，下游接口人是Nova的张驰
CPROPLAN_OFFLINE_FILE=cproplan_offline_${YESTERDAY}
cp ${PLAN_OFFLINE_TMP}.all ${OUTPUT_PATH}/${CPROPLAN_OFFLINE_FILE}
cd ${OUTPUT_PATH}
md5sum ${CPROPLAN_OFFLINE_FILE} > ${CPROPLAN_OFFLINE_FILE}.md5



#regist file to dts
msg="regist DTS for ${CPROPLAN_OFFLINE_FILE} failed."
md5=`getMd5FileMd5 ${OUTPUT_PATH}/${CPROPLAN_OFFLINE_FILE}.md5`
noahdt add ${BEIDOU_NOVA_PLANOFFTIME} -m md5=${md5} -i date=${YESTERDAY} bscp://${OUTPUT_PATH}/${CPROPLAN_OFFLINE_FILE}
noahdt add ${BEIDOU_NOVA_PLANOFFTIME_MD5} -i date=${YESTERDAY} bscp://${OUTPUT_PATH}/${CPROPLAN_OFFLINE_FILE}.md5
alert $? "${msg}"
