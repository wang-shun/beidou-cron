package com.baidu.beidou.cprounit.mcdriver.constant;

public class AmConstant {
	
	public static final int AM_GRANT_RETRY_TIMES = 3;
	
	/**
	 * grantAuthority接口statusCode: 
	 * 		0：正常，包含正确的mcId和versiondId
	 * 		1: 需重试
	 * 		2: AM_GRANT_RETRY_TIMES次重试失败，则创意创建失败
	 * 		-1：失败，说明该创意并不需要admaker进行处理，走北斗正常上传逻辑分支即可
	 */
	public static final int AM_GRANT_STATUS_OK = 0;
	public static final int AM_GRANT_STATUS_RETRY = 1;
	public static final int AM_GRANT_STATUS_RETRY_FAIL = 2;
	public static final int AM_GRANT_STATUS_FAIL = -1;

}
