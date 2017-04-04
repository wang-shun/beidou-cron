/**
 * 2009-4-20 下午08:09:09
 */
package com.baidu.beidou.unionsite.task.impl;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.ErrorParameterException;
import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.bo.UnionSiteIndex;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.unionsite.service.BDSiteStatService;
import com.baidu.beidou.unionsite.service.QValueService;
import com.baidu.beidou.unionsite.service.SiteStatService;
import com.baidu.beidou.unionsite.service.UnionSiteService;
import com.baidu.beidou.unionsite.task.SiteImportTask;
import com.baidu.beidou.unionsite.vo.SiteCmpLevelCalculateVo;
import com.baidu.beidou.util.LogUtils;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteImportTaskImpl implements SiteImportTask {
	private static final Log LOG = LogFactory.getLog(SiteImportTaskImpl.class);

	/**
	 * 联盟站点的文件名
	 */
	private String unionSiteFileName = null;
	/**
	 * cpro的Q值配置文件名
	 */
	private String mainQValueFileName = null;
	private String siteQValueFileName = null;
	
	private UnionSiteService siteService = null;
	private QValueService qvalueService = null;
	private SiteStatService siteStatService = null;
	private BDSiteStatService bdStatService = null;
	
	private static final String DEFAUL_QCACHEFILENAME= "../data/unionsite/qcachefilename";

	/**
	 * 载入联盟站点数据，进行验证，排序，存入二进制文件中
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ErrorParameterException
	 */
	public void importUnionSite() throws FileNotFoundException, IOException,
			ErrorParameterException {
		List<UnionSiteIndex> indexList = siteService
				.sortUnionSite(unionSiteFileName);
		siteService.persistentSiteIndex(indexList);

	}

	/**
	 * Q值读取并进行排序，获得域名级别的数据，存入二进制文件中
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public String importQValue(boolean forceGenerate, String qCacheFileName)
			throws FileNotFoundException, IOException {
		String result = qvalueService.loadQValue(mainQValueFileName, siteQValueFileName,
				forceGenerate);
		BufferedWriter file = new BufferedWriter(new FileWriter(qCacheFileName));
		try {
			file.write(result);
		} catch (IOException e) {
			LogUtils.fatal(LOG, "无法写入q值缓存文件名"+qCacheFileName, e);
			throw e;
		} finally{
			file.close();
		}
		
		return result;
	}

	/**
	 * 导入cpro的统计数据
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws InternalException
	 */
	public void importDaySiteStat(String[] siteStatFile)
			throws InternalException, IOException {
		for (String file : siteStatFile) {
			siteStatService.sortDaySiteStat(file);
		}
	}

	/**
	 * 进行统计，计算，获得7日平均数据
	 * 
	 * @author zengyunfeng
	 * @param recompute
	 *            是否需要重新计算
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws InternalException
	 */
	public void genAvgSiteStat() throws InternalException, IOException,
			ClassNotFoundException, ErrorFormatException {
		siteStatService.averageSiteStat();
	}

	/**
	 * 导入beidou的站点全库任务。
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws InternalException
	 * @throws ErrorFormatException 
	 */
	public void importBDSite(String[] siteStatFile)
			throws FileNotFoundException, IOException, ClassNotFoundException,
			InternalException, ErrorFormatException {
		LOG.info("Begin to importBDSite");
		// 读取Q值文件
		String qFile = importQValue(false,DEFAUL_QCACHEFILENAME);
		// 读取日统计文件
		importDaySiteStat(siteStatFile);

		// 对7日数据进行汇总
		genAvgSiteStat();
		// 站点信息入库
		SiteCmpLevelCalculateVo bdSiteRes = bdStatService.bdSiteStore(
				siteService.getUnionSiteFile(), siteService.getIndexFileName(), qFile,
				siteStatService.getAvgSiteStatFile(), siteStatService.getValidDomainFromCurrentDayFileAfterImported());

		// 计算全库的等级和热度，并更新数据库
		bdStatService.bdSiteCalculate(bdSiteRes);
		LOG.info("End to importBDSite");
	}
	
	/**
	 * 已有平均数据，导入站点数据并计算热度
	 * @author zengyunfeng
	 * @param qFile
	 * @throws FileNotFoundException
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void bdSiteStoreAndCalculate(String qFile) throws FileNotFoundException, InternalException, IOException, ClassNotFoundException{
		LOG.info("Begin to bdSiteStoreAndCalculate");
		// 站点信息入库
		SiteCmpLevelCalculateVo bdSiteRes = bdStatService.bdSiteStore(
				siteService.getUnionSiteFile(), siteService.getIndexFileName(), qFile,
				siteStatService.getAvgSiteStatFile(), siteStatService.getValidDomainFromCurrentDayFileAfterImported());
		gc();
		LOG.info("site count="+bdSiteRes.getSiteList().size());
		// 计算全库的等级和热度，并更新数据库
		bdStatService.bdSiteCalculate(bdSiteRes);
		LOG.info("End to bdSiteStoreAndCalculate");
	}
	
	private void gc(){	
		Runtime rt=Runtime.getRuntime();
		long maxMemory=rt.maxMemory();
		long freeMemory=rt.freeMemory();
		LOG.info("gc:   free:"+freeMemory+"  ,max:"+maxMemory+", percent:"+(double)freeMemory/maxMemory);
		rt.gc();
	}
	

	/**
	 * QValue错误，强制更新QValue, 然后重新计算等级，热度等
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ErrorFormatException 
	 */
	public void recoverQvalue(String[] siteStatFile) throws InternalException,
			IOException, ClassNotFoundException, ErrorFormatException {
		// 读取Q值文件
		String qFile = importQValue(true,DEFAUL_QCACHEFILENAME);

		// 读取日统计文件
		importDaySiteStat(siteStatFile);

		// 对7日数据进行汇总
		genAvgSiteStat();

		// 站点信息入库
		SiteCmpLevelCalculateVo bdSiteRes = bdStatService.bdSiteStore(
				siteService.getUnionSiteFile(), siteService.getIndexFileName(), qFile,
				siteStatService.getAvgSiteStatFile(), siteStatService.getValidDomainFromCurrentDayFileAfterImported());

		// 计算全库的等级和热度，并更新数据库
		bdStatService.bdSiteCalculate(bdSiteRes);
	}

	/**
	 * 日数据导入正确，只是进行平均数据计算，然后进行统计
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ErrorFormatException 
	 */
	public void recoverAvgBDSite() throws InternalException, IOException,
			ClassNotFoundException, ErrorFormatException {
		// 读取Q值文件
		String qFile = importQValue(false,DEFAUL_QCACHEFILENAME);

		genAvgSiteStat();
		// 站点信息入库
		SiteCmpLevelCalculateVo bdSiteRes = bdStatService.bdSiteStore(
				siteService.getUnionSiteFile(), siteService.getIndexFileName(), qFile,
				siteStatService.getAvgSiteStatFile(), siteStatService.getValidDomainFromCurrentDayFileAfterImported());

		// 计算全库的等级和热度，并更新数据库
		bdStatService.bdSiteCalculate(bdSiteRes);
	}

	/**
	 * 平均数据汇总正确，只是进行数据统计
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void recoverBDStatSite() throws InternalException, IOException,
			ClassNotFoundException {
		// 读取Q值文件
		String qFile = importQValue(false,DEFAUL_QCACHEFILENAME);

		// 站点信息入库
		SiteCmpLevelCalculateVo bdSiteRes = bdStatService.bdSiteStore(
				siteService.getUnionSiteFile(), siteService.getIndexFileName(), qFile,
				siteStatService.getAvgSiteStatFile(), siteStatService.getValidDomainFromCurrentDayFileAfterImported());
		// 计算全库的等级和热度，并更新数据库
		bdStatService.bdSiteCalculate(bdSiteRes);
	}
	
	public void transform(String[] fileName) throws InternalException, IOException{
		for (String file : fileName) {
			siteStatService.transform(file);
		}
	}

	/**
	 * @param unionSiteFileName
	 *            the unionSiteFileName to set
	 */
	public void setUnionSiteFileName(String unionSiteFileName) {
		this.unionSiteFileName = unionSiteFileName;
	}

	/**
	 * @param siteService
	 *            the siteService to set
	 */
	public void setSiteService(UnionSiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * @param mainQValueFileName
	 *            the mainQValueFileName to set
	 */
	public void setMainQValueFileName(String mainQValueFileName) {
		this.mainQValueFileName = mainQValueFileName;
	}

	/**
	 * @param siteQValueFileName
	 *            the siteQValueFileName to set
	 */
	public void setSiteQValueFileName(String siteQValueFileName) {
		this.siteQValueFileName = siteQValueFileName;
	}

	/**
	 * @param siteStatService
	 *            the siteStatService to set
	 */
	public void setSiteStatService(SiteStatService siteStatService) {
		this.siteStatService = siteStatService;
	}

	/**
	 * @param bdStatService
	 *            the bdStatService to set
	 */
	public void setBdStatService(BDSiteStatService bdStatService) {
		this.bdStatService = bdStatService;
	}

	/**
	 * @param valueService
	 *            the qvalueService to set
	 */
	public void setQvalueService(QValueService valueService) {
		qvalueService = valueService;
	}

}
