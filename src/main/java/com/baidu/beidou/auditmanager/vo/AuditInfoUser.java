package com.baidu.beidou.auditmanager.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AuditInfoUser implements  Serializable{

	private static final long serialVersionUID = -7687878296374986344L;
	
	private Integer userId;
	private List<AuditCprounit> auditUnits=new ArrayList<AuditCprounit>();
	private Integer userRole;
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public List<AuditCprounit> getAuditUnits() {
		return auditUnits;
	}
	public void setAuditUnit(List<AuditCprounit> auditUnits) {
		this.auditUnits = auditUnits;
	}
	public Integer getUserRole() {
		return userRole;
	}
	public void setUserRole(Integer userRole) {
		this.userRole = userRole;
	}
	
	
}
