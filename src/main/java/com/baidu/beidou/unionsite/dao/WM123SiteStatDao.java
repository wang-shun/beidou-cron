/**
 * 2009-4-27 下午04:46:22
 */
package com.baidu.beidou.unionsite.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.unionsite.bo.WMSiteBo;
import com.baidu.beidou.unionsite.vo.SiteElement;
import com.baidu.beidou.unionsite.vo.SiteTradeVo;
import com.baidu.beidou.unionsite.vo.WM123SiteCprodataVo;

/**
 * ClassName:WM123SiteStatDao Function: WM123项目访问beidou数据库专用DAO
 * 
 * @author <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created May 22, 2010
 * @version $Id: WM123SiteStatDao.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 * 
 *          refactor by kanghongwei since 2012-10-31
 */
public interface WM123SiteStatDao {

	/**
	 * loadAllWMSite:加载所有的WM需要的Site数据。
	 * 
	 * @return 所有的含部分字段的Site数据信息列表。
	 */
	public List<WMSiteBo> loadAllWMSite();

	/**
	 * 获取所有SiteId和SiteUrl映射信息
	 * 
	 * @return 数据库中的所有SiteId和SiteUrl信息
	 */
	public Map<Integer, String> getAllSiteIdUrlMapping();

	/**
	 * 根据一级行业ID,选取该行业分类下的展现TOPN个网站
	 * 
	 * @param tradeId
	 * @param topN
	 * @return
	 */
	public List<SiteElement> findSearchTopNSitesByFirstTradeId(final int tradeId, final int topN);

	/**
	 * getTradeList:获取站点行业列表
	 * 
	 * @since cpweb-263
	 */
	public List<SiteTradeVo> getSiteTradeList();

	/**
	 * 获取所有SiteUrl和SiteId映射信息
	 * 
	 * @return 数据库中的所有SiteUrl和SiteId信息
	 * @since cpweb-263
	 */
	public Map<String, Integer> getAllSiteUrl2IdMapping();

	// added by lvzichan,since cpweb650 2013-10-10
	/**
	 * 删除beidouext.unionsitecprodata表中某天的数据
	 * 
	 * @param date
	 *            形如20131010
	 */
	public void delSiteCprodataByDate(String date);

	/**
	 * 在表beidouext.unionsitecprodata中插入站点推广数据
	 * 
	 * @param cprodataVos
	 */
	public void saveSiteCprodata(List<WM123SiteCprodataVo> cprodataVos);
}
