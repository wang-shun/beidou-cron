package com.baidu.beidou.cprounit.dao;

import java.util.List;

import com.baidu.beidou.cprounit.bo.PreMater;

public interface PreMaterDao {
	/**
	 * findNotSyncPreMater: 获取未同步到ubmc的上一次物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<PreMater> findNotSyncPreMater(int index, int maxMaterNum);
	
	/**
	 * updatePreMater: 更新上一次物料mcId、mcVersionId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updatePreMater(int index, Long id, Long mcId, Integer mcVersionId, Integer userId);
}
