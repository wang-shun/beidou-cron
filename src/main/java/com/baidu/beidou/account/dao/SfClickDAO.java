package com.baidu.beidou.account.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhuqian
 *
 */
public interface SfClickDAO {
	
	public static final String CLK_TABLE_NAME_BASE = "SF_Click.clk";
	public static final SimpleDateFormat CLK_TABLE_NAME_DF = new SimpleDateFormat("yyMMdd");

	/**
	 * 获取用户某一天的累积消费。
	 * 
	 * @param userid 用户Shifen ID
	 * @param targetDate 日期，不为空
	 * @return 累积消费值，浮点数，单位：元
	 */
	public double getTotalExpenseByUserDate(final int userid, final Date targetDate);
	
	/**
	 * 获取用户截止当前的当日累积消费
	 * 
	 * @param userid 用户Shifen ID
	 * @param targetDate 日期
	 * @return 累积消费值，浮点数，单位：元
	 */
	public double getTodaysExpenseByUser(final int userid);
}
