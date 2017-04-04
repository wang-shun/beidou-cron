/**
 * 2009-12-21 下午01:50:57
 * @author zengyunfeng
 */
package com.baidu.beidou.user.driver.vo;

/**
 * @author zengyunfeng
 * 
 */
public class DrmUserTrade {

	private int userId;
	private int t1_id; // 用户直属上级行业id
	private int t2_id; // 父级行业的行业id, 如果无父级行业，该字段不存在
	private String t1_name;
	private String t2_name;

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the t1_id
	 */
	public int getT1_id() {
		return t1_id;
	}

	/**
	 * @param t1_id
	 *            the t1_id to set
	 */
	public void setT1_id(int t1_id) {
		this.t1_id = t1_id;
	}

	/**
	 * @return the t2_id
	 */
	public int getT2_id() {
		return t2_id;
	}

	/**
	 * @param t2_id
	 *            the t2_id to set
	 */
	public void setT2_id(int t2_id) {
		this.t2_id = t2_id;
	}

	/**
	 * @return the t1_name
	 */
	public String getT1_name() {
		return t1_name;
	}

	/**
	 * @param t1_name
	 *            the t1_name to set
	 */
	public void setT1_name(String t1_name) {
		this.t1_name = t1_name;
	}

	/**
	 * @return the t2_name
	 */
	public String getT2_name() {
		return t2_name;
	}

	/**
	 * @param t2_name
	 *            the t2_name to set
	 */
	public void setT2_name(String t2_name) {
		this.t2_name = t2_name;
	}

}
