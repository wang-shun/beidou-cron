/**
 * beidou-cron-trunk#com.baidu.beidou.auditmanager.dao.impl.UrlJumpCheckDaoImplTest.java
 * 上午1:39:12 created by kanghongwei
 */
package com.baidu.beidou.auditmanager.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.base.BaseMultiDataSourceTest;

/**
 * 
 * @author kanghongwei
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UrlJumpCheckDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private UrlJumpCheckDaoImpl urlJumpCheckDaoImpl;

	@Test
	@NotTransactional
	public void testFindUrlUnitById() {
		int userId = 8;
		Long unitId = Long.MAX_VALUE;
		UrlUnit result = urlJumpCheckDaoImpl.findUrlUnitById(userId, unitId);
		Assert.assertNull(result);
	}

	@Test
	@NotTransactional
	public void testFindValidUrlList() {
		int tableIndex = 1;
		List<UrlCheckUnit> result = urlJumpCheckDaoImpl.findValidUrlList(tableIndex);
		Assert.assertThat(result.size(), org.hamcrest.Matchers.greaterThan(0));
	}
}
