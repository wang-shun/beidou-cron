/**
 * 2009-4-27 下午02:50:50
 */
package com.baidu.beidou.unionsite.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.vo.SiteCmpLevelCalculateVo;

/**
 * beidou站点全库数据的统计和计算，包括基本的展现信息合并，等级，热度等计算
 * @author zengyunfeng
 * @version 1.0.7
 */
public interface BDSiteStatService {

	/**
	 * 计算，并存入数据库
	 * 
	 * @author zengyunfeng
	 * @param unionsiteFile
	 * @param unionSiteList
	 * @param qList
	 * @param siteStatList
	 * @param currentValidDomain 当前有效域名
	 * @return 返回新的站点id信息(热度计算中需要使用的值)，用于热度的计算
	 * @throws InternalException 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public SiteCmpLevelCalculateVo bdSiteStore(final String unionsiteFile,
			final String unionSiteListFile, final String qListFile,
			final String siteStatListFile,
			final Set<String> currentValidDomain) throws InternalException, FileNotFoundException, IOException, ClassNotFoundException;
	
	/**
	 * 计算全库站点的热度：	cmplevel , ratecmp, scorecmp;
	 * @author zengyunfeng
	 * @param siteid
	 */
	public void bdSiteCalculate(SiteCmpLevelCalculateVo siteListVo);
	
}
