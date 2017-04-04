package com.baidu.beidou.accountmove.ubmc.util;

import com.baidu.beidou.accountmove.ubmc.util.exception.RpcServiceException;


public interface ExtServiceInvoker {

	/**
	 * 获取服务调用接口
	 * @return 
	 */
	public Object getInvoker() throws RpcServiceException ;
}
