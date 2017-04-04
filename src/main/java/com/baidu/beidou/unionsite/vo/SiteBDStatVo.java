/**
 * 2009-4-28 上午01:06:03
 */
package com.baidu.beidou.unionsite.vo;

import java.io.Serializable;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteBDStatVo implements Serializable{
	private static final long serialVersionUID = -1363162604158632169L;
	private int siteid;
	/**
	 * 等级
	 */
	private byte scale ;
	
	/**
	 * 热度
	 */
	private byte cmplevel = 1;
	private double ratecmp;
	private double scorecmp;
	

	//中间数据
	private long retrieve;
	private double ctr2;
	private double num_ad_retrieve;
	private int unique_ip;
	private int unique_cookie;
	
	//辅助数据
	private int parentid;
	private byte domainFlag;
	private Float q1 ; //	4个字节
	private Float q2 ; //	4个字节
	
	private int firsttradeid;
	private int secondtradeid;
	

	/**
	 * 用于等级的计算数据
	 */
	private double F;
	
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
	
	/**
	 * @return the cmplevel
	 */
	public byte getCmplevel() {
		return cmplevel;
	}

	/**
	 * @param cmplevel
	 *            the cmplevel to set
	 */
	public void setCmplevel(byte cmplevel) {
		this.cmplevel = cmplevel;
	}

	/**
	 * @return the ratecmp
	 */
	public double getRatecmp() {
		return ratecmp;
	}

	/**
	 * @param ratecmp
	 *            the ratecmp to set
	 */
	public void setRatecmp(double ratecmp) {
		this.ratecmp = ratecmp;
	}

	/**
	 * @return the scorecmp
	 */
	public double getScorecmp() {
		return scorecmp;
	}

	/**
	 * @param scorecmp
	 *            the scorecmp to set
	 */
	public void setScorecmp(double scorecmp) {
		this.scorecmp = scorecmp;
	}
	/**
	 * @return the scale
	 */
	public byte getScale() {
		return scale;
	}
	/**
	 * @param scale the scale to set
	 */
	public void setScale(byte scale) {
		this.scale = scale;
	}
	/**
	 * @return the retrieve
	 */
	public long getRetrieve() {
		return retrieve;
	}
	/**
	 * @param retrieve the retrieve to set
	 */
	public void setRetrieve(long retrieve) {
		this.retrieve = retrieve;
	}
	/**
	 * @return the ctr2
	 */
	public double getCtr2() {
		return ctr2;
	}
	/**
	 * @param ctr2 the ctr2 to set
	 */
	public void setCtr2(double ctr2) {
		this.ctr2 = ctr2;
	}
	/**
	 * @return the num_ad_retrieve
	 */
	public double getNum_ad_retrieve() {
		return num_ad_retrieve;
	}
	/**
	 * @param num_ad_retrieve the num_ad_retrieve to set
	 */
	public void setNum_ad_retrieve(double num_ad_retrieve) {
		this.num_ad_retrieve = num_ad_retrieve;
	}
	/**
	 * @return the unique_ip
	 */
	public int getUnique_ip() {
		return unique_ip;
	}
	/**
	 * @param unique_ip the unique_ip to set
	 */
	public void setUnique_ip(int unique_ip) {
		this.unique_ip = unique_ip;
	}
	/**
	 * @return the unique_cookie
	 */
	public int getUnique_cookie() {
		return unique_cookie;
	}
	/**
	 * @param unique_cookie the unique_cookie to set
	 */
	public void setUnique_cookie(int unique_cookie) {
		this.unique_cookie = unique_cookie;
	}
	/**
	 * @return the f
	 */
	public double getF() {
		return F;
	}
	/**
	 * @param f the f to set
	 */
	public void setF(double f) {
		F = f;
	}
	/**
	 * @return the q1
	 */
	public Float getQ1() {
		return q1;
	}
	/**
	 * @param q1 the q1 to set
	 */
	public void setQ1(Float q1) {
		this.q1 = q1;
	}
	/**
	 * @return the q2
	 */
	public Float getQ2() {
		return q2;
	}
	/**
	 * @param q2 the q2 to set
	 */
	public void setQ2(Float q2) {
		this.q2 = q2;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(siteid).append('\t');
		result.append(scale).append('\t');
		result.append(cmplevel).append('\t');
		result.append(ratecmp).append('\t');
		result.append(scorecmp).append('\t');
		result.append(retrieve).append('\t');
		result.append(ctr2).append('\t');
		result.append(num_ad_retrieve).append('\t');
		result.append(unique_ip).append('\t');
		result.append(unique_cookie).append('\t');
		result.append(parentid).append('\t');
		result.append(domainFlag).append('\t');
		result.append(q1).append('\t');
		result.append(q2).append('\t');
		result.append(firsttradeid).append('\t');
		result.append(secondtradeid).append('\t');
		result.append(F);
		return result.toString();
		
	}
	
	
	
}
