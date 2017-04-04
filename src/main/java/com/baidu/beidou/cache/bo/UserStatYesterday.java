/*******************************************************************************
 * CopyRight (c) 2000-2012 Baidu Online Network Technology (Beijing) Co., Ltd. All rights reserved.
 * Filename:    UserStatYesterday.java
 * Creator:     <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * Create-Date: 2013-1-30 下午4:43:37
 *******************************************************************************/
package com.baidu.beidou.cache.bo;

/**
 * 存放和beidoureport.stat_user_yest表关联的数据对象
 *
 * @author <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * @version 2013-1-30 下午4:43:38
 */
public class UserStatYesterday extends StatInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 321517074331292214L;
	/**
	 * 消费环比增幅，计算为:(昨日消费/前日消费-1)*10000 取整
	 */
	private int lastDayGrowth;
	/**
	 * 消费同比增幅，计算为:（昨日消费/上周同日消费-1)*10000 取整
	 */
	private int lastWeekDayGrowth;
	
	public UserStatYesterday(){};
	
	public UserStatYesterday(long srchs, long clks, long cost,
			int lastDayGrowth, int lastWeekDayGrowth) {
		super(srchs, clks, cost);
		this.lastDayGrowth = lastDayGrowth;
		this.lastWeekDayGrowth = lastWeekDayGrowth;
	}

	public int getLastDayGrowth() {
		return lastDayGrowth;
	}

	public void setLastDayGrowth(int lastDayGrowth) {
		this.lastDayGrowth = lastDayGrowth;
	}

	public int getLastWeekDayGrowth() {
		return lastWeekDayGrowth;
	}

	public void setLastWeekDayGrowth(int lastWeekDayGrowth) {
		this.lastWeekDayGrowth = lastWeekDayGrowth;
	}
	

}
