package com.baidu.beidou.account;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.account.service.UserFundService;

public class OutputShifenData {
	private static final Log log = LogFactory.getLog(OutputShifenData.class);
	private static final String TABLESERVICE_BEAN_NAME = "userFundService";
	private static UserFundService userFundService = null;

	public static void main(String[] args) {
		String[] fn = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/account/applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				fn);
		userFundService = (UserFundService) ctx.getBean(TABLESERVICE_BEAN_NAME);
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			Date date = calendar.getTime();
			userFundService.importClkData();
			userFundService.createDailyLogTable(date);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}
}
