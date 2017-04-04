/**
 * 
 */
package com.baidu.beidou.cache;

/**
 * @author wangqiang04
 * 
 */
public interface CacheConstants {
	// Column index field definition
	int DETAIL_COLUMN_UNITID_INDEX = 0;
	int DETAIL_COLUMN_GROUPID_INDEX = 1;
	int DETAIL_COLUMN_PLANID_INDEX = 2;
	int DETAIL_COLUMN_USERID_INDEX = 3;
	int DETAIL_COLUMN_CLKS_INDEX = 4;
	int DETAIL_COLUMN_SRCHS_INDEX = 5;
	int DETAIL_COLUMN_COST_INDEX = 6;

	// Constants for cache type
	byte CACHE_TYPE_USER_YEST = 1;
	byte CACHE_TYPE_PLAN_YEST = 2;
	byte CACHE_TYPE_GROUP_YEST = 3;
	byte CACHE_TYPE_UNIT_YEST = 4;
	byte CACHE_TYPE_USER_ALL = 11;

	int USER_YEST_INIT_CAPACITY = 50000;
	int PLAN_YEST_INIT_CAPACITY = 8000;
	int GROUP_YEST_INIT_CAPACITY = 120000;
	int UNIT_YEST_INIT_CAPACITY = 1000000;

	int MAX_BATCH_SIZE = 5000;

	String STAT_USER_ALL_DATA_DATE = "STAT_USER_ALL_DATA_DATE";
	String CACHE_LAST_UPD_TIME = "CACHE_LAST_UPD_TIME";

	long MILLISECONDS_IN_DAY = 86400000L;

	// 数据字段
	String PRICE = "price"; // 价格
	String BUDGET = "budget"; // 投放预算
	String DATE = "date"; // 日期
	String FROMDATE = "fromdate"; // 日期范围 起
	String TODATE = "todate"; // 日期范围 至
	String USER = "user"; // 用户信息
	String PLAN = "cproplan"; // 推广计划
	String GROUP = "cprogroup"; // 推广组
	String UNIT = "cprounit"; // 推广单元
	String CLKS = "clks"; // 点击
	String SRCHS = "srchs"; // 展现
	String COST = "cost"; // 点击价值
	String CTR = "ctr"; // 点击率
	String ACP = "acp"; // 平均点击价格
	String CPM = "cpm"; // 千次展现价值
	String SITE = "site"; // 网站
	String MAINSITE = "mainsite"; // 网站主域

	// 时间单位
	int TU_NONE = 0;
	int TU_DAY = 1;
	int TU_WEEK = 2;
	int TU_MONTH = 3;
	int TU_QUART = 4;
	int TU_YEAR = 5;

	// 相对时间类型
	int RLT_NONE = 0; // 无
	int RLT_YEST = 1; // 昨天
	int RLT_L7DS = 2; // 上7天（上周）
	int RLT_L14DS = 3; // 上14天（上两周）
	int RLT_L30DS = 4; // 上个月
	int RLT_CWEK = 5; // 本周
	int RLT_LWEK = 6; // 上周
	int RLT_CMON = 7; // 本月
	int RLT_LMON = 8; // 上月
	int RLT_CQUT = 9; // 本季度
	int RLT_LQUT = 10; // 上季度
	int RLT_ALLT = 11; // 全部时间
	
	/** 用户维度全部数据缓存保留的天数 */
	int USER_ALL_CACHE_BACKUP_DAYS = 5;
	/** 用户昨日点击消费数据缓存天数:7*/
	int STAT_USER_YEST_CACHE_BACKUP_DAYS = 7;
}
