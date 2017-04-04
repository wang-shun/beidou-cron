/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.vo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ClassName:StatCounter url文件统计计数器
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    TODO
 * @Date	 2010	2010-5-24		下午12:21:46
 *
 * @see 	 
 */

public class StatCounter {

	/**
	 * 尺寸无效的计数器
	 */
	private int sizeError = 0;
	
	/**
	 * 尺寸和物料类型不对应的计数器
	 */
	private int supportError = 0;

	/**
	 * sizeError
	 *
	 * @return  the sizeError
	 */
	
	public int getSizeError() {
		return sizeError;
	}

	/**
	 * sizeError
	 *
	 * @param   sizeError    the sizeError to set
	 */
	
	public void increaseSizeError() {
		++this.sizeError;
	}

	/**
	 * supportError
	 *
	 * @return  the supportError
	 */
	
	public int getSupportError() {
		return supportError;
	}

	/**
	 * supportError
	 *
	 * @param   supportError    the supportError to set
	 */
	
	public void increaseSupportError() {
		++this.supportError;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	
	
	
}
