package com.baidu.beidou.accountmove.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.table.TableSchemaInfo;

/**
 * string utils
 * @author work
 *
 */
public class StringUtil {
	
	/**
	 *  stract string from list
	 * @param strlist strlist
	 * @param seperator seperator
	 * @return return
	 */
	public static String getStringFromList (final List<String> strlist, String seperator) {
		if (CollectionUtils.isEmpty(strlist)) {
			return null;
		}
		String listStr = null;
		for (int i = 0; i < strlist.size(); i++) {
			if (i == 0) {
				listStr = strlist.get(i).toString();
			} else {
				listStr = listStr + seperator + strlist.get(i).toString();
			}
		}
		return listStr;
	}
	
	/**
	 * stract string from array
	 * @param strArray strArray
	 * @param seperator seperator
	 * @return return
	 */
	public static String getStringFromArray (final String[] strArray, String seperator) {
		if (strArray == null) {
			return null;
		}
		String listStr = null;
		for (int i = 0; i < strArray.length; i++) {
			if (i == 0) {
				listStr = strArray[i].toString();
			} else {
				listStr = listStr + seperator + strArray[i].toString();
			}
		}
		return listStr;
	}
	
	/**
	 * get a string use the subStr,use seperator between them
	 * @param subStr subStr
	 * @param seperator seperator
	 * @param repeatTime repeatTime
	 * @return return
	 */
	public static String getRepetitionString (String subStr, String seperator, int repeatTime) {
		String listStr = null;
		for (int i = 0; i < repeatTime; i++) {
			if (i == 0) {
				listStr = subStr;
			} else {
				listStr = listStr + seperator + subStr;
			}
		}
		return listStr;
	}
	
	/**
	 * change string to array
	 * @param stringLine stringLine
	 * @param seperator seperator
	 * @return return
	 */
	public static String[] getArrayFromString (String stringLine, String seperator) {
		if (stringLine == null) {
			return null;
		}
		return stringLine.split("\t");
		
	}
	
	/**
	 * change string to list
	 * @param stringLine stringLine
	 * @param seperator seperator
	 * @return return
	 */
	public static List<String> getListFromString (String stringLine, String seperator) {
		if (stringLine == null) {
			return null;
		}
		String[] strArray = stringLine.split("\t");
		return Arrays.asList(strArray);
		
	}
	
	/**
	 * get the index of colName in the arrays
	 * @param colName colName
	 * @param arrays arrays
	 * @return return
	 */
	public static int getIndex (String colName, final String[] arrays) {
		int index = 0;
		for (index = 0; index < arrays.length; index++) {
			if (arrays[index].equals(colName)) {
				return index;
			}
		}
		return -1;
	}
	
	public static void main(String[] args) {
		String line = "a\tb\tc";
		List<String> l = StringUtil.getListFromString(line, "\t");
		System.out.println(l.size());
		System.out.println(l);
		
		String s = StringUtil.getStringFromArray(TableSchemaInfo.cproplan, ",");
		System.out.println(s);
	}
	
}
