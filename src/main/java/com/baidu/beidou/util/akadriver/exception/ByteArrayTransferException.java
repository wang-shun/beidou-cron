package com.baidu.beidou.util.akadriver.exception;

public class ByteArrayTransferException extends Exception {
	
	/**
	 * 
	 * @param message
	 */
	public ByteArrayTransferException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ByteArrayTransferException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param cause
	 */
	public ByteArrayTransferException(Throwable cause) {
		super(cause);
	}

}
