package com.baidu.beidou.cprounit.dao.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprounit.bo.Unit;

/**
 * @author hanxu03
 *
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UnitWriteDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private UnitWriteDaoImpl unitWriteDaoImpl;
	
	@Autowired
	private UnitDaoImpl unitDaoImpl;
	
	@Test
	@NotTransactional
	public void testModUnitInfo(){
		int userId = 848896;
		Unit unit = unitDaoImpl.findUnitById(848896, 11893L);
		
		unit.getMaterial().setWidth(100);
		unitWriteDaoImpl.modUnitInfo(userId, unit);
		
		unit = unitDaoImpl.findUnitById(848896, 11893L);
		Assert.assertEquals(unit.getMaterial().getWidth().intValue(), 100);
	}
}
