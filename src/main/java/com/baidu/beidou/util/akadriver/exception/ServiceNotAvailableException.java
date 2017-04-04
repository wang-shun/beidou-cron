package com.baidu.beidou.util.akadriver.exception;

public class ServiceNotAvailableException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ServiceNotAvailableException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ServiceNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public ServiceNotAvailableException(Throwable cause) {
		super(cause);
	}



}
