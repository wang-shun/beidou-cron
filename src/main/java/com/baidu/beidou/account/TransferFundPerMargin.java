package com.baidu.beidou.account;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.account.service.UserFundService;
import com.baidu.beidou.util.LogUtils;

public class TransferFundPerMargin {
	
	private static final Log LOG = LogFactory.getLog(TransferFundPerMargin.class);
	private static final String TABLESERVICE_BEAN_NAME = "userFundService";
	private static UserFundService userFundService = null;
	
	public static void main(String[] args){
		
		String[] fn = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/account/applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				fn);
		userFundService = (UserFundService)ctx.getBean(TABLESERVICE_BEAN_NAME);
		
		try {
			userFundService.autoTransferFundPerMargin();
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
		}
	}
}
