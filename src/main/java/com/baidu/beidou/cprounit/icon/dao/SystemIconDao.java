package com.baidu.beidou.cprounit.icon.dao;

import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.SystemIcon;

public interface SystemIconDao{
	/**
	 * @function 插入系统图标到图标库
	 * @param icon
	 */
	public void insertSystemIcon(SystemIcon icon);
	
	/**
	 * findNotSyncSystemIcon: 获取未同步到ubmc的系统图标
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<SystemIcon> findNotSyncSystemIcon(int maxMaterNum);
	
	/**
	 * updateSystemIcon: 更新系统图标mcId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateSystemIcon(Long id, Long mcId);
}