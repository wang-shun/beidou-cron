package com.baidu.beidou.cprogroup.dao.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.cprogroup.dao.KrRecycleDao;

@ContextConfiguration(locations = { "/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "xdbTransactionManager")
public class KrRecycleDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	public void setDataSource(@Qualifier("xdbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Autowired
	private KrRecycleDao krRecycleDao;

	@Test
	public void testGetUserRecycleWordIds() {
		List<Long> result = krRecycleDao.getUserRecycleWordIds(289140);
		for (Long lr : result) {
			System.out.println(lr);
		}
		assertEquals(krRecycleDao.getUserRecycleWordIds(0).size(), 0);
	}
}
