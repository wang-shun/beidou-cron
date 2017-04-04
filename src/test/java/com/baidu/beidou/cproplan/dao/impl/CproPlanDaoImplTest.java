package com.baidu.beidou.cproplan.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cproplan.bo.CproPlan;

/**
 * @author hanxu03
 * 
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class CproPlanDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private CproPlanDaoImpl cproPlanDaoImpl;

	@Test
	@NotTransactional
	public void testCountAllPlan() {
		Long cnt = cproPlanDaoImpl.countAllPlan();
		Assert.assertThat(cnt.intValue(), org.hamcrest.Matchers.greaterThan(0));
	}

	@Test
	@NotTransactional
	public void testFindPlanInfo() {
		List<CproPlan> result = cproPlanDaoImpl.findPlanInfo();
		Assert.assertNotNull(result);
	}

	@Test
	@NotTransactional
	public void testFindPlanInfoOrderbyPlanId() {
		List<CproPlan> result = cproPlanDaoImpl.findPlanInfoOrderbyPlanId();
		Assert.assertNotNull(result);
	}

}
