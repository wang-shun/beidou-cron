package com.baidu.beidou.bes.user.template.tencent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bes.user.bean.PushUserResultBean;
import com.baidu.beidou.bes.user.constant.UserConstant;
import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.bes.user.request.TencentRequestType;
import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;
import com.baidu.beidou.bes.user.util.HttpClientUtil;
import com.baidu.beidou.util.JsonUtils;
/**
 * 推送腾讯广告主信息任务类
 * 
 * @author caichao
 */
public class TencentPushUserTask implements Runnable {
	private static final Log log = LogFactory.getLog(TencentPushUserTask.class);
	private List<AuditUserInfo> users;
	private AuditUserServiceMgr auditUserMgr;

	private Map<String, String> authMap;
	private HttpClientUtil httpClientUtil;
	private String syncUrl;
	private Integer company;
	
	//name——userid的倒排
	private Map<String,Integer> userMap;

	public TencentPushUserTask() {

	}

	@Override
	public void run() {
		if (CollectionUtils.isEmpty(users)) {
			return ;
		}
		
		//建立name--userid倒排，用於解析api result
		generateMap();
		
		//call api
		PushUserResultBean result = callTencentApi();

		//解析api结果更新DB
		if (result == null) {
			return ;
		}
		updateDB(result);

	}

	private void generateMap() {
		userMap = new HashMap<String, Integer>(users.size());
		for (AuditUserInfo info : users) {
			userMap.put(info.getName(), info.getUserId());
		}
		
	}

	

	//调用腾讯api
	private PushUserResultBean callTencentApi() {
		PushUserResultBean resultBean = new PushUserResultBean();
		try {
			String clientInfo = JsonUtils.toJson(convert(users));
			authMap.put("client_info", clientInfo);

			String result = httpClientUtil.post(authMap, syncUrl);
			log.info(result);
			
			if (result == null || "".equals(result)) {
				return null;
			}
			//由于api返回正常时ret_message为字符串，jsonutil无法正确反射出bean，故先判断ret_code
			Map<String, String> jsonMap = JsonUtils.toMap(result);

			String retCode = jsonMap.get("ret_code");
			log.info("ret_code is : " + retCode);

			if (UserConstant.SUC_CODE.equals(retCode) || UserConstant.SYS_ERROR_CODE.equals(retCode)) {
				resultBean.setRet_code(Integer.valueOf(retCode));
			} else {
				resultBean = (PushUserResultBean) JsonUtils.json2Object(result, PushUserResultBean.class);
			}

		} catch (Exception e) {
			log.error("parse api result occur error", e);
		}

		return resultBean;
	}

	//修改数据库状态
	private void updateDB(PushUserResultBean resultBean) {
		Long start = System.currentTimeMillis();
		//全部推送成功，修改users 为状态为审核中
		if (resultBean.getRet_code() == UserConstant.SUCCESS) {
			auditUserMgr.updateAuditStatus(users, company, UserConstant.AUDITING);
		}
		//部分成功，修改成功的为审核中，失败的改成“推送失败”
		else if (resultBean.getRet_code() == UserConstant.PART_SUCCESS) {
			Map<Integer, List<String>> msgMap = resultBean.getRet_msg();
			
			Map<Integer,List<Integer>> codeUseid = mapping(msgMap);
			
			//處理失敗
			auditUserMgr.updateAuditStatus(codeUseid,company, UserConstant.PUSH_AUDIT_FAIL);
			
			List<AuditUserInfo> sucList = getSucList(codeUseid);

			//处理成功的

			auditUserMgr.updateAuditStatus(sucList, company, UserConstant.AUDITING);

			
		}
		//全部失败，修改状态为“全部失败”，并写入失败原因
		else if (resultBean.getRet_code() == UserConstant.FAIL) {

			//map [key] error_code [value] --> list[userid]
			Map<Integer,List<Integer>> failMap = mapping(resultBean.getRet_msg());
			
			log.info(failMap.size());
			auditUserMgr.updateAuditStatus(failMap,company, UserConstant.PUSH_AUDIT_FAIL);
		}
		//系统失败或api内部错误
		else if (resultBean.getRet_code() == UserConstant.SYS_FAIL) {
			//记录日志，下次继续请求
			log.error("adx api inner occur error [ret_code] " + resultBean.getRet_code());
		}

		log.info("update database status cost time " + (System.currentTimeMillis() - start) + "ms");
	}

	private List<AuditUserInfo> getSucList(Map<Integer, List<Integer>> codeUseid) {
		List<Integer> failList = new ArrayList<Integer> ();
		for (Map.Entry<Integer, List<Integer>> entry : codeUseid.entrySet()) {
			failList.addAll(entry.getValue());
		}
		List<AuditUserInfo> sucList = new ArrayList<AuditUserInfo>();
		
		for (AuditUserInfo info : users) {
			if (!failList.contains(info.getUserId())) {
				sucList.add(info);
			}
		}
		
		return sucList;
	}

	private Map<Integer,List<Integer>> mapping(Map<Integer,List<String>> resultMap) {
		Map<Integer,List<Integer>> codeMap = new HashMap<Integer,List<Integer>>(resultMap.size());
		
		for (Map.Entry<Integer, List<String>> entry : resultMap.entrySet()) {
			Integer errorCode = entry.getKey();
  			if (!CollectionUtils.isEmpty(entry.getValue())) {
  				List<Integer> userIds = new ArrayList<Integer>(entry.getValue().size());
				for (String name : entry.getValue()) {
					userIds.add(userMap.get(name));
				}
				codeMap.put(errorCode, userIds);
			}
		}
		return codeMap;
	}

	private List<TencentRequestType> convert(List<AuditUserInfo> infos) {
		if (CollectionUtils.isEmpty(infos)) {
			return new ArrayList<TencentRequestType>(0);
		}
		List<TencentRequestType> types = new ArrayList<TencentRequestType>(infos.size());

		for (AuditUserInfo info : infos) {
			TencentRequestType type = new TencentRequestType();
			type.setName(info.getName());
			type.setUrl(info.getUrl());
			type.setMemo(info.getMemo());

			types.add(type);
		}

		return types;
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
