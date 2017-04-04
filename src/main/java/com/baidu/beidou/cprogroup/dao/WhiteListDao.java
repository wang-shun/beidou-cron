package com.baidu.beidou.cprogroup.dao;

import java.util.List;

/**
 * @author zhuqian
 *
 */
public interface WhiteListDao {

	/**
	 * 获取所有可投放百度自有流量的白名单用户
	 * @return userid列表
	 */
	public List<Integer> findAllWhiteUsers();
	
	/**
	 * 获取所有百度自有流量网站白名单
	 * @return
	 */
	public List<Integer> findAllWhiteSites();
	
	/**
	 * 获取所有百度自有流量行业白名单
	 * @return
	 */
	public List<Integer> findAllWhiteTrades();
	
	/**
	 * 获取所有可投放贴片广告的白名单用户
	 * @return
	 */
	public List<Integer> findAllWhiteFilm();
	
	/**
	 * 删除所有可投放百度自有流量的白名单用户列表
	 */
	public void removeAllWhiteUsers();
	
	/**
	 * 删除所有可投放贴片广告的白名单用户列表
	 */
	public void removeAllWhiteFilm();
	
	/**
	 * 更新可投放百度自有流量的白名单用户列表
	 */
	public void updateWhiteUsers(List<Integer> userList);
	
	/**
	 * 更新可投放贴片广告的白名单用户列表
	 */
	public void updateWhiteFilm(List<Integer> userList);
	
	
}
