/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.impl.CproPlanStatDaoImplTest.java
 * 上午11:27:04 created by kanghongwei
 */
package com.baidu.beidou.aot.dao.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.aot.bo.PlanAotInfo;

/**
 * 
 * @author kanghongwei
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class CproPlanStatDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Autowired
	private CproPlanStatDaoImpl cproPlanStatDaoImpl;
	
	@Autowired
	private CproPlanOfflineStatDaoImpl cproPlanOfflineStatDaoImpl;

	@Test
	public void testFindAllPlanInfo() {
		int weekday = Calendar.TUESDAY;
		List<PlanAotInfo> result = cproPlanStatDaoImpl.findAllPlanInfo(weekday);
		Assert.assertNotNull(result);
	}

	@Test
	@Rollback(false)
	public void testFindLastOfftimeByDate() {
		int planId = 153036;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -4);
		Date from = calendar.getTime();
		Date to = new Date();
		Integer userId = 1003109;
		Date offTime = cproPlanOfflineStatDaoImpl.findLastOfftimeByDate(planId, from, to, userId);
		Assert.assertNotNull(offTime);
	}
}
