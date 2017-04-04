#!/bin/bash

CONF_SH="../conf/stat_budget.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH} "


mkdir -p ${INPUT_PATH}
mkdir -p ${OUTPUT_PATH}
mkdir -p ${TMP_PATH}
    
rm -f ${PID_TMP}
runsql_sharding_read "select distinct s.pid,s.uid,g.userid from beidou.cprogroup g, beidou.cprounitstate? s where s.gid=g.groupid and s.state=0 and g.groupstate=0 and [s.uid]" "${PID_TMP}" ${TAB_UNIT_SLICE} 
MSG="${PID_TMP}: PID列表生成失败"
if [ ! -s ${PID_TMP} ]
then
	alert 1 "${MSG}"
	exit 1
else
	echo "${PID_TMP}: PID列表生成成功"
fi

rm -f ${PLAN_ID_TMP}
runsql_sharding_read "select t.budget, t.userid, t.planid from beidou.cproplan t where t.planstate=0 and [t.userid];" "${PLAN_ID_TMP}"
MSG="${PLAN_ID_TMP}: PLAN_ID列表生成失败"
if [ ! -s ${PLAN_ID_TMP} ]
then
	alert 1 "${MSG}"
else
	echo "${PLAN_ID_TMP}: PLAN_ID列表生成成功"
fi
     
rm -f ${USER_LIST_TMP}
runsql_cap_read "select u.userid from beidoucap.useraccount u where u.ustate=0 and u.userid > 30 and (u.userid < 1381000 or u.userid > 1381999) and u.ushifenstatid in (2,3,6);"  "${USER_LIST_TMP}"
MSG="${USER_LIST_TMP}: 用户列表文件生成失败"
if [ ! -s ${USER_LIST_TMP} ]
then
	alert 1 "${MSG}"
	exit 1
else
	echo "${USER_LIST_TMP}: 用户列表生成成功"
fi

rm -f ${BUDGET_TMP}
awk 'ARGIND==1{
	   		map1[$1]
	  }ARGIND==2{
	   if($2 in map1){
	   	 	map2[$1]
	   }
	  }ARGIND==3{
	   if($3 in map2){
	   		printf("%s\t%s\t%s\n",$2,$3,$1)
	   }
}' ${USER_LIST_TMP} ${PID_TMP} ${PLAN_ID_TMP} | sort -k1n > ${BUDGET_TMP}


MSG="${BUDGET_TMP}: 计算用户预算数据失败,请RD排查"
if [ ! -s ${BUDGET_TMP} ]
then
	alert 1 "${MSG}"
	exit 1
fi
    
#regist DTS Date:YYYYmmDD
msg="regist DTS for ${STAT_USER_BUDGET_BUDGET_TXT} failed."
noahdt add ${STAT_USER_BUDGET_BUDGET_TXT} -i date=${TODAY} bscp://${BUDGET_TMP}
alert $? "${msg}"
    
MSG="删除本地余额文件错误"
rm -f bduserfund.txt.md5
alert $? "$MSG"

rm -f bduserfund.txt
alert $? "$MSG"

MSG="远程获取财务端余额数据文件失败"
wget ${BALANCE_FTP_MD5}
wget ${BALANCE_FTP} --limit-rate=${BALANCE_FTP_RATE}
alert $? "$MSG"

MSG="文件完整性验证失败"
md5sum -c bduserfund.txt.md5
alert $? "$MSG"

awk '{printf("%s\t%.2f\n",$1,$2/100);}' bduserfund.txt | sort -k1n > ${BALANCE_TMP}

MSG="${BALANCE_TMP}: 获取用户余额数据失败,请RD排查"
if [ ! -s ${BALANCE_TMP} ]
then
	alert 1 "${MSG}"
	exit 1
else 
	echo "财务端余额数据获取成功"
fi


awk 'ARGIND==1{
	if($1 in map){
		map[$1]=map[$1]+$3
	}else{
		map[$1]=$3
	}
}ARGIND==2{
	if($1 in map){
		if(map[$1]>$2){
			map[$1]=$2;
		}
		map1[$1];
	}
}END{
	for(userid in map){
		if(userid in map1){
			printf("%s\t%s\n", userid, map[userid]);
		}else{
			printf("%s\t%s\n", userid, 0);
		}
	}
}' ${BUDGET_TMP} ${BALANCE_TMP} | sort -k1n > ${USER_BUDGET}.${TODAY}.txt

#regist DTS Date:YYYYmmDD
msg="regist DTS for ${STAT_USER_BUDGET_USER_BUDGET_TXT} failed."
noahdt add ${STAT_USER_BUDGET_USER_BUDGET_TXT} -i date=${TODAY} bscp://${USER_BUDGET}.${TODAY}.txt
alert $? "${msg}"
