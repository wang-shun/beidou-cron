/**
 * 2009-12-17 上午11:32:47
 * @author zengyunfeng
 */
package com.baidu.beidou.user.vo;

/**
 * @author zengyunfeng
 *
 */
public class NameStatusInfo {
	private int userId;
	private String userName;
	private int sfStatus;
	private int ulevelid;	//对api接受通知使用ulevelid判断角色，确保不受延时的影响
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the sfStatus
	 */
	public int getSfStatus() {
		return sfStatus;
	}
	/**
	 * @param sfStatus the sfStatus to set
	 */
	public void setSfStatus(int sfStatus) {
		this.sfStatus = sfStatus;
	}
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
	 * @return the ulevelid
	 */
	public int getUlevelid() {
		return ulevelid;
	}
	/**
	 * @param ulevelid the ulevelid to set
	 */
	public void setUlevelid(int ulevelid) {
		this.ulevelid = ulevelid;
	}
	
	
}
