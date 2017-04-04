/**
 * StringUtil.java 
 */
package com.baidu.beidou.util.string;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具集
 * 全部为静态方法
 * 
 * @author lixukun
 * @date 2013-12-24
 */
public class StringUtil {
	/** UTF-8编码常量 */
	public static final String ENC_UTF8 = "UTF-8";
	/** GBK编码常量 */
	public static final String ENC_GBK = "GBK";
	/** GBK的Charset */
	public static final Charset GBK = Charset.forName("GBK");
	/** UTF-8的Charset */
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	/** 精确到秒的日期时间格式化的格式字符串 */
	public static final String FMT_DATETIME = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 转换&#123;这种编码为正常字符<br/>
	 * 有些手机会将中文转换成&#123;这种编码,这个函数主要用来转换成正常字符.
	 * 
	 * @param str
	 * @return String
	 */
	public static String decodeNetUnicode(String str) {
		if (str == null)
			return null;

		String pStr = "&#(\\d+);";
		Pattern p = Pattern.compile(pStr);
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String mcStr = m.group(1);
			int charValue = StringUtil.convertInt(mcStr, -1);
			String s = charValue > 0 ? (char) charValue + "" : "";
			m.appendReplacement(sb, Matcher.quoteReplacement(s));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 过滤SQL字符串,防止SQL inject
	 * 
	 * @param sql
	 * @return String
	 */
	public static String encodeSQL(String sql) {
		if (sql == null) {
			return "";
		}
		// 不用正则表达式替换，直接通过循环，节省cpu时间
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sql.length(); ++i) {
			char c = sql.charAt(i);
			switch (c) {
			case '\\':
				sb.append("\\\\");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\'':
				sb.append("\'\'");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\u200B':// ZERO WIDTH SPACE
			case '\uFEFF':// ZERO WIDTH NO-BREAK SPACE
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 返回移除非法xml字符后的字符串，确保json和xml中的字符串能被正常解析
	 * 
	 * @param str
	 * @return
	 * @see org.jdom.Verifier
	 */
	public static String removeInvalidXmlChar(String str) {
		if (str == null || str.length() < 1) {
			return str;
		}
		for (int k = 0, len = str.length(); k < len; k++) {
			char c = str.charAt(k);
			if (!isXMLCharacter(c)) {
				StringBuilder sb = new StringBuilder(str.length() + 48);
				sb.append(str, 0, k);
				for (int i = k; i < len; i++) {
					c = str.charAt(i);
					if (Character.isHighSurrogate(c)) {// 如果已经是高代理字符，则可能是超过\uFFFF的unicode了
						int codePoint = str.codePointAt(i);// 进行代码点解析
						if (codePoint == c) {// 解析后的值与单个字符相同，说明只有单个高代理字符，则编码有问题，需要过滤该字符
							continue;
						} else if (!isXMLCharacter(codePoint)) {// 非法xml字符滤掉
							// System.err.println(codePoint + "|"
							// + Integer.toHexString(codePoint)
							// + " is not xml char a,i=" + i + ",len=" + len);
							i++;
							continue;
						} else {
							i++;
							sb.appendCodePoint(codePoint);
							continue;
						}
					} else if (isXMLCharacter(c)) {
						sb.append(c);
					}
				}
				return sb.toString();
			}
		}
		return str;
	}

	/**
	 * 判断一个unicode值是否为合法的xml字符，从org.jdom.Verifier复制过来的
	 * 
	 * @param c
	 * @return
	 * @see org.jdom.Verifier#isXMLCharacter(int)
	 */
	public static boolean isXMLCharacter(int c) {
		if (c == '\n')
			return true;
		if (c == '\r')
			return true;
		if (c == '\t')
			return true;

		if (c < 0x20)
			return false;
		if (c <= 0xD7FF)
			return true;
		if (c < 0xE000)
			return false;
		if (c <= 0xFFFD)
			return true;
		if (c < 0x10000)
			return false;
		if (c <= 0x10FFFF)
			return true;

		return false;
	}

	/**
	 * 格式化日期
	 * 
	 * @param dateStr
	 *            输入的日期字符串
	 * @param inputFormat
	 *            输入日期格式
	 * @param format
	 *            输出日期格式
	 * @return String 格式化后的日期,如果不能格式化则输出原日期字符串
	 */
	public static String formatDate(String dateStr, String inputFormat,
			String format) {
		String resultStr = dateStr;
		try {
			Date date = new SimpleDateFormat(inputFormat).parse(dateStr);
			resultStr = formatDate(date, format);
		} catch (ParseException e) {
		}
		return resultStr;
	}

	/**
	 * 格式化日期 输入日期格式只支持yyyy-MM-dd HH:mm:ss 或 yyyy/MM/dd HH:mm:ss
	 * 
	 * @param dateStr
	 *            输入的日期字符串
	 * @param format
	 *            输出日期格式
	 * @return String 格式化后的日期,如果不能格式化则输出原日期字符串
	 */
	public static String formatDate(String dateStr, String format) {
		String resultStr = dateStr;
		String inputFormat = "yyyy-MM-dd HH:mm:ss";
		if (dateStr == null) {
			return "";
		}
		if (dateStr
				.matches("\\d{1,4}\\-\\d{1,2}\\-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}")) {
			inputFormat = "yyyy-MM-dd HH:mm:ss.SSS";
		} else if (dateStr
				.matches("\\d{4}\\-\\d{1,2}\\-\\d{1,2} +\\d{1,2}:\\d{1,2}")) {
			inputFormat = "yyyy-MM-dd HH:mm:ss";
		} else if (dateStr
				.matches("\\d{4}\\-\\d{1,2}\\-\\d{1,2} +\\d{1,2}:\\d{1,2}")) {
			inputFormat = "yyyy-MM-dd HH:mm";
		} else if (dateStr.matches("\\d{4}\\-\\d{1,2}\\-\\d{1,2} +\\d{1,2}")) {
			inputFormat = "yyyy-MM-dd HH";
		} else if (dateStr.matches("\\d{4}\\-\\d{1,2}\\-\\d{1,2} +\\d{1,2}")) {
			inputFormat = "yyyy-MM-dd";
		} else if (dateStr
				.matches("\\d{1,4}/\\d{1,2}/\\d{1,2}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}\\.\\d{1,3}")) {
			inputFormat = "yyyy/MM/dd HH:mm:ss.SSS";
		} else if (dateStr
				.matches("\\d{4}/\\d{1,2}/\\d{1,2} +\\d{1,2}:\\d{1,2}")) {
			inputFormat = "yyyy/MM/dd HH:mm:ss";
		} else if (dateStr
				.matches("\\d{4}/\\d{1,2}/\\d{1,2} +\\d{1,2}:\\d{1,2}")) {
			inputFormat = "yyyy/MM/dd HH:mm";
		} else if (dateStr.matches("\\d{4}/\\d{1,2}/\\d{1,2} +\\d{1,2}")) {
			inputFormat = "yyyy/MM/dd HH";
		} else if (dateStr.matches("\\d{4}/\\d{1,2}/\\d{1,2} +\\d{1,2}")) {
			inputFormat = "yyyy/MM/dd";
		}
		resultStr = formatDate(dateStr, inputFormat, format);
		return resultStr;
	}

	/**
	 * 格式化日期
	 * 
	 * @param date
	 *            输入日期
	 * @param format
	 *            输出日期格式
	 * @return String
	 */
	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 获取字符型参数，若输入字符串为null，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return 字符串参数
	 */
	public static String convertString(String str, String defaults) {
		if (str == null) {
			return defaults;
		} else {
			return str;
		}
	}

	/**
	 * 获取int参数，若输入字符串为null或不能转为int，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return int参数
	 */
	public static int convertInt(String str, int defaults) {
		if (str == null) {
			return defaults;
		}
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return defaults;
		}
	}

	/**
	 * 获取long型参数，若输入字符串为null或不能转为long，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return long参数
	 */
	public static long convertLong(String str, long defaults) {
		if (str == null) {
			return defaults;
		}
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			return defaults;
		}
	}

	/**
	 * 获取double型参数，若输入字符串为null或不能转为double，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return double型参数
	 */
	public static double convertDouble(String str, double defaults) {
		if (str == null) {
			return defaults;
		}
		try {
			return Double.parseDouble(str);
		} catch (Exception e) {
			return defaults;
		}
	}

	/**
	 * 获取short型参数，若输入字符串为null或不能转为short，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return short型参数
	 */
	public static short convertShort(String str, short defaults) {
		if (str == null) {
			return defaults;
		}
		try {
			return Short.parseShort(str);
		} catch (Exception e) {
			return defaults;
		}
	}

	/**
	 * 获取float型参数，若输入字符串为null或不能转为float，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return float型参数
	 */
	public static float convertFloat(String str, float defaults) {
		if (str == null) {
			return defaults;
		}
		try {
			return Float.parseFloat(str);
		} catch (Exception e) {
			return defaults;
		}
	}

	/**
	 * 获取boolean型参数，若输入字符串为null或不能转为boolean，则返回设定的默认值
	 * 
	 * @param str
	 *            输入字符串
	 * @param defaults
	 *            默认值
	 * @return boolean型参数
	 */
	public static boolean convertBoolean(String str, boolean defaults) {
		if (str == null) {
			return defaults;
		}
		try {
			return Boolean.parseBoolean(str);
		} catch (Exception e) {
			return defaults;
		}
	}

	/**
	 * 分割字符串
	 * 
	 * @param line
	 *            原始字符串
	 * @param seperator
	 *            分隔符
	 * @return 分割结果
	 */
	public static String[] split(String line, String seperator) {
		if (line == null || seperator == null || seperator.length() == 0)
			return null;
		ArrayList<String> list = new ArrayList<String>();
		int pos1 = 0;
		int pos2;
		for (;;) {
			pos2 = line.indexOf(seperator, pos1);
			if (pos2 < 0) {
				list.add(line.substring(pos1));
				break;
			}
			list.add(line.substring(pos1, pos2));
			pos1 = pos2 + seperator.length();
		}
		// 去掉末尾的空串，和String.split行为保持一致
		for (int i = list.size() - 1; i >= 0 && list.get(i).length() == 0; --i) {
			list.remove(i);
		}
		return list.toArray(new String[0]);
	}

	/**
	 * 分割字符串，并转换为int
	 * 
	 * @param line
	 *            原始字符串
	 * @param seperator
	 *            分隔符
	 * @param def
	 *            默认值
	 * @return 分割结果
	 */
	public static int[] splitInt(String line, String seperator, int def) {
		String[] ss = split(line, seperator);
		int[] r = new int[ss.length];
		for (int i = 0; i < r.length; ++i) {
			r[i] = convertInt(ss[i], def);
		}
		return r;
	}

	/**
	 * 分割字符串，并转换为long
	 * 
	 * @param line
	 *            原始字符串
	 * @param seperator
	 *            分隔符
	 * @param def
	 *            默认值
	 * @return 分割结果
	 */
	public static long[] splitLong(String line, String seperator, long def) {
		String[] ss = split(line, seperator);
		long[] r = new long[ss.length];
		for (int i = 0; i < r.length; ++i) {
			r[i] = convertLong(ss[i], def);
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	public static String join(String separator, Collection c) {
		if (c.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		Iterator i = c.iterator();
		sb.append(i.next());
		while (i.hasNext()) {
			sb.append(separator);
			sb.append(i.next());
		}
		return sb.toString();
	}

	public static String join(String separator, String[] s) {
		return joinArray(separator, s);
	}

	public static String joinArray(String separator, Object[] s) {
		if (s == null || s.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(s[0]);
		for (int i = 1; i < s.length; ++i) {
			if (s[i] != null) {
				sb.append(separator);
				sb.append(s[i].toString());
			}
		}
		return sb.toString();
	}

	public static String joinArray(String separator, int[] s) {
		if (s == null || s.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(s[0]);
		for (int i = 1; i < s.length; ++i) {
			sb.append(separator);
			sb.append(s[i]);
		}
		return sb.toString();
	}

	public static String joinArray(String separator, long[] s) {
		if (s == null || s.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append(s[0]);
		for (int i = 1; i < s.length; ++i) {
			sb.append(separator);
			sb.append(s[i]);
		}
		return sb.toString();
	}

	public static String join(String separator, Object... c) {
		return joinArray(separator, c);
	}

	/**
	 * 字符串全量替换
	 * 
	 * @param s
	 *            原始字符串
	 * @param src
	 *            要替换的字符串
	 * @param dest
	 *            替换目标
	 * @return 结果
	 */
	public static String replaceAll(String s, String src, String dest) {
		if (s == null || src == null || dest == null || src.length() == 0)
			return s;
		int pos = s.indexOf(src); // 查找第一个替换的位置
		if (pos < 0)
			return s;
		int capacity = dest.length() > src.length() ? s.length() * 2 : s
				.length();
		StringBuilder sb = new StringBuilder(capacity);
		int writen = 0;
		for (; pos >= 0;) {
			sb.append(s, writen, pos); // append 原字符串不需替换部分
			sb.append(dest); // append 新字符串
			writen = pos + src.length(); // 忽略原字符串需要替换部分
			pos = s.indexOf(src, writen); // 查找下一个替换位置
		}
		sb.append(s, writen, s.length()); // 替换剩下的原字符串
		return sb.toString();
	}

	/**
	 * 只替换第一个
	 * 
	 * @param s
	 * @param src
	 * @param dest
	 * @return
	 */
	public static String replaceFirst(String s, String src, String dest) {
		if (s == null || src == null || dest == null || src.length() == 0)
			return s;
		int pos = s.indexOf(src);
		if (pos < 0) {
			return s;
		}
		StringBuilder sb = new StringBuilder(s.length() - src.length()
				+ dest.length());

		sb.append(s, 0, pos);
		sb.append(dest);
		sb.append(s, pos + src.length(), s.length());
		return sb.toString();
	}

	/**
	 * Returns <tt>true</tt> if s is null or <code>s.trim().length()==0<code>.
	 * 
	 * @see java.lang.String#isEmpty()
	 */
	public static boolean isEmpty(String s) {
		if (s == null)
			return true;
		return s.trim().isEmpty();
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	/**
	 * @see java.lang.String#trim()
	 */
	public static String trim(String s) {
		if (s == null)
			return null;
		return s.trim();
	}

	public static String removeAll(String s, String src) {
		return replaceAll(s, src, "");
	}

	/**
	 * 以某一长度缩写字符串（1个中文或全角字符算2个长度单位，英文或半角算一个长度单位）. 如果要显示n个汉字的长度，则maxlen= 2* n
	 * 
	 * @param src
	 *            utf-8字符串
	 * @param maxlen
	 *            缩写后字符串的最长长度（1个中文或全角字符算2个单位长度）
	 * @param replacement
	 *            替换的字符串，该串长度会计算到maxlen中
	 * @return String
	 */
	public static String abbreviate(String src, int maxlen, String replacement) {
		if (src == null)
			return "";

		if (replacement == null) {
			replacement = "";
		}

		StringBuilder dest = new StringBuilder(); // 初始值设定为源串

		try {
			maxlen = maxlen - computeDisplayLen(replacement);

			if (maxlen < 0) {
				return src;
			}

			int i = 0;
			for (; i < src.length() && maxlen > 0; ++i) {
				char c = src.charAt(i);
				if (c >= '\u0000' && c <= '\u00FF') {
					maxlen = maxlen - 1;
				} else {
					maxlen = maxlen - 2;
				}
				if (maxlen >= 0) {
					dest.append(c);
				}
			}

			// 没有取完 src 所有字符时，才需要加后缀 ...
			if (i < src.length() - 1) {
				dest.append(replacement);
			}
			return dest.toString();
		} catch (Throwable e) {

		}
		return src;
	}

	/**
	 * @see abbreviate(String src, int maxlen, String replacement)
	 * @param src
	 * @param maxlen
	 * @return
	 */
	public static String abbreviate(String src, int maxlen) {
		return abbreviate(src, maxlen, "");
	}

	/**
	 * 将字符串截短,功能与abbreviate()类似
	 * 全角字符算一个字,半角字符算半个字,这样做的目的是为了显示的时候排版整齐,因为全角字占的位置要比半角字小
	 * 
	 * @param str
	 * @param maxLen
	 * @return String
	 */
	public static String toShort(String str, int maxLen, String replacement) {
		if (str == null) {
			return "";
		}
		if (str.length() <= maxLen) {
			return str;
		}
		StringBuilder dest = new StringBuilder();
		double len = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c >= '\u0000' && c <= '\u00FF') {// 半角字算半个字
				len += 0.5;
			} else {
				len += 1;
			}
			if (len > maxLen)
				return dest.toString() + replacement;
			else
				dest.append(c);
		}
		return dest.toString();
	}

	public static String toShort(String str, int maxLen) {
		return toShort(str, maxLen, "...");
	}

	/**
	 * 计算字符串的显示长度，半角算１个长度，全角算两个长度
	 * 
	 * @param s
	 * @return
	 */
	public static int computeDisplayLen(String s) {
		int len = 0;
		if (s == null) {
			return len;
		}

		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c >= '\u0000' && c <= '\u00FF') {
				len = len + 1;
			} else {
				len = len + 2;
			}
		}
		return len;
	}

	/**
	 * 获取字符串的UTF-8编码字节数组
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] getUTF8Bytes(String s) {
		if (s != null && s.length() >= 0) {
			return s.getBytes(UTF_8);
		}
		return null;
	}

	/**
	 * 获取字符串的GBK编码字节数组
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] getGBKBytes(String s) {
		if (s != null && s.length() >= 0) {
			return s.getBytes(GBK);
		}
		return null;
	}

	/**
	 * 获取字节数组的UTF-8编码字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String getUTF8String(byte[] b) {
		if (b != null) {
			return new String(b, UTF_8);
		}
		return null;
	}

	/**
	 * 获取字节数组的GBK编码字符串
	 * 
	 * @param s
	 * @return
	 */
	public static String getGBKString(byte[] b) {
		if (b != null) {
			return new String(b, GBK);
		}
		return null;
	}

	/**
	 * 对字符串以 GBK编码方式进行URLEncode
	 * 
	 * @param s
	 * @return
	 */
	public static String URLEncodeGBK(String s) {
		if (s != null && s.length() > 0) {
			try {
				return URLEncoder.encode(s, ENC_GBK);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return s;
	}

	/**
	 * 对字符串以 UTF-8编码方式进行URLEncode
	 * 
	 * @param s
	 * @return
	 */
	public static String URLEncodeUTF8(String s) {
		if (s != null && s.length() > 0) {
			try {
				return URLEncoder.encode(s, ENC_UTF8);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return s;
	}

	/**
	 * 对字符串以 GBK编码方式进行URLDecode
	 * 
	 * @param s
	 * @return
	 */
	public static String URLDecodeGBK(String s) {
		if (s != null && s.length() > 0) {
			try {
				return URLDecoder.decode(s, ENC_GBK);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return s;
	}

	/**
	 * 对字符串以 UTF-8编码方式进行URLDecode
	 * 
	 * @param s
	 * @return
	 */
	public static String URLDecodeUTF8(String s) {
		if (s != null && s.length() > 0) {
			try {
				return URLDecoder.decode(s, ENC_UTF8);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return s;
	}

	/**
	 * 替换in语句，动态组装or查询条件
	 * 
	 * @param num
	 * @param unit
	 * @return
	 */
	public static String formateOrCondition(int num, final String unit) {

		if (num <= 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder(num * (unit.length() + 2));

		builder.append(unit);

		for (int i = 1; i < num; i++) {
			builder.append("or");
			builder.append(unit);
		}

		return builder.toString();
	}


	/**
	 * 将数字转换为百分数表示
	 * 
	 * @param num
	 *            double 需要表示为百分数的数字
	 * @param fraction
	 *            int 百分数中的小数位
	 * @return String 代表百分数的字符串
	 */
	public static String getPercent(double num, int fraction) {
		NumberFormat fmt = NumberFormat.getPercentInstance();

		fmt.setMaximumFractionDigits(fraction);
		return fmt.format(num);
	}

	/**
	 * 转换数值双精度型保留2位小数
	 * 
	 * @param number
	 *            要转换的数值
	 * @param formate
	 *            格式串 "0.00" 返回转换后的数值
	 */
	public static String convNumber(double number, String formate) {
		DecimalFormat df = new DecimalFormat(formate);
		return df.format(number);
	}

	public static boolean isTraditionalChineseCharacter(char c, boolean checkGbk) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		if (!Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
				&& !Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
						.equals(block)
				&& !Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
						.equals(block)) {
			return false;
		}
		if (checkGbk) {
			try {
				String s = "" + c;
				return s.equals(new String(s.getBytes("GBK"), "GBK"));
			} catch (java.io.UnsupportedEncodingException e) {
				return false;
			}
		}

		return true;
	}

	public static boolean validBeidouGbkStr(String input,
			final boolean checkGbk, final int minLength, final int maxLength) {

		// 对长度有个预先判断
		if ((minLength > -1) && (input.length() < minLength)) {
			return false;
		} else if ((maxLength > -1) && (input.length() > maxLength)) {
			return false;
		}

		// 验证字符合法性和特殊长度要求
		char[] ch = input.toCharArray();

		int length = 0;

		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (isNeedAlph(c)) {
				length += 1;
			} else if (isTraditionalChineseCharacter(c, checkGbk)) {
				length += 2;
			} else {
				return false;// 是否为合法字符集
			}
		}

		if ((minLength > -1) && (length < minLength)) {
			return false;
		} else if ((maxLength > -1) && (length > maxLength)) {
			return true;
		}

		return true;
	}

	private static boolean isNeedAlph(char c) {

		if (c >= 'a' && c <= 'z') {
			return true;
		} else if (c >= 'A' && c <= 'Z') {
			return true;
		} else if (c >= '0' && c <= '9') {
			return true;
		} else if (c == '-') {
			return true;
		} else if (c == '_') {
			return true;
		}

		return false;
	}

	public static boolean isLatinCharacter(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		if (!Character.UnicodeBlock.BASIC_LATIN.equals(block)) {
			return false;
		}

		return true;
	}

	/**
	 * 获得字符串的GBK编码的字节长度
	 * 
	 * @author zengyunfeng
	 * @param input
	 * @return
	 */
	public static int byteLength(String input) {
		byte[] array;
		try {
			array = input.getBytes("GBK");
			return array.length;
		} catch (UnsupportedEncodingException e) {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * 字符串反向排序
	 * @author zengyunfeng
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static int reverseCompare(String str1, String str2) {
		if(str1==null||str2==null){
			if(str1==str2){
				return 0;
			}else if(str1== null){
				return -1;
			}else{
				return 1;
			}
		}
		
		int len1 = str1.length();
		int len2 = str2.length();
		int n = Math.min(len1, len2);
		char v1[] = str1.toCharArray();
		char v2[] = str2.toCharArray();
		int i = len1 - 1;
		int j = len2 - 1;

		if (i == j) {
			int k = i;
			int lim = 0;
			while (k >= lim) {
				char c1 = v1[k];
				char c2 = v2[k];
				if (c1 != c2) {
					return c1 - c2;
				}
				k--;
			}
		} else {
			while (n-- != 0) {
				char c1 = v1[i--];
				char c2 = v2[j--];
				if (c1 != c2) {
					return c1 - c2;
				}
			}
		}
		return len1 - len2;
	}
	
	   public static String getMd5(String s){
	    	char hexDigits[] = {
	                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
	                'a', 'b', 'c', 'd', 'e', 'f'
	            };
	    	try{
	            char str[];
	            byte strTemp[] = s.getBytes();
	            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
	            mdTemp.update(strTemp);
	            byte md[] = mdTemp.digest();
	            int j = md.length;
	            str = new char[j * 2];
	            int k = 0;
	            for (int i = 0; i < j; i++)
	            {
	                byte byte0 = md[i];
	                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
	                str[k++] = hexDigits[byte0 & 0xf];
	            }

	            return new String(str);
	    	}catch(Exception e){
	            return null;    	
	    	}
	    }

	/**
	 * 将一个list组装成字符串，list各个元素之间以split分割,<br>
	 * 为了保持站点和行业的内容与之前的一致,最后有一个spilit分隔符，<br>
	 * 
	 * 
	 * @param collection
	 * @param split
	 *            分隔符, 如果为null, 则返回长度为0的字符串
	 * @return
	 */
	public static String makeStrFromCollectionForSite(
			final Collection<? extends Object> collection, final String split) {

		if (collection == null || collection.isEmpty()) {
			return "";
		}
		if (split == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		for (Object element : collection) {
			builder.append(element).append(split);
		}

		return builder.toString();
	}
	
	public static List<Integer> splitIntToList(String line, String seperator){
		String[] integers = split(line, seperator);
		if(isEmpty(line) && integers != null){
			return new ArrayList<Integer>(0);
		}
		
		List<Integer> result = new ArrayList<Integer>(integers.length);
		for(int i = 0; i < integers.length; i++){
			try{
				result.add(Integer.parseInt(integers[i].trim()));
			}catch(NumberFormatException e){
				continue;
			}
		}
		
		return result;		
	}
	
	public static String getSystemLineSeperator() {
		return System.getProperty("line.separator");
	}
	 /**
     * java unicode 转化为中文
     */
	 public static String unicodeToString(String str) {
	        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");    
	        Matcher matcher = pattern.matcher(str);
	        char ch;
	        while (matcher.find()) {
	            ch = (char) Integer.parseInt(matcher.group(2), 16);
	            str = str.replace(matcher.group(1), ch + "");    
	        }
	        return str;
	    }
}
