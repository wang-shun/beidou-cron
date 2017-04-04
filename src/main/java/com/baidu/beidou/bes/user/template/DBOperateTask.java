package com.baidu.beidou.bes.user.template;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;
/**
 * 根据adx_user_to_add adx_user_to_update文件写入或更新广告主信息
 * 
 * @author caichao
 */
public class DBOperateTask implements Runnable {
	private List<AuditUserInfo> viewList;
	private Integer opType;
	private AuditUserServiceMgr userServiceMgr;
	
	public DBOperateTask() {
		
	}
	public DBOperateTask(List<AuditUserInfo> viewList,Integer opType,AuditUserServiceMgr userServiceMgr) {
		this.viewList = viewList;
		this.opType = opType;
		this.userServiceMgr = userServiceMgr;
	}
	
	@Override
	public void run() {
		if (CollectionUtils.isEmpty(viewList) 
				|| opType == null || userServiceMgr == null) {
			return ;
		}
		
		if (opType == 0) {
			userServiceMgr.insertAuditUser(viewList);
		} else {
			userServiceMgr.updateAuditUser(viewList);
		}
	}
	public List<AuditUserInfo> getViewList() {
		return viewList;
	}
	public void setViewList(List<AuditUserInfo> viewList) {
		this.viewList = viewList;
	}
	public Integer getOpType() {
		return opType;
	}
	public void setOpType(Integer opType) {
		this.opType = opType;
	}
	public AuditUserServiceMgr getUserServiceMgr() {
		return userServiceMgr;
	}
	public void setUserServiceMgr(AuditUserServiceMgr userServiceMgr) {
		this.userServiceMgr = userServiceMgr;
	}
	
	
}
