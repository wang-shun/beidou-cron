package com.baidu.beidou.util.akadriver.exception;

public class ByteArrayIndexNotMatchException extends Exception {
	/**
	 * 
	 * @param message
	 */
	public ByteArrayIndexNotMatchException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ByteArrayIndexNotMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public ByteArrayIndexNotMatchException(Throwable cause) {
		super(cause);
	}
}
