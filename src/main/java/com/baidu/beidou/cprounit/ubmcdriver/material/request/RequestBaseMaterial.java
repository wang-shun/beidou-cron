package com.baidu.beidou.cprounit.ubmcdriver.material.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ClassName: RequestBaseMaterial
 * Function: 北斗物料基础结构请求
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
abstract public class RequestBaseMaterial {
	
	protected Long mcId;
	
	protected Integer versionId;
	
	protected Integer wuliaoType;
	
	/**
	 * tranformToString: 其子类必须实现该函数，以便将该请求转化成ubmc所需要的value
	 * @version cpweb-587
	 * @author genglei01
	 * @date Apr 25, 2013
	 */
	abstract public String tranformToValueString();
	
	public RequestBaseMaterial(Long mcId, Integer versionId) {
		this.mcId = mcId;
		this.versionId = versionId;
	}
	
	public RequestBaseMaterial(Long mcId, Integer versionId, Integer wuliaoType) {
		this.mcId = mcId;
		this.versionId = versionId;
		this.wuliaoType = wuliaoType;
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof RequestBaseMaterial)) {
			return false;
		}

		RequestBaseMaterial castOther = (RequestBaseMaterial) other;
		
		if (this.mcId != null && this.mcId.equals(castOther.getMcId())
				&& this.versionId != null && this.versionId.equals(castOther.getVersionId())) {
			return true;
		}

		return false;
	}
	
	public int hashCode() {
		int result = 17;

		result = 37 * result + (mcId == null ? 0 : mcId.hashCode());
		return result;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this,	ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	public Long getMcId() {
		return mcId;
	}

	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public Integer getWuliaoType() {
		return wuliaoType;
	}

	public void setWuliaoType(Integer wuliaoType) {
		this.wuliaoType = wuliaoType;
	}
}
