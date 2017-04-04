/**
 * 2009-4-28 下午03:30:40
 */
package com.baidu.beidou.unionsite.vo;

import java.util.List;


/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteCmpLevelCalculateVo {
	private List<SiteBDStatVo> siteList = null;
	
	private long minRetrieveForCmp = Long.MAX_VALUE;
	private long minRetrieve = Long.MAX_VALUE;
	private double minCtr2 = Double.MAX_VALUE;
	private double minNum_ad_retrieve = Double.MAX_VALUE;
	private int minNum_ip = Integer.MAX_VALUE;
	private int minNum_cookie = Integer.MAX_VALUE;

	private long maxRetrieveForCmp = 0;
	private long maxRetrieve = 0;
	private double maxCtr2 = 0;
	private double maxNum_ad_retrieve = 0;
	private int maxNum_ip = 0;
	private int maxNum_cookie = 0;
	

	/**
	 * @return the siteList
	 */
	public List<SiteBDStatVo> getSiteList() {
		return siteList;
	}

	/**
	 * @param siteList
	 *            the siteList to set
	 */
	public void setSiteList(List<SiteBDStatVo> siteList) {
		this.siteList = siteList;
	}

	/**
	 * @return the minRetrieve
	 */
	public long getMinRetrieve() {
		return minRetrieve;
	}

	/**
	 * @param minRetrieve the minRetrieve to set
	 */
	public void setMinRetrieve(long minRetrieve) {
		this.minRetrieve = minRetrieve;
	}

	/**
	 * @return the minCtr2
	 */
	public double getMinCtr2() {
		return minCtr2;
	}

	/**
	 * @param minCtr2 the minCtr2 to set
	 */
	public void setMinCtr2(double minCtr2) {
		this.minCtr2 = minCtr2;
	}

	/**
	 * @return the minNum_ad_retrieve
	 */
	public double getMinNum_ad_retrieve() {
		return minNum_ad_retrieve;
	}

	/**
	 * @param minNum_ad_retrieve the minNum_ad_retrieve to set
	 */
	public void setMinNum_ad_retrieve(double minNum_ad_retrieve) {
		this.minNum_ad_retrieve = minNum_ad_retrieve;
	}

	/**
	 * @return the minNum_ip
	 */
	public int getMinNum_ip() {
		return minNum_ip;
	}

	/**
	 * @param minNum_ip the minNum_ip to set
	 */
	public void setMinNum_ip(int minNum_ip) {
		this.minNum_ip = minNum_ip;
	}

	/**
	 * @return the minNum_cookie
	 */
	public int getMinNum_cookie() {
		return minNum_cookie;
	}

	/**
	 * @param minNum_cookie the minNum_cookie to set
	 */
	public void setMinNum_cookie(int minNum_cookie) {
		this.minNum_cookie = minNum_cookie;
	}

	/**
	 * @return the maxRetrieve
	 */
	public long getMaxRetrieve() {
		return maxRetrieve;
	}

	/**
	 * @param maxRetrieve the maxRetrieve to set
	 */
	public void setMaxRetrieve(long maxRetrieve) {
		this.maxRetrieve = maxRetrieve;
	}

	/**
	 * @return the maxCtr2
	 */
	public double getMaxCtr2() {
		return maxCtr2;
	}

	/**
	 * @param maxCtr2 the maxCtr2 to set
	 */
	public void setMaxCtr2(double maxCtr2) {
		this.maxCtr2 = maxCtr2;
	}

	/**
	 * @return the maxNum_ad_retrieve
	 */
	public double getMaxNum_ad_retrieve() {
		return maxNum_ad_retrieve;
	}

	/**
	 * @param maxNum_ad_retrieve the maxNum_ad_retrieve to set
	 */
	public void setMaxNum_ad_retrieve(double maxNum_ad_retrieve) {
		this.maxNum_ad_retrieve = maxNum_ad_retrieve;
	}

	/**
	 * @return the maxNum_ip
	 */
	public int getMaxNum_ip() {
		return maxNum_ip;
	}

	/**
	 * @param maxNum_ip the maxNum_ip to set
	 */
	public void setMaxNum_ip(int maxNum_ip) {
		this.maxNum_ip = maxNum_ip;
	}

	/**
	 * @return the maxNum_cookie
	 */
	public int getMaxNum_cookie() {
		return maxNum_cookie;
	}

	/**
	 * @param maxNum_cookie the maxNum_cookie to set
	 */
	public void setMaxNum_cookie(int maxNum_cookie) {
		this.maxNum_cookie = maxNum_cookie;
	}

	/**
	 * @return the minRetrieveForCmp
	 */
	public long getMinRetrieveForCmp() {
		return minRetrieveForCmp;
	}

	/**
	 * @param minRetrieveForCmp the minRetrieveForCmp to set
	 */
	public void setMinRetrieveForCmp(long minRetriveForCmp) {
		this.minRetrieveForCmp = minRetriveForCmp;
	}

	/**
	 * @return the maxRetrieveForCmp
	 */
	public long getMaxRetrieveForCmp() {
		return maxRetrieveForCmp;
	}

	/**
	 * @param maxRetrieveForCmp the maxRetrieveForCmp to set
	 */
	public void setMaxRetrieveForCmp(long maxRetriveForCmp) {
		this.maxRetrieveForCmp = maxRetriveForCmp;
	}
}
