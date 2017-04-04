/**
 * 2009-12-14 下午10:24:04
 * @author zengyunfeng
 */
package com.baidu.beidou.user.constant;

/**
 * 用户在memcache缓存中的key
 * @author zengyunfeng
 *
 */
public class UserCacheConstant {

	/**
	 * 用户的权限缓存<br>
	 * 每个用户的缓存为USER_PRIVILEGE_KEY+userid<br>
	 */
	public static final String USER_AUTH_KEY = "BEIDOU_USER_AUTH_";
	
	/**
	 * 用于权限判断的用户管辖关系缓存<br>
	 * 每个用户的缓存为USER_RELATION_AUTH_KEY+userid
	 */
	public static final String USER_RELATION_AUTH_KEY = "BEIDOU_USERRELATION_AUTH_";
	
	/**
	 * 用于权限判断和用户列表的用户管辖关系缓存<br>
	 * 每个用户的缓存为USER_RELATION_AUTH_KEY+userid
	 */
	public static final String USER_RELATION_USERLIST_KEY="BEIDOU_USERRELATION_USERLIST_";
	
	/**
	 * 所有的审核员缓存
	 */
	public static final String GLOBAL_AUDITER_TYPE_LIST = "BEIDOU_GLOBAL_AUDITER_TYPE_LIST";
	
	/**
	 * 所有审核员的用户名缓存
	 */
	public static final String GLOBAL_AUDITER_NAME = "GLOBAL_AUDITER_NAME";
	
	/**
	 * 大客户用户集合，用于发送url页面跳转请求时做验证，大客户不做url页面跳转验证
	 */
	public static final String GLOBAL_USER_ROLE_SEM = "GLOBAL_USER_ROLE_SEM";
	
	/**
	 * 具有大客户和vip角色的用户
	 */
	public static final String GLOBAL_USER_TYPE_HEAVY = "BEIDOU_GLOBAL_USER_TYPE_HEAVY";
	
	
	/**
	 * 非vip的中小客户缓存
	 */
	public static final String GLOBAL_USER_TYPE_NORMAL = "BEIDOU_GLOBAL_USER_TYPE_NORMAL";
	
	
}
