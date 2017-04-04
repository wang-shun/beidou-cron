/**
 * 2009-4-22 上午03:27:40
 */
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class QValue implements Serializable{

	private static final long serialVersionUID = -374672515181336104L;
	private byte domainFlag; 		//0表示一级域名，1表示二级域名
	private String domain;  // 域名
	private float q1 ; //	4个字节
	private float q2 ; //	4个字节
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
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the q1
	 */
	public float getQ1() {
		return q1;
	}
	/**
	 * @param q1 the q1 to set
	 */
	public void setQ1(float q1) {
		this.q1 = q1;
	}
	/**
	 * @return the q2
	 */
	public float getQ2() {
		return q2;
	}
	/**
	 * @param q2 the q2 to set
	 */
	public void setQ2(float q2) {
		this.q2 = q2;
	}

}
