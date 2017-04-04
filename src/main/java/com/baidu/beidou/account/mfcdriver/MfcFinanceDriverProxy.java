package com.baidu.beidou.account.mfcdriver;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountCacheResult;

/**
 * @author hanxu03
 *
 * 2013-6-24
 */
public interface MfcFinanceDriverProxy {

	public final int TECH_ACCOUNT = 1;		//技术资金池
	public final int AD_ACCOUNT = 2;		//广告资金池
	public final int EXTRA_ACCOUNT = 3;		//增值资金池
	public final int WANGMENG_ACCOUNT = 7;	//网盟专属资金池
	
	/**
	 * 查询用户资金池待加款项信息
	 * 
	 * @param userIds    用户ID数组，例如[5, 252, 2021]
	 * @param accountIds    资金池类型ID数组，例如[1, 2, 3, 7]，其中1表示技术资金池，2表示广告资金池，3表示增值资金池，7表示网盟专属资金池。
	 * @param opuid   操作人ID
	 * @param params  Rpc调用时需要传递的参数列表，通常是消息头。
	 * @return
	 */
	UserAccountCacheResult getUserAccountCache(List<Integer> userIds, List<Integer> accountIds, int opuid, Map<String, String> params);
}
