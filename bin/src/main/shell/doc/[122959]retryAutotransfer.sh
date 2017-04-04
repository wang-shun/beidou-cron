【任务名】	每日定时转账补充续费
【时间驱动】	1   6-22   *   *   *	运行超时	30分钟	等待超时	30分钟
【运行命令】	cd /home/work/beidou-cron/bin && sh retryAutotransfer.sh  
【日志地址】	/home/work/beidou-cron/log/retryAutotransfer.log
【变更库表】	beidou.autotransfer

========================================================================================
【任务描述】

1.	进入目录：/home/work/beidou-cron/data/mfc/autofundtransfer
2.	从财务中心抓取凤巢余额文件
wget -t 3 -q --limit-rate=30M 
ftp://jx-dr-fnct00.jx.baidu.com//home/work/var/dr-finance/toshifen//userfund.txt(.md5)
校验md5正确
文件格式：
uid（用户id），
sumcash（总现金），
extend（总优惠+总补偿），
sumturnin（总转入），
balance（余额）
 
3.	将userfund.txt中的userid和余额信息输出到userfund.txt
4.	从autotransfer表中删除已经不是自动转账的用户
delete from autotransfer where userid not in (select userid from userfundperday where transfertype=1)

5.	从db中查找当前转账失败的有效用户列表（这里需要从主库查找，避免同步延迟引起状态不一致）


select userid, fund from userfundperday where transfertype =1 and userid in 
	(select userid from useraccount where ustate=0 and ushifenstatid in (2,3,6)) and  userid in 
	(select userid from autotransfer where is_success=1)

结果输出到retryAutotransfer.data.tmp

6.	比较userfund.txt和retryAutotransfer.data.tmp，将二者中都有的userid，并且其余额balance大于转账金额fund的用户筛选出来，结果输出到retryAutotransfer.data
格式为：userid，fund（单位：分）
 
retryAutotransfer.data中的就是目前可以尝试补账的用户列表
7.	判断retryAutotransfer.data的行数是否为0，如果为0，到此结束，否则执行下一步
8.	Cron调用
com.baidu.beidou.account.RetryAutotransferPerDay
（1）	生成bean：service = (UserFundService) ctx.getBean("userFundService");
（2）	service.retryMoveFundPerDay(inputFileName);其中inputFileName就是retryAutotransfer.data
a)	将retryAutotransfer.data的userid及fund信息存到list列表中，过滤了userid=0或者fund=0的异常数据
b)	首先调用autoTransferDao.setSuccessFlag(userid, fund);置成功标记位，所执行的sql为：
update autotransfer set is_success=0 ,fund=?, rcv_time=now() where userid=?;"

c)	调用mfc接口执行转账信息
int flag = mfcService.autoProductTransfer(
userid, 
AccountConfig.MFC_FENGCHAO_PRODUCTID, AccountConfig.MFC_BEIDOU_PRODUCTID, 
retry_fund);
d)	判断flag，如果flag=0，表示转账成功，那么写入成功日志；如果flag=14，表示当前周期仍然余额不足，写入失败日志；其他状态表示系统错误，不写入日志
retry_autotransfer_always.log.yyyymmddhh_系统毫秒数
日志的格式为：
字段	含义说明
转账时间	当前执行转账操作的时间
Userid	当前转账的账户userid
Fund	转账金额（单位：元）
方向	转款方向，方向=0表示向beidou转钱；方向=1表示向外转钱
转账结果	0表示转账成功，1表示搜索账户余额不足转账失败

e)	批量发送成功邮件，这一点和凌晨的自动转账不同，只发送成功的邮件/短信，失败的不发送
sendRetryTransferResultMailAndSms(reTranferLogFile);
需要先从userremind表中查找用户是否设置了提醒，然后发送邮件和短信，其中email信息来自UC，需要调用sfDrmDriver接口；而手机信息则来自userremind表

==========================================================================================
【报警内容】
1.	从财务中心抓取凤巢余额文件失败
2.	生成搜索账户用户余额文件失败
3.	从db中删除已经非自动转账用户列表失败
4.	从db中查找当前转账失败的有效用户列表失败
5.	retryAutotransfer.data文件不存在
6.	每日自动转账补充汇款任务失败，请追查

【备注】
凤巢的余额文件是每5分钟更新一次，时间点是2,7,12,17…抓取的时候注意避免时间点重合，不过脚本中已经做了重试机制
这个任务目前只能在tc_cron00上跑，有IP限制
