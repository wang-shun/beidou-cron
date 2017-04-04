/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.executor.GoogleAdxAuditApiExecutor.java
 * 下午3:01:53 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.executor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxAuditApiExecutor.java
 * @dateTime 2013-10-22 下午3:01:53
 */

public class GoogleAdxAuditApiExecutor {

	public static int AUDIT_TASK_NUM = 100;

	private static final ExecutorService pool = Executors.newFixedThreadPool(AUDIT_TASK_NUM);

	public static void submit(String domain, List<UnitAdxGoogleApiVo> auditList, ApplicationContext context) {
		if (CollectionUtils.isEmpty(auditList)) {
			return;
		}

		GoogleAdxAuditApiTask task = (GoogleAdxAuditApiTask) context.getBean("googleAdxAuditApiTask");
		task.setDomain(domain);
		task.setAuditList(auditList);

		pool.submit(task);
	}

	public static void shutdown() {
		pool.shutdown();
	}

	public void setAUDIT_TASK_NUM(int aUDIT_TASK_NUM) {
		AUDIT_TASK_NUM = aUDIT_TASK_NUM;
	}

}
