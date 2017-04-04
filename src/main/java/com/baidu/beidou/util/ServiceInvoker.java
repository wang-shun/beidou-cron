/**
 * 2009-8-20 下午05:22:18
 * @author zengyunfeng
 */
package com.baidu.beidou.util;

import com.baidu.beidou.exception.RpcServiceException;

/**
 * @author zengyunfeng
 *
 */
public interface ServiceInvoker<T> {

	/**
	 * 服务调用接口,返回
	 * @return 
	 */
	public T invoke() throws RpcServiceException ;
}
