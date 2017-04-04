package com.baidu.beidou.unionsite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.WM123SiteScoreService;
import com.baidu.beidou.util.ServiceLocator;

/**
 * ClassName: WM123SiteScoreRefresher 
 * Function:
 * 根据从河图取得的domain粒度和domain_tu粒度的数据文件，计算联盟网站得分，并且更新到数据库中
 * 
 * @author lvzichan
 * @since 2013-08-01
 */

public class WM123SiteScoreRefresher {

	private static final Log LOG = LogFactory.getLog(WM123SiteScoreRefresher.class);

	private static WM123SiteScoreService wm123SiteScoreService = null;

	private static void contextInitialized() {
	}

	public static void main(String[] args) throws Exception {
		LOG.info("start to refresh wm123 sitescore data....");

		long start = System.currentTimeMillis();
		wm123SiteScoreService = (WM123SiteScoreService) ServiceLocator
				.getInstance().factory.getBean("wm123SiteScoreService");
		try {
			contextInitialized();

			LOG.info("---------------------------------refresh wm123 sitescore data. ");
			wm123SiteScoreService.refreshSiteScore();
			long end = System.currentTimeMillis();
			LOG.info("---------------------------------costTime=" + (end - start));
		} catch (Exception e) {
			LOG.fatal(e.getMessage(), e);
			throw e;
		}
		LOG.info("end to refresh wm123 sitescore data....");
	}
}