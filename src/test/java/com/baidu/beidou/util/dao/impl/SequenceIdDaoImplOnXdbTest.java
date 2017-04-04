package com.baidu.beidou.util.dao.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.util.dao.SequenceIdDaoImplOnXdb;

/**
 * @author hanxu03
 *
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class SequenceIdDaoImplOnXdbTest extends BaseMultiDataSourceTest {
	
	@Autowired
	private SequenceIdDaoImplOnXdb sequenceIdDaoImplOnXdb;
	
	@Test
	public void testGetUnionSiteidTypeId(){
		Long nextId_1 = sequenceIdDaoImplOnXdb.getUnionSiteidTypeId();
		Long nextId_2 = sequenceIdDaoImplOnXdb.getUnionSiteidTypeId();
		Assert.assertEquals(nextId_1.longValue() + 1, nextId_2.longValue());
	}
}
