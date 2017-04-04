package com.baidu.beidou.cache.doris;

/**
 * @author zhuqian
 * @modified yangyun 2010.04.07
 * 
 */
public class ReportConstants {

	// 数据字段
	public static final String PRICE = "price"; // 价格
	public static final String BUDGET = "budget"; // 投放预算
	public static final String DATE = "date"; // 日期
	public static final String FROMDATE = "fromdate"; // 日期范围 起
	public static final String TODATE = "todate"; // 日期范围 至
	public static final String USER = "user"; // 用户信息
	public static final String PLAN = "cproplan"; // 推广计划
	public static final String GROUP = "cprogroup"; // 推广组
	public static final String UNIT = "cprounit"; // 推广单元
	public static final String CLKS = "clks"; // 点击
	public static final String SRCHS = "srchs"; // 展现
	public static final String COST = "cost"; // 点击价值
	public static final String CTR = "ctr"; // 点击率
	public static final String ACP = "acp"; // 平均点击价格
	public static final String CPM = "cpm"; // 千次展现价值
	public static final String SITE = "site"; // 网站
	public static final String MAINSITE = "mainsite"; // 网站主域

	// 时间单位
	public static final int TU_NONE = 0;
	public static final int TU_DAY = 1;
	public static final int TU_WEEK = 2;
	public static final int TU_MONTH = 3;
	public static final int TU_QUART = 4;
	public static final int TU_YEAR = 5;

	// 相对时间类型
	public static final int RLT_NONE = 0; // 无
	public static final int RLT_YEST = 1; // 昨天
	public static final int RLT_L7DS = 2; // 上7天（上周）
	public static final int RLT_L14DS = 3; // 上14天（上两周）
	public static final int RLT_L30DS = 4; // 上个月
	public static final int RLT_CWEK = 5; // 本周
	public static final int RLT_LWEK = 6; // 上周
	public static final int RLT_CMON = 7; // 本月
	public static final int RLT_LMON = 8; // 上月
	public static final int RLT_CQUT = 9; // 本季度
	public static final int RLT_LQUT = 10; // 上季度
	public static final int RLT_ALLT = 11; // 全部时间
}
