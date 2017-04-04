/**
 * 2009-11-20 下午05:13:55
 */
package com.baidu.beidou.crm.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class DateRangeTest {
	DateFormat format = new SimpleDateFormat("yyyyMMdd");

	private String setWeekStartDate(Calendar startDate) {
		startDate.setFirstDayOfWeek(Calendar.MONDAY);
		startDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String result = format.format(startDate.getTime());
		return result;
	}

	@Test
	public void testWeekStartDate() {
		Calendar startDate = Calendar.getInstance();

		startDate.set(2009, 10, 16);
		assertEquals("20091116", setWeekStartDate(startDate));

		startDate.set(2009, 10, 17);
		assertEquals("20091116", setWeekStartDate(startDate));

		startDate.set(2009, 10, 15);
		assertEquals("20091116", setWeekStartDate(startDate));

	}

	private String setMonthStartDate(Calendar startDate) {
		startDate.set(Calendar.DATE, 1);
		String result = format.format(startDate.getTime());
		return result;
	}

	@Test
	public void testMonthStartDate() {
		Calendar startDate = Calendar.getInstance();

		startDate.set(2009, 10, 1);
		assertEquals("20091101", setMonthStartDate(startDate));

		startDate.set(2009, 9, 31);
		assertEquals("20091001", setMonthStartDate(startDate));

		startDate.set(2009, 2, 1);
		assertEquals("20090301", setMonthStartDate(startDate));

		startDate.set(2009, 1, 28);
		assertEquals("20090201", setMonthStartDate(startDate));

		startDate.set(2009, 0, 1);
		assertEquals("20090101", setMonthStartDate(startDate));

		startDate.set(2008, 11, 31);
		assertEquals("20081201", setMonthStartDate(startDate));
	}

	private String setSeasonStartDate(Calendar startDate) {
		startDate.set(Calendar.DATE, 1);
		int month = startDate.get(Calendar.MONTH); // 0-11
		startDate.set(Calendar.MONTH, (month / 3) * 3); // 季度初
		String result = format.format(startDate.getTime());
		return result;
	}

	@Test
	public void testSeasonStartDate() {
		Calendar startDate = Calendar.getInstance();

		startDate.set(2009, 10, 1);
		assertEquals("20091001", setSeasonStartDate(startDate));

		startDate.set(2009, 9, 31);
		assertEquals("20091001", setSeasonStartDate(startDate));

		startDate.set(2009, 4, 31);
		assertEquals("20090401", setSeasonStartDate(startDate));

		startDate.set(2009, 1, 28);
		assertEquals("20090101", setSeasonStartDate(startDate));

		startDate.set(2009, 0, 1);
		assertEquals("20090101", setSeasonStartDate(startDate));

		startDate.set(2008, 11, 31);
		assertEquals("20081001", setSeasonStartDate(startDate));
	}
}
