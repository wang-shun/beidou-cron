package com.baidu.beidou.stat;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.stat.service.StatTableService;
import com.baidu.beidou.util.LogUtils;

public class DailyOutputAllSiteIds {
	private static final Log log = LogFactory.getLog(DailyOutputAllSiteIds.class);
	private static final String TABLESERVICE_BEAN_NAME = "statTableService";
	private static StatTableService statTableService = null;

	private static void printUsage() {
		log.error("\nUSAGE: java com.baidu.beidou.stat.DailyOutputAllSiteIds adfile/$ADFILE_NAME");
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			printUsage();
			return;
		}
		String adfile = args[0];

		Date date = new Date();
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		statTableService = (StatTableService) ctx.getBean(TABLESERVICE_BEAN_NAME);
		try {
			statTableService.setFulladName(adfile);
			statTableService.outputAllSiteUnitId(date);
		} catch (Exception e) {
			LogUtils.error(log, e);
			System.exit(1);
		}
	}
}
