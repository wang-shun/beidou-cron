/**
 * 2009-4-23 下午01:56:04
 */
package com.baidu.beidou.unionsite.service;

import java.io.IOException;
import java.util.Set;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * cpro-stat统计信息：站点统计信息接口
 */
public interface SiteStatService {

	/**
	 * 对日统计数据进行校验，排序，然后存储
	 * @author zengyunfeng
	 * @param cproFileName cpro-stat原始文件的文件名
	 * @throws InternalException 
	 * @throws IOException 
	 */
	public void sortDaySiteStat(String cproFileName) throws InternalException, IOException;
	
	/**
	 * 获得7天的平均统计数据,并存储
	 * @author zengyunfeng
	 * @return 返回排好序的统计数据列表
	 * @throws InternalException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void averageSiteStat() throws InternalException, IOException, ClassNotFoundException, ErrorFormatException;
	
	/**
	 * 获得站点平均数据的文件名
	 * @author zengyunfeng
	 * @return
	 */
	public String getAvgSiteStatFile();
	
	/**
	 * 获取当天有效的域名,当天的统计数据文件名为导入操作完成之后文件名
	 *
	 * @return      当天有效的域名
	 * @since Cpweb218，wm123网站优化
	*/
//	public Set<String> getValidDomainFromCurrentDayFileAfterImported();

	/**
	 * 读取老的文件修改成老的对象
	 * @author zengyunfeng
	 * @param file
	 * @throws InternalException 
	 */
	public void transform(String file) throws InternalException, IOException;
	

	/**
	 * <p>getValidDomainFromCurrentDayFileAfterImported:从当天的统计文件中获取有效的一二级域名列表
	 *
	 * @return      
	 * @since 
	*/
	public Set<String> getValidDomainFromCurrentDayFileAfterImported();
	
	/**
	 * 读取7日平均统计数据信息
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
//	public List<SiteStatBo> loadAvgSiteStat() throws IOException,
//			ClassNotFoundException;
}
