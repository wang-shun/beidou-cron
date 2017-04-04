package com.baidu.beidou.bes.user.template.tencent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.bes.user.po.AuditUserInfo;
/**
 * 提交任务公共类
 * 
 * @author caichao
 */
public class TenctentApiExcutor {
	public static int AUDIT_TASK_NUM = 8;

	private static final ExecutorService pool = Executors.newFixedThreadPool(AUDIT_TASK_NUM);

	public static void submit(List<AuditUserInfo> list,Integer company, ApplicationContext context) {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		TencentPushUserTask task = (TencentPushUserTask) context.getBean("tencentPushUserTask");
		task.setUsers(list);
		task.setCompany(company);
		pool.execute(task);
	}
	
	public static void submitResult(List<AuditUserInfo> list,Integer company, ApplicationContext context) {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		TencentGetResultTask task = (TencentGetResultTask) context.getBean("tencentAuditResultTask");
		task.setUsers(list);
		task.setCompany(company);
		pool.execute(task);
	}

	public static void shutdown() {
		pool.shutdown();
	}
}
