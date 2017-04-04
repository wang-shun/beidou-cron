/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxSnapshot.java
 * 下午2:18:07 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxSnapshot.java
 * @dateTime 2013-10-16 下午2:18:07
 */

public class GoogleAdxSnapshot {

	private static final Log log = LogFactory.getLog(GoogleAdxSnapshot.class);

	public static void main(String[] args) throws Exception {

		if (args == null || args.length != 1) {
			log.error("The param's is not right. Usage: GoogleAdxSnapshot snapshotUpdateDate");
			System.exit(1);
		}

		String updateDate = args[0];

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml", "classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(paths);

		long startTime = System.currentTimeMillis();

		GoogleAdxSnapshotMgr googleAdxSnapshotMgr = (GoogleAdxSnapshotMgr) context.getBean("googleAdxSnapshotMgr");
		googleAdxSnapshotMgr.snapshot4Google(updateDate, context);

		log.info("GoogleAdxSnapshot end, spend: " + (System.currentTimeMillis() - startTime) / 1000 + " s.");
	}
}
