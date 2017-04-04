package com.baidu.beidou.cprounit.service.syncubmc.vo;

import java.io.PrintWriter;

public class LogPrinter {
	protected PrintWriter errorWriter = null;
	protected PrintWriter logWriter = null;
	
	public LogPrinter(PrintWriter errorWriter, PrintWriter logWriter) {
		this.errorWriter = errorWriter;
		this.logWriter = logWriter;
	}
	
	public synchronized void error(String msg) {
		errorWriter.println(msg);
		errorWriter.flush();
	}
	
	public synchronized void log(String msg) {
		logWriter.println(msg);
		logWriter.flush();
	}
}
