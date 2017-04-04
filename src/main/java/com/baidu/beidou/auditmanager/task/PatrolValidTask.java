package com.baidu.beidou.auditmanager.task;

/**
 * ClassName: PatrolValidTask
 * Function: 北斗aka轮询有效性物料
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version beidou-cron 1.1.2
 * @since TODO
 * @date Aug 3, 2011
 * @see 
 */
public interface PatrolValidTask {
	/**
	 * patrolValid: 轮询有效性物料全库
	 * @version PatrolValidTask
	 * @author genglei01
	 * @date Aug 3, 2011
	 */
	public void patrolValid(int maxThread, String outFileName, String logFileName);
	
	/**
	 * sendMail: 发送邮件
	 * @version PatrolValidTask
	 * @author genglei01
	 * @date Aug 3, 2011
	 */
	public void sendMail(String inFileName);
}
