package com.baidu.beidou.accountmove.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.accountmove.process.MoveProcessor;

/**
 * main entrance
 * 
 * @author work
 * 
 */
public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	// accept a userid, execute account move for this user;
	public static void main(String[] args) {

		if (args == null || args.length != 2) {
			logger.error("wrong parameter number, you should add old userid and new userid as parameters");
			return;
		}
		int oldUserId = Integer.parseInt(args[0]);
		int newUserId = Integer.parseInt(args[1]);

		// use spring contain replace this entrace
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"com/baidu/beidou/accountmove/applicationContext-accountmove.xml");
		MoveProcessor moveMaster = ctx.getBean("moveProcessor",
				MoveProcessor.class);

		boolean result = moveMaster.moveAccount(oldUserId, newUserId);

		if (result) {
			logger.info("move account for " + oldUserId
					+ " is successed, new userid:" + newUserId);
		} else {
			logger.error("move account for " + oldUserId
					+ " is failed, new userid:" + newUserId);
		}

	}

}
