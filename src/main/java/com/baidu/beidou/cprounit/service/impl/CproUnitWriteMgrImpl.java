package com.baidu.beidou.cprounit.service.impl;

import java.util.Date;

import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.dao.UnitWriteDao;
import com.baidu.beidou.cprounit.service.CproUnitWriteMgr;

/**
 * @author hanxu03
 *
 */
public class CproUnitWriteMgrImpl implements CproUnitWriteMgr {
	
	private UnitWriteDao unitWriteDao = null;
	
	public void modUnitInfo(Integer userId, Unit unit) {
		if (userId != null) {
			unitWriteDao.modUnitInfo(userId, unit);
		}
	}
	
	public void updateUnitInfo(Integer userId, Long unitId, String fileSrc, Long wid, Date chaTime, Integer state) {
		if (userId != null) {
			unitWriteDao.updateUnitInfo(userId, unitId, fileSrc, wid, chaTime, state);
		}
	}

	public void updateUnitForUbmcToDrmc(Integer userId, Long unitId, String fileSrc, Long wid, Date chaTime, Integer state) {
		unitWriteDao.updateUnitForUbmcToDrmc(userId, unitId, fileSrc, wid, chaTime, state);
	}
	
	public UnitWriteDao getUnitWriteDao() {
		return unitWriteDao;
	}

	public void setUnitWriteDao(UnitWriteDao unitWriteDao) {
		this.unitWriteDao = unitWriteDao;
	}
	
	
}
