/**
 * beidou-cron-640#com.baidu.beidou.cprounit.bo.UnitAdx.java
 * 下午3:18:28 created by kanghongwei
 */
package com.baidu.beidou.cprounit.bo;

import java.io.Serializable;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdx.java
 * @dateTime 2013-10-16 下午3:18:28
 */

public class UnitAdx implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5596946631206067025L;

	private long adid;
	private int userid;
	private int adxType;
	private int googleAuditState;
	private int googleSnapshot;

	public long getAdid() {
		return adid;
	}

	public void setAdid(long adid) {
		this.adid = adid;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public int getAdxType() {
		return adxType;
	}

	public void setAdxType(int adxType) {
		this.adxType = adxType;
	}

	public int getGoogleAuditState() {
		return googleAuditState;
	}

	public void setGoogleAuditState(int googleAuditState) {
		this.googleAuditState = googleAuditState;
	}

	public int getGoogleSnapshot() {
		return googleSnapshot;
	}

	public void setGoogleSnapshot(int googleSnapshot) {
		this.googleSnapshot = googleSnapshot;
	}

	public UnitAdx() {
		super();
	}

	public UnitAdx(long adid, int userid, int adxType, int googleAuditState, int googleSnapshot) {
		super();
		this.adid = adid;
		this.userid = userid;
		this.adxType = adxType;
		this.googleAuditState = googleAuditState;
		this.googleSnapshot = googleSnapshot;
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
		UnitAdx other = (UnitAdx) obj;
		if (adid != other.adid)
			return false;
		if (userid != other.userid)
			return false;
		return true;
	}

}
