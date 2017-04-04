/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxAuditApiMgr.java
 * 下午1:11:51 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import org.springframework.context.ApplicationContext;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxAuditApiMgr.java
 * @dateTime 2013-10-22 下午1:11:51
 */

public interface GoogleAdxAuditApiMgr {

	/**
	 * 为google adx 的可投放创意进行审核
	 * 
	 * (1) 获取可审核物料
	 * (2) 抽取创意的主域
	 * (3) 发起google api调用
	 * (4) 更新数据库
	 * 
	 * @param updateDate【默认是昨天，格式“yyyy-MM-dd”】
	 * @param context
	 */
	public void audit4Google(String updateDate, ApplicationContext context);

}
