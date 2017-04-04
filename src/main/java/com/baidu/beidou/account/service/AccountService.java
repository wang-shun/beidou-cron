package com.baidu.beidou.account.service;

import java.util.Date;

public interface AccountService {
	
	/**
	 * 获取用户某一天的累积消费。
	 * 
	 * @param userid 用户Shifen ID
	 * @param targetDate 日期，不为空
	 * @return 累积消费值，浮点数，单位：分
	 */
	public double getTotalExpenseByUserDate(final int userid, final Date targetDate);

}
