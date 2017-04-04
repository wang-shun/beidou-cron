package com.baidu.beidou.unionsite.strategy.impl;

import org.apache.commons.lang.StringUtils;

import com.baidu.beidou.unionsite.strategy.ISiteLinkPrefixGenerator;

/**
 * ClassName:GenerateSiteLinkPrefixByDotCount
 * Function: 根据域名中点的个数来生成SiteLink，此处为三个点及以则不加www.，其它的要加。
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-7-8
 * @version  $Id: Exp $
 */
public class GenerateSiteLinkPrefixByDotCount implements ISiteLinkPrefixGenerator{

	public static final String DEFAULT_PREFIX = "http://www.";
	
	/** 点的个数阈值 */
	public static final int DOT_THRESHOLD = 3;
	
	public String generatePrefix(String domain) {
		
		//3个点及以上的不加www.，其它要加。
		if (StringUtils.isEmpty(domain)) {
			return DEFAULT_PREFIX;
		}
		int dotCount = 0;
		for (int i = 0; i < domain.length(); i++) {
            if( domain.charAt(i) == '.') {
            	dotCount ++;
            }
        }
		if (dotCount >= DOT_THRESHOLD) {
			return PROTOCOL_PREFIX ;
		}
		return DEFAULT_PREFIX ;
	}
}
