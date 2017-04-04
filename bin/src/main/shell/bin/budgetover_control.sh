#!/bin/sh

CONF_SH="../conf/common.conf"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}" 

CONF_SH=../lib/db_sharding.sh
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

CONF_SH="./alert.sh"
[ -f "${CONF_SH}" ] && source $CONF_SH || echo "not exist ${CONF_SH}"

DATA_PATH=/home/work/beidou-cron/data/budgetover_control
LOG_FILE=/home/work/beidou-cron/log/budgetover_control.log

#删除6小时前的数据
DEL_SUFFIX=`date -d"6 hour ago" +%Y%m%d%H`
mkdir -p ${DATA_PATH}
cd ${DATA_PATH}  && rm -f *${DEL_SUFFIX}*
sleep 20

function PRINT_LOG()
{
    echo "[`date +"%Y%m%d-%H:%M:%S"`]$1" >> ${LOG_FILE}
}

program=budgetover_control.sh
reader_list=zhangpeng

#默认3min为超时时间
OVER_TIME=600
###################################################
#超时自kill机制，暂时不要用
#${BIN_PATH}/process_monitor.sh $$ $0 ${OVER_TIME} &
###################################################

FILE_SUFFIX=`date  +%Y%m%d%H%M`
PLAN_BUDGETOVER_ALL=plan_budget.data
PLAN_BUDGETOVER_IS_0=plan_budget_0.data
PLAN_BUDGETOVER_IS_1=plan_budget_1.data
PLAN_CURRENT_COST=plan_cost.data
PLAN_BUDGETOVER_WILL_BE_1=plan_budget_willbe1_${FILE_SUFFIX}.data
PLAN_BUDGETOVER_WILL_BE_0=plan_budget_willbe0_${FILE_SUFFIX}.data
PLAN_BUDGETOVER_SET_1=plan_budget_set_1_${FILE_SUFFIX}.data
PLAN_BUDGETOVER_SET_0=plan_budget_set_0_${FILE_SUFFIX}.data
UPDATE_TO_1_SQL=${DATA_PATH}/update_to_1.sql
UPDATE_TO_0_SQL=${DATA_PATH}/update_to_0.sql

msg="查询所有计划当前消费失败"

#查询所有计划当前消费
runsql_clk_read "select wgrpid, sum(bid) from SF_Click.clk`date +%y%m%d` group by wgrpid;" ${PLAN_CURRENT_COST}
alert $? "${msg}"


msg="获取全库计划预算失败"


runsql_sharding_read "select planid, budget, budgetover from beidou.cproplan"  ${PLAN_BUDGETOVER_ALL}
alert $? "${msg}"
#查询当前为上线的计划列表
awk -F'\t' '{if($3==0){printf("%s\t%s\n",$1,$2)}}'  ${PLAN_BUDGETOVER_ALL} > ${PLAN_BUDGETOVER_IS_0}
awk -F'\t' '{if($3==1){printf("%s\t%s\n",$1,$2)}}'  ${PLAN_BUDGETOVER_ALL} > ${PLAN_BUDGETOVER_IS_1}

#消费至少>=预算，生成置下线预选列表
msg="生成置下线预选列表失败"
awk -F'\t' 'ARGIND==1{cost[$1]=$2}ARGIND==2{$3=0;if($1 in cost){$3=cost[$1]};if($3>=$2){printf("%s\t%s\t%s\n",$1,$2,$3)}}' ${PLAN_CURRENT_COST}  ${PLAN_BUDGETOVER_IS_0} > ${PLAN_BUDGETOVER_WILL_BE_1}
awk -F'\t' 'ARGIND==1{cost[$1]=$2}ARGIND==2{$3=0;if($1 in cost){$3=cost[$1]};if($3<$2){printf("%s\t%s\t%s\n",$1,$2,$3)}}' ${PLAN_CURRENT_COST}  ${PLAN_BUDGETOVER_IS_1} > ${PLAN_BUDGETOVER_WILL_BE_0}
alert $? "${msg}"

#计算下线阈值使用的随机种子数，该逻辑很重要，请勿随意更改
s_rand_array=(
    37  39  20  88   5  84  17  34  11  47 
    44  66  49  25   9  68  55   1  30  96 
    38  79   2  65  51  97  43   4  48  77 
    89  35  59  29  33  64  76  92  42  53 
    67  95  75  70  72  74  18  60  78   6 
    40  63   8  82  94  87  91  27  71  62 
    86  90  56  13  26  52  31  73  12  81 
    57  83  36  80  98  24  14  54  10   0 
    23  16  22  93   7  19  61  46  41  85 
    28  58  69   3  99  32  21  50  15  45);

upper_limit=125
lower_limit=115

cur_day__=`date +%s`
cur_day__=$((($cur_day__+28800)/86400))


#${PLAN_BUDGETOVER_WILL_BE_1}——————planid, budget, cost
#limit得到的是当前预算下的消费阈值

awk -F'\t' -v u_limit=$upper_limit -v l_limit=$lower_limit -v day_num=$cur_day__ -v rand_arry="${s_rand_array[*]}" 'BEGIN{split(rand_arry,s_array," ")}{rands=s_array[(day_num+$1)%100+1]; tmp_value=int(rands/100.0*(u_limit-l_limit+1));rate=u_limit-tmp_value; limit=int(rate*$2+0.5);printf("%d\t%d\t%.2f\t%.2f\t%s\n",$1,$2*100,$3*100,limit,rate)}' ${PLAN_BUDGETOVER_WILL_BE_1} > ${PLAN_BUDGETOVER_SET_1}.all

awk -F'\t' -v u_limit=$upper_limit -v l_limit=$lower_limit -v day_num=$cur_day__ -v rand_arry="${s_rand_array[*]}" 'BEGIN{split(rand_arry,s_array," ")}{rands=s_array[(day_num+$1)%100+1]; tmp_value=int(rands/100.0*(u_limit-l_limit+1));rate=u_limit-tmp_value; limit=int(rate*$2+0.5);printf("%d\t%d\t%.2f\t%.2f\t%s\n",$1,$2*100,$3*100,limit,rate)}' ${PLAN_BUDGETOVER_WILL_BE_0} > ${PLAN_BUDGETOVER_SET_0}.all

msg="生成置下线列表失败"
awk -F'\t' '{if($3>=$4){print $0}}' ${PLAN_BUDGETOVER_SET_1}.all > ${PLAN_BUDGETOVER_SET_1}
alert $? "${msg}"

lines=`cat ${PLAN_BUDGETOVER_SET_1} | wc -l`
if [ $lines -eq 0 ];then
PRINT_LOG "当前周期无需要下线计划列表"
else
PRINT_LOG "当前周期需要下线计划列表数:${lines}"
#updateSQL=`awk -F'\t' 'BEGIN{printf("update beidou.cproplan set budgetover=1 where budgetover=0 and planid in (-1")}{printf(",%s",$1)}END{printf(");")}' ${PLAN_BUDGETOVER_SET_1}`
awk -F'\t' 'BEGIN{printf("update beidou.cproplan set budgetover=1 where budgetover=0 and planid in (-1")}{printf(",%s",$1)}END{printf(");")}' ${PLAN_BUDGETOVER_SET_1} > ${UPDATE_TO_1_SQL}
cat  "${UPDATE_TO_1_SQL}" >> ${LOG_FILE}
PRINT_LOG "" 
#runsql_sharding "${updateSQL}"
runfilesql_sharding "${UPDATE_TO_1_SQL}"
msg="计划下线置位失败"
alert $? "${msg}"
fi

curr_hour=`date +%H`

if [ ${curr_hour} -lt 8 ];then
	PRINT_LOG "计划07:00开始上线，8点以后处理"
	exit 0
fi

msg="生成置上线列表失败"
awk -F'\t' '{if($3<$4){print $0}}' ${PLAN_BUDGETOVER_SET_0}.all > ${PLAN_BUDGETOVER_SET_0}
alert $? "${msg}"

lines=`cat ${PLAN_BUDGETOVER_SET_0} | wc -l`
if [ $lines -eq 0 ];then
PRINT_LOG "当前周期无需要上线计划列表"
else
PRINT_LOG "当前周期需要上线计划列表数:${lines}"
#updateSQL=`awk -F'\t' 'BEGIN{printf("update beidou.cproplan set budgetover=0 where budgetover=1 and planid in (-1")}{printf(",%s",$1)}END{printf(");")}' ${PLAN_BUDGETOVER_SET_0}`
awk -F'\t' 'BEGIN{printf("update beidou.cproplan set budgetover=0 where budgetover=1 and planid in (-1")}{printf(",%s",$1)}END{printf(");")}' ${PLAN_BUDGETOVER_SET_0} > ${UPDATE_TO_0_SQL}
cat  "${UPDATE_TO_0_SQL}" >> ${LOG_FILE}
PRINT_LOG "" 
#runsql_sharding "${updateSQL}"
runfilesql_sharding "${UPDATE_TO_0_SQL}"
msg="计划上线置位失败"
alert $? "${msg}"
fi
