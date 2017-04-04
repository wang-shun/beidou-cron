package com.baidu.beidou.unionsite.strategy;

public interface ISiteLinkPrefixGenerator {


	/** 域名前加www. */
	public static final String WWW_PREFIX = "www.";

	/** 协议前缀 */
	public static final String PROTOCOL_PREFIX = "http://";

	/** 默认前缀 */
	public static final String DEFAULT_PREFIX = PROTOCOL_PREFIX + WWW_PREFIX;
	
	/**
	 * 根据域名生成SiteLink前缀
	 * @param domain 域名，如baidu.com, jiaju.sina.com.cn
	 * @return SiteLink前缀。
	 */
	String generatePrefix(String domain) ;
	
}
