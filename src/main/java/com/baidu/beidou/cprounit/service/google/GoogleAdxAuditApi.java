/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxAuditApi.java
 * 下午12:31:23 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxAuditApi.java
 * @dateTime 2013-10-22 下午12:31:23
 */

public class GoogleAdxAuditApi {
	private static final Log log = LogFactory.getLog(GoogleAdxAuditApi.class);

	public static void main(String[] args) throws Exception {

		if (args == null || args.length != 1) {
			log.error("The param's is not right. Usage: GoogleAdxAuditApi googleAuditApiDay");
			System.exit(1);
		}

		String updateDate = args[0];

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml", "classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(paths);

		long startTime = System.currentTimeMillis();

		GoogleAdxAuditApiMgr googleAdxAuditApiMgr = (GoogleAdxAuditApiMgr) context.getBean("googleAdxAuditApiMgr");
		googleAdxAuditApiMgr.audit4Google(updateDate, context);

		log.info("GoogleAdxAuditApi end, spend: " + (System.currentTimeMillis() - startTime) / 1000 + " s.");
	}
}
