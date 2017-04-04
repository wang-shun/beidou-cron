/**
 * 2009-4-27 下午04:46:22
 */
package com.baidu.beidou.unionsite.dao;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.bo.BDSiteBo;
import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.vo.SiteBDStatVo;
import com.baidu.beidou.unionsite.vo.SiteInfo4KeepInDB;
import com.baidu.beidou.unionsite.vo.WM123SiteScoreVo;

/**
 * beidou站点全库存储
 * 
 * @author zengyunfeng
 * @version 1.0.7
 * 
 *          refactor by kanghongwei since 2012-10-29
 */
public interface BDSiteStatDao {

	/**
	 * 根据url查找siteid
	 * 
	 * @author zengyunfeng
	 * @param url
	 * @return
	 */
	int findIdByUrl(String url);

	/**
	 * 
	 * @author zengyunfeng
	 * @param bdSiteList
	 * @param siteTradeList
	 * @param unionSiteReader
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InternalException
	 * @throws IOException
	 */
	void insertBDSite(final List<BDSiteBo> bdSiteList, final List<SiteBDStatVo> siteTradeList, RandomAccessFile unionSiteReader) throws IOException, InternalException, ClassNotFoundException;

	/**
	 * 更新网站等级和热度
	 * 
	 * @author zengyunfeng
	 * @param siteList
	 * @return
	 */
	int updateSiteScaleAndCmp(final List<SiteBDStatVo> siteList);

	/**
	 * 把所有有效的站点设置为待处理<br/>
	 * modify by
	 * liangshimu,201012123,修改内容包括：unionsite.valid和unionsite.currentinvalid
	 * 
	 * @author zengyunfeng
	 */
	void updateSiteStatusDealing();

	/**
	 * 把所有待处理的站点设置为失效 modify by
	 * liangshimu,201012123,修改内容包括：unionsite.valid和unionsite.currentinvalid
	 * 
	 * @author zengyunfeng
	 */
	void updateSiteStatusInvalid();

	/**
	 * 
	 * getAllDomainId: 获取所有主域的siteid,不考虑是否有效
	 * 
	 * @return key为主域 ,value为siteid
	 * @since 1.0.51
	 */
	public Map<String, Integer> getAllMainDomainId();

	/**
	 * 
	 * insertInvalidDomainSite: unionsite中插入占位siteid的无效主域，其主域为WhiteUrl.url字段
	 * 
	 * @param list
	 * @since 1.0.51
	 */
	public void insertInvalidDomainSite(List<WhiteUrl> list);

	/**
	 * 
	 * findAllValidDomainId:获得有效的主域id(包括没有主域的二级域名)
	 * 
	 * @return 以siteurl为key,siteid为value的Map
	 * @since 1.0.51
	 */
	public Map<String, Integer> findAllValidDomainId();

	/**
	 * 
	 * findAllValidSiteUrlAndJointime:获得有效的网站Url和加入时间的对应关系
	 * 
	 * @return 以siteurl为key,siteid为value的Map
	 * @since 1.0.51
	 */
	public Map<String, SiteInfo4KeepInDB> findAllValidSiteUrlAndJointime();

	/**
	 * 获取百度自有流量中通用白名单行业所对应的网站
	 * 
	 * @param tradeIdList
	 * @return
	 */
	public List<Integer> findSiteIdByBaiduCommonTrade(List<Integer> tradeIdList);
	
	/**
	 * 更新网站得分
	 * 
	 * @author lvzichan,2013-08-01
	 * @param refreshScoreList
	 * @return
	 */
	int updateSiteScore(List<WM123SiteScoreVo> refreshScoreList);
	
	/**
	 * 根据siteid查找网站得分
	 * @author lvzichan,2013-08-26
	 * @param siteid
	 * @return
	 */
	int findScoreById(int siteid);
}
