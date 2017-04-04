/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.bo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ClassName:UrlStat
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    1.
 * @Date	 2010	2010-5-21		下午06:47:37
 *
 * @see 	 
 */

public class UrlStat {
	private String url;
	private int displaytype;
	private int supporttype;
	private Map<Integer, Integer> size = new HashMap<Integer, Integer>();	//sizeid以”|”拼装的字符串，以及对应的流量
	private long srchs;
	private int siteid;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getDisplaytype() {
		return displaytype;
	}
	public void setDisplaytype(int displaytype) {
		this.displaytype = displaytype;
	}
	public int getSupporttype() {
		return supporttype;
	}
	public void setSupporttype(int supporttype) {
		this.supporttype = supporttype;
	}
	public Map<Integer, Integer> getSize() {
		return size;
	}
	public void setSize(Map<Integer, Integer> size) {
		this.size = size;
	}
	public long getSrchs() {
		return srchs;
	}
	public void setSrchs(long srchs) {
		this.srchs = srchs;
	}
	public int getSiteid() {
		return siteid;
	}
	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}		
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
