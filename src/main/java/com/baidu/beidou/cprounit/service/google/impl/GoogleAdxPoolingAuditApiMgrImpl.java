/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.impl.GoogleAdxPoolingAuditApiMgrImpl.java
 * 下午4:32:53 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.UnitAdxMgr;
import com.baidu.beidou.cprounit.service.UnitAdxMgrOnRead;
import com.baidu.beidou.cprounit.service.google.GoogleAdxPoolingAuditApiMgr;
import com.baidu.beidou.cprounit.service.google.api.GoogleApiHelper;
import com.baidu.beidou.util.page.DataPage;
import com.google.api.services.adexchangebuyer.Adexchangebuyer;
import com.google.api.services.adexchangebuyer.model.Creative;
import com.google.api.services.adexchangebuyer.model.CreativesList;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxPoolingAuditApiMgrImpl.java
 * @dateTime 2013-10-22 下午4:32:53
 * 
 * google api: https://developers.google.com/ad-exchange/buyer-rest/v1.3/creatives/list
 */

public class GoogleAdxPoolingAuditApiMgrImpl implements GoogleAdxPoolingAuditApiMgr {

	private static final Log log = LogFactory.getLog(GoogleAdxPoolingAuditApiMgrImpl.class);

	private long maxResultsPerPage;

	// 分页获取“审核结果”的间隔时间[ms]
	private long callInterval;

	// api调用重试次数
	private int apiCallRetryTime;

	// 全局的“pageToken”， 用于记录每次的请求之后是否存在下一页
	private String pageToken = null;

	// 审核结果页数
	private int pageCount = 0;

	// 审核结果列表(只包含“审核通过”和“审核拒绝”的创意)
	List<CreativeVo> auditResultList = new ArrayList<GoogleAdxPoolingAuditApiMgrImpl.CreativeVo>();

	// 创意-用户关联关系
	private Map<Long, Integer> adUserMap;

	// 按“userid”分组后，审核成功的db的map
	private Map<Integer, List<Long>> auditSuccessMap;

	// 按“userid”分组后，审核失败的db的map
	private Map<Integer, List<Long>> auditFailedMap;

	// 单次批量更新最大限制
	private int upadteMaxNum;

	// update db间隔时间[ms]
	private long updateSleepTime;

	private UnitAdxMgr unitAdxMgr;

	private UnitAdxMgrOnRead unitAdxMgrOnRead;

	public void dealAuditResult4Google(ApplicationContext context) {
		long startTime = System.currentTimeMillis();
		log.info("GoogleAdxPoolingAuditApiMgr start.");

		// 调用获取审核结果api
		callGoogleAuditApi();

		// 查询创意-用户关联关系
		queryAdUserRelation();

		// 根据用户分组
		groupUnitByUser();

		// 更新成功审核db
		updateDB4GoogleApi(auditSuccessMap, CproUnitConstant.GOOGLE_AUDIT_STATE.APPROVED.getStateValue());

		// 更新失败审核db
		updateDB4GoogleApi(auditFailedMap, CproUnitConstant.GOOGLE_AUDIT_STATE.DISAPPROVED.getStateValue());

		log.info("GoogleAdxPoolingAuditApiMgr end," + (System.currentTimeMillis() - startTime) / 1000 + "s");

	}

	public void callGoogleAuditApi() {

		while ((pageCount == 0) || (StringUtils.isNotEmpty(pageToken))) {

			CreativesList creativesList = callApiWithRetry(pageToken);

			if (creativesList != null) {
				pageToken = creativesList.getNextPageToken();

				List<Creative> items = creativesList.getItems();
				if (CollectionUtils.isNotEmpty(items)) {
					for (Creative creative : items) {
						String status = creative.getStatus();
						if (status.equalsIgnoreCase(CproUnitConstant.GOOGLE_AUDIT_STATE.APPROVED.getStateName()) || status.equalsIgnoreCase(CproUnitConstant.GOOGLE_AUDIT_STATE.DISAPPROVED.getStateName())) {
							auditResultList.add(new CreativeVo(creative.getBuyerCreativeId(), status));
						}
					}
				}

				if (StringUtils.isNotEmpty(pageToken)) {
					try {
						TimeUnit.MILLISECONDS.sleep(callInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			pageCount++;
		}

		log.info("GoogleAdxAuditApiTask:(1) callGoogleAuditApi, auditResultList size: " + auditResultList.size());

	}

	private CreativesList callApiWithRetry(String pageToken) {

		boolean apiCallSuccess = false;

		CreativesList creativesList = null;

		for (int i = 0; (i < apiCallRetryTime && apiCallSuccess == false); i++) {
			try {

				Adexchangebuyer client = GoogleApiHelper.getAdexchangebuyerClient();

				if (client == null) {
					apiCallSuccess = false;
					continue;
				}

				if (StringUtils.isEmpty(pageToken)) {
					creativesList = client.creatives().list().setPageToken(pageToken).setMaxResults(maxResultsPerPage).execute();
				} else {
					creativesList = client.creatives().list().setPageToken(pageToken).setMaxResults(maxResultsPerPage).setPageToken(pageToken).execute();
				}

				if (creativesList != null) {
					apiCallSuccess = true;
				} else {
					apiCallSuccess = false;
				}
			} catch (Exception e) {
				apiCallSuccess = false;
			} finally {
				if ((!apiCallSuccess) && (i < (apiCallRetryTime - 1))) {
					try {
						TimeUnit.MILLISECONDS.sleep(callInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return creativesList;

	}

	public void queryAdUserRelation() {
		if (CollectionUtils.isEmpty(auditResultList)) {
			adUserMap = new HashMap<Long, Integer>(0);
			return;
		}
		adUserMap = unitAdxMgrOnRead.getAdUserRelation();

		log.info("GoogleAdxAuditApiTask:(2) queryAdUserRelation, adUserMap size: " + adUserMap.size());
	}

	public void groupUnitByUser() {
		if (MapUtils.isEmpty(adUserMap)) {
			auditSuccessMap = new HashMap<Integer, List<Long>>(0);
			auditFailedMap = new HashMap<Integer, List<Long>>(0);
			return;
		}

		auditSuccessMap = new HashMap<Integer, List<Long>>(auditResultList.size());
		auditFailedMap = new HashMap<Integer, List<Long>>(auditResultList.size());

		for (CreativeVo vo : auditResultList) {

			long adid = Long.valueOf(vo.getBuyerCreativeId());
			String status = vo.getStatus();

			if (adUserMap.containsKey(adid)) {
				int userid = adUserMap.get(adid);

				if (status.equalsIgnoreCase(CproUnitConstant.GOOGLE_AUDIT_STATE.APPROVED.getStateName())) {

					if (auditSuccessMap.containsKey(userid)) {
						auditSuccessMap.get(userid).add(Long.valueOf(vo.getBuyerCreativeId()));
					} else {
						List<Long> innerList = new ArrayList<Long>();
						innerList.add(Long.valueOf(vo.getBuyerCreativeId()));
						auditSuccessMap.put(userid, innerList);
					}

				} else if (status.equalsIgnoreCase(CproUnitConstant.GOOGLE_AUDIT_STATE.DISAPPROVED.getStateName())) {

					if (auditFailedMap.containsKey(userid)) {
						auditFailedMap.get(userid).add(Long.valueOf(vo.getBuyerCreativeId()));
					} else {
						List<Long> innerList = new ArrayList<Long>();
						innerList.add(Long.valueOf(vo.getBuyerCreativeId()));
						auditFailedMap.put(userid, innerList);
					}

				}

			}

		}

		log.info("GoogleAdxAuditApiTask:(3) groupUnitByUser, auditSuccessMap : " + auditSuccessMap.size() + ", auditFailedMap : " + auditFailedMap.size());
	}

	public void updateDB4GoogleApi(Map<Integer, List<Long>> auditMap, int googleAuditState) {
		if (MapUtils.isEmpty(auditMap)) {
			return;
		}
		for (int userid : auditMap.keySet()) {
			List<Long> adIdList = auditMap.get(userid);
			if (CollectionUtils.isNotEmpty(adIdList)) {
				// 分批更新数据库
//				List<List<Long>> adIdPageList = PageUtil.pageAds(adIdList, upadteMaxNum);
//				for (List<Long> adIdListInPage : adIdPageList) {
				int pageNo = 1;
				boolean next = false;
				do {
					DataPage<Long> adIdListInPage = DataPage.getByList(adIdList, upadteMaxNum, pageNo);
					unitAdxMgr.updateGoogleAdxAPiState(userid, adIdListInPage.getRecord(), googleAuditState);
					next = adIdListInPage.hasNextPage();
					pageNo++;

					try {
						TimeUnit.MILLISECONDS.sleep(updateSleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} while (next);
			}
		}
		log.info("GoogleAdxAuditApiTask:(4) updateDB4GoogleApi, " + " auditMap size " + auditMap.size() + ", googleAuditState " + googleAuditState);
	}

	public void setUnitAdxMgr(UnitAdxMgr unitAdxMgr) {
		this.unitAdxMgr = unitAdxMgr;
	}

	public void setMaxResultsPerPage(long maxResultsPerPage) {
		this.maxResultsPerPage = maxResultsPerPage;
	}

	public void setCallInterval(long callInterval) {
		this.callInterval = callInterval;
	}

	public void setApiCallRetryTime(int apiCallRetryTime) {
		this.apiCallRetryTime = apiCallRetryTime;
	}

	public void setUpadteMaxNum(int upadteMaxNum) {
		this.upadteMaxNum = upadteMaxNum;
	}

	public void setUpdateSleepTime(long updateSleepTime) {
		this.updateSleepTime = updateSleepTime;
	}

	public void setUnitAdxMgrOnRead(UnitAdxMgrOnRead unitAdxMgrOnRead) {
		this.unitAdxMgrOnRead = unitAdxMgrOnRead;
	}

	class CreativeVo {

		// 对应beidou的adid
		private String buyerCreativeId;

		// 对应beidou的google unit的审核状态
		private String status;

		private CreativeVo() {
			super();
		}

		private CreativeVo(String buyerCreativeId, String status) {
			super();
			this.buyerCreativeId = buyerCreativeId;
			this.status = status;
		}

		public String getBuyerCreativeId() {
			return buyerCreativeId;
		}

		public void setBuyerCreativeId(String buyerCreativeId) {
			this.buyerCreativeId = buyerCreativeId;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}

}
