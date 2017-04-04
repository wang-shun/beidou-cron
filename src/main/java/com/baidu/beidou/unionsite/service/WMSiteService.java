package com.baidu.beidou.unionsite.service;

import java.io.IOException;
import java.util.List;

import com.baidu.beidou.unionsite.vo.TradeSiteElement;

/**
 * ClassName:WMSiteStatService
 * Function: 实现WM123项目的站点统计和入库相关操作
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  May 23, 2010
 * @version  $Id: WMSiteService.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 */
public interface WMSiteService {

	
	/**
	 * 计算全库站点的热度、IP访问量、UV量;
	 */
	public void wm123SiteCalculate();
	
	/**
	 * wm123SiteIndexCalculate:计算Site的Index属性信息
	*/
	public void wm123SiteIndexCalculate();
	
	/**
	 * storeOriginIndexInfo:存储BA传递过来的原样Index数据，此处不计算。
	 * 计算放在wm123SiteIndexCalculate中做。
	 * 注意：此处要对SiteId和SiteUrl做校验，避免BA的数据不合法。
	 * 
	 * @param files 要导入的文件名
	 * @see #wm123SiteIndexCalculate     
	 * @since 
	*/
	public void storeOriginIndexInfo(String[] files) throws IOException ;
	
	/**
	 * 获取每个一级行业，流量TOPN的站点信息
	 * @param topN
	 * @return
	 */
	public List<TradeSiteElement> getTopNSitesByTrade(final int topN);
	
	/**
	 * storeVisitorIndexInfo:存储司南传递过来的原样访客特征数据，此处不计算。
	 * 计算放在WM123缓存加载中做。
	 * 注意：此处要对SiteId和SiteUrl做映射。
	 * 
	 * @param files 要导入的文件名
	 * @since cpweb-263
	*/
	public void storeVisitorIndexInfo(String file) throws IOException ;
	
}
