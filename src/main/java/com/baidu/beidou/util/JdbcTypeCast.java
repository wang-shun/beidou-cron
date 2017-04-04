/**
 * 
 */
package com.baidu.beidou.util;

import org.apache.commons.lang.math.NumberUtils;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class JdbcTypeCast {

	public static int CastToInt(Object obj){
		if(obj == null)
			return 0;
		int result = 0;
		String tmp = obj.toString();
		if(!NumberUtils.isNumber(tmp)){
			return 0;
		}
		result = NumberUtils.toInt(tmp);
		return result;
	}
	
	public static long CastToLong(Object obj){
		if(obj == null)
			return 0;
		long result = 0;
		String tmp = obj.toString();
		if(!NumberUtils.isNumber(tmp)){
			return 0;
		}
		result = NumberUtils.toLong(tmp);
		return result;
	}
	
	public static double CastToDouble(Object obj){
		if(obj == null)
			return 0;
		double result = 0;
		String tmp = obj.toString();
		if(!NumberUtils.isNumber(tmp)){
			return 0;
		}
		result = NumberUtils.toDouble(tmp);
		return result;
	}

}
