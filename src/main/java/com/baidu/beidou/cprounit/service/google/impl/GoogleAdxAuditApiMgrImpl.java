/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.impl.GoogleAdxAuditApiMgrImpl.java
 * 下午1:31:33 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.service.UnitAdxMgrOnRead;
import com.baidu.beidou.cprounit.service.google.GoogleAdxAuditApiMgr;
import com.baidu.beidou.cprounit.service.google.executor.GoogleAdxAuditApiExecutor;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.page.DataPage;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxAuditApiMgrImpl.java
 * @dateTime 2013-10-22 下午1:31:33
 */
public class GoogleAdxAuditApiMgrImpl implements GoogleAdxAuditApiMgr {

	private int auditBatchNumber;

	private long auditBatchSleepTime;

	private UnitAdxMgrOnRead unitAdxMgrOnRead;

	public void audit4Google(String updateDate, ApplicationContext context) {

		// 获取全部待审核的创意
		List<UnitAdxGoogleApiVo> auditList = unitAdxMgrOnRead.getGoogleAdxApiUnitList(updateDate);
		if (CollectionUtils.isEmpty(auditList)) {
			return;
		}

		int pageNo = 1;
		boolean next = false;
		// 分页待审核创意
		do {
			// 分页提交审核
			DataPage<UnitAdxGoogleApiVo> page = DataPage.getByList(auditList, auditBatchNumber, pageNo);
			List<UnitAdxGoogleApiVo> innerAuditList = page.getRecord();
			next = page.hasNextPage();
			pageNo++;
			

			// 抽取出创意主域
			Map<String, List<UnitAdxGoogleApiVo>> auditMap = groupUnitByDomain(innerAuditList);
			if (MapUtils.isEmpty(auditMap)) {
				return;
			}

			// 分主域提交审核
			for (String domain : auditMap.keySet()) {
				GoogleAdxAuditApiExecutor.submit(domain, auditMap.get(domain), context);
			}
			
			try {
				TimeUnit.MILLISECONDS.sleep(auditBatchSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} while (next);

		// 关闭服务
		GoogleAdxAuditApiExecutor.shutdown();
	}

	private Map<String, List<UnitAdxGoogleApiVo>> groupUnitByDomain(List<UnitAdxGoogleApiVo> auditList) {
		if (CollectionUtils.isEmpty(auditList)) {
			return Collections.emptyMap();
		}

		Map<String, List<UnitAdxGoogleApiVo>> result = new HashMap<String, List<UnitAdxGoogleApiVo>>(auditList.size());
		for (UnitAdxGoogleApiVo vo : auditList) {
			String targetUrl = vo.getTargetUrl();
			String domain = UrlParser.getMainDomain4GoogleApi(targetUrl);
			if (StringUtils.isNotEmpty(domain)) {
				if (result.containsKey(domain)) {
					result.get(domain).add(vo);
				} else {
					List<UnitAdxGoogleApiVo> innerList = new ArrayList<UnitAdxGoogleApiVo>();
					innerList.add(vo);
					result.put(domain, innerList);
				}
			}
		}

		return result;
	}

	public void setUnitAdxMgrOnRead(UnitAdxMgrOnRead unitAdxMgrOnRead) {
		this.unitAdxMgrOnRead = unitAdxMgrOnRead;
	}

	public void setAuditBatchNumber(int auditBatchNumber) {
		this.auditBatchNumber = auditBatchNumber;
	}

	public void setAuditBatchSleepTime(long auditBatchSleepTime) {
		this.auditBatchSleepTime = auditBatchSleepTime;
	}

}
