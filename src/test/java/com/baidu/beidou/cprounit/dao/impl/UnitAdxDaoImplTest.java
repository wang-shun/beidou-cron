/**
 * beidou-cron-640#com.baidu.beidou.cprounit.dao.impl.UnitAdxDaoImplTest.java
 * 下午5:51:02 created by kanghongwei
 */
package com.baidu.beidou.cprounit.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxDaoImplTest.java
 * @dateTime 2013-10-16 下午5:51:02
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UnitAdxDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private UnitAdxDaoImpl unitAdxDao;

	@Test
	public void testGetGoogleAdxSnapshotUnitList() {
		String updateDate = "2007-10-11";
		List<UnitAdxSnapshotVo> result = unitAdxDao.getGoogleAdxSnapshotUnitList(updateDate);
		Assert.assertNotNull(result);
	}

	@Test
	public void testGetGoogleAdxApiUnitList() {
		String updateDate = "2017-10-11";
		List<UnitAdxGoogleApiVo> result = unitAdxDao.getGoogleAdxApiUnitList(updateDate);
		System.out.println("-----" + result.size());
	}

	@Test
	public void testGetAdUserRelation() {
		Map<Long, Integer> result = unitAdxDao.getAdUserRelation();
		System.out.println(result.size());
		for (long adid : result.keySet()) {
			System.out.println(adid + " ," + result.get(adid));
		}

	}

	@Test
	@Rollback(false)
	public void testUpdateGoogleAdxSnapshotState() {
		int userId = 585555;
		List<Long> adIdList = new ArrayList<Long>();
		adIdList.add(22187L);
		adIdList.add(14697L);

		int snapshotState = 7;

		unitAdxDao.updateGoogleAdxSnapshotState(userId, adIdList, snapshotState);
	}

	@Test
	@Rollback(false)
	public void testUpdateGoogleAdxAPiState() {
		int userId = 697344;
		List<Long> adIdList = new ArrayList<Long>();
		adIdList.add(11403L);

		int googleAuditState = 2;
		unitAdxDao.updateGoogleAdxAPiState(userId, adIdList, googleAuditState);
	}

}
