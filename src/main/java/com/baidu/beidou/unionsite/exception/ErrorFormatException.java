/**
 * 2009-4-20 下午10:28:44
 */
package com.baidu.beidou.unionsite.exception;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class ErrorFormatException extends Exception {

	private static final long serialVersionUID = -4219506684988317917L;

	public ErrorFormatException() {
		super();
	}

	public ErrorFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ErrorFormatException(String message) {
		super(message);
	}

	public ErrorFormatException(Throwable cause) {
		super(cause);
	}

}
