package com.baidu.beidou.account.bo;
/*
 * 用户提醒定制记录类型
 */
public class UserRemind {
	private int remindId;
	private int userId;
	private int remindType;
	private String email;
	private String mobile;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public int getRemindId() {
		return remindId;
	}
	public void setRemindId(int remindId) {
		this.remindId = remindId;
	}
	public int getRemindType() {
		return remindType;
	}
	public void setRemindType(int remintType) {
		this.remindType = remintType;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}

}
