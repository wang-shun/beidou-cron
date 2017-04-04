【任务名】	北斗给CMDA提供广告数据(开通数据)
【时间驱动】	1   0   *   *   *	运行超时	120分钟	等待超时	0分钟
【运行命令】	cd /home/work/beidou-cron/bin && sh beidou2nova_adinfo.sh 1 
【日志地址】	/home/work/beidou-cron/log/beidou2nova_adinfo.log
【变更库表】	

========================================================================================
【任务描述】
传递参数为1，重试3次，执行exportAdDatFromDB函数：
exportAdDatFromDB函数逻辑：
1.	查询全库user列表数据
select u.userid,u.ustate,u.balancestat, u.ushifenstatid from beidou.useraccount u
结果存储为all_user.${TODAY}.txt（userid，ustate，ubalancestat，ushifenstatid）
生成有效状态用户列表userid (ustate=0 and balancestat=1 and ushifenstatid in 2,3,6)
awk '{if($2==0 && $3==1 && ($4==2 || $4==3 || $4==6)){printf("%s\n",$1)}}' all_user.${TODAY}.txt > effect_plan.${TODAY}.txt

2.	查询全库plan列表数据
select planid, userid,  planstate, budgetover from beidou.cproplan
结果存储为all_plan.${TODAY}.txt（planid，userid，planstate，budgetover）
生成有效状态计划列表planid,userid (planstate=0)
awk '{if($3==0) {printf("%s\t%s\n",$1,$2)}}' all_plan.${TODAY}.txt > effect_plan.${TODAY}.txt
导出计划撞线状态
awk '{if($4==1) {printf("%s\n",$1)}}' all_plan.${TODAY}.txt > plan.offline.${TODAY}

3.	查询全库group列表数据
select groupid,planid,userid,targettype,grouptype,groupstate from beidou.cprogroup
结果存储为all_group.${TODAY}.txt（groupid，planid，userid，targettype，groupstate）
生成有效状态组列表: groupid,planid,userid,targettype,grouptype
awk '{if($6==0) {printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}}' all_group.${TODAY}.txt >  effect_group.${TODAY}.txt

4.	查询全库unit列表数据cprounitstate${i},cprounitmater${i}
select s.id,s.gid,s.pid,s.uid,m.wuliaoType,s.state,m.adtradeid from beidou.cprounitstate${i} s,beidou.cprounitmater${i} m where s.id=m.id
结果存储为all_unit.${TODAY}.txt（id,groupid,planid,userid,wuliaoType,state, adtradeid）
生成有效状态创意列表: id,groupid, planid, userid, wuliaotype
awk '{if($6==0) {printf("%s\t%s\t%s\t%s\t%s\n",$1,$2,$3,$4,$5)}}' all_unit.${TODAY}.txt >  effect_unit.${TODAY}.txt

5.	输出开通数据
对all_unit.${TODAY}.txt和all_group.${TODAY}.txt进行awk处理，输出最终结果到
/home/work/beidou-cron/data/beidou2nova/output/路径下

6.	计算有效的四层级数据
从第5步生成的bd_all_info_yyyymmdd中过滤出unitid，groupid，planid，userid均在各自的有效列表（1-4中生成）中的数据，然后再讲这些数据中的四层级分别去重输出，作为有效的数据，结果分别存储为
effect_user.${TODAY}_filter.txt
effect_plan.${TODAY}_filter.txt
effect_group.${TODAY}_filter.txt
effect_unit.${TODAY}_filter.txt

7.	导出全库的CT关键词和QT关键词（这个是为了给7点的那个任务准备数据）
调用64次子函数subQuery，传递表名和输出文件名，每次查询重试3次
select userid, groupid, keywordid, wordid from beidou.cprokeyword${0-63}
结果存储为：qt_keyword_table${0-63}
调用64次子函数subQuery，传递表名和输出文件名，每次查询重试3次
select userid, groupid, keywordid, worded,keyword from beidou.cproqtkeyword${0-63}
结果存储为：ct_keyword_table${0-63}


==========================================================================================
【报警内容】
导出全库广告数据及生成开通数据失败

下游任务：
北斗给CMDA提供广告数据(生效数据，有效预算）(任务ID：111713)
北斗给CMDA提供广告数据(ATOM词表)(任务ID:121545)
