/**
 * @version 1.1.0
 * 2009-9-10 下午07:26:43
 * @author zengyunfeng
 */
package com.baidu.beidou;

import com.baidu.gson.Gson;
import com.baidu.gson.GsonBuilder;

/**
 * @author zengyunfeng
 *
 */
public class JsonOutputTestHelper {
	public final static Gson gson = new GsonBuilder().serializeNulls()
	.disableHtmlEscaping().serializeSpecialFloatingPointValues()
	.create();
	
	
	public static void main(String[] args) {
	}
}
