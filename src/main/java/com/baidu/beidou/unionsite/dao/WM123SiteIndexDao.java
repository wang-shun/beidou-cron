/**
 * 2009-4-27 下午04:46:22
 */
package com.baidu.beidou.unionsite.dao;

import java.util.List;

import com.baidu.beidou.unionsite.bo.WMSiteBo;
import com.baidu.beidou.unionsite.bo.WMSiteIndexBo;
import com.baidu.beidou.unionsite.vo.WMSiteIndexVo;
import com.baidu.beidou.unionsite.vo.WMSiteVisitorIndexVo;

/**
 * ClassName:WM123SiteIndexDao Function: WM123项目访问UnionSite数据库专用DAO
 * 
 * @author <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created May 22, 2010
 * @version $Id: WM123SiteIndexDao.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 * 
 *          refactor by kanghongwei since 2012-10-31
 */
public interface WM123SiteIndexDao {

	/**
	 * insertSiteAdditionalInfo:将WMSiteBo中的IP，UV，Siteheat信息写到数据库。
	 * 
	 * @param list
	 *            WMSiteBo列表
	 */
	void addSiteAdditionalInfo(List<WMSiteBo> list);

	/**
	 * loadAllSiteIndexInfo:加载所有的Index原始信息
	 */
	List<WMSiteIndexVo> loadAllSiteIndexInfo();

	/**
	 * insertSiteIndexStat:插入WMSiteIndexBo信息到数据库中
	 * 
	 * @param list
	 * @since
	 */
	void addSiteIndexStat(List<WMSiteIndexBo> list);

	/**
	 * insertSiteIndex:将WMSiteIndexVo保存
	 * 
	 * @param list
	 *            WMSiteIndexVo集合
	 */
	void addSiteIndex(List<WMSiteIndexVo> list);

	/**
	 * insertSiteVisitorIndex:将WMSiteVisitorIndexVo保存
	 * 
	 * @param list
	 *            WMSiteIndexVo集合
	 */
	void addSiteVistorIndex(List<WMSiteVisitorIndexVo> list);
}
