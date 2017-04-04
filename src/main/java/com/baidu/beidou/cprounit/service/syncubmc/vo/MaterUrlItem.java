package com.baidu.beidou.cprounit.service.syncubmc.vo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class MaterUrlItem {

	private Long unitId;
	private Integer userId;
	private Integer typeId;
	private String url;
	
	public MaterUrlItem(Long unitId, Integer userId, Integer typeId, String url) {
		this.unitId = unitId;
		this.userId = userId;
		this.typeId = typeId;
		this.url = url;
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof MaterUrlItem)) {
			return false;
		}

		MaterUrlItem castOther = (MaterUrlItem) other;
		
		if (this.unitId != null && this.unitId.equals(castOther.getUnitId())
				&& this.typeId != null && this.typeId.equals(castOther.getTypeId())
				&& this.url != null && this.url.equals(castOther.getUrl())) {
			return true;
		}

		return false;
	}
	
	public int hashCode() {
		int result = 17;

		result = 37 * result + (unitId == null ? 0 : unitId.hashCode());
		return result;
	}
	
	public Long getUnitId() {
		return unitId;
	}
	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}
	public Integer getTypeId() {
		return typeId;
	}
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
