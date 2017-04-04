/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.impl.UnitAdxMgrOnReadImpl.java
 * 下午3:59:10 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;
import com.baidu.beidou.cprounit.dao.UnitAdxDao;
import com.baidu.beidou.cprounit.service.UnitAdxMgrOnRead;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxMgrOnReadImpl.java
 * @dateTime 2013-10-30 下午3:59:10
 */

public class UnitAdxMgrOnReadImpl implements UnitAdxMgrOnRead {

	private UnitAdxDao unitAdxDao;

	public List<UnitAdxSnapshotVo> getGoogleAdxSnapshotUnitList(String updateDate) {
		if (StringUtils.isEmpty(updateDate)) {
			return Collections.emptyList();
		}
		return unitAdxDao.getGoogleAdxSnapshotUnitList(updateDate);
	}

	public List<UnitAdxGoogleApiVo> getGoogleAdxApiUnitList(String updateDate) {
		if (StringUtils.isEmpty(updateDate)) {
			return Collections.emptyList();
		}
		return unitAdxDao.getGoogleAdxApiUnitList(updateDate);
	}

	public Map<Long, Integer> getAdUserRelation() {
		return unitAdxDao.getAdUserRelation();
	}

	public void setUnitAdxDao(UnitAdxDao unitAdxDao) {
		this.unitAdxDao = unitAdxDao;
	}

}
