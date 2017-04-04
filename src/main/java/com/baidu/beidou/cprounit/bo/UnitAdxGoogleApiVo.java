/**
 * beidou-cron-640#com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo.java
 * 上午11:39:03 created by kanghongwei
 */
package com.baidu.beidou.cprounit.bo;

import java.io.Serializable;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxGoogleApiVo.java
 * @dateTime 2013-10-22 上午11:39:03
 */

public class UnitAdxGoogleApiVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -878005088694174319L;

	private int userid;

	private long adid;

	private int width;

	private int height;

	private String targetUrl;

	public UnitAdxGoogleApiVo() {
		super();
	}

	public UnitAdxGoogleApiVo(int userid, long adid, int width, int height, String targetUrl) {
		super();
		this.userid = userid;
		this.adid = adid;
		this.width = width;
		this.height = height;
		this.targetUrl = targetUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (adid ^ (adid >>> 32));
		result = prime * result + userid;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnitAdxGoogleApiVo other = (UnitAdxGoogleApiVo) obj;
		if (adid != other.adid)
			return false;
		if (userid != other.userid)
			return false;
		return true;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public long getAdid() {
		return adid;
	}

	public void setAdid(long adid) {
		this.adid = adid;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

}
