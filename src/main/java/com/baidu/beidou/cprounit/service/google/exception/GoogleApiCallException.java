/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.exception.GoogleApiCallException.java
 * 上午11:55:49 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.exception;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleApiCallException.java
 * @dateTime 2013-10-23 上午11:55:49
 */

public class GoogleApiCallException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7749821525161478030L;

	/**
	 * 
	 * @param message
	 */
	public GoogleApiCallException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public GoogleApiCallException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public GoogleApiCallException(Throwable cause) {
		super(cause);
	}
}
