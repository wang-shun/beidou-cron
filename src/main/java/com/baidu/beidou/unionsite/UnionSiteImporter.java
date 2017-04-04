/**
 * 2009-4-20 下午06:10:02
 */
package com.baidu.beidou.unionsite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.SiteConstantMgr;
import com.baidu.beidou.unionsite.task.SiteImportTask;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class UnionSiteImporter {

	private static final Log LOG = LogFactory.getLog(UnionSiteImporter.class);
	
	private static SiteImportTask importTask = null; 
	
	public static int ignore = 0;
	
	public static int empty1 = 0;
	public static int empty2 = 0;
	public static int empty3 = 0;
	
	private static void contextInitialized() {
		//初始化站点配置
		SiteConstantMgr globalConstantMgr = (SiteConstantMgr) ServiceLocator.getInstance().factory.getBean("siteConstantService");
		globalConstantMgr.loadConfFile();
	}
	
	/**
	 * @author zengyunfeng
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		LogUtils.info(LOG, "start to import union site data....");
		long start = System.currentTimeMillis();
		importTask = ServiceLocator.getInstance().getSiteImportTask();
		try {
			contextInitialized();
			importTask.importUnionSite();
			long end = System.currentTimeMillis();
			LogUtils.info(LOG, "ignore records= "+ignore+"\tcostTime="+(end-start));
			LogUtils.info(LOG, empty1+" records with sitename is empty");
			LogUtils.info(LOG, empty2+" records with sitedesc is empty");
			LogUtils.info(LOG, empty3+" records with firsttrade is empty");
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		LogUtils.info(LOG, "end to import union site data.");
	}

}
