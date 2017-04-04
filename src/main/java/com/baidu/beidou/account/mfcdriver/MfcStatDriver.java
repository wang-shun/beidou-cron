package com.baidu.beidou.account.mfcdriver;

import com.baidu.beidou.account.mfcdriver.bean.response.BeidouVipCacheResult;

/**
 * 挂接财务中心所需服务
 * 
 * @author zhangp
 * @version 1.2.22
 */
public interface MfcStatDriver {	
	/**
	 * 
	 * 分用户统计待加入beidou账面的缓存金额
	 * 返回值描述如下：
	 * Map的Key为用户ID
	 * Map的Value为对应用户的缓存余额
	 * @return      
	 * @since
	 */
	BeidouVipCacheResult statBeidouVipCache();
}