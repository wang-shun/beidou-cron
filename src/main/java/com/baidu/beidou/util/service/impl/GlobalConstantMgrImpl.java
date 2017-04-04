package com.baidu.beidou.util.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.baidu.beidou.util.BeidouConstant;
import com.baidu.beidou.util.service.GlobalConstantMgr;

/**
 * beidou全局配置加载类
 * 
 * @author zhangpeng
 * @version 1.0.0
 * 
 */
public class GlobalConstantMgrImpl implements GlobalConstantMgr {
	private static Logger log = Logger.getLogger(GlobalConstantMgrImpl.class);

	/**
	 * 配置项加载统一出口
	 */
	public void loadGlobalConf() {
		this.loadConfFile();

	}

	/**
	 * 从文件中加载配置项
	 * 
	 */
	private void loadConfFile() {

		Properties properties = new Properties();

		InputStream is = null;

		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("platform-related.properties");
			properties.load(is);

			BeidouConstant.setLOG_MAILFROM(properties.getProperty("LOG_MAILFROM"));
			log.info("BeidouConstant.LOG_MAILFROM=" + BeidouConstant.getLOG_MAILFROM());

			BeidouConstant.setLOG_MAILTO(properties.getProperty("LOG_MAILTO"));
			log.info("BeidouConstant.LOG_MAILTO=" + BeidouConstant.getLOG_MAILTO());

		} catch (IOException e) {
			e.printStackTrace();
			log.error("load config file error:GlobalConstantMgrImpl");
		} finally {

			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}