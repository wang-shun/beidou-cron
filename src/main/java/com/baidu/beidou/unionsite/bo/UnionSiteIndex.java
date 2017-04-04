/**
 * 2009-4-20 下午09:43:52
 */
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class UnionSiteIndex implements Serializable {
	
	private static final long serialVersionUID = -3249746568273420382L;
	
	private String domain; // 域名，包括一级域名和二级域名
	
	private String cname; // 计费名
	
	private byte domainFlag = 0;	//0表示一级域名，1表示二级域名
	
	private long start;
	
	private int length; // 联盟数据记录对象的长度
	
	/** Cpweb-250，增加按优先级位进行信息选取，当前以1为优先，如果全为则随机选一个 */
	private int showFlag;

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
	 * @return the cname
	 */
	public String getCname() {
		return cname;
	}

	/**
	 * @param cname the cname to set
	 */
	public void setCname(String charged) {
		this.cname = charged;
	}

	/**
	 * @return the domainFlag 0表示一级域名，1表示二级域名
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
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	public int getShowFlag() {
		return showFlag;
	}

	public void setShowFlag(int showFlag) {
		this.showFlag = showFlag;
	}


}
