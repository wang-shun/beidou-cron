package com.baidu.beidou.util.akadriver.exception;

public class ObjectClassNotMatchException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ObjectClassNotMatchException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ObjectClassNotMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public ObjectClassNotMatchException(Throwable cause) {
		super(cause);
	}

}
