/**
 * 2009-12-23 下午08:37:09
 * @author zengyunfeng
 */
package com.baidu.beidou.user.driver.vo;

/**
 * @author zengyunfeng
 * 
 */
public class UcRole {

	private Integer ucid;
	private String roletag;
	private String descr;

	/**
	 * @return the userid
	 */
	public Integer getUcid() {
		return ucid;
	}

	/**
	 * @param userid
	 *            the userid to set
	 */
	public void setUcid(Integer userid) {
		this.ucid = userid;
	}

	/**
	 * @return the roletag
	 */
	public String getRoletag() {
		return roletag;
	}

	/**
	 * @param roletag
	 *            the roletag to set
	 */
	public void setRoletag(String roletag) {
		this.roletag = roletag;
	}

	/**
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * @param descr
	 *            the descr to set
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

}
