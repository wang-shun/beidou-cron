/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.executor.GoogleAdxAuditApiTask.java
 * 下午3:04:03 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.UnitAdxMgr;
import com.baidu.beidou.cprounit.service.google.api.GoogleApiHelper;
import com.baidu.beidou.util.page.DataPage;
import com.google.api.services.adexchangebuyer.Adexchangebuyer;
import com.google.api.services.adexchangebuyer.model.Creative;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxAuditApiTask.java
 * @dateTime 2013-10-22 下午3:04:03
 * 
 * google api: https://developers.google.com/ad-exchange/buyer-rest/v1.3/creatives/insert
 */

public class GoogleAdxAuditApiTask implements Callable<Boolean> {

	private static final Log log = LogFactory.getLog(GoogleAdxAuditApiTask.class);

	private String domain;

	private List<UnitAdxGoogleApiVo> auditList;

	private UnitAdxMgr unitAdxMgr;

	// google adx账号Id
	private int googleAdxAccountId;

	// api调用重试次数
	private int apiCallRetryTime;

	// 单次批量更新最大限制
	private int upadteMaxNum;

	// 待更新db的审核状态
	private int googleApiAuditState;

	// update db间隔时间[ms]
	private long auditUpdateSleepTime;

	// 调用api服务间隔时间[ms]
	private long auditCallInterval;

	// 按“userid”分组后，待更新db的map
	private Map<Integer, List<Long>> resultMap;

	public GoogleAdxAuditApiTask() {
		super();
	}

	public GoogleAdxAuditApiTask(String domain, List<UnitAdxGoogleApiVo> auditList) {
		super();
		this.domain = domain;
		this.auditList = auditList;
	}

	public Boolean call() throws Exception {
		long startTime = System.currentTimeMillis();
		log.info("GoogleAdxAuditApiTask start, domain: " + domain + " auditList size: " + auditList.size());

		// 调用审核api
		callGoogleAuditApi();

		// 根据用户分组
		groupUnitByUser();

		// 分批更新数据库
		updateDB4GoogleApi();

		log.info("GoogleAdxAuditApiTask end, domain: " + domain + ", expend " + (System.currentTimeMillis() - startTime) / 1000 + "s");

		return true;
	}

	public void callGoogleAuditApi() {

		Collections.shuffle(auditList);
		UnitAdxGoogleApiVo vo = auditList.get(0);

		Creative creative = new Creative();
		creative.setAccountId(googleAdxAccountId);
		creative.setAdvertiserName(String.valueOf(vo.getUserid()));
		creative.setBuyerCreativeId(String.valueOf(vo.getAdid()));
		// google审核api调用，需要强制设置hTMLSnippet，这里采用domain填充
		creative.setHTMLSnippet(domain);

		List<String> clickThroughUrls = new ArrayList<String>();
		clickThroughUrls.add(domain);
		creative.setClickThroughUrl(clickThroughUrls);

		creative.setHeight(vo.getHeight());
		creative.setWidth(vo.getWidth());

		boolean apiCallSuccess = false;
		Creative response = null;

		for (int i = 0; (i < apiCallRetryTime && apiCallSuccess == false); i++) {
			try {
				Adexchangebuyer client = GoogleApiHelper.getAdexchangebuyerClient();
				if (client == null) {
					apiCallSuccess = false;
					continue;
				}

				response = client.creatives().insert(creative).execute();
				if (response != null) {
					apiCallSuccess = true;
				} else {
					apiCallSuccess = false;
				}
			} catch (Exception e) {
				apiCallSuccess = false;
			} finally {
				if ((!apiCallSuccess) && (i < (apiCallRetryTime - 1))) {
					try {
						TimeUnit.MILLISECONDS.sleep(auditCallInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}

		if ((response != null)) {
			String state = response.getStatus();
			if (CproUnitConstant.GOOGLE_AUDIT_STATE.isAuditStateValie(state)) {
				googleApiAuditState = CproUnitConstant.GOOGLE_AUDIT_STATE.getStateValue(state);
			} else {
				googleApiAuditState = CproUnitConstant.GOOGLE_AUDIT_STATE.DISAPPROVED.getStateValue();
			}
		} else {
			googleApiAuditState = CproUnitConstant.GOOGLE_AUDIT_STATE.DISAPPROVED.getStateValue();
		}

		log.info("GoogleAdxAuditApiTask:(1) callGoogleAuditApi, auditList: " + auditList.size() + ", domain: " + domain);

	}

	public void groupUnitByUser() {
		if (CollectionUtils.isEmpty(auditList)) {
			resultMap = new HashMap<Integer, List<Long>>(0);
		}

		resultMap = new HashMap<Integer, List<Long>>(auditList.size());

		for (UnitAdxGoogleApiVo vo : auditList) {
			int userid = vo.getUserid();
			if (resultMap.containsKey(userid)) {
				resultMap.get(userid).add(vo.getAdid());
			} else {
				List<Long> innerList = new ArrayList<Long>();
				innerList.add(vo.getAdid());
				resultMap.put(userid, innerList);
			}
		}
		log.info("GoogleAdxAuditApiTask:(2) groupUnitByUser, resultMap size: " + resultMap.size());
	}

	public void updateDB4GoogleApi() {
		if (MapUtils.isEmpty(resultMap)) {
			return;
		}
		for (int userid : resultMap.keySet()) {
			List<Long> adIdList = resultMap.get(userid);
			if (CollectionUtils.isNotEmpty(adIdList)) {
				// 分批更新数据库

//				List<List<Long>> adIdPageList = PageUtil.pageAds(adIdList, upadteMaxNum);
//				for (List<Long> adIdListInPage : adIdPageList) {
				int pageNo = 1;
				boolean next = false;
				do {
					DataPage<Long> adIdListInPage = DataPage.getByList(adIdList, upadteMaxNum, pageNo);
					unitAdxMgr.updateGoogleAdxAPiState(userid, adIdListInPage.getRecord(), googleApiAuditState);
					next = adIdListInPage.hasNextPage();
					pageNo++;
					
					try {
						TimeUnit.MILLISECONDS.sleep(auditUpdateSleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} while (next);
			}
		}
		log.info("GoogleAdxAuditApiTask:(3) updateDB4GoogleApi, googleApiAuditState: " + googleApiAuditState);
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setAuditList(List<UnitAdxGoogleApiVo> auditList) {
		this.auditList = auditList;
	}

	public void setUnitAdxMgr(UnitAdxMgr unitAdxMgr) {
		this.unitAdxMgr = unitAdxMgr;
	}

	public void setApiCallRetryTime(int apiCallRetryTime) {
		this.apiCallRetryTime = apiCallRetryTime;
	}

	public void setUpadteMaxNum(int upadteMaxNum) {
		this.upadteMaxNum = upadteMaxNum;
	}

	public void setAuditUpdateSleepTime(long auditUpdateSleepTime) {
		this.auditUpdateSleepTime = auditUpdateSleepTime;
	}

	public void setAuditCallInterval(long auditCallInterval) {
		this.auditCallInterval = auditCallInterval;
	}

	public void setGoogleAdxAccountId(int googleAdxAccountId) {
		this.googleAdxAccountId = googleAdxAccountId;
	}

}
