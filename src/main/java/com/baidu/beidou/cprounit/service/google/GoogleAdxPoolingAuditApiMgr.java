/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxPoolingAuditApiMgr.java
 * 下午4:30:15 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import org.springframework.context.ApplicationContext;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxPoolingAuditApiMgr.java
 * @dateTime 2013-10-22 下午4:30:15
 */

public interface GoogleAdxPoolingAuditApiMgr {

	/**
	 * 获取google adx的审核结果，并更新db
	 * 
	 * (1) 获取api调用的审核结果
	 * (2) 更新数据库
	 * 
	 * @param context
	 */
	public void dealAuditResult4Google(ApplicationContext context);

}
