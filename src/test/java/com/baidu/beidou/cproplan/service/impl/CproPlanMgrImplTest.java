package com.baidu.beidou.cproplan.service.impl;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.cproplan.service.CproPlanMgr;

@ContextConfiguration(locations = { "/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class CproPlanMgrImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private CproPlanMgr planMgr;

	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Test
	public void testInnerName() {
		System.out.println("aa");
	}

	// @Test
	public void testCountAllPlan() {
		assertEquals(planMgr.countAllPlan(), Long.valueOf(489));
	}

}
