/**
 * 
 */
package com.baidu.beidou.cache;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cache.dao.BeidouDao;
import com.baidu.beidou.cache.dao.ReportDao;
import com.baidu.beidou.cache.parser.ClickLogParser;
import com.baidu.beidou.cache.util.DateUtils;
import com.baidu.beidou.olap.constant.Constants;
import com.baidu.beidou.olap.driver.OlapStatDriver;

/**
 * @author wangqiang04
 * 
 */
public class CacheBuilder {
	private static final Log logger = LogFactory.getLog(CacheBuilder.class);

	private BeidouDao beidouDao;

	private ReportDao reportDao;

	private ClickLogParser parser;

	private OlapStatDriver olapDriver;

	public void build() {
		parser.parseYest();
	}

	public void buildYest() {
		parser.parseYest();
	}
	
	public void buildUserRealtimeStat(){
		parser.parseUserRealtimeStat();
	}

	/**
	 * Query user all statistic info(2008-11-13~yesterday) from Doris
	 */
	public void buildUserAllCache() {
		String to = DateUtils.formatDate(
				DateUtils.getDateFloor(DateUtils.getPreviousDay(new Date()))
						.getTime(), Constants.TIMESTR_HMS);
		buildUserAllCache(to);
	}

	/**
	 * Query user all statistic info(2008-11-13~to) from Doris
	 * 
	 * @param to
	 *            the format is "yyyy-MM-dd"
	 */
	public void buildUserAllCache(String to) {
		reportDao.truncate("stat_user_all");
		List<Integer> userids = beidouDao.findAllUserids();
		logger.warn("Try to query " + userids.size()
				+ " accounts all stat info(from 2008-11-13 to " + to
				+ ") from Olap...");
		olapDriver.init();
		for (int userid : userids) {
			olapDriver.send(userid, to);
		}
		olapDriver.close();

		// Set the data date
		Date userAllCacheDate = DateUtils.getPreviousDay(new Date());
		reportDao.setSysEnvValue(CacheConstants.STAT_USER_ALL_DATA_DATE,
				DateUtils.formatDate(userAllCacheDate, "yyyy-MM-dd"));
	}

	/**
	 * @param dateStr
	 *            the format is "yyyyMMdd"
	 */
	public void updateUserAllCache(String dateStr) {
		String filename = "detail" + dateStr;
		parser.parseUserData(filename);
	}

	public void setParser(ClickLogParser parser) {
		this.parser = parser;
	}

	public void setOlapDriver(OlapStatDriver olapDriver) {
		this.olapDriver = olapDriver;
	}

	public void setBeidouDao(BeidouDao beidouDao) {
		this.beidouDao = beidouDao;
	}

	public void setReportDao(ReportDao reportDao) {
		this.reportDao = reportDao;
	}

}
