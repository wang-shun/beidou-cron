#!/bin/bash
#@file: getFinalBudget.sh
#@author: xiehao
#@date: 2011-05-12
#@version: 1.0.0.0
#@brief: get final budget 

scriptName=$0

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="${CONF_PATH}/getFinalBudget.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

LIB_FILE="${BIN_PATH}/beidou_lib.sh"
source $LIB_FILE
alert $? "Conf error: Fail to load libfile[$LIB_FILE]!"


program=getFinalBudget.sh
reader_list=xiehao

# $1 : file name to be checked
function checkFile(){
	if [ -f ${TMP_FILE_PATH}/$1 ];then
		mv ${TMP_FILE_PATH}/$1 ${TMP_FILE_PATH}/$1.bak
		[ $? -ne 0 ] && echo "fail to rename ${TMP_FILE_PATH}/$1"
        fi
        touch ${TMP_FILE_PATH}/$1
	[ $? -ne 0 ] && echo "fail to create file ${TMP_FILE_PATH}/$1"
}


function getFinalBudget(){
	YESTERDAY=`date -d'-2 day' +%Y%m%d`
	TODAY=`date -dyesterday +%Y%m%d`

	if ! [ -f ${TMP_FILE_PATH}/plans.out ];then
		alert 1 "$scriptName error : 'input file '${TMP_FILE_PATH}' not exits'"
	fi

	#step 2 : 获取余额大于0的用户id及其余额
	checkFile users.out
	
	#runsql_xdb_read "SELECT userid,balance from beidouext.userbalance where balance > 0;"  ${TMP_FILE_PATH}/users.out
	

	wget -t 3 -q --limit-rate=30M ftp://${BALANCE_SERVER}/${BALANCE_PATH}/${BALANCE_FILE} -O ${TMP_FILE_PATH}/${BALANCE_FILE}
	if [ $? -ne 0 ];then
		alert 1 "抓取用户余额数据失败"
    fi
	awk -F'\t' '{if($2>0){printf("%s\t%.2f\n",$1,$2/100)}}' ${TMP_FILE_PATH}/${BALANCE_FILE} > ${TMP_FILE_PATH}/users.out
	
    alert $? "$scriptName error : failed select with index $idx"

	#step 3 : 统计合并users.out和plans.out
	checkFile userbudgebalance.out
	awk 'ARGIND==1{
		user2budget[$1]=$2
	}
	ARGIND==2{
		user2balance[$1]=$2
	}
	END{
		for( u in user2budget ){
			if( u in user2balance){
				print u,user2budget[u],user2balance[u] ;
			}
			else{
				print u,user2budget[u],0;
			}
		}
	}' ${TMP_FILE_PATH}/plans.out ${TMP_FILE_PATH}/users.out > ${TMP_FILE_PATH}/userbudgebalance.out

	#如果userbudgetbalance_${TODAY}已经产生，则drop掉它然后重新创建
	runsql_xdb "use beidoureport; drop table if exists userbudgetbalance_${TODAY};"
	if [ $? -ne 0 ];then
		echo "table userbudgetbalance_${TODAY} not exits";
	fi
	
	#创建表userbudgetbalance_${TODAY}
	runsql_xdb "use beidoureport; create table userbudgetbalance_${TODAY}(userid int(10),budget int(10),balance decimal(12,2));"
	alert $? "$scriptName error : fail to create table userbudgetbalance_${TODAY}"
	
	#将数据文件userbudgebalance.out批量插入userbudgetbalance_${TODAY}
	runsql_xdb "use beidoureport; load data local infile \"${TMP_FILE_PATH}/userbudgebalance.out\" into table userbudgetbalance_${TODAY} fields terminated by ' ';"
	alert $? "$scriptName error : fail to insert data into table userbudgetbalance_${TODAY}"

	#step 4 : 获取昨日的用户预算
	checkFile userbudget_last.out
	runsql_xdb_read "use beidoureport; select userid,budget from userbudgetbalance_${YESTERDAY}" "${TMP_FILE_PATH}/userbudget_last.out"
	if [ $? -ne 0 ];then
        echo "table userbudgetbalance_${YESTERDAY} doesn't exist";
    fi

	#step 5 : 获取用户昨日展现数据
	#查询每个用户的消费和
	checkFile usercost.out
	runsql_clk_read "Select userid,sum(price) from beidoufinan.cost_${TODAY} group by userid;"  ${TMP_FILE_PATH}/usercost.out
	alert $? "$scriptName error : fail to select from table beidoufinan.cost_${TODAY}"

	#获取昨天的展现数据并校验
	if ! [ -f ${DAY_SRCHS_FILE_PATH}/day_srchs ];then
		alert 1 "$scriptName error : '${DAY_SRCHS_FILE_PATH}/day_srchs file not exits'"
	fi
	
	cp ${DAY_SRCHS_FILE_PATH}/day_srchs ${TMP_FILE_PATH}/day_srchs
	alert $? "$scriptName error : fail to copy day_srchs to directory ${TMP_FILE_PATH}"

	#以day_srchs为基准合并day_srchs和usercost.out这两个文件
	awk 'ARGIND==1{
		if($2 > 0)	cost[$1] = 0;
	}
	ARGIND==2{
		if($1 in cost) cost[$1] += $2;
	}
	END{
		for( u in cost )	print u,cost[u];
	}' ${TMP_FILE_PATH}/day_srchs ${TMP_FILE_PATH}/usercost.out > ${TMP_FILE_PATH}/user_show_cost.out
	alert $? "$scriptName error : fail to merge files day_srchs and usercost.out"

	# step 6 : 根据算法和已有数据的到用户最终预算
	checkFile day_final_budget.out
	awk -vpath=${TMP_FILE_PATH} -vdebug=$DEBUG_MOD '
	function showedUser(budget,budget_last,cost,balance){
		res = 0;
		if(budget < budget_last){
			if(cost < budget){
				if( cost+balance >= budget)	res = budget;
				else res = cost + balance;
			}
			else if(cost > budget_last)	res = budget_last;
			else	res = cost;
		}
		else{
			if(cost > budget)	res = budget;
			else if(budget > balance+cost)	res = balance+cost;
			else	res = budget;
		}
		return res
	}
	ARGIND==1{
		u2cost[$1] = $2;
	}
	ARGIND==2{
		u2budget[$1] = $2;
		u2balance[$1] = $3;
	}
	ARGIND==3{
		u2budget_last[$1] = $2;
		if(!($1 in u2budget)){
			u2budget[$1] = 0;
			u2balance[$1] = 0;
		}
	}
	END{
		for( u in u2cost ){
			if( !(u in u2budget)){	u2budget[u]=0;u2balance[u]=0;}
			if( !(u in u2budget_last))	u2budget_last[u]=0;
		}
		for( u in u2budget ){
			if(!(u in u2budget_last)) u2budget_last[u] = 0;

			if(u in u2cost){
				if(u2budget[u]==0 && u2balance[u]==0 && u2budget_last[u]==0)	res = u2cost[u];
				else res = showedUser(u2budget[u],u2budget_last[u],u2cost[u],u2balance[u]);
			}
			else{
				if(u2budget[u] > u2balance[u])	res = u2balance[u];
				else	res = u2budget[u];
			}
			print u,u2cost[u],int(res) > path"/day_final_budget.out"
		}

		if(debug==1) for(u in u2budget) print u,u2budget[u],u2balance[u],u2budget_last[u] > path"/budgetbalance_T_L.out"
	}' ${TMP_FILE_PATH}/user_show_cost.out ${TMP_FILE_PATH}/userbudgebalance.out ${TMP_FILE_PATH}/userbudget_last.out 
	alert $? "$scriptName error : fail to merge files userbudgebalance.out, userbudget_last.out and user_show_cost.out"

	runsql_xdb "use beidouext;drop table if exists day_final_budget_tmp;create table day_final_budget_tmp(userid int(10),cost decimal(10,2),finalbudget int(10),PRIMARY KEY (userid))ENGINE=InnoDB;"
	alert $? "$scriptName error : failed to create table day_final_budget"
	runsql_xdb "load data local infile \"${TMP_FILE_PATH}/day_final_budget.out\" into table beidouext.day_final_budget_tmp fields terminated by ' ';"
	alert $? "$scriptName error : failed to insert data into table day_final_budget"
	runsql_xdb "use beidouext;drop table if exists day_final_budget_${YESTERDAY};alter table day_final_budget rename to day_final_budget_${YESTERDAY};alter table day_final_budget_tmp rename to day_final_budget;"
	alert $? "$scriptName error : failed to rename day_final_budget_tmp to day_final_budget"
	SEVENDAYSBEFORE=`date -d'-7 days' +%Y%m%d`;
	runsql_xdb "drop table if exists beidouext.day_final_budget_${SEVENDAYSBEFORE}"
	alert_return $? "$scriptName error : failed to drop table day_final_budget_${SEVENDAYSBEFORE}"
	runsql_xdb "drop table if exists beidouext.userbudgetbalance_${SEVENDAYSBEFORE}"
	alert_return $? "$scriptName error : failed to drop table userbudgetbalance_${SEVENDAYSBEFORE}"
	return 0;
}

getFinalBudget
alert $? "$scriptName error : error occurs during running"
exit 0
