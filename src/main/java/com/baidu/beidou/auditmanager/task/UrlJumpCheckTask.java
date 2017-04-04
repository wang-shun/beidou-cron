package com.baidu.beidou.auditmanager.task;

/**
 * ClassName: UrlJumpCheckTask
 * Function: 新建及修改URL时，接受返回结果
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date 2011-10-18
 * @see 
 */
public interface UrlJumpCheckTask {
	/**
	 * startRecvInstantResult: 接受新建或者修改URL结果
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void startRecvInstantResult(String configFileName);
	
	/**
	 * patrolUrl: 轮巡有效URL，发送给bmq，让urlchecker进行处理
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void patrolUrl(String mapFileName, String dateStr);
	
	/**
	 * patrolUrl: 轮巡有效URL，从文件中获取数据，发送给bmq，让urlchecker进行处理
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void patrolUrlFromInputFile(String inputFileName, String mapFileName, String dateStr);
	
	/**
	 * startRecvPatrolResult: 接受轮巡URL结果
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void startRecvPatrolResult(String configFileName, String mapFileName);
	
	/**
	 * sendMailForInstantUrl: 对新建或者修改url的审核拒绝结果，发送邮件
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-23
	 */
	public void sendMailForInstantUrl();
	
	/**
	 * sendMailForPatrolUrl: 对轮巡url的审核拒绝结果，发送邮件
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-23
	 */
	public void sendMailForPatrolUrl();
}
