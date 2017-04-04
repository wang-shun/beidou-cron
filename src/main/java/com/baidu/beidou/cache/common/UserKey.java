/**
 * 
 */
package com.baidu.beidou.cache.common;

/**
 * @author wangqiang04
 * 
 */
public class UserKey {
	private int userid;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		UserKey other = (UserKey) obj;
		if (userid != other.userid)
			return false;
		return true;
	}

	public UserKey(int userid) {
		this.userid = userid;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}
}
