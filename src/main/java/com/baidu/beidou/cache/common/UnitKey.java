/**
 * 
 */
package com.baidu.beidou.cache.common;

/**
 * @author wangqiang04
 * 
 */
public class UnitKey extends GroupKey {
	private long unitid;

	/**
	 * @param userid
	 * @param planid
	 * @param groupid
	 * @param unitid
	 */
	public UnitKey(int userid, int planid, int groupid, long unitid) {
		super(userid, planid, groupid);
		this.unitid = unitid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (unitid ^ (unitid >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnitKey other = (UnitKey) obj;
		if (unitid != other.unitid)
			return false;
		return true;
	}

	public long getUnitid() {
		return unitid;
	}

	public void setUnitid(long unitid) {
		this.unitid = unitid;
	}

}
