/**
 * ??2009 Baidu
 */
package com.baidu.beidou.tool.exception;

/**
 * @author zhuqian
 * 
 */
public class ReportEngineException extends Exception {

	public ReportEngineException(String msg) {
		super(msg);
	}

	public ReportEngineException(Exception e){
		super(e);
	}
}
