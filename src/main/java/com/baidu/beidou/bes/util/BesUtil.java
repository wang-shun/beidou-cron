/**
 * BesUtil.java 
 */
package com.baidu.beidou.bes.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.beidou.cprounit.bo.UnitMaterView;


/**
 * bes util类
 * 
 * @author lixukun
 * @date 2014-01-01
 */
public class BesUtil {
	public static String getBittagVar(String company) {
		if (company == null) {
			return null;
		}
		return System.getenv(company.toUpperCase() + "_BITTAG");
	}
	
	public static String getWorkPath() {
		return System.getenv("WORK_PATH");
	}
	
	public static String getBinPath() {
		return System.getenv("BIN_PATH");
	}
	
	public static String getAbsoluteFile(String file, String company) {
		return getWorkPath() + "/" + company + "/" + file;
	}
	
	public static String getAbsoluteWorkPath(String company) {
		return getWorkPath() + "/" + company;
	}
	
	public static int getMaterSizeHashCode(int height, int width) {
		return height * 100000 + width;
	}
	
	public static String getAbsoluteScriptPath(String script) {
		return getBinPath() + "/" + script;
	}
	
	/**
	 * 根据目前的hash规则，生成userId
	 * 
	 * @param db
	 * @param table
	 * @param db_sharding
	 * @param table_sharding
	 * @return
	 */
	public static int generateFakeUserId(int db, int table, int db_sharding, int table_sharding) {
		return (db << db_sharding) + table_sharding;
	}
	
	/**
	 * 解码经过转义后的Unicode
	 * 
	 * @param str
	 * @return
	 */
	public static String decodeUnicode(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
	}
	
	public static String generateHtmlSnippet(UnitMaterView mater, String tempUrl) {
		if (mater.getWuliaoType() == 2) {
			return generateImgHtml(tempUrl);
		} else if (mater.getWuliaoType() == 3) {
			return generateFlashHtml(mater, tempUrl);
		}
		
		return "<html/>";
	}
	
	private static String generateImgHtml(String tempUrl) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>")
		  .append("<img src=\"").append(tempUrl).append("\" />")
		  .append("</body></html>");
		
		return sb.toString();
	}
	
	private static String generateFlashHtml(UnitMaterView mater, String tempUrl) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body>")
		  .append("<embed type=\"application/x-shockwave-flash\" src=\"").append(tempUrl).append("\"  ")
		  .append(" height=\"").append(mater.getHeight()).append("\"")
		  .append(" width=\"").append(mater.getWidth()).append("\" wmode=\"opaque\"")
		  .append("</body></html>");
		
		return sb.toString();
	}
}
