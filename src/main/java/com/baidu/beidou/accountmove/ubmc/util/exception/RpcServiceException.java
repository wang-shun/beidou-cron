/**
 * 
 */
package com.baidu.beidou.accountmove.ubmc.util.exception;

/**
 * @author zengyunfeng
 * @version 2.0.3
 * RPC服务发生错误的异常
 */
public class RpcServiceException extends InternalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3271411485435660656L;
	
	public RpcServiceException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RpcServiceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RpcServiceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RpcServiceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
