/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxSnapshotExecutor.java
 * 下午12:00:17 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.executor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxSnapshotExecutor.java
 * @dateTime 2013-10-17 下午12:00:17
 */

public class GoogleAdxSnapshotExecutor {

	public static int SNAPSHOT_TASK_NUM = 100;

	private static final ExecutorService pool = Executors.newFixedThreadPool(SNAPSHOT_TASK_NUM);

	public static void submit(int userid, List<UnitAdxSnapshotVo> snapshotList, ApplicationContext context) {
		if (CollectionUtils.isEmpty(snapshotList)) {
			return;
		}
		GoogleAdxSnapshotTask task = (GoogleAdxSnapshotTask) context.getBean("googleAdxSnapshotTask");
		task.setUserid(userid);
		task.setSnapshotList(snapshotList);

		pool.submit(task);
	}

	public static void shutdown() {
		pool.shutdown();
	}

	public void setSNAPSHOT_TASK_NUM(int sNAPSHOT_TASK_NUM) {
		SNAPSHOT_TASK_NUM = sNAPSHOT_TASK_NUM;
	}

}
