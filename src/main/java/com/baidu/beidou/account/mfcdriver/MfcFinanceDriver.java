package com.baidu.beidou.account.mfcdriver;

import java.util.List;

import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountCacheResult;

/**
 * @author hanxu03
 *
 * 2013-6-24
 */
public interface MfcFinanceDriver {

	/**
	 * 查询用户资金池待加款项信息
	 * 
	 * @param userIds	用户ID数组，例如[5, 252, 2021]
	 * @param accountIds	资金池类型ID数组，例如[1, 2, 3, 7]，其中1表示技术资金池，2表示广告资金池，3表示增值资金池，7表示网盟专属资金池。
	 * @param opuid	操作人ID
	 * @return
	 */
	UserAccountCacheResult getUserAccountCache(List<Integer> userIds, List<Integer> accountIds, int opuid);
}
