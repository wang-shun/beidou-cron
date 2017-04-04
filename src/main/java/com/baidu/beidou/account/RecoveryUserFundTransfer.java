package com.baidu.beidou.account;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.account.service.UserFundService;

public class RecoveryUserFundTransfer {
	/**
	 * @author zhangpingan
	 * 北斗自动转账升级，增加修复功能 cpweb-464
	 *
	 */
	private static final Log log = LogFactory.getLog(RecoveryUserFundTransfer.class);
	private static UserFundService service = null;
	public static void main(String[] args) {
		String[] fn = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/account/applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				fn);
		service = (UserFundService) ctx.getBean("userFundService");
		String inputFileName="recoveryUserFundTransfer.data";
		try {
			service.recoveryUserFundTransfer(inputFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			System.exit(1);
		}
	}
}
