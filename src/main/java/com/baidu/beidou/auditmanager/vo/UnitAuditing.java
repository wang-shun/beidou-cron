package com.baidu.beidou.auditmanager.vo;

// Generated 2008-7-5 22:46:51 by Hibernate Tools 3.2.1.GA

/**
 * @author zengyunfeng
 */
public class UnitAuditing implements java.io.Serializable {

	private Long id;
	private String refuseReason;
	private Integer userId;

	public UnitAuditing() {
	}

	public UnitAuditing(long id, String refuseReason, Integer userId) {
		this.id = id;
		this.refuseReason = refuseReason;
		this.userId = userId;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRefuseReason() {
		return this.refuseReason;
	}

	public void setRefuseReason(String refuseReason) {
		this.refuseReason = refuseReason;
	}

	/**
	 * toString
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(
				Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UnitAuditing))
			return false;
		UnitAuditing castOther = (UnitAuditing) other;

		return (this.getId().longValue() == castOther.getId().longValue());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getId() == null ? 0 : this.getId().hashCode());

		return result;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}
