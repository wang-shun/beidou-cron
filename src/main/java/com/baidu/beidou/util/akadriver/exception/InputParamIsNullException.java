package com.baidu.beidou.util.akadriver.exception;

public class InputParamIsNullException extends Exception {

	/**
	 * 
	 * @param message
	 */
	public InputParamIsNullException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public InputParamIsNullException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public InputParamIsNullException(Throwable cause) {
		super(cause);
	}
}
