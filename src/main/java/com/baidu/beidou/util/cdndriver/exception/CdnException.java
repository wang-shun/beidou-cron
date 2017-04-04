package com.baidu.beidou.util.cdndriver.exception;

public class CdnException extends Exception {

	private static final long serialVersionUID = -525799633410276027L;

	public CdnException(String errorMessage){
		super(errorMessage);
	}
	
	public CdnException(String errorMessage, Throwable cause){
		super(errorMessage, cause);
	}
	
	public CdnException(Throwable cause){
		super(cause);
	}
}
