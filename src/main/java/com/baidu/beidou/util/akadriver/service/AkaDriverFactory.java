package com.baidu.beidou.util.akadriver.service;

import com.baidu.beidou.util.akadriver.exception.ConfigFileNotFoundException;
import com.baidu.beidou.util.akadriver.exception.InitLoadConfigFileException;

public interface AkaDriverFactory {
	
	/**
	 * 
	 * @see 完成指定配置文件的加载
	 * @param configFile
	 * @throws ConfigFileNotFoundException
	 * @throws InitLoadConfigFileException
	 * @author puyuda
	 * @date 2008-6-26
	 * @version 1.0.0
	 */
	public void init(String configFile) throws ConfigFileNotFoundException,InitLoadConfigFileException;
	
	/**
	 * 
	 * @see 完成默认配置文件的加载
	 * @throws ConfigFileNotFoundException
	 * @throws InitLoadConfigFileException
	 * @author puyuda
	 * @date 2008-6-26
	 * @version 1.0.0
	 */
	public void init() throws ConfigFileNotFoundException,InitLoadConfigFileException;
	
	public AkaDriver getAkaDriver() throws ConfigFileNotFoundException,
	InitLoadConfigFileException;

}
