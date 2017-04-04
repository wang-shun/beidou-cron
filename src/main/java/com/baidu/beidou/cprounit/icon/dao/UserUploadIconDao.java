package com.baidu.beidou.cprounit.icon.dao;

import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.UserUploadIcon;

public interface UserUploadIconDao {

	/**
	 * findNotSyncUserUploadIcon: 获取未同步到ubmc的用户上传图标
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UserUploadIcon> findNotSyncUserUploadIcon(int maxMaterNum);
	
	/**
	 * updateUserUploadIcon: 更新用户上传图标mcId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateUserUploadIcon(Integer id, Long mcId);
}
