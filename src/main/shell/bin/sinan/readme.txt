说明：
beidou-cron/bin/sinan下的脚本都是运行在司南server上的，需要部署在yf-beidou-sinan00.yf01:/home/work/beidou-cron/bin下。

脚本说明：
1. alert.sh 报警与通知函数
2. common.conf 配置文件
3. formateSinanData.sh 在司南端运行，格式化司南生成的访客特征数据，汇总成一个visitor.${TIMESTAMP}的文件供beidou端的importWmSiteVisitorIndex.sh抓取。
4. wm_import_task_scheduler.sh 在司南端运行，定时查看系统里还有没有任务在跑，如果没有则抓起beidou端的siteurl列表文件，重新启动系统。
