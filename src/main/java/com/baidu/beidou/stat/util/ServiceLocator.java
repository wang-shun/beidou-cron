package com.baidu.beidou.stat.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.stat.service.StatTableService;

/**
 * 
 * @author zhangpeng
 * @version 1.0.0
 */
public class ServiceLocator {

	private static ServiceLocator locator = null;
	private BeanFactory factory = null;

	private ServiceLocator() {
		String[] fn = new String[] { "applicationContext.xml" };
		factory = new ClassPathXmlApplicationContext(fn);
	}

	public static ServiceLocator getInstance() {

		if (locator == null) {
			locator = new ServiceLocator();
		}
		return locator;
	}

	public StatTableService getStatTableService() {
		return (StatTableService) factory.getBean("statTableService");
	}
}
