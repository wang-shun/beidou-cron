package com.baidu.beidou.account;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.account.service.UserFundService;
import com.baidu.beidou.util.DateUtils;

public class RecoverClkLogRec {
	private static final Log log = LogFactory.getLog(RecoverClkLogRec.class);
	private static final String TABLESERVICE_BEAN_NAME = "userFundService";
	private static UserFundService userFundService = null;

	public static void main(String[] args) {
		String[] fn = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/account/applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				fn);
		userFundService = (UserFundService) ctx.getBean(TABLESERVICE_BEAN_NAME);
		String dateStr = args[0];
		if (dateStr == null) {
			log.error("no recover date input");
			System.exit(1);
		}
		if (!DateUtils.validateDateString(dateStr)) {
			log.error("date formate error");
			System.exit(1);
		}
		try {
			Date date = DateUtils.StrToDate(dateStr);
			userFundService.dropDailyLogTable(date);
			userFundService.createDailyLogTable(date);
		} catch (Exception e) {
			log.error(e);
			System.exit(1);
		}
	}

}
