package com.baidu.beidou.account.constant;

public class AccountConstant {
	//提醒定制相关常量
	public static int EMAIL_LENGTH=50;
	public static int MOBILE_LENGTH=20;
	
	public static int PERFUND_FLAG_BEIDOU=0;//beidou转shifen的标识
	public static int PERFUND_FLAG_SHIFEN=1;//shifen转beidou的标识
	/*
	 * shifen转账同步标识
	 */
	public static int PERFUND_SYNC_FLAG=1;
	public static int PERFUND_UNSYNC_FLAG=0;
	public static int PERFUND_SCRAP_FLAG=2;
	
	/*
	 * 缓存表是否删除标识
	 */
	public static int CACHE_DEL_FLAG=1;
	public static int CACHE_UNDEL_FLAG=0;
	/*
	 * 每天的限制和每小时的转账次数限制
	 */
	public static int NUM_LIMIT_HOUR=2;
	public static int NUM_LIMIT_DAY=4;
	
	public static String FILE_SEPSTR="\t";
	public static String FILE_ENDSTR="\n";
	
	public static String DOUBLE_DFT_VALUE="0.00";
	public static String INT_DFT_VALUE="0";
	public static String DATE_DFT_VALUE="0000-00-00 00:00:00";
	
	public static int SYNC_FILE_COLNUM=9;
	//需要 过滤的测试用户最大id
	/**
	 * 测试用户范围调整，调整到userid < 30 并且（userid < 1381000 || userid > 1381999）
	 * ZHANGPENG 20110315
	 */
	public static int TEST_USER_MAX_ID=30;
	
	public static int TEST_USER_THRESHOLD_1_ID=30;
	public static int TEST_USER_THRESHOLD_2_ID=1381000;
	public static int TEST_USER_THRESHOLD_3_ID=1381999;
	
	
	//每天日志表前缀
	public static String DAILY_LOG_TABLE_PRE="cost_";
	//保存每天日志表的数据库名
	public static String DAILY_LOG_DATABASE="beidoufinan";
	
	public static String BUSINESS_MAILFROM = "nrhelp@baidu.com";
	
	public static String BEIDOU_FLAG_STR="由网盟推广账户转入搜索推广账户";
	public static String SHIFEN_FLAG_STR="由搜索推广账户转入网盟推广账户";
	
	public static int AUTO_TRANSFER_NO=0;//自动转账：不设置
	public static int AUTO_TRANSFER_ALWAYS=1;//自动转账：每日定量转fund元
	public static int AUTO_TRANSFER_WHEN=2;//自动转账：当余额不足margin元时转fund元
	
	public static String WARN_MAILFROM = "beidou-cron@baidu.com";
	public static String WARN_MAILTO = "genglei01@baidu.com";
}
