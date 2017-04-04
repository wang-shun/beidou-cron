package com.baidu.beidou.auditmanager.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.service.ResultSetCallBack;
import com.baidu.beidou.auditmanager.vo.AuditCprounit;
import com.baidu.beidou.auditmanager.vo.AuditInfoUser;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.util.redis.RedisCacheManager;

public class AuditTaskSendResultSetCallBack implements ResultSetCallBack<AuditCprounit>{

	private static final Log log = LogFactory.getLog(AuditTaskSendResultSetCallBack.class);
	
	private UserInfoMgr userInfoMgr;
	
	private RedisCacheManager auditredisCacheMgr;
	
	private Set<Integer> blackUserList;
	
	/**
	 * 拿到一个库的扫描结果之后，根据用户聚合，填充用户类型，并发送到redis中
	 */
	public int dealWithResultSet(List<AuditCprounit> resultList) {
		
		if(CollectionUtils.isEmpty(resultList)){
			return 0;
		}
		List<AuditInfoUser> auditUsers = new ArrayList<AuditInfoUser>();
		
		int[] clients = userInfoMgr.getAllClientCustomersFromUc(); //获取所有大客户用户名单
		int[] vips = userInfoMgr.getAllVipsFromUc();
		AuditInfoUser auditUser = null;
		for(AuditCprounit unit:resultList){//遍历list，根据userid归并成消息
			if(auditUser == null){
				auditUser = new AuditInfoUser();
				auditUser.setUserId(unit.getUserId());
				if(ArrayUtils.contains(clients, unit.getUserId())){//是大客户
					auditUser.setUserRole(AuditConstant.AUDIT_USER_ROLE_KA);
				}else if(ArrayUtils.contains(vips, unit.getUserId())){//vip客户
					auditUser.setUserRole(AuditConstant.AUDIT_USER_ROLE_KA);
				}else{//是普通用户
					auditUser.setUserRole(AuditConstant.AUDIT_USER_ROLE_SME);
				}
			}else if(!(auditUser.getUserId().equals(unit.getUserId()))){
				auditUsers.add(auditUser);
				auditUser = new AuditInfoUser();
				auditUser.setUserId(unit.getUserId());
				if(ArrayUtils.contains(clients, unit.getUserId())){//是大客户
					auditUser.setUserRole(AuditConstant.AUDIT_USER_ROLE_KA);
				}else if(ArrayUtils.contains(vips, unit.getUserId())){//vip客户
					auditUser.setUserRole(AuditConstant.AUDIT_USER_ROLE_KA);
				}else{//是普通用户
					auditUser.setUserRole(AuditConstant.AUDIT_USER_ROLE_SME);
				}
			}
			auditUser.getAuditUnits().add(unit);
		}
		auditUsers.add(auditUser);
		//把封装好的message发送到redis中
		sendMessage(auditUsers);
		log.info("send audit user message,user number is:"+auditUsers.size());
		return auditUsers.size();
	}
	private void sendMessage(List<AuditInfoUser> auditUsers) {
		try {
			for(AuditInfoUser auditInfouser:auditUsers){
				
				if (!isBlackList(auditInfouser.getUserId())) {
					auditredisCacheMgr.rpush(redisKey, auditInfouser);
				} else {
					log.info("filter userid:" + auditInfouser.getUserId());
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isBlackList(int userId) {
		// black list calculate
		if (blackUserList.contains(userId)) {
			return true;
		}
		return false;
	}
	
	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}
	
	public RedisCacheManager getAuditredisCacheMgr() {
		return auditredisCacheMgr;
	}
	public void setAuditredisCacheMgr(RedisCacheManager auditredisCacheMgr) {
		this.auditredisCacheMgr = auditredisCacheMgr;
	}

	public Set<Integer> getBlackUserList() {
		return blackUserList;
	}
	public void setBlackUserList (Set<Integer> blackUserList) {
		this.blackUserList = blackUserList;
	}


	private static final String redisKey = "AUDIT_BEIDOU_TASK_LIST";
}
