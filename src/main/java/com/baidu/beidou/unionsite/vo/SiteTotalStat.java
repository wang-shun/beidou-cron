/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.vo;

import java.util.Set;
import java.util.TreeSet;

/**
 * ClassName:SiteTotalStat
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    TODO
 * @Date	 2010	2010-5-23		上午01:40:38
 *
 * @see 	 
 */

public class SiteTotalStat {
	private int displayType=0;	//支持的广告形式：1：固定；2：悬浮；3：固定&悬浮'
	private int supporttype=0;	//1：仅文字，2：仅图片，3：文字&图片
	private Set<Integer> size= new TreeSet<Integer>();	//sizeid以”|”拼装的字符串
	/**
	 * supporttype
	 *
	 * @return  the supporttype
	 */
	
	public int getSupporttype() {
		return supporttype;
	}
	/**
	 * supporttype
	 *
	 * @param   supporttype    the supporttype to set
	 */
	
	public void setSupporttype(int supporttype) {
		this.supporttype = supporttype;
	}
	/**
	 * size
	 *
	 * @return  the size
	 */
	
	public Set<Integer> getSize() {
		return size;
	}
	/**
	 * size
	 *
	 * @param   size    the size to set
	 */
	
	public void setSize(Set<Integer> size) {
		this.size = size;
	}
	/**
	 * displayType
	 *
	 * @return  the displayType
	 */
	
	public int getDisplayType() {
		return displayType;
	}
	/**
	 * displayType
	 *
	 * @param   displayType    the displayType to set
	 */
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
	}
	
	

}
