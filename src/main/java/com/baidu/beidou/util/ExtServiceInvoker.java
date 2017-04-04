package com.baidu.beidou.util;

import com.baidu.beidou.exception.RpcServiceException;


public interface ExtServiceInvoker {

	/**
	 * 获取服务调用接口
	 * @return 
	 */
	public Object getInvoker() throws RpcServiceException ;
}
