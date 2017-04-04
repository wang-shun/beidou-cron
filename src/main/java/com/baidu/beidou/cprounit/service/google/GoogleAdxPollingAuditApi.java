/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxPollingAuditApi.java
 * 下午4:24:07 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxPollingAuditApi.java
 * @dateTime 2013-10-22 下午4:24:07
 */

public class GoogleAdxPollingAuditApi {

	private static final Log log = LogFactory.getLog(GoogleAdxPollingAuditApi.class);

	public static void main(String[] args) throws Exception {

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml", "classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(paths);

		long startTime = System.currentTimeMillis();

		GoogleAdxPoolingAuditApiMgr googleAdxPoolingAuditApiMgr = (GoogleAdxPoolingAuditApiMgr) context.getBean("googleAdxPoolingAuditApiMgr");
		googleAdxPoolingAuditApiMgr.dealAuditResult4Google(context);

		log.info("GoogleAdxPollingAuditApi end, spend: " + (System.currentTimeMillis() - startTime) / 1000 + " s.");
	}
}
