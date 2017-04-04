package com.baidu.beidou.bes.user.template;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;
import com.baidu.beidou.bes.user.template.tencent.TenctentApiExcutor;
import com.baidu.beidou.util.page.DataPage;

public class CallAdxApi {
	private static final Log log = LogFactory.getLog(CallAdxApi.class);

	private AuditUserServiceMgr auditUserMgr;

	private Integer company;

	/**
	 * 调用api推送广告主信息 
	 */
	public void push(ApplicationContext context) {
		log.info(new Date() + "---------begin push user");
		long startTime = System.currentTimeMillis();
		//读取需推送数据
		List<AuditUserInfo> users = auditUserMgr.getAuditUserList(company);

		if (!CollectionUtils.isEmpty(users)) {

			int pageNo = 1;
			int pageSize = 50;
			boolean next = false;

			do {
				DataPage<AuditUserInfo> page = DataPage.getByList(users, pageSize, pageNo);

				TenctentApiExcutor.submit(page.getRecord(),company, context);
				pageNo++;
				next = page.hasNextPage();
			} while (next);
		}

		TenctentApiExcutor.shutdown();
		
		log.info("push user finish cost time " + (System.currentTimeMillis()-startTime) + "ms");
	}
	
	/**
	 * 调用api获取审核结果
	 * @return
	 */
	
	public void getAuditResult(ApplicationContext context) {
		log.info(new Date() + "---------begin get audit result");
		long startTime = System.currentTimeMillis();
		
		List<AuditUserInfo> users = auditUserMgr.getHasPushedUser(company);
		if (!CollectionUtils.isEmpty(users)) {

			int pageNo = 1;
			int pageSize = 50;
			boolean next = false;

			do {
				DataPage<AuditUserInfo> page = DataPage.getByList(users, pageSize, pageNo);

				TenctentApiExcutor.submitResult(page.getRecord(),company, context);
				pageNo++;
				next = page.hasNextPage();
			} while (next);
		}

		TenctentApiExcutor.shutdown();
		log.info("get audit result finish cost time " + (System.currentTimeMillis()-startTime) + "ms");
	}

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public AuditUserServiceMgr getAuditUserMgr() {
		return auditUserMgr;
	}

	public void setAuditUserMgr(AuditUserServiceMgr auditUserMgr) {
		this.auditUserMgr = auditUserMgr;
	}

	
}
