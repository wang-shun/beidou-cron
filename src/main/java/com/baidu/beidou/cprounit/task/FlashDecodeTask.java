/*******************************************************************************
 * CopyRight (c) 2000-2012 Baidu Online Network Technology (Beijing) Co., Ltd. All rights reserved.
 * Filename:    FlashDecodeService.java
 * Creator:     <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * Create-Date: 2013-6-17 下午5:46:51
 *******************************************************************************/
package com.baidu.beidou.cprounit.task;

/**
 * FlashDecodeService
 *
 * @author <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * @version 2013-6-17 下午5:46:51
 */
public interface FlashDecodeTask {
	
	/**
	 * Retrieve information from flash
	 * 
	 * @param isFull
	 * @return 
	 */
	boolean dealFlashDecode(boolean isFull);

}
