package com.baidu.beidou.util.bmqdriver.service.impl;

import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.bmqdriver.PropertiesReader;
import com.baidu.beidou.util.bmqdriver.exception.ConfigFileNotFoundException;
import com.baidu.beidou.util.bmqdriver.exception.InitLoadConfigFileException;
import com.baidu.beidou.util.bmqdriver.constant.Constant;
import com.baidu.beidou.util.bmqdriver.service.BmqDriver;
import com.baidu.beidou.util.bmqdriver.service.BmqDriverFactory;

/**
 * ClassName: BmqDriverFactoryImpl
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date Oct 10, 2011
 * @see 
 */
/**
 * ClassName: BmqDriverFactoryImpl
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 
 * @since TODO
 * @date Oct 18, 2011
 * @see 
 */
public class BmqDriverFactoryImpl implements BmqDriverFactory {
	private final static Log log = LogFactory
			.getLog(BmqDriverFactoryImpl.class);
	
	private static BmqDriverFactoryImpl factory = null;
	
	private static String configFileName = Constant.configFileName;
	
	private BmqDriverFactoryImpl() {
		super();
	}
	
	public void init(String configFile) throws ConfigFileNotFoundException, 
			InitLoadConfigFileException {
		try {
			Constant.CONFIG_MEM_POP = PropertiesReader
					.fillProperties(configFile);
		} catch (MissingResourceException e) {
			log.error("file not found", e);
			throw new ConfigFileNotFoundException("config file not found");
		}
		if (!isLoadConfigFileSuccess()) {
			log.error("InitLoadConfigFileException");
			throw new InitLoadConfigFileException("init config file failed");
		}
		
		// 装载成功后将配置文件的路径进行修改
		configFileName = configFile;
	}

	public void init() throws ConfigFileNotFoundException,
			InitLoadConfigFileException {
		init(configFileName);
	}
	
	public BmqDriver getBmqDriver(int type) throws ConfigFileNotFoundException,
			InitLoadConfigFileException {
		if (!isLoadConfigFileSuccess()) {
			init();
		}
		
		return new BmqDriverImpl(type);
	}
	
	/**
	 * 
	 * @see 单例构造函数
	 * @return
	 * @author puyuda
	 * @date 2008-6-18
	 * @version 1.0.0
	 */
	public static BmqDriverFactoryImpl getInstance() {
		if (factory == null) {
			synchronized (BmqDriverFactoryImpl.class) {
				if (factory == null) {
					factory = new BmqDriverFactoryImpl();
				}
			}
		}
		return factory;
	}
	
	/**
	 * isLoadConfigFileSuccess: 判定配置文件装载是否成功
	 * @return 成功 true，否则 false
	 * @version BmqDriverFactoryImpl
	 * @author genglei01
	 * @date Oct 18, 2011
	 */
	private boolean isLoadConfigFileSuccess() {
		boolean result = true;
		if (Constant.CONFIG_MEM_POP == null
				|| Constant.CONFIG_MEM_POP.size() == 0) {
			result = false;
		}

		return result;
	}
	
	/**
	 * main: 
	 * @version BmqDriverFactoryImpl
	 * @author genglei01
	 * @date Oct 10, 2011
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
}
