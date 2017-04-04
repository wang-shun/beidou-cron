/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.impl.UnitAdxMgrImpl.java
 * 下午4:33:42 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.cprounit.dao.UnitAdxDao;
import com.baidu.beidou.cprounit.service.UnitAdxMgr;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxMgrImpl.java
 * @dateTime 2013-10-20 下午4:33:42
 */

public class UnitAdxMgrImpl implements UnitAdxMgr {

	private UnitAdxDao unitAdxDao;

	public void updateGoogleAdxSnapshotState(int userId, List<Long> adIdList, int snapshotState) {
		if (CollectionUtils.isEmpty(adIdList)) {
			return;
		}
		unitAdxDao.updateGoogleAdxSnapshotState(userId, adIdList, snapshotState);
	}

	public void updateGoogleAdxAPiState(int userId, List<Long> adIdList, int googleAuditState) {
		if (CollectionUtils.isEmpty(adIdList)) {
			return;
		}
		unitAdxDao.updateGoogleAdxAPiState(userId, adIdList, googleAuditState);
	}

	public void setUnitAdxDao(UnitAdxDao unitAdxDao) {
		this.unitAdxDao = unitAdxDao;
	}

}
