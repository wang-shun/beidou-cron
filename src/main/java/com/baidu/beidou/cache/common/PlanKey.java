/**
 * 
 */
package com.baidu.beidou.cache.common;

/**
 * @author wangqiang04
 * 
 */
public class PlanKey extends UserKey {
	private int planid;

	/**
	 * @param userid
	 * @param planid
	 */
	public PlanKey(int userid, int planid) {
		super(userid);
		this.planid = planid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + planid;
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
		PlanKey other = (PlanKey) obj;
		if (planid != other.planid)
			return false;
		return true;
	}

	public int getPlanid() {
		return planid;
	}

	public void setPlanid(int planid) {
		this.planid = planid;
	}

}
