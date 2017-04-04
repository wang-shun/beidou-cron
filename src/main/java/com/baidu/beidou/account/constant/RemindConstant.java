package com.baidu.beidou.account.constant;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.baidu.beidou.account.vo.TransferResult;
import com.baidu.beidou.util.string.StringUtil;

/**
 * ClassName:RemindConstant
 * Function: 提醒相关的常量设置
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-8-26
 * @since    Cpweb-166 提醒升级
 * @version  $Id: Exp $
 */
public abstract class RemindConstant {

	/** 账户充值提醒类型 */
	public static final int REMIND_TYPE_ACCOUNT = 1;

	/** 账户转账提醒类型 */
	public static final int REMIND_TYPE_TRANSFER = 2;
	
	/** 最大时间配置 */
	public static final int MAX_HOUR = 23;
	/** 最小时间配置 */
	public static final int MIN_HOUR = 0;

	public static final DateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	/** 小时格式 */
	public static final DecimalFormat nn = new DecimalFormat("00");

	/** 由于之前用的是date.toString，因此需要用这种方式来解析日期 */
	public static final DateFormat defaultFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
	
	public static boolean isValidTime(int hour) {
		return hour <= MAX_HOUR &&  hour >= MIN_HOUR;
	}
	
	/**
	 * generateTimeStringArray:生成小时粒度的时间字符串，如2010082001
	 *
	 * @param from
	 * @param to
	 * @param now
	 * @return      
	 * @since 
	*/
	public static String[] generateTimeStringArray(int from, int to, Date now) {
		String[] result;
		if (isValidTime(from) && isValidTime(to) && now != null) {
			result = new String[getLenth(from, to)];
			if (from <= to) {
				for (int i = from, j = 0; i <= to; i++, j++) {
					result[j] = yyyyMMdd.format(now) + nn.format(i);
				}
			} else {
				int j = 0;
				Calendar ca = Calendar.getInstance();
				ca.setTime(now);
				ca.add(Calendar.DATE,  -1); 
				Date yesterday = ca.getTime();
				for(int i = from; i <= MAX_HOUR; i++, j++) {
					result[j] = yyyyMMdd.format(yesterday) + nn.format(i);
				}
				
				for (int i = MIN_HOUR; i <= to; i++, j++) {
					result[j] = yyyyMMdd.format(now) + nn.format(i);
				}
			}
		} else {
			result = new String[0];
		}
		return result;
	}
	
	public static int getLenth(int from, int to) {
		if (isValidTime(from) && isValidTime(to) ) {
			return (to - from + 1 + 24) % 24;
		}
		throw new IllegalArgumentException("hour must between " + MIN_HOUR + " and " + MAX_HOUR);
	}
	public static void main(String[] args) throws ParseException {
		System.out.println(getLenth(7,8));
		System.out.println(getLenth(22,8));
		String s[] = generateTimeStringArray(7,8,new Date());
		System.out.println(s);
		s = generateTimeStringArray(22,8,new Date());
		System.out.println(s);
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
//		Date d = df.parse(new Date().toString());
		Date d = new Date();
		System.out.println(df.format(d));
		System.out.println(d.toString());
		TransferResult vo = generateVO("Sun Aug 29 23:12:40 CST 2010	446201	30.0	0	0");
		System.out.println(vo);
		df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.out.println(df.format(vo.getTime()));
		System.out.println(isValidHour(22,8,23));
		System.out.println(isValidHour(22,8,5));
		System.out.println(isValidHour(22,8,10));
		
	}

	public static TransferResult generateVO(String line) {
		TransferResult vo = null;
		if (!StringUtil.isEmpty(line)) {
			try {
				String[] domain = line.split("\t");
				if(domain.length != 5){
					return null;
				}
				vo = new TransferResult();
				vo.setTime(defaultFormat.parse(domain[0]));
				vo.setUserId(Integer.valueOf(domain[1]));
				vo.setFund(Double.valueOf(domain[2]));
				vo.setSuccess(Integer.valueOf(domain[4]));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return vo;
	}
	
	public static boolean isValidHour(int from, int to, int hour2check) {
		if (isValidTime(from) && isValidTime(to) && isValidTime(hour2check) ) {
			if (from <= to) {
				//如：1<2<10
				return hour2check >= from && hour2check <= to;
			} else {
				//如：23 > 22 || 1 < 7
				return hour2check >= from || hour2check <= to;
			}
		} else {
			return false;
		}
	}
	
}
