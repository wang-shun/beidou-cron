【任务名】	北斗给CMDA提供广告数据(QT、CT关键词统计)
【时间驱动】0   7   *   *   *	运行超时	60分钟	等待超时	40分钟
【运行命令】	cd /home/work/beidou-cron/bin && sh beidou2nova_adinfo.sh 3
【日志地址】	/home/work/beidou-cron/log/beidou2nova_adinfo.log
【变更库表】	

========================================================================================
【任务描述】
传递参数为3，重试3次，执行
exportUserVipInfo、exportQTCTData、exportEffectKeyword、devideSrchData、mergeToEffectQTKeywordData、mergeToEffectCTKeywordData、exportUserKeywordSum、exportTotalEffectKeywordSum函数
1.	exportUserVipInfo逻辑（导出用户信息（即用户是否为VIP））
下载shifen的用户文件
wget -q ftp://ftp:ftp @ tc-sf-cron03.tc.baidu.com//home/work/var/sf_data/ /userinfo${YESTERDAY}.dat(.md5)，进行md5验证
文件有3列（username，userid，ulevel）
过滤该文件，如果ulevel=10104，则vip=1，如果ulevel=10101，则vip=0，结果输出到文件uservip_${YESTERDAY}，并生成md5

2.	exportQTCTData逻辑（下载QT、CT关键词展现点击数据）
（1）	wget -t 3 -q --limit-rate=30M ftp://yf-beidou-cron00.yf01.baidu.com//home/work/beidou-stat/data/stat_qt_trans_merge/${YESTERDAY} /beidouqtstat.${YESTERDAY}.merged
这个文件是上游83820任务生成
userId	用户id
planId	计划id
groupId	推广组id
keywordId	关键词id
wordId	阿童木id
srch	展现
click	点击
cost	消费

（2）	wget -t 3 -q --limit-rate=30M ftp://yf-beidou-cron00.yf01.baidu.com//home/work/beidou-stat/data/stat_kt_merge/${YESTERDAY}/beidouktstat.${YESTERDAY}.merged
这个文件是上游120192任务生成
userId	用户id
planId	计划id
groupId	推广组id
keywordId	关键词id
wordId	阿童木id
srch	展现
click	点击
cost	消费

以上两个文件输出到正式目录下，并生成md5
3.	exportEffectKeyword逻辑（获取生效CT\QT关键词数据，这个需要等到7点生效数据生成以后）
对于任务111712生成的128个QT和CT的表数据，根据任务111713生成的生效推广组及生效推广用户文件effect_group.${TODAY}_filter.txt、effect_group.${TODAY}_final.txt，过滤出有效的QT关键词数据和CT关键词数据，文件名分别为qt_keyword_effect{0-63}， ct_keyword_effect{0-63}

4.	devideSrchData逻辑（把QT、CT关键词展现数据分散到64个子文件中）
（1）	查询北斗id和userid 的关系（userid, id mod 64, userid mod 64），据此作为将qt，ct文件进行拆分的依据
（2）	将CT、QT关键词展现点击数据分别根据id和userid分散到64个子文件中，文件名分别为qt_keyword_srch{0-63}，ct_keyword_srch{0-63}

5.	mergeToEffectQTKeywordData逻辑（合并QT关键词展现数据到生效数据）
遍历64次，对于qt_keyword_effect{0-63}和qt_keyword_srch{0-63}的每一组文件，汇总用户维度的关键字数量，关键词数量
结果存储为：qt_word_num（userid，count of keyword， count of workid）

6.	mergeToEffectCTKeywordData逻辑（合并CT关键词展现数据到生效数据）
遍历64次，对于ct_keyword_effect{0-63}和ct_keyword_srch{0-63}的每一组文件，汇总用户维度的关键字数量，关键词数量
结果存储为：ct_word_num（userid，count of keyword， count of workid）

7.	exportUserKeywordSum（合并关键词数据到用户维度）
需要根据以下文件：
beidouqtstat.${YESTERDAY}.merged： QT关键词展现点击文件
beidouctstat.${YESTERDAY}.merged： CT关键词展现点击文件
effect_user.${TODAY}_final.txt： 生效用户列表文件
qt_word_num： 用户QT关键词统计文件
ct_word_num： 用户CT关键词统计文件


生成关键字数据（Keyword）
七列：userid, QT_ALL, QT_展现, QT_消费, CT_ALL, CT_展现，CT_消费
需要的文件列表为：
effect_user.${TODAY}_final.txt（生效用户列表：userid）
qt_word_num（用户生效QT关键字：userid，count of keyword, count of word）
qt_data.user.keyword.srch（QT有展现关键字：userid，count of keyword）
qt_data.user.keyword.clk（QT有消费关键字：userid，count of keyword）
ct_word_num（用户生效CT关键字：userid，count of keyword, count of word）
ct_data.user.keyword.srch（CT有展现关键字：userid，count of keyword）
ct_data.user.keyword.clk（CT有消费关键字：userid，count of keyword）
结果存储为keyword_user_${YESTERDAY}，输出到：
/home/work/beidou-cron/data/beidou2nova/output/路径下，并生成MD5

生成关键词数据（word）
七列：userid, QT_ALL, QT_展现, QT_消费, CT_ALL, CT_展现，CT_消费
需要的文件列表为：
effect_user.${TODAY}_final.txt（生效用户列表：userid）
qt_word_num（用户生效QT关键词：userid，count of keyword, count of word）
qt_data.user. word.srch（QT有展现关键词：userid，count of keyword）
qt_data.user. word.clk（QT有消费关键词：userid，count of keyword）
ct_word_num（用户生效CT关键词：userid，count of keyword, count of word）
ct_data.user. word.srch（CT有展现关键词：userid，count of keyword）
ct_data.user. word.clk（CT有消费关键词：userid，count of keyword）
结果存储为word_user_${YESTERDAY}，输出到：
/home/work/beidou-cron/data/beidou2nova/output/路径下，并生成MD5



8.	exportTotalEffectKeywordSum逻辑（计算总计生效关键词，关键字）
（1）	对于生效qt_keyword, 直接从汇总的user数据qt_word_num里计算
（2）	对于有展现、有消费qt_keyword和qt_word, 直接从展现数据beidouqtstat.${YESTERDAY}.merged里计算
（3）	对于生效ct_keyword, 直接从汇总的user数据ct_word_num里计算
（4）	对于有展现、有消费ct_keyword和ct_word, 直接从展现数据beidou ctstat.${YESTERDAY}.merged里计算
（5）	计算QT生效word总数，需要做去重操作（qt_keyword_effect${0-63}）
（6）	计算CT生效word总数，需要做去重操作（ct_keyword_effect${0-63}）
结果输出到keyword_all_${YESTERDAY}.tmp，

将这个文件中的参数依据关键词，关键字平铺到两个文件中（即每个文件只有一行）

最终输出到：
/home/work/beidou-cron/data/beidou2nova/output/路径下，并生成MD5


==========================================================================================
【报警内容】


上游任务：111713——北斗给CMDA提供广告数据(生效数据，有效预算）
