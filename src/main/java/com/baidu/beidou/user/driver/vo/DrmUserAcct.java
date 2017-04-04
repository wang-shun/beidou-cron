/**
 * 2009-12-21 下午07:19:00
 * @author zengyunfeng
 */
package com.baidu.beidou.user.driver.vo;

/**
 * @author zengyunfeng
 *
 */
public class DrmUserAcct {

	private int userid;	//用户id
	private String username;	// 用户名
	private int position;	// 可对应现在useracct的ulevelid
	private int extra;	// 见后面特别说明
	private int corp;	// 对应useracct的ucorpid
	private int ustatus;	// 用户状态，对应useracct.ustatid
	/**
	 * @return the userid
	 */
	public int getUserid() {
		return userid;
	}
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(int userid) {
		this.userid = userid;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	/**
	 * @return the extra
	 */
	public int getExtra() {
		return extra;
	}
	/**
	 * @param extra the extra to set
	 */
	public void setExtra(int extra) {
		this.extra = extra;
	}
	/**
	 * @return the corp
	 */
	public int getCorp() {
		return corp;
	}
	/**
	 * @param corp the corp to set
	 */
	public void setCorp(int corp) {
		this.corp = corp;
	}
	/**
	 * @return the ustatus
	 */
	public int getUstatus() {
		return ustatus;
	}
	/**
	 * @param ustatus the ustatus to set
	 */
	public void setUstatus(int ustatus) {
		this.ustatus = ustatus;
	}
	
	

}
