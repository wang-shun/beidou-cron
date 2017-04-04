package com.baidu.beidou.util.akadriver.exception;

public class ReponseResultNotMatchException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ReponseResultNotMatchException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ReponseResultNotMatchException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public ReponseResultNotMatchException(Throwable cause) {
		super(cause);
	}


}
