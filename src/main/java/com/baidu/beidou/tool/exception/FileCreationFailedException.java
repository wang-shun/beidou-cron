package com.baidu.beidou.tool.exception;

/**
 * @author zhuqian
 *
 */
public class FileCreationFailedException extends ReportEngineException {

	/**
	 * @param msg
	 */
	public FileCreationFailedException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}
	
	public FileCreationFailedException(Exception e){
		super(e);
	}

}
