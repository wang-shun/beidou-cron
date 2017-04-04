package com.baidu.beidou.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

import com.baidu.beidou.unionsite.task.SiteImportTask;
import com.baidu.beidou.util.service.GlobalConstantMgr;

/**
 * 重构，所有的Bean通过这个方法获得，而不是直接通过factory.getBean的方式
 * @author zengyunfeng
 * @version 1.0.7
 */
public class ServiceLocator {

	private static ServiceLocator locator = null;

	public BeanFactory factory = null;

	private ServiceLocator() {
		String[] fn = new String[] {"applicationContext.xml"};
		factory = new ClassPathXmlApplicationContext(fn);
	}

	public void setFactory(BeanFactory factory) {
		this.factory = factory;
	}

	private ServiceLocator(String[] xmls) {
		factory = new ClassPathXmlApplicationContext(xmls);
	}

	public static ServiceLocator getInstance(String[] xmls){
		if (locator == null) {
			locator = new ServiceLocator(xmls);
			locator.contextInitialized();
		}
		return locator;
	}

	public static ServiceLocator getInstance() {
		if (locator == null) {
			locator = new ServiceLocator();
			locator.contextInitialized();
		}
		return locator;
	}

	public JavaMailSender getJavaMailSender() {
		return (JavaMailSender) factory.getBean("mailSender");
	}
	
	
	public SiteImportTask getSiteImportTask() {
		return (SiteImportTask) factory.getBean("siteImportTask");
	} 
	
	private void contextInitialized() {
		//初始化系统全局配置
		GlobalConstantMgr globalConstantMgr = (GlobalConstantMgr) factory.getBean("globalConstantMgr");
		globalConstantMgr.loadGlobalConf();
	}
}
