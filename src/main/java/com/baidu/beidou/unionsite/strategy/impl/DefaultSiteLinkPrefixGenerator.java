package com.baidu.beidou.unionsite.strategy.impl;

import com.baidu.beidou.unionsite.strategy.ISiteLinkPrefixGenerator;

/**
 * ClassName:DefaultSiteLinkPrefixGenerator
 * Function: 默认实现
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-7-8
 * @version  $Id: Exp $
 */
public class DefaultSiteLinkPrefixGenerator implements ISiteLinkPrefixGenerator{
	public String generatePrefix(String domain) {
		return DEFAULT_PREFIX;
	}
}
