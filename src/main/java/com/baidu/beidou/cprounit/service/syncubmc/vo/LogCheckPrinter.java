package com.baidu.beidou.cprounit.service.syncubmc.vo;

import java.io.PrintWriter;

public class LogCheckPrinter {
	protected PrintWriter errorWriter = null;
	protected PrintWriter logWriter = null;
	protected PrintWriter invalidWriter = null;
	
	public LogCheckPrinter(PrintWriter errorWriter, PrintWriter logWriter
			, PrintWriter invalidWriter) {
		this.errorWriter = errorWriter;
		this.logWriter = logWriter;
		this.invalidWriter = invalidWriter;
	}
	
	public synchronized void error(String msg) {
		errorWriter.println(msg);
		errorWriter.flush();
	}
	
	public synchronized void log(String msg) {
		logWriter.println(msg);
		logWriter.flush();
	}
	
	public synchronized void invalid(String msg) {
		invalidWriter.println(msg);
		invalidWriter.flush();
	}
}
