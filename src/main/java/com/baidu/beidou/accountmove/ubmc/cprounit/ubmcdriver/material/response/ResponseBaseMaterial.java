package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ClassName: ResponseBaseMaterial
 * Function: 北斗物料基础结构响应
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
abstract public class ResponseBaseMaterial {
	
	protected final static Log log = LogFactory.getLog(ResponseBaseMaterial.class);
	
	protected Long mcId;
	
	protected Integer versionId;
	
	protected Integer wuliaoType;
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	public static Integer getInteger(String input) {
		if (StringUtils.isEmpty(input)) {
			return 0;
		}
		return Integer.parseInt(input);
	}
	
	public static Long getLong(String input) {
		if (StringUtils.isEmpty(input)) {
			return 0L;
		}
		return Long.parseLong(input);
	}
	
	public static String getString(String input) {
		if (StringUtils.isEmpty(input)) {
			return "";
		}
		return input;
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
