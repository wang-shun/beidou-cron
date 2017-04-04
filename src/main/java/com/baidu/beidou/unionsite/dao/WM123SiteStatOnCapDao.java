/**
 * beidou-cron-trunk#com.baidu.beidou.unionsite.dao.WM123SiteStatOnCapDao.java
 * 上午11:20:20 created by kanghongwei
 */
package com.baidu.beidou.unionsite.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.unionsite.bo.RegionInfo;
import com.baidu.beidou.unionsite.vo.TradeSiteElement;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public interface WM123SiteStatOnCapDao {

	/**
	 * 获得所有的地域信息需要上卷的一级地域ID列表
	 * 加载所有的regtype=1&&secondregid=0&&firstregid>6的firstregid。
	 * <p>
	 * 此处为所有的一有地域，但不包括四个直辖市和二个特别行政区。
	 * 
	 * @return
	 */
	public List<Integer> findExceptionalRegInfoId();

	/**
	 * 根据type获得所有的地域信息
	 */
	public List<RegionInfo> findAllRegInfoByType(int type);

	/**
	 * 获取所有的普通一级行业分类，不包括百名单，不包括一级分类的"其他"
	 * 
	 * @return
	 */
	public List<TradeSiteElement> findAllFirstTradeIdName();

	/**
	 * updateSysnvtab:更新sysnvtab表中的数据
	 * 
	 * @param name
	 *            key
	 * @param value
	 *            value
	 * @since cpweb-206
	 */
	void updateSysnvtab(String name, String value);

	/**
	 * getTradeList:获取行业信息列表
	 * 
	 * @since cpweb-263
	 */
	public List<Map<String, Object>> getTradeList();

	
	// added by lvzichan,since 2013-10-10
	/**
	 * 获取所有一级广告行业的id~name映射
	 */
	public Map<Integer, String> getFirstAdTradeMap();

	/**
	 * 获取所有一级网站行业的id~name映射
	 */
	public Map<Integer, String> getFirstSiteTradeMap();

	/**
	 * 获取所有网站行业id对应的一级网站行业id
	 */
	public Map<Integer, Integer> getSecond2FirstSiteTradeMap();
	// end added by lvzichan,since 2013-10-10
}
