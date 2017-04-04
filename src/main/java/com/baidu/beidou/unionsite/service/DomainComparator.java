/**
 * @version 1.1.0
 * 2009-11-11 下午04:50:13
 * @author zengyunfeng
 */
package com.baidu.beidou.unionsite.service;

import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zengyunfeng
 *
 */
public class DomainComparator {
	
	/**
	 * 站点的比较,必须都为小写或都为大写
	 * @param domain1
	 * @param domain2
	 * @return
	 */
	public static int domainCompare(String domain1, String domain2) {
		if(domain1==null && domain1==domain2){
			return 0;
		}else if(domain1 == null){
			return -1;
		}else if(domain2 == null){
			return 1;
		}
		
		//对.替换成'\0',保证同一个主域下的站点排序时，能够排在一块，例如baidu.com, cn-baidu.com, tieba.baidu.com, 排序后为baidu.com, tieba.baidu.com, cn-baidu.com
		String tmpO1 = domain1.replace('.', '\0');
		String tmpO2 = domain2.replace('.', '\0');
		
		
		return StringUtil.reverseCompare(tmpO1, tmpO2);
		
		
	}
	
	/**
	 * url的比较
	 * urlCompare: 先比较域名，域名一致然后比较路径。注意域名和http的大小写必须一致
	 *
	 * @param url1
	 * @param url2
	 * @return      
	 * @since 1.0.51
	 */
	public static int urlCompare(String url1, String url2){
		if(url1 == null && url2 == null){
			return 0;
		}else if(url1 == null && url2 != null){
			return -1;
		}else if(url1 != null && url2 == null){
			return 1;
		}
		
		String site1 = UrlParser.parseUrl(url1);
		String site2 = UrlParser.parseUrl(url2);
		int result = domainCompare(site1, site2);
		if(result !=0){
			return result;
		}
		return url1.compareTo(url2);
	}
	
	/**
	 * 
	 * urlContain: 判断url1是否包含url2, 注意域名和http的大小写必须一致，路径区分大小写
	 *
	 * @param url1
	 * @param url2
	 * @return    true:url1包含url2；  否则为false
	 * @since 1.0.51
	 */
	public static boolean urlContain(String url1, String url2){
		if(url1 == null || url2 == null){
			return false;
		}
		if(url1.equals(url2)){
			return true;
		}
		String site1 = UrlParser.parseUrl(url1);
		String site2 = UrlParser.parseUrl(url2);
		
		if((url1.equalsIgnoreCase(site1)&& site2.endsWith(site1))
				|| url2.startsWith(url1)){
			return true;
		}
		return false;
	}
	
}
