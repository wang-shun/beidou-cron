/**
 * 
 */
package com.baidu.beidou.cache.common;

/**
 * @author wangqiang04
 *
 */
public class GroupKey extends PlanKey {
	private int groupid;

	/**
	 * @param userid
	 * @param planid
	 * @param groupid
	 */
	public GroupKey(int userid, int planid, int groupid) {
		super(userid, planid);
		this.groupid = groupid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + groupid;
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
		GroupKey other = (GroupKey) obj;
		if (groupid != other.groupid)
			return false;
		return true;
	}

	public int getGroupid() {
		return groupid;
	}

	public void setGroupid(int groupid) {
		this.groupid = groupid;
	}

}
