package com.baidu.beidou.cprogroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprogroup.facade.CproGroupFacade;

public class CheckRepeateGroupInfo {

	private static final Log log = LogFactory.getLog(CheckRepeateGroupInfo.class);

	private static final String CPROGROUPFACADE_BEAN_NAME = "cproGroupFacade";

	private static CproGroupFacade cproGroupFacade;

	public static void main(String[] args) {

		String[] fn = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);

		cproGroupFacade = (CproGroupFacade) ctx.getBean(CPROGROUPFACADE_BEAN_NAME);

		if (cproGroupFacade == null) {
			log.error("facade class cproGroupFacade is null");
			System.exit(1);
		}
		cproGroupFacade.checkRepeateGroup();
	}
}