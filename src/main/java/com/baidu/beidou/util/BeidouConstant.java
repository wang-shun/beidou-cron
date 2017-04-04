/**
 * 
 */
package com.baidu.beidou.util;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class BeidouConstant {
	
	public final static String FILE_PATH_PREF = "WEB-INF/classes/"; 
	/**
	 * 日志类中发送报警邮件的From
	 */
	public static String LOG_MAILFROM = "union1@baidu.com";
	/**
	 * 日志类中发送报警邮件的to列表
	 */

	public static String LOG_MAILTO = "zhangpeng@baidu.com";

	/**
	 * 日志类中发送报警邮件的title
	 */
	public static final String LOGUTILS_MAILTITLE = "fatal message mail ";
	/**
	 * 组装多接受人时的email分隔符
	 */
	public static final String MAILLIST_DILIM = ",";

	public static final int BOOLEAN_TRUE = 1;
	public static final int BOOLEAN_FALSE = 0;
		
	  
	public static final int BEIDOU_DSP_ID = 1;

	/**
	 * @return the lOG_MAILFROM
	 */
	public static String getLOG_MAILFROM() {
		return LOG_MAILFROM;
	}

	/**
	 * @param log_mailfrom the lOG_MAILFROM to set
	 */
	public static void setLOG_MAILFROM(String log_mailfrom) {
		LOG_MAILFROM = log_mailfrom;
	}

	/**
	 * @return the lOG_MAILTO
	 */
	public static String getLOG_MAILTO() {
		return LOG_MAILTO;
	}

	/**
	 * @param log_mailto the lOG_MAILTO to set
	 */
	public static void setLOG_MAILTO(String log_mailto) {
		LOG_MAILTO = log_mailto;
	}
	  
	  
}
