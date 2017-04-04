package com.baidu.beidou.accountmove.ubmc.util.service;

import java.util.Map;

/**
 * ClassName:BaseRpcDriver
 * Function: 基本RPC服务接口
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-12-27
 * @since    TODO
 * @version  $Id: Exp $
 */
public interface BaseRpcDriver {

	/**
	 * <p>设置连接超时
	 *
	 * @param timeoutInMillSeconds 超时时间，单位为毫秒      
	 * @since 
	*/
	public void setConnectionTimeout(int timeoutInMillSeconds);
	

	/**
	 * <p>设置读超时
	 *
	 * @param timeoutInMillSeconds 超时时间，单位为毫秒      
	 * @since 
	*/
	public void setReadTimeout(int timeoutInMillSeconds);
	

    public void setEncoding(String encoding);
    
    public void setRetryTimes(int retryTimes);
    
    public void setServiceInterface(Class serviceInterface);
    
    public void setServices(String[] services);
    
    public boolean isErrorExit();
    
    public void setServers(String[] servers);
    
    public void setServiceUrl(String serviceUrl);
    
    public void setHeader(Map<String, String> headers);
    
}
