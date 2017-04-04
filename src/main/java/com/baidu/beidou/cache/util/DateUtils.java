package com.baidu.beidou.cache.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cache.doris.ReportConstants;

/**
 * <p>
 * Title: 日期处理工具类
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: capinfo
 * </p>
 * 
 * @author getup
 * @modified by zhangpingan(2011-02-24)
 * @version 1.0
 */
public class DateUtils {
	private static final Log logger = LogFactory.getLog(DateUtils.class);
	public static final long MILLISECONDS_IN_DAY = 86400000L;

	public static String format_yyyyMMdd(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

	public static String format_yyyyMMdd(Date fromDate, Date toDate) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(fromDate) + " - " + df.format(toDate);
	}

	public static Date getDateByOffset(Date date, int field, int offset) {
		if (date == null)
			return date;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(field, offset);

		return c.getTime();
	}

	public static boolean isSameDay(Date day1, Date day2) {

		if (day1 == null || day2 == null) {
			return false;
		}

		if (day1.equals(day2))
			return true;

		Date zeroDate = getStartOfDate(day1);
		Date nextDate = getDateByOffset(zeroDate, Calendar.DATE, 1);
		Date prevDate = getDateByOffset(zeroDate, Calendar.SECOND, -1);

		return day2.after(prevDate) && day2.before(nextDate);

	}

	public static int getDayDistance(Date day1, Date day2) {

		if (day1 == null || day2 == null) {
			return 0; // TODO: 这个逻辑需要再考虑一下
		}

		// 第一步：把小时、分钟、秒字段全部归零
		day1 = getStartOfDate(day1);
		day2 = getStartOfDate(day2);

		// 第二步：比较milliseconds的差别
		long diff = day1.getTime() - day2.getTime();

		// 第三步：转换成天为单位
		// 注：此处不考虑小数位，因为在第一步中已试图清零；date.getTime()有“小尾巴”，但不影响正常逻辑
		int days = (int) (diff / MILLISECONDS_IN_DAY);

		return days;
	}

	/*
	 * 获取指定日期所属周、月、季度的起止 added by yanjie at 20090307
	 * 
	 * ensured: yyyymmdd00, yyyymmdd23 modified by yanjie at 20090708
	 */
	public static Date getWeekBegin(Calendar tmp) {
		Calendar ctmp = new GregorianCalendar(tmp.get(Calendar.YEAR),
				tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH));

		int dw = ctmp.get(Calendar.DAY_OF_WEEK);
		while (dw != Calendar.MONDAY) {
			ctmp.add(Calendar.DATE, -1);
			dw = ctmp.get(Calendar.DAY_OF_WEEK);
		}
		return ctmp.getTime();
	}

	public static Date getWeekEnd(Calendar tmp) {
		Calendar ctmp = new GregorianCalendar(tmp.get(Calendar.YEAR),
				tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH), 23, 0);

		int dw = ctmp.get(Calendar.DAY_OF_WEEK);
		while (dw != Calendar.SUNDAY) {
			ctmp.add(Calendar.DATE, 1);
			dw = ctmp.get(Calendar.DAY_OF_WEEK);
		}
		return ctmp.getTime();
	}

	public static Date getMonthBegin(Calendar tmp) {
		Calendar ctmp = new GregorianCalendar(tmp.get(Calendar.YEAR),
				tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH));

		int dm = ctmp.get(Calendar.DAY_OF_MONTH);
		while (dm != 1) {
			ctmp.add(Calendar.DATE, -1);
			dm = ctmp.get(Calendar.DAY_OF_MONTH);
		}
		return ctmp.getTime();
	}

	public static Date getMonthEnd(Calendar tmp) {
		Calendar ctmp = new GregorianCalendar(tmp.get(Calendar.YEAR),
				tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH), 23, 0);

		int dm = ctmp.get(Calendar.DAY_OF_MONTH);
		String dstr = DateUtils.formatDate(ctmp.getTime(), "yyyyMMdd");
		long monlen = 30;
		try {
			monlen = DateUtils.getMonthLength(dstr);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		while (dm < monlen) {
			ctmp.add(Calendar.DATE, 1);
			dm = ctmp.get(Calendar.DAY_OF_MONTH);
		}
		return ctmp.getTime();
	}

	public static Date getQuarterBegin(Calendar tmp) {
		Calendar ctmp = new GregorianCalendar(tmp.get(Calendar.YEAR),
				tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH));

		int month = ctmp.get(Calendar.MONTH);
		int offset = -(month % 3); // TODO: 这里有问题，month要+1再mod
		ctmp.add(Calendar.MONTH, offset);

		return getMonthBegin(ctmp);
	}

	public static Date getQuarterEnd(Calendar tmp) {
		Calendar ctmp = new GregorianCalendar(tmp.get(Calendar.YEAR),
				tmp.get(Calendar.MONTH), tmp.get(Calendar.DAY_OF_MONTH), 23, 0);

		int month = ctmp.get(Calendar.MONTH);
		int offset = 2 - (month % 3);
		ctmp.add(Calendar.MONTH, offset);

		return getMonthEnd(ctmp);
	}

	/**
	 * 调用方需要判断返回值的大小，避免出现ArrayIndexOutOfBound的问题！
	 */
	public static Date[] transformRelativeTime(Integer type) {
		Date[] result = new Date[2];

		if (type == null) {
			return result;
		}
		// modified by yanjie at 20090708
		// yyyymmdd00,yyyymmdd23
		Calendar ctmp00 = DateUtils.getCurDateCeil();
		Calendar ctmp23 = DateUtils.getCurDateFloor();
		Calendar yest = DateUtils.getCurDateFloor();
		yest.add(Calendar.DATE, -1);

		switch (type) {
		case ReportConstants.RLT_YEST:
			ctmp00.add(Calendar.DATE, -1);
			result[0] = ctmp00.getTime();
			result[1] = yest.getTime();
			break;
		case ReportConstants.RLT_L7DS:
			ctmp00.add(Calendar.DATE, -7);
			result[0] = ctmp00.getTime();
			result[1] = yest.getTime();
			break;
		case ReportConstants.RLT_L14DS:
			ctmp00.add(Calendar.DATE, -14);
			result[0] = ctmp00.getTime();
			result[1] = yest.getTime();
			break;
		case ReportConstants.RLT_L30DS:
			ctmp00.add(Calendar.DATE, -30);
			result[0] = ctmp00.getTime();
			result[1] = yest.getTime();
			break;
		case ReportConstants.RLT_CWEK:
			result[0] = getWeekBegin(ctmp00);
			if (isSameDay(result[0], ctmp00.getTime())) {
				result[1] = ctmp23.getTime();
			} else {
				result[1] = yest.getTime();
			}
			break;
		case ReportConstants.RLT_LWEK:
			ctmp00.add(Calendar.WEEK_OF_YEAR, -1);
			result[0] = getWeekBegin(ctmp00);
			ctmp23.add(Calendar.WEEK_OF_YEAR, -1);
			result[1] = getWeekEnd(ctmp23);
			break;
		case ReportConstants.RLT_CMON:
			result[0] = getMonthBegin(ctmp00);
			if (isSameDay(result[0], ctmp00.getTime())) {
				result[1] = ctmp23.getTime();
			} else {
				result[1] = yest.getTime();
			}
			break;
		case ReportConstants.RLT_LMON:
			ctmp00.add(Calendar.MONTH, -1);
			result[0] = getMonthBegin(ctmp00);
			ctmp23.add(Calendar.MONTH, -1);
			result[1] = getMonthEnd(ctmp23);
			break;
		case ReportConstants.RLT_CQUT:
			result[0] = getQuarterBegin(ctmp00);
			if (isSameDay(result[0], ctmp00.getTime())) {
				result[1] = ctmp23.getTime();
			} else {
				result[1] = yest.getTime();
			}
			break;
		case ReportConstants.RLT_LQUT:
			ctmp00.add(Calendar.MONTH, -3);
			result[0] = getQuarterBegin(ctmp00);
			ctmp23.add(Calendar.MONTH, -3);
			result[1] = getQuarterEnd(ctmp23);
			break;
		case ReportConstants.RLT_ALLT:
			result[0] = DateUtils.getDate(2008, 11, 13); // TODO: remove
															// hardcode!!!
															// 可以把系统初始时间作为一个配置项
			result[1] = yest.getTime();
			break;
		default:
			logger.error("relativetime type [" + type + "] error");
		}

		return result;
	}

	/**
	 * 返回指定字符串表示的日期，时，分，秒为0
	 * 
	 * @author zengyunfeng
	 * @version 1.1.0
	 * @param date
	 *            ： 格式为yyyyMMdd
	 * @return
	 * @throws ParseException
	 */
	public static Date strToDate(String date) throws ParseException {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMdd");

		java.util.Date d = sdf.parse(date);

		return d;
	}

	/**
	 * 返回指定字符串表示的时间，分，秒为0
	 * 
	 * @author zengyunfeng
	 * @version 1.1.0
	 * @param time
	 *            :格式为yyyyMMdd:HH
	 * @return
	 * @throws ParseException
	 */
	public static Date strToTime(String time) throws ParseException {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMdd:HH");

		java.util.Date d = sdf.parse(time);

		return d;
	}

	/**
	 * 生成当前时间对应的包含小时的时间字符串：yyyyMMddHH
	 * 
	 * @return String 时间字符串
	 */
	public static String getHourStr() {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMddHH");
		return sdf.format(new Date());
	}

	/**
	 * 返回指定格式的时间字符串：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 * @throws ParseException下午01
	 *             :12:32
	 */
	public static String getDateStr(Date date) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/**
	 * 生成java.util.Date类型的对象
	 * 
	 * @param year
	 *            int 年
	 * @param month
	 *            int 月
	 * @param day
	 *            int 日
	 * @return Date java.util.Date类型的对象
	 */
	public static Date getDate(int year, int month, int day) {
		GregorianCalendar d = new GregorianCalendar(year, month - 1, day);
		return d.getTime();
	}

	public static Date getDate(int yyyyMMdd) {
		int dd = yyyyMMdd % 100;
		int yyyyMM = yyyyMMdd / 100;
		int mm = yyyyMM % 100;
		int yyyy = yyyyMM / 100;
		GregorianCalendar d = new GregorianCalendar(yyyy, mm - 1, dd);
		return d.getTime();
	}

	/**
	 * 生成java.util.Date类型的对象
	 * 
	 * @param year
	 *            int 年
	 * @param month
	 *            int 月
	 * @param day
	 *            int 日
	 * @param hour
	 *            int 小时
	 * @return Date java.util.Date对象
	 */
	public static Date getDate(int year, int month, int day, int hour) {
		GregorianCalendar d = new GregorianCalendar(year, month - 1, day, hour,
				0);
		return d.getTime();
	}

	/**
	 * 生成圆整至小时的当前时间 例如：若当前时间为（2004-08-01 11:30:58），将获得（2004-08-01 11:00:00）的日期对象
	 * 
	 * @return Date java.util.Date对象
	 */
	public static Date getRoundedHourCurDate() {

		Calendar cal = GregorianCalendar.getInstance();

		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		return cal.getTime();

	}

	/**
	 * 生成当天零时的日期对象 例如：若当前时间为（2004-08-01 11:30:58），将获得（2004-08-01 00:00:00）的日期对象
	 * 
	 * @return Date java.util.Date对象
	 */
	public static Date getRoundedDayCurDate() {
		Calendar cal = new GregorianCalendar();

		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
				.getTime();
	}

	/**
	 * 生成某天零时的日期对象 例如：若输入时间为（2004-08-01 11:30:58），将获得（2004-08-01 00:00:00）的日期对象
	 * 
	 * @return Date java.util.Date对象
	 */
	public static Date getRoundedDay(Date dt) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
				.getTime();
	}

	/**
	 * 生成圆整至小时的当前时间 例如：若给定时间为（2004-08-01 11:30:58），将获得（2004-08-01 11:00:00）的日期对象
	 * 
	 * @param dt
	 *            Date java.util.Date对象
	 * @return Date java.util.Date对象
	 */
	public static Date getRoundedHourDate(Date dt) {

		Calendar cal = new GregorianCalendar();

		cal.setTime(dt);

		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		return cal.getTime();
	}

	/**
	 * 获得给定时间的第二天零时的日期对象 例如：若给定时间为（2004-08-01 11:30:58），将获得（2004-08-02
	 * 00:00:00）的日期对象 若给定时间为（2004-08-31 11:30:58），将获得（2004-09-01 00:00:00）的日期对象
	 * 
	 * @param dt
	 *            Date 给定的java.util.Date对象
	 * @return Date java.util.Date对象
	 */

	public static Date getNextDay(Date dt) {

		if (dt == null)
			return dt;

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		// return new GregorianCalendar(cal.get(Calendar.YEAR), cal
		// .get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1) //这个方法会出现
		// 32 号的情况
		// .getTime();
		//

		// modified by zhuqian 2009-03-25
		cal.add(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();

	}

	// added by zhuqian 2009-03-25
	public static Date getPreviousDay(Date dt) {
		if (dt == null)
			return dt;

		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return cal.getTime();
	}
	
	/**
	 * @param dt
	 * @param days
	 * @return
	 */
	public static Date getDaysBefore(Date dt, int days) {
		if(dt == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(Calendar.DAY_OF_YEAR, -days);
		return cal.getTime();
	}

	/**
	 * @param dt
	 *            Date 给定的java.util.Date对象
	 * @param weekDay
	 *            int 就是周几的”几“，周日是7
	 * @return Date java.util.Date对象
	 */
	public static Date getWeekDay(Date dt, int weekDay) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);
		if (weekDay == 7)
			weekDay = 1;
		else
			weekDay++;
		cal.set(GregorianCalendar.DAY_OF_WEEK, weekDay);
		return cal.getTime();
	}

	/**
	 * 获得给定时间的第N天零时的日期对象 例如：若给定时间为（2004-08-01 11:30:58），将获得（2004-08-02
	 * 00:00:00）的日期对象 若给定时间为（2004-08-31 11:30:58），将获得（2004-09-01 00:00:00）的日期对象
	 * 
	 * @param dt
	 *            Date 给定的java.util.Date对象
	 * @return Date java.util.Date对象
	 */
	public static Date getNextDay(Date dt, Long n) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
						+ n.intValue()).getTime();

	}

	/**
	 * 如果当天在后续这个月不存在这天，则返回期望这个月的最后一天 20081231 -1 返回20081130
	 * 
	 * @param dt
	 * @param n
	 * @return上午11:16:22
	 */
	public static Date getNextMonth(Date dt, Long n) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		Calendar firstCal = new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + n.intValue(), 1);
		if (firstCal.getActualMaximum(Calendar.DAY_OF_MONTH) < cal
				.get(Calendar.DAY_OF_MONTH)) {
			return new GregorianCalendar(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + n.intValue(),
					firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)).getTime();
		} else {
			return new GregorianCalendar(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + n.intValue(),
					cal.get(Calendar.DAY_OF_MONTH)).getTime();
		}

	}

	/**
	 * 如果当天在后续这个月不存在这天，则返回期望这个月后一个月的第一天 20081231 - 1 返回20081201 20080831 + 1
	 * 返回20080930
	 * 
	 * @param dt
	 * @param n
	 * @return上午11:22:39
	 */
	public static Date getNextMonthExtention(Date dt, Long n) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		Calendar firstCal = new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + n.intValue(), 1);
		if (firstCal.getActualMaximum(Calendar.DAY_OF_MONTH) < cal
				.get(Calendar.DAY_OF_MONTH)) {
			return new GregorianCalendar(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + n.intValue() + 1, 1).getTime();

		} else {
			return new GregorianCalendar(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH) + n.intValue(),
					cal.get(Calendar.DAY_OF_MONTH)).getTime();
		}
	}

	public static long getBetweenDate(Date startDate, Date endDate) {
		long startDateTime = startDate.getTime();
		long endDateTime = endDate.getTime();
		long dayTime = 24 * 60 * 60 * 1000;
		long days = (endDateTime - startDateTime) / dayTime;
		return days;
	}

	public static long getMonthLength(String countDate) throws ParseException {
		String firstDay = countDate.substring(0, countDate.length() - 2) + "01";
		Date startDate = strToDate(firstDay);
		Date endDate = getNextMonth(startDate, new Long(1));
		long startDateTime = startDate.getTime();
		long endDateTime = endDate.getTime();
		long dayTime = 24 * 60 * 60 * 1000;
		long days = (endDateTime - startDateTime) / dayTime;
		return days;
	}

	/**
	 * 获得当前时间的第二天零时的日期对象 例如：若当前时间为（2004-08-01 11:30:58），将获得（2004-08-02
	 * 00:00:00）的日期对象 若当前时间为（2004-08-31 11:30:58），将获得（2004-09-01 00:00:00）的日期对象
	 * 
	 * @return Date java.util.Date对象
	 */
	public static Date getNextDay() {

		Calendar cal = GregorianCalendar.getInstance();
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1)
				.getTime();

	}

	/**
	 * 将java.util.Date类型的对象转换为java.sql.Timestamp类型的对象
	 * 
	 * @param dt
	 *            Date
	 * @return Timestamp
	 */
	public static java.sql.Timestamp convertSqlDate(Date dt) {
		if (dt == null) {
			return new java.sql.Timestamp(0);
		}
		return new java.sql.Timestamp(dt.getTime());
	}

	/**
	 * 格式化当前时间，返回如："yyyyMMdd"形式的字符串
	 * 
	 * @return String
	 */
	public static String formatCurrrentDate() {
		java.util.Date pdate = new Date();
		return formatDate(pdate, "yyyyMMdd");
	}

	/**
	 * 按照给定格式返回代表日期的字符串
	 * 
	 * @param pDate
	 *            Date
	 * @param format
	 *            String 日期格式
	 * @return String 代表日期的字符串
	 */
	public static String formatDate(java.util.Date pDate, String format) {

		if (pDate == null) {
			pDate = new java.util.Date();
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(pDate);
	}

	/**
	 * 返回给定时间的小时数 例如：时间（2004-08-01 3:12:23）将返回 03 时间（2004-08-01 19:12:23）将返回19
	 * 
	 * @param pDate
	 *            Date 给定时间
	 * @return String 代表小时数的字符串
	 */
	public static String getHour(Date pDate) {
		return formatDate(pDate, "HH");
	}

	/**
	 * 获得上一个月的最后一天
	 * 
	 * @return
	 */
	public static Calendar getTheLastDayOfTheMonth(int year, int month) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, month, 1);
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1);

	}

	public static boolean isFirstDayOfMonth(Date date) {
		if (date == null)
			return false;

		// 如果date减一天后变成另一个月，便是该月的第一天

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int thisMonth = c.get(Calendar.MONTH);
		c.add(Calendar.DATE, -1);

		return thisMonth != c.get(Calendar.MONTH);
	}

	public static Date getLastDayOfMonth(Date date) {
		if (date == null)
			return null;

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		c.set(Calendar.DATE, c.getActualMaximum(Calendar.DATE));

		return c.getTime();

	}

	/**
	 * 取得指定日期所在周的前一周的第一天
	 * 
	 * @param date
	 * @return
	 * @author yang_yun
	 */
	public static Date getFirstDayOfLastWeek(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime() - 604800000L);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date lastWeekDate = c.getTime();

		c = new GregorianCalendar();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setTime(new Date(lastWeekDate.getTime()));
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
		return c.getTime();
	}

	/**
	 * 验证字符串是不是合法的日期；严格判断日期格式YYYYMMDD的正则表达式：包括闰年的判断、大月小月的判断
	 * 
	 * @param dateString
	 *            待验证的日期字符串
	 * @return 满足则返回true，不满足则返回false
	 * @author zhangpeng mrd3.4.0
	 */
	public static boolean validateDateString(String dateString) {

		if (dateString == null || dateString.equals("")) {
			return false;
		}

		// 日期格式YYYYMMDD的正则表达式,世纪年为闰年，如2000
		String regDate = "^(((([02468][048])|([13579][26]))[0]{2})(02)(([0][1-9])|([1-2][0-9])))"
				+
				// 世纪年不为闰年如2100
				"|(((([02468][1235679])|([13579][01345789]))[0]{2})(02)(([0][1-9])|([1][0-9])|([2][0-8])))"
				+
				// 非世纪年为闰年，如1996
				"|(([0-9]{2}(([0][48])|([2468][048])|([13579][26])))(02)(([0][1-9])|([1-2][0-9])))"
				+
				// 非世纪年不为闰年，如1997
				"|(([0-9]{2}(([02468][1235679])|([13579][01345789])))(02)(([0][1-9])|([1][0-9])|([2][0-8])))"
				+
				// 大月，有31天
				"|(([0-9]{4})(([0]{1}(1|3|5|7|8))|10|12)(([0][1-9])|([1-2][0-9])|30|31))"
				+
				// 小月，只有30天
				"|(([0-9]{4})(([0]{1}(4|6|9))|11)(([0][1-9])|([1-2][0-9])|30))$";

		return dateString.matches(regDate);
	}

	/**
	 * 获取指定日期的0时 如输入2008-11-13 16:00，则输出2008-11-13 00:00
	 */
	public static Calendar getDateCeil(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 获取指定日期的23时 如输入2008-11-13 16:00，则输出2008-11-13 23:00
	 */
	public static Calendar getDateFloor(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 0);
	}

	/**
	 * 获取今日0时
	 */
	public static Calendar getCurDateCeil() {
		Calendar cal = new GregorianCalendar();
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 获取今日23时
	 */
	public static Calendar getCurDateFloor() {
		Calendar cal = new GregorianCalendar();
		return new GregorianCalendar(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 0);
	}

	public static Date getStartOfDate(Date date) {
		if (date == null)
			return date;

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);

		return c.getTime();
	}

	public static Date getEndOfDate(Date date) {
		if (date == null)
			return date;

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);

		return c.getTime();
	}

	/**
	 * 获取服务器时间的毫秒数
	 * 
	 * @author zhangpingan
	 * @param 无
	 * @return 返回服务器时间的毫秒数
	 * @author zhangpingan mrd Beidou2.0.0
	 */
	public static long getTimeInMillionSeconds() {
		Calendar c = Calendar.getInstance();
		return c.getTimeInMillis();
	}

	public static Date getLastSecondDateofYest() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);

		return cal.getTime();
	}

}
