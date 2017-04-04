/**
 * 
 */
package com.baidu.beidou.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zengyunfeng
 * @version 1.0.0
 * 
 */
public class TestLogUtils {
	private static Log log = LogFactory.getLog("test");
	
	public static boolean isDebugEnabled(){
		return log.isDebugEnabled();
	}

	/**
	 * 以debug模式打印出测试日志
	 * 
	 * @param info
	 */
	public static void testInfo(Object info) {
		if (log.isDebugEnabled()) {
			log.debug(info);
		}
	}


}
