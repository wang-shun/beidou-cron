/**
 * 2009-4-28 上午10:35:46
 */
package com.baidu.beidou.unionsite.vo;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteTradeVo {
	private int siteid;
	private int firsttradeid;
	private int secondtradeid;
	private int parentid;
	private byte domainFlag;
	/**
	 * @return the siteid
	 */
	public int getSiteid() {
		return siteid;
	}
	/**
	 * @param siteid the siteid to set
	 */
	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}
	/**
	 * @return the firsttradeid
	 */
	public int getFirsttradeid() {
		return firsttradeid;
	}
	/**
	 * @param firsttradeid the firsttradeid to set
	 */
	public void setFirsttradeid(int firsttradeid) {
		this.firsttradeid = firsttradeid;
	}
	/**
	 * @return the secondtradeid
	 */
	public int getSecondtradeid() {
		return secondtradeid;
	}
	/**
	 * @param secondtradeid the secondtradeid to set
	 */
	public void setSecondtradeid(int secondtradeid) {
		this.secondtradeid = secondtradeid;
	}
	/**
	 * @return the parentid
	 */
	public int getParentid() {
		return parentid;
	}
	/**
	 * @param parentid the parentid to set
	 */
	public void setParentid(int parentid) {
		this.parentid = parentid;
	}
	/**
	 * @return the domainFlag
	 */
	public byte getDomainFlag() {
		return domainFlag;
	}
	/**
	 * @param domainFlag the domainFlag to set
	 */
	public void setDomainFlag(byte domainFlag) {
		this.domainFlag = domainFlag;
	}
}
