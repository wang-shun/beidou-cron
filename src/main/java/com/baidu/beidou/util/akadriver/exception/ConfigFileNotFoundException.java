package com.baidu.beidou.util.akadriver.exception;



public class ConfigFileNotFoundException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ConfigFileNotFoundException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ConfigFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public ConfigFileNotFoundException(Throwable cause) {
		super(cause);
	}

}
