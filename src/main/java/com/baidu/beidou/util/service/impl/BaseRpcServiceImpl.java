package com.baidu.beidou.util.service.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.service.BaseRpcService;

/**
 * <p>ClassName:BaseRpcServiceImpl,RPC服务基类
 * <p>Function: 封装RPC服务用到的基本方法和属性
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-12-27
 * @since    1.0.0
 * @version  $Id: Exp $
 */
public abstract class BaseRpcServiceImpl implements BaseRpcService {
	
	protected final Log LOG = LogFactory.getLog(getClass());
	
	/**
	 * <p>getHeaders:返回需要传递的消息头
	 *
	 * @return      
	*/
	public abstract Map<String, String> getHeaders();
	
}
