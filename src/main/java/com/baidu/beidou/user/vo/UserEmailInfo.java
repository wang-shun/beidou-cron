/**
 * 2009-12-15 上午10:49:06
 * @author zengyunfeng
 */
package com.baidu.beidou.user.vo;

/**
 * @author zengyunfeng
 * //用户信息包括：用户id，用户名，联系人姓名，电子邮件。
 */
public class UserEmailInfo {
	private int userId;
	private String realname;
	private String email;
	
	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the realName
	 */
	public String getRealname() {
		return realname;
	}
	/**
	 * @param realName the realName to set
	 */
	public void setRealname(String realname) {
		this.realname = realname;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
}
