package com.baidu.beidou.cprounit.service.bo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * beidou物料结构基类
 * 
 * @author yanjie
 * 
 */
abstract public class BeidouMaterialBase {
	private long mcid;
	private String title;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;

	public BeidouMaterialBase(String title, String showUrl, String targetUrl,String wirelessShowUrl,String wirelessTargetUrl) {
		this.title = title;
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
	}

	public BeidouMaterialBase(long mcid, String title, String showUrl,
			String targetUrl,String wirelessShowUrl,String wirelessTargetUrl) {
		this(title, showUrl, targetUrl,wirelessShowUrl,wirelessTargetUrl);
		this.mcid = mcid;
	}

	public long getMcid() {
		return mcid;
	}

	public void setMcid(long mcid) {
		this.mcid = mcid;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getWirelessShowUrl() {
		return wirelessShowUrl;
	}

	public void setWirelessShowUrl(String wirelessShowUrl) {
		this.wirelessShowUrl = wirelessShowUrl;
	}

	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}

	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}

}
