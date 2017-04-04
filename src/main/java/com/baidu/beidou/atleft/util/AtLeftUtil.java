package com.baidu.beidou.atleft.util;

public class AtLeftUtil {
	public static String getWorkPath() {
		return System.getenv("DATA_PATH");
	}
	
	public static String getBinPath() {
		return System.getenv("BIN_PATH");
	}
	
	public static String getAbsoluteFile(String file) {
		return getWorkPath() + "/" + file;
	}
}
