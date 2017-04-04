/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.vo;

/**
 * ClassName:SiteSize
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    TODO
 * @Date	 2010	2010-5-21		下午06:55:29
 *
 * @see 	 
 */

public class SiteSize {
	private int siteid;
	private int size;	//0： 表示支持文字的url
	public int getSiteid() {
		return siteid;
	}
	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + siteid;
		result = prime * result + size;
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SiteSize other = (SiteSize) obj;
		if (siteid != other.siteid) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}
	
}
