/**
 * 2009-4-30 下午05:19:34
 */
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class IPCookieBo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4937616502077450687L;
	private String domain;
	private int unique_ip;
	private int unique_cookie;

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the unique_ip
	 */
	public int getUnique_ip() {
		return unique_ip;
	}

	/**
	 * @param unique_ip
	 *            the unique_ip to set
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
	 * @param unique_cookie
	 *            the unique_cookie to set
	 */
	public void setUnique_cookie(int unique_cookie) {
		this.unique_cookie = unique_cookie;
	}

}
