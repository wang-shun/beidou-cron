/**
 * 2009-4-20 下午08:06:54
 */
package com.baidu.beidou.unionsite.task;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.baidu.beidou.exception.ErrorParameterException;
import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * 北斗站点全库导入任务
 */
public interface SiteImportTask {

	/**
	 * 载入联盟站点数据，进行验证，排序，存入二进制文件中
	 * @author zengyunfeng
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ErrorParameterException 
	 */
	void importUnionSite() throws FileNotFoundException, IOException, ErrorParameterException;
	
	/**
	 * 导入beidou的站点全库任务。
	 * @author zengyunfeng
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws ErrorFormatException 
	 */
	void importBDSite(String[] siteStatFile) throws FileNotFoundException, IOException, ClassNotFoundException, InternalException, ErrorFormatException;
	
	/**
	 * QValue错误，强制更新QValue, 然后重新计算等级，热度等
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ErrorFormatException 
	 */
	void recoverQvalue(String[] siteStatFile) throws InternalException, IOException, ClassNotFoundException, ErrorFormatException;
	
	/**
	 * 日数据导入正确，只是进行平均数据计算，然后进行统计
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ErrorFormatException 
	 */
	void recoverAvgBDSite() throws InternalException, IOException, ClassNotFoundException, ErrorFormatException;
	
	/**
	 * 平均数据汇总正确，只是进行数据统计
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	void recoverBDStatSite() throws InternalException, IOException, ClassNotFoundException;
	
	/**
	 * 把统计输入文件转换成版本为version的二进制文件格式，并更新数据库的size和start
	 * 目前version只支持1
	 * @author zengyunfeng
	 * @param fileName
	 * @param version
	 * @throws InternalException 
	 * @throws IOException 
	 */
	void transform(String[] fileName) throws InternalException, IOException;
	
	/**
	 * 导入cpro的统计数据,生成二进制文件，并更新数据库的size和start
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws InternalException
	 */
	public void importDaySiteStat(String[] siteStatFile)
			throws InternalException, IOException;
	
	/**
	 * Q值读取并进行排序，获得域名级别的数据，存入二进制文件中
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public String importQValue(boolean forceGenerate, String qCacheFileName)
	throws FileNotFoundException, IOException;
	
	/**
	 * 进行统计，计算，获得7日平均数据
	 * 
	 * @author zengyunfeng
	 * @param recompute
	 *            是否需要重新计算
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InternalException
	 * @throws ErrorFormatException 
	 */
	public void genAvgSiteStat() throws InternalException, IOException,
			ClassNotFoundException, ErrorFormatException;
	
	/**
	 * 已有平均数据，导入站点数据并计算热度
	 * @author zengyunfeng
	 * @param qFile
	 * @throws FileNotFoundException
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void bdSiteStoreAndCalculate(String qFile) throws FileNotFoundException, InternalException, IOException, ClassNotFoundException;
}
