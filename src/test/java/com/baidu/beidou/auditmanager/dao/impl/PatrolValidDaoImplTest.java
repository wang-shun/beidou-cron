/**
 * beidou-cron-trunk#com.baidu.beidou.auditmanager.dao.impl.PatrolValidDaoImplTeset.java
 * 上午2:42:13 created by kanghongwei
 */
package com.baidu.beidou.auditmanager.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.base.BaseMultiDataSourceTest;

/**
 * 
 * @author kanghongwei
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class PatrolValidDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private PatrolValidDaoImpl patrolValidDaoImpl;

	@Test
	@NotTransactional
	public void testFindValidUnitList() {
		int tableIndex = 1;
		List<AkaAuditUnit> result = patrolValidDaoImpl.findValidUnitList(tableIndex);
		Assert.assertThat(result.size(), org.hamcrest.Matchers.greaterThan(0));
	}

	@Test
	public void testFindUnitById() {
		int userId = 8;
		Long unitId = Long.MAX_VALUE;
		Unit unit = patrolValidDaoImpl.findUnitById(userId, unitId);
		Assert.assertNull(unit);
	}
}
