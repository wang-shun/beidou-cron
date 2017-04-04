package com.baidu.beidou.auditmanager.vo;

public class UrlUnitForMail {
	private Integer userId;
	private String userName;
	private Integer groupId;
	private String groupName;
	private String planName;
	private String reason;
	private Integer count;
	
	public UrlUnitForMail() {
		
	}
	
	public UrlUnitForMail(Integer userId, String userName, Integer groupId, 
			String planName, String groupName, Integer count, String reason) {
		this.userId = userId;
		this.userName = userName;
		this.groupId = groupId;
		this.planName = planName;
		this.groupName = groupName;
		this.count = count;
		this.reason = reason;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
