/**
 * 
 */
package com.baidu.beidou.cprounit.service.impl;

import java.util.List;

import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.CproUnitMgr;
import com.baidu.beidou.stat.vo.AdLevelInfo;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class CproUnitMgrImpl implements CproUnitMgr {

	private UnitDao unitDao = null;

	public AdLevelInfo findUnitByUserId(Integer userId, Long unitid) {
		if (userId == null) {
			return null;
		}
		AdLevelInfo unit = unitDao.findById(userId, unitid);
		unit.setUserId(userId);
		unit.setUnitId(unitid);

		return unit;
	}

	/**
	 * 获得一个推广组下非删除状态的推广单元
	 * 
	 * @param groupId
	 * @param partid
	 *            userID
	 * @return 下午03:16:24
	 */
	public List<CproUnit> findUnDeletedUnitbyGroupId(final Integer groupId, final int userId) {
		return unitDao.findUnDeletedUnitbyGroupId(groupId, userId);
	}

	public Unit findUnitById(Integer userId, Long unitid) {
		if (userId == null) {
			return null;
		}
		return unitDao.findUnitById(userId, unitid);
	}

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}

}
