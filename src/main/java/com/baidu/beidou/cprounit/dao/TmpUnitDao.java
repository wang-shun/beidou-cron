package com.baidu.beidou.cprounit.dao;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.cprounit.bo.TmpUnit;

public interface TmpUnitDao {
	
	/**
	 * findNotSyncTmpUnit: 获取未同步到ubmc的临时物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<TmpUnit> findNotSyncTmpUnit(int maxMaterNum);
	
	/**
	 * updateTmpUnit: 更新临时物料mcId、mcVersionId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateTmpUnit(Long id, Long mcId, Integer mcVersionId, Integer userId);
	
	/**
	 * updateTmpUnit: 更新临时物料的同步标记字段，仅当已chaTime相同时进行
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateTmpUnitSyncFlag(Long id, Date chaTime, Integer userId);

}
