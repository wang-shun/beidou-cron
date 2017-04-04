/**
 * 2009-4-23 下午05:51:11
 */
package com.baidu.beidou.cprounit.icon.dao;

import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.AdTradeInfo;


/**
 * @author zengyunfeng
 * @version 1.1.3
 */
public interface AdConfigDao {
	
	
	/**
	 * 获得所有的广告id分类
	 * 2009-4-24
	 * zengyunfeng
	 * @version 1.1.3
	 * @return
	 */
	public List<AdTradeInfo> findAdTrade();
	

}
