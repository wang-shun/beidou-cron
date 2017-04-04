package com.baidu.beidou.account;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.account.service.NotifyZeroBalanceTask;

/**
 * @author zhuqian
 *
 */
public class SendZeroBalanceRemind {

	private static final Log log = LogFactory.getLog(SendZeroBalanceRemind.class);

	private static NotifyZeroBalanceTask task = null;

	public static void main(String[] args) {
		String[] fn = new String[] {"applicationContext.xml","classpath:/com/baidu/beidou/account/applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
		task = (NotifyZeroBalanceTask) ctx.getBean("notifyZeroBalanceTask");
		
		try {
			task.execute();
		} catch (Exception e) {
			log.error(e);
			System.exit(1);
		}
	}

}
