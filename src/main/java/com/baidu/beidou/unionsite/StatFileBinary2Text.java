package com.baidu.beidou.unionsite;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.dao.SiteStatFileDao;
import com.baidu.beidou.unionsite.service.SiteConstantMgr;
import com.baidu.beidou.util.ServiceLocator;

/**
 * @author zhuqian
 * 
 */
public class StatFileBinary2Text {

	private static final Log LOG = LogFactory.getLog(StatFileBinary2Text.class);

	private static SiteStatFileDao statFileDao = null;

	private static void contextInitialized() {
		//初始化站点配置
		SiteConstantMgr globalConstantMgr = (SiteConstantMgr) ServiceLocator.getInstance().factory.getBean("siteConstantService");
		globalConstantMgr.loadConfFile();
	}
	
	public static void main(String[] args) throws Exception {

		try {
			
			contextInitialized();

			if (args == null || args.length == 0) {
				throw new Exception("usage: StatFileBinary2Text [file] \n"
						+ "\t -file: filename of the binary file to convert");
			}

			String filename = args[0];

			LOG.info("==== PROCESSING FILE '" + filename + "' ====");
			long ms = System.currentTimeMillis();
			
			//从二进制文件中读入
			ObjectInputStream input = new ObjectInputStream(
					new FileInputStream(filename));
			
			if (input == null) {
				throw new Exception(
						"ObjectInputStream is null, check inputfile: '"
								+ filename + "'");
			}
			
			List<SiteStatBo> result = new ArrayList<SiteStatBo>();
			
			while(true){
				SiteStatBo bo = statFileDao.next(input);
				
				if(bo == null){
					break;
				}
				
				//从总量和悬浮流量中，获取固定流量				
				bo.setFixedAds(bo.getAds() - bo.getFlowAds());
				bo.setFixedClicks(bo.getClicks() - bo.getFlowClicks());
				bo.setFixedCost(bo.getCost() - bo.getFlowCost());
				bo.setFixedRetrieve(bo.getRetrieve() - bo.getFlowRetrieve());
				
				result.add(bo);
			}
			
			//写出至明文文件
			FileOutputStream output = new FileOutputStream(filename + ".txt");
			statFileDao.persistentAll(output, result);
			
			
			LOG.info("==== DONE PROCESSING FILE '" + filename + "' in ["
					+ (System.currentTimeMillis() - ms) + "] ms ====");

		} catch (Exception e) {
			LOG.fatal(e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * @return the statFileDao
	 */
	public SiteStatFileDao getStatFileDao() {
		return statFileDao;
	}

	/**
	 * @param statFileDao the statFileDao to set
	 */
	public void setStatFileDao(SiteStatFileDao statFileDao) {
		StatFileBinary2Text.statFileDao = statFileDao;
	}
	
	
	
}
