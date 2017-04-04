/**
 * beidou-cron-640#com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo.java
 * 下午4:03:34 created by kanghongwei
 */
package com.baidu.beidou.cprounit.bo;

import java.io.Serializable;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxSnapshotVo.java
 * @dateTime 2013-10-16 下午4:03:34
 */

public class UnitAdxSnapshotVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8138445133320327011L;

	private int userid;

	private long adid;

	private int wuliaoType;

	private int width;

	private int height;

	private long mcId;

	private int mcVersionId;

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

	public int getWuliaoType() {
		return wuliaoType;
	}

	public void setWuliaoType(int wuliaoType) {
		this.wuliaoType = wuliaoType;
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

	public long getMcId() {
		return mcId;
	}

	public void setMcId(long mcId) {
		this.mcId = mcId;
	}

	public int getMcVersionId() {
		return mcVersionId;
	}

	public void setMcVersionId(int mcVersionId) {
		this.mcVersionId = mcVersionId;
	}

	public UnitAdxSnapshotVo(int userid, long adid, int wuliaoType, int width, int height, long mcId, int mcVersionId) {
		super();
		this.userid = userid;
		this.adid = adid;
		this.wuliaoType = wuliaoType;
		this.width = width;
		this.height = height;
		this.mcId = mcId;
		this.mcVersionId = mcVersionId;
	}

	public UnitAdxSnapshotVo() {
		super();
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
		UnitAdxSnapshotVo other = (UnitAdxSnapshotVo) obj;
		if (adid != other.adid)
			return false;
		if (userid != other.userid)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UnitAdxSnapshotVo [userid=" + userid + ", adid=" + adid + ", wuliaoType=" + wuliaoType + ", width=" + width + ", height=" + height + ", mcId=" + mcId + ", mcVersionId=" + mcVersionId + "]";
	}

}
