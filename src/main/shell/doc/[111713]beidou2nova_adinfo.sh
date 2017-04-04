【任务名】	北斗给CMDA提供广告数据(生效数据，有效预算)
【时间驱动】30   6   *   *   *	运行超时	30分钟	等待超时	60分钟
【运行命令】	cd /home/work/beidou-cron/bin && sh beidou2nova_adinfo.sh 2
【日志地址】	/home/work/beidou-cron/log/beidou2nova_adinfo.log
【变更库表】	

========================================================================================
【任务描述】
传递参数为2，重试3次，执行
mergeValidData、exportValidBudgetFiles、exportValidBudgetResult函数
mergeValidData逻辑
1.	抓取昨日展现数据
wget -t 3 -q --limit-rate=30M ftp:// yf-beidou-cron00.yf01.baidu.com / /home/work/beidou-stat/data/output/stat_ad_${YESTERDAY}
文件格式： 创意id，展现， 点击，消费， userid，planid，groupid
将其中的 adid, groupid, planid, userid输出到srch_${YESTERDAY}

2.	merge生效的四层级数据
将111712任务生成的
effect_user.${TODAY}_filter.txt
effect_plan.${TODAY}_filter.txt
effect_group.${TODAY}_filter.txt
effect_unit.${TODAY}_filter.txt
merge 第1步srch_${YESTERDAY}中对应的四层级数据并进行去重，得到的结果分别存储到
effect_user.${TODAY}_final.txt
effect_plan.${TODAY}_final.txt
effect_group.${TODAY}_final.txt
effect_unit.${TODAY}_final.txt
以上4个文件是自user，plan，group到unit均有效的数据，合并昨日同一层级展现数据的结果

3.	根据第2步中生成的四层级生效数据，最终从111712的结果文件bd_all_info_yyyymmdd
中过滤出生效数据
将生成的文件bd_effect_info_yyyymmdd拷贝到/home/work/beidou-cron/data/beidou2nova/output/路径下，并生成MD5

4.	从bd_effect_info_yyyymmdd中输出三、四列（planid，userid），排序去重，结果存储为validplan.${TODAY}



exportValidBudgetFiles逻辑
1.	查询全库计划及其预算
select planid,userid,budget from beidou.cproplan
   结果存储为plan.budget.all
   根据以上生成的validplan.${TODAY}（planid，userid），从plan.budget.all中读取生效计划的预算信息，结果存储为plan.budget.${TODAY}（planid，userid，budget），即今天有效计划DB预算
2.	获取当日用户余额数据
wget -t 3 -q --limit-rate=30M ftp:// tc-beidou-cron00.tc.baidu.com / /home/work/beidou-cron/data/mfc/bak/userblns.txt.${TODAY}0030
打印第1列和第11列(userid,balance)，并过滤出属于validplan.${TODAY}集合的记录存储为plan.balance.${TODAY}
3.	获取计划昨天消费值： planid,  消费， 出价
select planid,sum(price) from beidoufinan.cost_${YESTERDAY} group by planid

结果按planid升序排列，存储到plan.cost.${YESTERDAY}
4.	获取计划昨天撞线数据
5.	wget -t 3 -q --limit-rate=50M ftp:// tc-beidou-cron00.tc.baidu.com/home/work/beidou-cron/data/planoffline/input/${YESTERDAY}/bdbudget.${YESTERDAY}-*.log
将所有log文件都打印到cpro_plan_offline.${YESTERDAY}.tmp文件中
Awk操作，提供一个全库的撞线计划信息


将结果cpro_plan_offline.yyyymmdd存储到
/home/work/beidou-cron/data/beidou2nova/output/路径下，并生成MD5

exportValidBudgetResult逻辑
通过plan.budget.${TODAY}（生效计划）、plan.offline.${TODAY}（凌晨111712生成的撞线计划），plan.cost.${YESTERDAY}（昨日消费），awk操作得到“当前账户未撞线的有效计划消费总和（planid，cost_sum）”、“当前账户未撞线的有效计划预算总和（planid,budget_sum）”，结果分别存储为：
plan.online.cost_total.${TODAY}
plan.online.budget_total.${TODAY}
注意，两个文件的每一列“其实是某一个计划id，对应该计划所属userid的cost，budget值，也就是同一userid下的plan，这两个值应该相同



==========================================================================================
【报警内容】
导出生效广告数据失败
获取生成有效预算的数据文件失败
导出有效预算数据失败


上游任务：
小时粒度数据汇总成天粒度(任务ID:22007)
下游任务：
北斗给CMDA提供广告数据(QT、CT关键词统计）(任务ID:115943)

