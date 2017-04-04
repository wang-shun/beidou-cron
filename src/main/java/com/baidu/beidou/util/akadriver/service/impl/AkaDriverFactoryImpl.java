package com.baidu.beidou.util.akadriver.service.impl;

import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.baidu.beidou.util.BeidouConfig;
import com.baidu.beidou.util.akadriver.PropertiesReader;
import com.baidu.beidou.util.akadriver.constant.Constant;
import com.baidu.beidou.util.akadriver.exception.ConfigFileNotFoundException;
import com.baidu.beidou.util.akadriver.exception.InitLoadConfigFileException;
import com.baidu.beidou.util.akadriver.service.AkaDriver;
import com.baidu.beidou.util.akadriver.service.AkaDriverFactory;

public class AkaDriverFactoryImpl implements AkaDriverFactory {

	private final static Log log = LogFactory
			.getLog(AkaDriverFactoryImpl.class);

	private static String configFileName = Constant.configFileName;

	private static AkaDriverFactoryImpl factory = null;

	private AkaDriverFactoryImpl() {
		super();
	}

	public void init(final String configFile)
			throws ConfigFileNotFoundException, InitLoadConfigFileException {
		try {
			Constant.CONFIG_MEM_POP = PropertiesReader
					.fillProperties(configFile);
			Constant.CONFIG_MEM_POP.put("server", BeidouConfig.AKA_SERVIER);

		} catch (MissingResourceException e) {
			log.error( "file not found", e);
			throw new ConfigFileNotFoundException("file not found");
		}
		if (!isLoadConfigFileSuccess()) {
			log.error( "InitLoadConfigFileException");
			throw new InitLoadConfigFileException("InitLoadConfigFileException");
		}
		// 装载成功后将配置文件的路径进行修改
		configFileName = configFile;

	}

	public void init() throws ConfigFileNotFoundException,
			InitLoadConfigFileException {
		init(Constant.configFileName);

	}

	/**
	 * 
	 * @see 单例构造函数
	 * @return
	 * @author puyuda
	 * @date 2008-6-18
	 * @version 1.0.0
	 */
	public static AkaDriverFactoryImpl getInstance() {
		if (factory == null) {
			synchronized (AkaDriverFactoryImpl.class) {
				if (factory == null) {
					factory = new AkaDriverFactoryImpl();
				}
			}
		}
		return factory;
	}

	public AkaDriver getAkaDriver() throws ConfigFileNotFoundException,
			InitLoadConfigFileException {
		if (!isLoadConfigFileSuccess()) {
			init();
		}
		return new AkaDriverBeidouImpl();
	}

	/**
	 * 
	 * @see 判定配置文件装载是否成功
	 * @return 成功 true，否则 false
	 * @author puyuda
	 * @date 2008-6-11
	 * @version 1.0.0
	 */
	private boolean isLoadConfigFileSuccess() {
		boolean result = true;
		if (Constant.CONFIG_MEM_POP == null
				|| Constant.CONFIG_MEM_POP.size() == 0) {
			result = false;
		}

		return result;
	}

	public static String getConfigFileName() {
		return configFileName;
	}

	public static void setConfigFileName(final String configFileName) {
		AkaDriverFactoryImpl.configFileName = configFileName;
	}

}
