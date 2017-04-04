package com.baidu.beidou.util.akadriver.bo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AkaCheckInfo {
	
	private int userid=-1;
	
	private String ideaUrl="";
	
	private String ideaShowUrl="";
	
	private String wirelessTargetUrl = "";
	
	private String wirelessShowUrl = "";

	public String getIdeaShowUrl() {
		return ideaShowUrl;
	}

	public void setIdeaShowUrl(String ideaShowUrl) {
		this.ideaShowUrl = ideaShowUrl;
	}

	public String getIdeaUrl() {
		return ideaUrl;
	}

	public void setIdeaUrl(String ideaUrl) {
		this.ideaUrl = ideaUrl;
	}
	
	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}

	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}

	public String getWirelessShowUrl() {
		return wirelessShowUrl;
	}

	public void setWirelessShowUrl(String wirelessShowUrl) {
		this.wirelessShowUrl = wirelessShowUrl;
	}

	public int getUserid() {
		return userid;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
