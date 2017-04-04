package com.baidu.beidou.bes.user.template.tencent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bes.user.bean.AuditResultBean;
import com.baidu.beidou.bes.user.bean.ResultInfo;
import com.baidu.beidou.bes.user.constant.UserConstant;
import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;
import com.baidu.beidou.bes.user.util.HttpClientUtil;
import com.baidu.beidou.util.JsonUtils;

/**
 * 调用tencent api获取审核结果并更新数据库任务
 * 
 * @author caichao
 */
public class TencentGetResultTask implements Runnable {

	private static final Log log = LogFactory.getLog(TencentGetResultTask.class);
	private List<AuditUserInfo> users;
	private AuditUserServiceMgr auditUserMgr;

	private Map<String, String> authMap;
	private HttpClientUtil httpClientUtil;
	private String syncUrl;
	private Integer company;

	public TencentGetResultTask() {

	}

	//name——userid的倒排
	private Map<String, Integer> userMap;

	@Override
	public void run() {
		if (CollectionUtils.isEmpty(users)) {
			return;
		}
		//建立name--userid倒排，用於解析api result
		generateMap();

		//请求tencent 获取审核结果api
		AuditResultBean result = callTencentApi();

		//解析api结果更新DB
		if (result == null) {
			return;
		}
		Long startTime = System.currentTimeMillis();
		updateDB(result);
		log.info("api result update database cost time : " + (System.currentTimeMillis() - startTime) + "ms");
	}

	private void updateDB(AuditResultBean result) {
		//通过审核的用户list
		List<Integer> passList = new ArrayList<Integer>();

		//待审核用户list
		List<Integer> waitAuditList = new ArrayList<Integer>();

		//不通过审核用户map 每个不通过审核用户原因不一致，故需要单独更新数据库
		Map<Integer, String> unpassMap = new HashMap<Integer, String>();

		if (result.getRet_code() == 0) {
			Map<String, ResultInfo> resultMap = result.getRet_msg();

			filter(resultMap, passList, waitAuditList, unpassMap);

		}
		//更新通过审核用户状态
		auditUserMgr.updateAuditPass(passList, company, UserConstant.AUDIT_SUCCESS);

		//更新等待审核用户状态
		auditUserMgr.updateAuditPass(waitAuditList, company, UserConstant.AUDITING);

		//更新不通过审核用户状态和原因
		auditUserMgr.updateAuditUnPass(unpassMap, company, UserConstant.AUDIT_FAILT);
	}

	/**
	 * 解析api result
	 * @param resultMap
	 * @param passList
	 * @param waitAuditList
	 * @param unpassList
	 * 下午7:33:10 created by caichao
	 */
	private void filter(Map<String, ResultInfo> resultMap, List<Integer> passList, List<Integer> waitAuditList, Map<Integer, String> unpassList) {

		for (Map.Entry<String, ResultInfo> entry : resultMap.entrySet()) {
			if (UserConstant.PASS.equals(entry.getValue().getVerify_status())) {
				passList.add(userMap.get(entry.getKey()));
			} else if (UserConstant.WAIT_AUDIT.equals(entry.getValue().getVerify_status())) {
				waitAuditList.add(userMap.get(entry.getKey()));
			} else {
				unpassList.put(userMap.get(entry.getKey()), entry.getValue().getAudit_info());
			}
		}
	}

	private void generateMap() {
		userMap = new HashMap<String, Integer>(users.size());
		for (AuditUserInfo info : users) {
			userMap.put(info.getName(), info.getUserId());
		}

	}

	//调用腾讯api
	private AuditResultBean callTencentApi() {
		AuditResultBean resultBean = new AuditResultBean();
		try {
			String clientInfo = JsonUtils.toJson(convert(users));
			authMap.put("names", clientInfo);

			String result = httpClientUtil.post(authMap, syncUrl);

			if (result == null || "".equals(result)) {
				return null;
			}
			//由于api返回正常时ret_message为字符串，jsonutil无法正确反射出bean，故先判断ret_code
			Map<String, String> jsonMap = JsonUtils.toMap(result);

			String retMsg = jsonMap.get("ret_msg");
			log.info("ret_msg is : " + retMsg);

			if ("[]".equals(retMsg)) {
				return null;
			} else {
				resultBean = (AuditResultBean) JsonUtils.json2Object(result, AuditResultBean.class);
			}

		} catch (Exception e) {
			log.error("parse api result occur error", e);
		}

		return resultBean;
	}

	private List<String> convert(List<AuditUserInfo> users) {
		List<String> result = new ArrayList<String>();

		if (!CollectionUtils.isEmpty(users)) {
			for (AuditUserInfo info : users) {
				result.add(info.getName());
			}
		}
		return result;
	}

	public List<AuditUserInfo> getUsers() {
		return users;
	}

	public void setUsers(List<AuditUserInfo> users) {
		this.users = users;
	}

	public AuditUserServiceMgr getAuditUserMgr() {
		return auditUserMgr;
	}

	public void setAuditUserMgr(AuditUserServiceMgr auditUserMgr) {
		this.auditUserMgr = auditUserMgr;
	}

	public Map<String, String> getAuthMap() {
		return authMap;
	}

	public void setAuthMap(Map<String, String> authMap) {
		this.authMap = authMap;
	}

	public HttpClientUtil getHttpClientUtil() {
		return httpClientUtil;
	}

	public void setHttpClientUtil(HttpClientUtil httpClientUtil) {
		this.httpClientUtil = httpClientUtil;
	}

	public String getSyncUrl() {
		return syncUrl;
	}

	public void setSyncUrl(String syncUrl) {
		this.syncUrl = syncUrl;
	}

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

}
