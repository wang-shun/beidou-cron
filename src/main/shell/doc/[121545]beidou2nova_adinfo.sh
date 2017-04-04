【任务名】	北斗给CMDA提供广告数据(ATOM词表)
【时间驱动】	0   2  *   *   *	运行超时	180分钟	等待超时	0分钟
【运行命令】	cd /home/work/beidou-cron/bin && sh beidou2nova_adinfo.sh 4
【日志地址】	/home/work/beidou-cron/log/beidou2nova_adinfo.log
【变更库表】	

========================================================================================
【任务描述】
执行exportAtomDictionary函数

exportAtomDictionary逻辑：
1.	检查凌晨的任务是否已经将全库的QT,CT关键词数据导出来了
2.	遍历64个QT词文件，将其中的wordid，keyword去重输出到
word_dictionary_yyyymmdd.qt
遍历64个CT词文件，将其中的wordid，keyword去重输出到
word_dictionary_yyyymmdd.ct
3.	将word_dictionary_yyyymmdd.qt，word_dictionary_yyyymmdd.qt再次合并去重，结果为word_dictionary_yyyymmdd
4.	输出正式文件到/home/work/beidou-cron/data/beidou2nova/output/路径下，并生成MD5
keyword_table${0-63}


==========================================================================================
【报警内容】

【上游任务】
北斗给CMDA提供广告数据(开通数据)(任务ID:111712)
