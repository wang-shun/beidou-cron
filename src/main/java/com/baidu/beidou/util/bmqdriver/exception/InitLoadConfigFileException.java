package com.baidu.beidou.util.bmqdriver.exception;

public class InitLoadConfigFileException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public InitLoadConfigFileException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public InitLoadConfigFileException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public InitLoadConfigFileException(Throwable cause) {
		super(cause);
	}

}
