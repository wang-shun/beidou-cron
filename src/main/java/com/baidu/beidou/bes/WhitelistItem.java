/**
 * WhitelistItem.java 
 */
package com.baidu.beidou.bes;

/**
 *
 * @author lixukun
 * @date 2013-12-25
 */
public class WhitelistItem {
	private int userid;
	private int planid;
	private long companyTag;
	
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
	 * @return the planid
	 */
	public int getPlanid() {
		return planid;
	}
	/**
	 * @param planid the planid to set
	 */
	public void setPlanid(int planid) {
		this.planid = planid;
	}
	/**
	 * @return the companyTag
	 */
	public long getCompanyTag() {
		return companyTag;
	}
	/**
	 * @param companyTag the companyTag to set
	 */
	public void setCompanyTag(long companyTag) {
		this.companyTag = companyTag;
	}
}
