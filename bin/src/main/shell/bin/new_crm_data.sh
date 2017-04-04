#!/bin/sh
# query crm data yesterday
# author: wangchongjie
#input $1、操作类型（main/srch/all） $2、日期format:yyyyMMdd
#注意：此任务用来恢复历史数据时，需要带时间戳重跑上游的9114任务，生成crm_user和day_finan文件

CONF_SH="/home/work/.bash_profile"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/db_sharding.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/classpath_recommend.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=alert.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../conf/new_crm_data.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="../lib/dts_service.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

program=new_crm_data.sh
reader_list=wangchongjie

cd ${ROOT_PATH}
if [ $? -ne 0 ] ; then
	exit 1
fi
mkdir -p ${LOG_PATH}
mkdir -p ${DATA_PATH}

# get all data of table cproplan_offline yesterday
today=`date +%Y%m%d`
yesterday=`date +%Y%m%d -d"1 days ago"`
deleteday=`date +%Y%m%d -d"15 days ago"`
if [ -n "$2" ] ;then
	yesterday=`date -d "$2" +%Y%m%d`
	today=`date -d "-1 days ago ${yesterday}" +%Y%m%d`
fi

cd ${DATA_PATH}
rm -rf *${deleteday}*

#####################################################################################
# STEP1 : 获取北斗有效用户，并过滤crm_user数据,结果文件crm_user.effect
#####################################################################################
function get_effect_user()
{
	rm -rf beidou_user.${today}
	rm -rf beidou_user.${today}.md5
	wget -t 3 ${BEIDOU_USER_URL}.${today}
	wget -t 3 ${BEIDOU_USER_URL}.${today}.md5
	msg="failed to download crm user list"
	md5sum -c beidou_user.${today}.md5
	alert $? "{msg}"
	#filter ineffective user
	cp $CRM_USER_FILE crm_user.bak
	awk -F'\t' 'ARGIND==1{map[$1]}ARGIND==2{if($1 in map){print $1}}' beidou_user.${today}  crm_user.bak > crm_user.effect
}


#####################################################################################
# STEP2 : 获取"昨日最早下线推广计划名","昨日最早下线时间",结果文件user.offtime
#####################################################################################
function get_offline()
{
	msg="get plan offline time failed"
	runsql_sharding_read "select p.pid, p.uid, p.time, p.flag from (select planid as pid, userid as uid, offtime as time, 1 as flag from beidoucap.cproplan_offline where offtime>=${yesterday} and offtime<${today} and [userid]) p order by p.uid, p.pid, p.time" "plan.time"
	alert $? "${msg}"

	#plan.time(four columns):
	#planid, userid, time, flag(order by userid, planid, time asc)
	msg="get latest time of plan offline failed"
	awk -F"\t" 'BEGIN{
			pid=0;offtime=0;uid=0;
		}
		{
			if(pid==$1)
			{       
				if($4==1){offtime=$3;}else{offtime=0;}
			}           
			else    
			{       
				if(offtime!=0){print pid"\t"uid"\t"offtime;}
				uid=$2;pid=$1;
				if($4==1){offtime=$3;}else{offtime=0;}
			}           
		}
		END{    
			if(offtime!=0){print pid"\t"uid"\t"offtime;}
	}' plan.time | sort -k2n -k3 | awk -F"\t" 'BEGIN{
			uid=0;pid=0; 
		}
		{
			if(uid!=$2){print $1"\t"$2"\t"$3;}
			uid=$2; 
		}' > plan.offtime
	alert $? "${msg}"

	#get planname from table cproplan
	msg="get planname from table cproplan failed"
	runsql_sharding_read "select planid, planname from beidou.cproplan where [userid]" "plan.name"
	alert $? "${msg}"

	#merge planname, output: userid,planname,offtime
	msg="get planname into file(user.offtime) failed"
	awk -F"\t" 'ARGIND==1{map[$1]=$2}ARGIND==2{print $2"\t"map[$1]"\t"$3}' plan.name plan.offtime > user.offtime
	alert $? "${msg}"
}


#####################################################################################
# STEP3 : 获取day_finan数据,结果文件day.finan
#####################################################################################
#userid, SUM(price*rrate), SUM(price), COUNT(*) FROM beidoufinan.cost_${STAT_DATE}
function get_finan()
{
	cp ${DAY_FINAN_FILE} day.finan.tmp
	#+0.000000000001因为：shell的bug，小数点后第三位为5，第四位为0则5不进位
	awk -F"\t" '{printf("%d\t%.2f\t%.2f\t%d\n", $1,$2+0.000000000001,$3,$4)}' day.finan.tmp > day.finan
}


#####################################################################################
# STEP4 : 获取预算数据，结果文件：budget.dat
#####################################################################################
function get_final_budget()
{
	rm -rf budget.dat
	SQL="select userid, finalbudget FROM beidouext.day_final_budget"
	runsql_xdb_read "${SQL}" budget.dat
}


#####################################################################################
# STEP5 : 获取有效计划、组、创意数据，结果文件：plan_etc.sum
#####################################################################################
function get_plan_etc_sum(){
	rm -rf plan_etc.sum
	SQL="select userid, count(planid), sum(groups), sum(units) from (SELECT b.userid, b.planid, count(distinct groupid) groups, count(d0.id)+count(d1.id) +count(d2.id) +count(d3.id) +count(d4.id) +count(d5.id) +count(d6.id) +count(d7.id)  units FROM beidou.cproplan b left join beidou.cprogroup c on c.userid=b.userid and c.groupstate=0  and c.planid=b.planid left join beidou.cprounitstate0 d0 on d0.uid=b.userid and d0.state=0  and d0.gid=c.groupid left join beidou.cprounitstate1 d1 on d1.uid=b.userid and d1.state=0  and d1.gid=c.groupid left join beidou.cprounitstate2 d2 on d2.uid=b.userid and d2.state=0  and d2.gid=c.groupid left join beidou.cprounitstate3 d3 on d3.uid=b.userid and d3.state=0  and d3.gid=c.groupid left join beidou.cprounitstate4 d4 on d4.uid=b.userid and d4.state=0  and d4.gid=c.groupid left join beidou.cprounitstate5 d5 on d5.uid=b.userid and d5.state=0  and d5.gid=c.groupid left join beidou.cprounitstate6 d6 on d6.uid=b.userid and d6.state=0  and d6.gid=c.groupid left join beidou.cprounitstate7 d7 on d7.uid=b.userid and d7.state=0  and d7.gid=c.groupid where [b.userid] and b.planstate=0 group by b.userid, b.planid ) t group by userid";
	
	runsql_sharding_read "${SQL}" plan_etc.sum
}


#####################################################################################
# STEP6 : 批量执行4、5, 此处逻辑删除
#####################################################################################
function get_all_data()
{
	rm -rf *_userid_tmp*
	local TimeStamp=`date +%H%M`
	local File_Preifx=${TimeStamp}_userid_tmp
	local File_Lines=5000
	
	while read -r line
	do
		echo $line >> mod_userid_tmp.$(($line / 64 % 8))
	done < crm_user.effect
	
	msg="split crm_user.effect fail"
	for i in `ls mod_userid_tmp*`
	do
		split -l ${File_Lines} $i ${File_Preifx}.$i.
	done
	
	totalLines=`wc -l crm_user.effect| cut -d" " -f1`;
	sliceLines=`cat ${File_Preifx}* | wc -l | cut -d" " -f1`;
	if [ $totalLines -ne $sliceLines ]
	then
		alert 1 "${msg}"
	fi

	for file in `ls ${File_Preifx}*`
	do
		retryCount=0
		sucFlag=0
		while [[ $retryCount -lt 3 ]] && [[ $sucFlag -eq 0 ]]
		do
			retryCount=$(($retryCount+1))
			userids=`awk '{printf(",%s ",$0)}' ${file}`
			userids=${userids#,}
			userids=${userids%,}
			
			get_plan_etc_sum "$userids"
			get_final_budget "$userids"
			
			if [ $? -eq 0 ]
			then
				sucFlag=1
			else
				sleep 0.5
			fi
		done
		rm -f ${file}
		sleep 2
	done
}


#####################################################################################
# STEP7 : 获取是否为大客户文件,结果文件为ulevel.dat
#####################################################################################
function get_ulevel()
{
	rm -rf ${ULEVEL_FILE_NAME_PRE}${yesterday}.dat*

	msg="wget shifen user fail"
	wget -q ftp://${ULEVEL_FILE_FTP_NAME}:${ULEVEL_FILE_FTP_PWD}@${ULEVEL_FILE_SERVER}/${ULEVEL_FILE_PATH}/${ULEVEL_FILE_NAME_PRE}${yesterday}.dat*
	alert $? "${msg}"

	msg="check shifen user file md5 fail"
	ULEVEL_MD5=`md5sum ${ULEVEL_FILE_NAME_PRE}${yesterday}.dat | cut -d' ' -f1`
	alert $? "${msg}"
	if [[ "${ULEVEL_MD5}" != `cat ${ULEVEL_FILE_NAME_PRE}${yesterday}.dat.md5` ]] ;then
		alert 1 "${msg}"
	fi

	msg="生成ulevel文件失败"
	awk -F"\t" 'BEGIN{OFS="\t"} {if($2==10101){print $3,0} else if($2==10104) {print $3,1}}' ${ULEVEL_FILE_NAME_PRE}${yesterday}.dat | sort -k1,1 >ulevel.dat
	alert $? "${msg}"
}


#####################################################################################
# STEP6 : 将以上文件merge成一个文件
#####################################################################################
function merge_all_file()
{
	awk -F "\t" '
	FILENAME=="user.offtime"{PLANNAME[$1]=$2;OFFTIME[$1]=$3;} \
	FILENAME=="day.finan"{CASH[$1]=$2;COST[$1]=$3;CLK[$1]=$4} \
	FILENAME=="budget.dat"{BUDGET[$1]=$2} \
	FILENAME=="plan_etc.sum"{PLAN[$1]=$2;GROUP[$1]=$3;UNIT[$1]=$4;}\
	FILENAME=="ulevel.dat"{ULEVEL[$1]=$2;}\
	FILENAME=="crm_user.effect"{HITLINE[$1]=0;if(COST[$1]>0 && COST[$1]>=BUDGET[$1])HITLINE[$1]=1;if(CASH[$1]<=0)CASH[$1]=0;if(COST[$1]<=0)COST[$1]=0;if(CLK[$1]<=0)CLK[$1]=0;if(BUDGET[$1]<=0)BUDGET[$1]=0;if(PLAN[$1]<=0)PLAN[$1]=0;if(GROUP[$1]<=0)GROUP[$1]=0;if(UNIT[$1]<=0)UNIT[$1]=0;if(ULEVEL[$1]<=0)ULEVEL[$1]=0;print $1"\t"CLK[$1]"\t"COST[$1]"\t"CASH[$1]"\t"BUDGET[$1]"\t"PLAN[$1]"\t"GROUP[$1]"\t"UNIT[$1]"\t"HITLINE[$1]"\t"ULEVEL[$1]"\t"PLANNAME[$1]"\t"OFFTIME[$1]}' \
    user.offtime	\
    day.finan	\
	budget.dat \
	plan_etc.sum \
	ulevel.dat \
	crm_user.effect > ${CRM_FILE}
	
	md5sum ${CRM_FILE} >  ${CRM_FILE}".md5"
	 
	cp ${CRM_FILE} ${CRM_FILE}.${yesterday}
	md5sum ${CRM_FILE}.${yesterday} > ${CRM_FILE}.${yesterday}".md5"
}

#####################################################################################
# STEP7 : merge 展现文件
#####################################################################################
function merge_srch_file()
{	
	sed 's/ /\t/g' ${DAY_SRCH_FILE} > day_srch.dat
	
	awk -F "\t" -vfile1=day_srch.dat -vfile2=${CRM_FILE} \
	'FILENAME==file1 {SRCH[$1]=$2} \
	FILENAME==file2 {if(SRCH[$1]<=0) SRCH[$1]=0;print $1"\t"SRCH[$1]"\t"$2"\t"$3"\t"$4"\t"$5"\t"$6"\t"$7"\t"$8"\t"$9"\t"$10"\t"$11"\t"$12}' \
    day_srch.dat	\
	${CRM_FILE} > ${CRM_FILE2}
	
	md5sum ${CRM_FILE2} > ${CRM_FILE2}".md5"
	
	cp ${CRM_FILE2} ${CRM_FILE2}.${yesterday}
	md5sum ${CRM_FILE2}.${yesterday} > ${CRM_FILE2}.${yesterday}".md5"
}


#####################################################################################
# MAIN
#####################################################################################
function main_process()
{
	get_effect_user
	get_offline
	get_finan
	get_final_budget
	get_plan_etc_sum
	get_ulevel
	merge_all_file
}

#main
#$1:操作类型 $2:日志参数
if [ $# -ne 0 ];then
	if [[ "$1" == "main" ]];then
		main_process
	fi
	if [[ "$1" == "srch" ]];then
		merge_srch_file
	fi
	if [[ "$1" == "all" ]];then
		main_process
		merge_srch_file
	fi
fi	

	
	
	

	
	
	



