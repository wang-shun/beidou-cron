package com.baidu.beidou.account.mfcdriver;

import java.util.Map;

import com.baidu.beidou.account.mfcdriver.bean.response.BeidouVipCacheResult;

/**
 * 挂接财务中心Stat服务
 * 
 * @author zhangp
 * @version 1.2.22
 */
public interface MfcStatDriverProxy {	

	/**
	 * statBeidouVipCache()
     * 分用户统计待加入beidou账面的缓存金额
	 * @return      
	 * @since
	 */
	BeidouVipCacheResult statBeidouVipCache(Map<String, String> params);
}