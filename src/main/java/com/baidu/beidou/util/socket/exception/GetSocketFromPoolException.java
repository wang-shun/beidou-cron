package com.baidu.beidou.util.socket.exception;

/**
 * 从连接池中获取socket失败异常
 * @author piggie
 *
 */
public class GetSocketFromPoolException extends Exception {
	/**
	 * 默认空构造函数
	 *
	 */
	public GetSocketFromPoolException(){
		
	}
	
	/**
	 * 带参数的构造函数
	 * @param message
	 */
	public GetSocketFromPoolException(String message){
		super(message);
	}
	
	
    /**
     * Constructor for InternalException.
     */
    public GetSocketFromPoolException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor for InternalException.
     */
    public GetSocketFromPoolException(Throwable cause)
    {
        super(cause);
    }
    
}
