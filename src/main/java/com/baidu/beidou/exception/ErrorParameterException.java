/**
 * 2009-4-20 下午10:28:44
 */
package com.baidu.beidou.exception;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class ErrorParameterException extends Exception {

	private static final long serialVersionUID = -4219506684988317917L;

	public ErrorParameterException() {
		super();
	}

	public ErrorParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErrorParameterException(String message) {
		super(message);
	}

	public ErrorParameterException(Throwable cause) {
		super(cause);
	}

}
