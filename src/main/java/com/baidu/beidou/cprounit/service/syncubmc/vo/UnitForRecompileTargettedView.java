package com.baidu.beidou.cprounit.service.syncubmc.vo;

import java.util.Date;


public class UnitForRecompileTargettedView {

	private Long id;
	private Integer userId; //add by chongjie since 535

	private Long mcId = 0L; // UBMC物料ID
	private Integer mcVersionId = 0; // UBMC物料ID指定的版本ID
	
	private Date chaTime;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Long getMcId() {
		return mcId;
	}

	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}

	public Integer getMcVersionId() {
		return mcVersionId;
	}

	public void setMcVersionId(Integer mcVersionId) {
		this.mcVersionId = mcVersionId;
	}

	public Date getChaTime() {
		return chaTime;
	}

	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("mcId").append("='").append(getMcId()).append("' ");
		buffer.append("mcVersionId").append("='").append(getMcVersionId()).append("' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UnitForRecompileTargettedView))
			return false;
		UnitForRecompileTargettedView castOther = (UnitForRecompileTargettedView) other;

		return (this.getId().longValue() == castOther.getId().longValue());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getId() == null ? 0 : this.getId().hashCode());
		return result;
	}

}
