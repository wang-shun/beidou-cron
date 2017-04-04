/**
 * 2009-4-20 下午09:32:44
 */
package com.baidu.beidou.unionsite.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.baidu.beidou.exception.ErrorParameterException;
import com.baidu.beidou.unionsite.bo.UnionSiteIndex;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * 读取联盟站点，进行排序，建立索引，读取索引
 */
public interface UnionSiteService {

	/**
	 * 读取联盟的站点信息，并进行排序，建立索引
	 * @author zengyunfeng
	 * @param filename
	 * @throws FileNotFoundException 文件不存在
	 * @throws IOException 
	 */
	List<UnionSiteIndex> sortUnionSite(final String filename) throws FileNotFoundException, IOException;
	
	/**
	 * 存储联盟站点索引信息
	 * @author zengyunfeng
	 * @param indexes 排好序的索引列表
	 * @throws ErrorParameterException 参数为null
	 * @throws IOException 不能打开文件，或者是文件读取异常
	 */
	void persistentSiteIndex(final List<UnionSiteIndex> indexes) throws ErrorParameterException, FileNotFoundException, IOException;
	

	/**
	 * 获得联盟站点的索引信息文件名
	 * 
	 * @return the indexFileName
	 */
	String getIndexFileName();
	
	/**
	 * 获得联盟站点的对象文件名
	 * 
	 * @return the unionSiteFile
	 */
	String getUnionSiteFile() ;
	
	/**
	 * getCurrentValidDomainFromIndexFile:从联盟索引文件获取当天有效的域名列表
	 * @author liangshimu
	 * @return 域名列表，集合包括一级域名和没有一级域名的二级域名。
	*/
	public Set<String> getCurrentValidDomainFromIndexFile();
	
}
