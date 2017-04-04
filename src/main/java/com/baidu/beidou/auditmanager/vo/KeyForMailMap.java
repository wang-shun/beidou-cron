package com.baidu.beidou.auditmanager.vo;



public class KeyForMailMap {
	private Integer groupId;
	
	private Integer reasonId;

	public KeyForMailMap(int groupId, int reasonId) {
		this.groupId = groupId;
		this.reasonId = reasonId;
	}
	
	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public Integer getReasonId() {
		return reasonId;
	}

	public void setReasonId(Integer reasonId) {
		this.reasonId = reasonId;
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof KeyForMailMap)) {
			return false;
		}
	
		KeyForMailMap castOther = (KeyForMailMap) other;
		if (this.getGroupId().equals(castOther.getGroupId())
				&& this.getReasonId().equals(castOther.getReasonId())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((reasonId == null) ? 0 : reasonId.hashCode());
		return result;
		}

	public String toString() {
		return "[groupId=" + this.getGroupId() 
				+ ",reasonId=" + this.getReasonId() + "]";
	}
}
