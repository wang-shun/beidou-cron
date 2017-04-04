package com.baidu.beidou.stat.constant;

public class StatConstant {
	public static final String PAGE_DATE_RANGE_YESTERDAY = "yesterday";
	public static final String PAGE_DATE_RANGE_LAST7DAYS = "last7Days";
	public static final String PAGE_DATE_RANGE_LASTWEEK = "lastWeek";
	public static final String PAGE_DATE_RANGE_THISMONTH = "thisMonth";
	public static final String PAGE_DATE_RANGE_LASTMONTH = "lastMonth";
	public static final String PAGE_DATE_RANGE_ALLDATE = "allDate";
	public static final int QUERY_TYPE_YESTERDAY = 1;
	public static final int QUERY_TYPE_LAST7 = 2;
	public static final int QUERY_TYPE_LASTWEEK = 3;
	public static final int QUERY_TYPE_THISMONTH = 4;
	public static final int QUERY_TYPE_LASTMONTH = 5;
	
	public static final String TABLE_NAME_PERDAY_PRE = "detail";
	public static final String TABLE_NAME_PERMONTH_PRE = "monthdetail";
	public static final String TABLE_NAME_LAST7 = "lastsevendetail";
	public static final String TABLE_NAME_LASTWEEK = "lastweekdetail";
	public static final String TABLE_NAME_THISMONTH = "curmonthdetail";
	
	public static final String TABLE_NAME_ALLDATE = "alltimestat";
	public static final String TABLE_NAME_YESTERDAY_STAT = "yesterdaystat";
	public static final String TABLE_NAME_THISMONTH_STAT = "curmonthstat";
	public static final String TABLE_NAME_LAST7_STAT="lastsevenstat";
	public static final String TABLE_NAME_LASTMONTH_STAT = "lastmonthstat";
	public static final String TABLE_NAME_LASTWEEK_STAT = "lastweekstat";

	// added by yanjie at 2008.11.17
	public static final String TABLE_NAME_TMPSUFFIX = "tmp";
	public static final String TABLE_NAME_ALLDATE_TMP = "alltimestat" + TABLE_NAME_TMPSUFFIX;
	public static final String TABLE_NAME_LASTWEEK_STAT_TMP = TABLE_NAME_LASTWEEK_STAT + TABLE_NAME_TMPSUFFIX;
	public static final String TABLE_NAME_LASTWEEK_TMP = TABLE_NAME_LASTWEEK + TABLE_NAME_TMPSUFFIX;
	public static final String TABLE_NAME_LASTMONTH_STAT_TMP = TABLE_NAME_LASTMONTH_STAT + TABLE_NAME_TMPSUFFIX;
	// end added
	
	// modified by yanjie at 2008.11.17
	// : replace "tmp" with constant(TABLE_NAME_TMP)
	public static final String TABLE_NAME_LAST7_TMP = TABLE_NAME_LAST7 + TABLE_NAME_TMPSUFFIX;
	public static final String TABLE_NAME_THISMONTH_TMP = TABLE_NAME_THISMONTH + TABLE_NAME_TMPSUFFIX;
	
	public static final String TABLE_NAME_LAST7STAT_TMP = TABLE_NAME_LAST7_STAT + TABLE_NAME_TMPSUFFIX;
	public static final String TABLE_NAME_YESTERDAY_STAT_TMP = TABLE_NAME_YESTERDAY_STAT + TABLE_NAME_TMPSUFFIX;
	public static final String TABLE_NAME_THISMONTH_STAT_TMP = TABLE_NAME_THISMONTH_STAT + TABLE_NAME_TMPSUFFIX;
	// end modified

}
