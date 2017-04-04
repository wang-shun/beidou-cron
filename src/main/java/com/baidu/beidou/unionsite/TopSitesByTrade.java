package com.baidu.beidou.unionsite;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.WMSiteService;
import com.baidu.beidou.unionsite.vo.TradeSiteElement;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

/**
 * @author zhuqian
 *
 */
public class TopSitesByTrade {

	private static final Log LOG = LogFactory.getLog(TopSitesByTrade.class);
	
	private static WMSiteService wmSiteService;
	
	public static void main(String[] args) throws Exception {
		
		String usage = "Useage: TopSitesByTrade [topN] [output_filename]";
		
		if(args == null || args.length < 2){
			throw new Exception(usage);
		}
		
		int topN = 0;
		try{
			topN = Integer.parseInt(args[0]);
		}catch(NumberFormatException e){
			throw new Exception(usage);
		}
		
		String filepath = args[1];
		
		LogUtils.info(LOG, "Start calculating top sites...");

		long ms = System.currentTimeMillis();
		
		wmSiteService = (WMSiteService)ServiceLocator.getInstance().factory.getBean("WMSiteService");
		
		try {
						
			//获取XML文件内容
			List<TradeSiteElement> trades = wmSiteService.getTopNSitesByTrade(topN);
			
			//生成XML文件
			SiteTradeXmlParser parser = new SiteTradeXmlParser(filepath);
			parser.createXmlFile(trades);
			

		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		
		ms = System.currentTimeMillis() - ms;
		
		LogUtils.info(LOG, "Done calculating top sites in [" + ms + "] ms");
	}

	/**
	 * @return the wmSiteService
	 */
	public WMSiteService getWmSiteService() {
		return wmSiteService;
	}

	/**
	 * @param wmSiteService the wmSiteService to set
	 */
	public void setWmSiteService(WMSiteService wmSiteService) {
		this.wmSiteService = wmSiteService;
	}
	
	

}
