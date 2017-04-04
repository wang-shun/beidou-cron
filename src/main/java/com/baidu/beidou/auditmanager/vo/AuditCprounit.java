package com.baidu.beidou.auditmanager.vo;

import java.io.Serializable;
import java.util.Date;

public class AuditCprounit implements Serializable{

	private static final long serialVersionUID = -4732875913668927871L;
	private Long unitId;
	private Integer groupId;
	private Integer planId;
	private Integer userId;
	private Date subTime;
	private int isSmart = 0; // 智能创意标识，0：普通创意，默认值；1：智能创意
	
	public Long getUnitId() {
		return unitId;
	}
	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Integer getPlanId() {
		return planId;
	}
	public void setPlanId(Integer planId) {
		this.planId = planId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Date getSubTime() {
		return subTime;
	}
	public void setSubTime(Date subTime) {
		this.subTime = subTime;
	}
	public int getIsSmart() {
		return isSmart;
	}
	public void setIsSmart(int isSmart) {
		this.isSmart = isSmart;
	}
}
