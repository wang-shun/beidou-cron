package com.baidu.beidou.util.socket.exception;

/**
 * socket重试后仍然连接失败异常
 * @author piggie
 *
 */
public class SocketConnectException extends Exception{
	
	/**
	 * 默认空构造函数
	 *
	 */
	public SocketConnectException(){
		
	}
	
	/**
	 * 带参数的构造函数
	 * @param message
	 */
	public SocketConnectException(String message){
		super(message);
	}
	
	
    /**
     * Constructor for InternalException.
     */
    public SocketConnectException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor for InternalException.
     */
    public SocketConnectException(Throwable cause)
    {
        super(cause);
    }
}
