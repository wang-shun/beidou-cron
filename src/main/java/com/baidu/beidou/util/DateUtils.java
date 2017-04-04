package com.baidu.beidou.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtils {

	public static Date StrToDate(String time) throws ParseException {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMdd");

		java.util.Date d = sdf.parse(time);

		return d;
	}

	public static Date StrToTime(String time) throws ParseException {

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMdd:HH");

		java.util.Date d = sdf.parse(time);

		return d;
	}

	/**
	 * ��ɵ�ǰʱ���Ӧ�İ�Сʱ��ʱ���ַ�
	 * 
	 * @return String ʱ���ַ�
	 */
	public static String getHourStr() throws ParseException {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyyMMddHH");
		return sdf.format(new Date());
	}

	/**
	 * ���java.util.Date���͵Ķ���
	 * 
	 * @param year
	 *            int ��
	 * @param month
	 *            int ��
	 * @param day
	 *            int ��
	 * @return Date java.util.Date���͵Ķ���
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
	 * ���java.util.Date���͵Ķ���
	 * 
	 * @param year
	 *            int ��
	 * @param month
	 *            int ��
	 * @param day
	 *            int ��
	 * @param hour
	 *            int Сʱ
	 * @return Date java.util.Date����
	 */
	public static Date getDate(int year, int month, int day, int hour) {
		GregorianCalendar d = new GregorianCalendar(year, month - 1, day, hour,
				0);
		return d.getTime();
	}

	/**
	 * ���Բ����Сʱ�ĵ�ǰʱ�� ���磺��ǰʱ��Ϊ��2004-08-01 11:30:58��������ã�2004-08-01
	 * 11:00:00�������ڶ���
	 * 
	 * @return Date java.util.Date����
	 */
	public static Date getRoundedHourCurDate() {

		Calendar cal = GregorianCalendar.getInstance();

		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		return cal.getTime();

	}

	/**
	 * ��ɵ�����ʱ�����ڶ��� ���磺��ǰʱ��Ϊ��2004-08-01 11:30:58��������ã�2004-08-01
	 * 00:00:00�������ڶ���
	 * 
	 * @return Date java.util.Date����
	 */
	public static Date getRoundedDayCurDate() {
		Calendar cal = new GregorianCalendar();

		return new GregorianCalendar(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).getTime();
	}

	/**
	 * ���Բ����Сʱ�ĵ�ǰʱ�� ���磺���ʱ��Ϊ��2004-08-01 11:30:58��������ã�2004-08-01
	 * 11:00:00�������ڶ���
	 * 
	 * @param dt
	 *            Date java.util.Date����
	 * @return Date java.util.Date����
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
	 * ��ø�ʱ��ĵڶ�����ʱ�����ڶ��� ���磺���ʱ��Ϊ��2004-08-01
	 * 11:30:58��������ã�2004-08-02 00:00:00�������ڶ��� ���ʱ��Ϊ��2004-08-31
	 * 11:30:58��������ã�2004-09-01 00:00:00�������ڶ���
	 * 
	 * @param dt
	 *            Date ���java.util.Date����
	 * @return Date java.util.Date����
	 */
	public static Date getNextDay(Date dt) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);
		return new GregorianCalendar(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1)
				.getTime();

	}

	/**
	 * @param dt
	 *            Date ���java.util.Date����
	 * @param weekDay
	 *            int �����ܼ��ġ�������������7
	 * @return Date java.util.Date����
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
	 * ��ø�ʱ��ĵ�N����ʱ�����ڶ��� ���磺���ʱ��Ϊ��2004-08-01
	 * 11:30:58��������ã�2004-08-02 00:00:00�������ڶ��� ���ʱ��Ϊ��2004-08-31
	 * 11:30:58��������ã�2004-09-01 00:00:00�������ڶ���
	 * 
	 * @param dt
	 *            Date ���java.util.Date����
	 * @return Date java.util.Date����
	 */
	public static Date getNextDay(Date dt, Long n) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		return new GregorianCalendar(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
				+ n.intValue()).getTime();

	}

	public static Date getNextMonth(Date dt, Long n) {

		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		Calendar firstCal = new GregorianCalendar(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH)
				+ n.intValue(), 1);
		if (firstCal.getActualMaximum(Calendar.DAY_OF_MONTH) < cal
				.get(Calendar.DAY_OF_MONTH)) {
			return new GregorianCalendar(cal.get(Calendar.YEAR), cal
					.get(Calendar.MONTH)
					+ n.intValue(), firstCal
					.getActualMaximum(Calendar.DAY_OF_MONTH)).getTime();
		} else {
			return new GregorianCalendar(cal.get(Calendar.YEAR), cal
					.get(Calendar.MONTH)
					+ n.intValue(), cal.get(Calendar.DAY_OF_MONTH)).getTime();
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
		Date startDate = StrToDate(firstDay);
		Date endDate = getNextMonth(startDate, new Long(1));
		long startDateTime = startDate.getTime();
		long endDateTime = endDate.getTime();
		long dayTime = 24 * 60 * 60 * 1000;
		long days = (endDateTime - startDateTime) / dayTime;
		return days;
	}

	/**
	 * ��õ�ǰʱ��ĵڶ�����ʱ�����ڶ��� ���磺��ǰʱ��Ϊ��2004-08-01
	 * 11:30:58��������ã�2004-08-02 00:00:00�������ڶ��� ��ǰʱ��Ϊ��2004-08-31
	 * 11:30:58��������ã�2004-09-01 00:00:00�������ڶ���
	 * 
	 * @return Date java.util.Date����
	 */
	public static Date getNextDay() {

		Calendar cal = GregorianCalendar.getInstance();
		return new GregorianCalendar(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1)
				.getTime();

	}

	/**
	 * ��java.util.Date���͵Ķ���ת��Ϊjava.sql.Timestamp���͵Ķ���
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

	// /**
	// * ��java.util.Date���͵Ķ���ת��Ϊjava.sql.Timestamp���͵Ķ���
	// * @param dt Date
	// * @return Timestamp
	// */
	// public static java.sql.Date convertDateForToSqlDate(Date dt) {
	// if (dt == null) {
	// return new java.sql.Date(0);
	// }
	//
	// return new java.sql.Date(dt.getTime());
	// }

	/**
	 * ��ʽ����ǰʱ�䣬�����磺2004��8��1����ʽ���ַ�
	 * 
	 * @return String
	 */
	public static String formatCurrrentDate() {
		java.util.Date pdate = new Date();
		return formatDate(pdate, "yyyy-MM-dd");
	}

	/**
	 * ���ո��ʽ���ش�����ڵ��ַ�
	 * 
	 * @param pDate
	 *            Date
	 * @param format
	 *            String ���ڸ�ʽ
	 * @return String ������ڵ��ַ�
	 */
	public static String formatDate(java.util.Date pDate, String format) {

		if (pDate == null) {
			pDate = new java.util.Date();
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(pDate);
	}

	/**
	 * ���ظ�ʱ���Сʱ�� ���磺ʱ�䣨2004-08-01 3:12:23�������� 03 ʱ�䣨2004-08-01
	 * 19:12:23��������19
	 * 
	 * @param pDate
	 *            Date ��ʱ��
	 * @return String ���Сʱ����ַ�
	 */
	public static String getHour(Date pDate) {
		return formatDate(pDate, "HH");
	}

	/**
	 * �����һ���µ����һ��
	 * 
	 * @return
	 */
	public static Calendar getTheLastDayOfTheMonth(int year, int month) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, month, 1);
		return new GregorianCalendar(cal.get(Calendar.YEAR), cal
				.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) - 1);

	}

	/**
	 * ��֤�ַ��ǲ��ǺϷ������ڣ��ϸ��ж����ڸ�ʽYYYYMMDD��������ʽ����(������жϡ�����С�µ��ж�
	 * 
	 * @param dateString
	 *            ����֤�������ַ�
	 * @return �����򷵻�true���������򷵻�false
	 * @author zhangpeng mrd3.4.0
	 */
	public static boolean validateDateString(String dateString) {

		if (dateString == null || dateString.equals("")) {
			return false;
		}

		// ���ڸ�ʽYYYYMMDD��������ʽ,�<���Ϊ���꣬��2000
		String regDate = "^(((([02468][048])|([13579][26]))[0]{2})(02)(([0][1-9])|([1-2][0-9])))"
				+
				// �<��겻Ϊ������2100
				"|(((([02468][1235679])|([13579][01345789]))[0]{2})(02)(([0][1-9])|([1][0-9])|([2][0-8])))"
				+
				// ���<���Ϊ���꣬��1996
				"|(([0-9]{2}(([0][48])|([2468][048])|([13579][26])))(02)(([0][1-9])|([1-2][0-9])))"
				+
				// ���<��겻Ϊ���꣬��1997
				"|(([0-9]{2}(([02468][1235679])|([13579][01345789])))(02)(([0][1-9])|([1][0-9])|([2][0-8])))"
				+
				// ���£���31��
				"|(([0-9]{4})(([0]{1}(1|3|5|7|8))|10|12)(([0][1-9])|([1-2][0-9])|30|31))"
				+
				// С�£�ֻ��30��
				"|(([0-9]{4})(([0]{1}(4|6|9))|11)(([0][1-9])|([1-2][0-9])|30))$";

		return dateString.matches(regDate);
	}

	//added by zhuqian 2009-03-25
	public static Date getPreviousDay(Date dt){
		if(dt == null) return dt;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		return cal.getTime();
	}
	
	/**
	 * 返回指定格式的时间字符串：yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 * @throws ParseException下午01:12:32
	 */
	public static String getDateStr(Date date) {
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	
	public static Date getDateFromStr(String dateStr) {
		Date date = null;
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
	
	public static void main(String[] args) {

		// Calendar cal = new GregorianCalendar();
		// cal.set(2004, 8, 6);
		// Calendar cal1 = Calendar.getInstance();
		// cal1.set(2004, 8, 9);
		// System.out.println("cal1.getTime() = " + cal1.getTime());
		// LinkedHashMap r = getNullLogDateList(cal.getTime(), cal1.getTime(),
		// 2);
		// int i = 0;
		// for (Iterator iter = r.keySet().iterator(); iter.hasNext(); ) {
		// Object item = (Object) iter.next();
		// System.out.println("i++ = " + i++);
		// System.out.println("item = " + item);
		// }
		// Date d=new Date();
		// d=DateUtils.getNextDay(d,new Long(-1));
		// String date=DateUtils.formatDate(d,"yyyyMMdd");
		// System.out.println(date);

		// int year=2004;
		// for (int month=0;month<12;month++)
		// {
		// Calendar cal=DateUtils.getTheLastDayOfTheMonth(year, month);
		// Date d=cal.getTime();
		// System.out.println(DateUtils.formatDate(d,"yyyy��MM��" ));
		// Long mo=new
		// Long(cal.get(Calendar.YEAR)*100+cal.get(Calendar.MONTH)+1);
		// System.out.println(mo);
		// }

		// Date date = getDate(2006,1,31);
//		Date date = getDate(20060131);
//		System.out.println("next month date:"
//				+ formatDate(getNextMonth(date, new Long(-11)), "yyyyMMdd"));
//		System.out.println(DateUtils.formatDate(new Date(), "yyyyMMdd"));
//		String registerTime = DateUtils.formatDate(new Date(), "yyyyMMddmmss");
//		System.out.println(registerTime);
		
		String dateStr = "2013-05-01 01:02:03";
		System.out.println(DateUtils.getDateFromStr(dateStr));

		// System.out.println("first day of week: "
		// +getDate(2005,8,1,4).getTime());

	}
}
