package com.baidu.beidou.util;

import java.util.Date;

public abstract class TokenUtil {
	
	/**
	 * 获得随机字符串
	 */
	public static final String getTokenId(int length) {
		if (0 == length) {
			return "";
		}
		
		Date tt = new Date();
		String mTimeStr = "" + tt.getTime();
		int realLength = mTimeStr.length();
		if(realLength >= length){
			return mTimeStr.substring(0, length);
		}
		
		String tempString = "";
		String randomString = mTimeStr;		
		
		while (realLength < length) {
			tempString = "" + Math.random();
			randomString += tempString.substring(2);
			realLength = randomString.length();
		}
		
		return randomString.substring(0, length);
	}
}
