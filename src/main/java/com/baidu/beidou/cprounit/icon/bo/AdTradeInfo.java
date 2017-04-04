package com.baidu.beidou.cprounit.icon.bo;

import java.io.Serializable;

/**
 * 2009-4-24
 * zengyunfeng
 * @version 1.1.3
 */
public class AdTradeInfo implements Serializable {

	private static final long serialVersionUID = -238855736842426847L;
	private int tradeid;
	private String tradename;
	private int parentid;
	
	/**
	 * 表示是否为其他。1：其他
	 */
	private int viewstat ;
	
	/**
	 * @return the tradeid
	 */
	public int getTradeid() {
		return tradeid;
	}
	/**
	 * @param tradeid the tradeid to set
	 */
	public void setTradeid(int tradeid) {
		this.tradeid = tradeid;
	}
	/**
	 * @return the tradename
	 */
	public String getTradename() {
		return tradename;
	}
	/**
	 * @param tradename the tradename to set
	 */
	public void setTradename(String tradename) {
		this.tradename = tradename;
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
	 * @return the viewstat
	 */
	public int getViewstat() {
		return viewstat;
	}
	/**
	 * @param viewstat the viewstat to set
	 */
	public void setViewstat(int viewstat) {
		this.viewstat = viewstat;
	}
	
	
}
