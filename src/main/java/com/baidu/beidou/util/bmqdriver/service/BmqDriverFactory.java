package com.baidu.beidou.util.bmqdriver.service;

import com.baidu.beidou.util.bmqdriver.exception.ConfigFileNotFoundException;
import com.baidu.beidou.util.bmqdriver.exception.InitLoadConfigFileException;

/**
 * ClassName: BmqDriverFactory
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date 2011-10-10
 * @see 
 */
public interface BmqDriverFactory {
	
	/**
	 * init: 完成指定配置文件的加载
	 * @version BmqDriverFactory
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void init(String configFile) throws ConfigFileNotFoundException, 
			InitLoadConfigFileException;
		
	/**
	 * init: 完成指定配置文件的加载
	 * @version BmqDriverFactory
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void init() throws ConfigFileNotFoundException,
			InitLoadConfigFileException;
	
	/**
	 * getBmqDriver: 获取BmqDriver，需要参数type
	 * @use {@link com.baidu.beidou.util.bmqdriver.constant.Constant.URL_CHECK_TYPE_INSTANT}
	 * @version BmqDriverFactory
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public BmqDriver getBmqDriver(int type) throws ConfigFileNotFoundException,
			InitLoadConfigFileException;
}
