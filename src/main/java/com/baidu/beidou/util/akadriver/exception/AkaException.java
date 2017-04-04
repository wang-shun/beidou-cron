package com.baidu.beidou.util.akadriver.exception;

public class AkaException extends Exception {
	/**
	 * 
	 * @param message
	 */
	public AkaException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public AkaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public AkaException(Throwable cause) {
		super(cause);
	}
}
