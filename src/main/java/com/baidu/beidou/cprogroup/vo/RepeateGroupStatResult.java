package com.baidu.beidou.cprogroup.vo;
/**
 * @author Administrator
 * @version 1.0.7
 */
public class RepeateGroupStatResult {
	
	private Integer userId;
	
	private String userName;
	
	private String planName;
	
	private Integer repeateGroupNum;
	
	private String repeateGroupNames;

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getRepeateGroupNames() {
		return repeateGroupNames;
	}

	public void setRepeateGroupNames(String repeateGroupNames) {
		this.repeateGroupNames = repeateGroupNames;
	}

	public Integer getRepeateGroupNum() {
		return repeateGroupNum;
	}

	public void setRepeateGroupNum(Integer repeateGroupNum) {
		this.repeateGroupNum = repeateGroupNum;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}