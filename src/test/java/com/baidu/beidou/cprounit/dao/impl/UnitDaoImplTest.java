package com.baidu.beidou.cprounit.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.auditmanager.vo.DelMaterial;
import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.stat.vo.AdLevelInfo;

/**
 * @author hanxu03
 *
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UnitDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private UnitDaoImpl unitDaoImpl;
	
	@Ignore
	@Test
	public void testFindById(){
		int userId = 848896;
		Long id = 11060L;
		AdLevelInfo info = unitDaoImpl.findById(userId, id);
		Assert.assertNotNull(info);
//		Assert.assertEquals(info.getGroupId(), 192);
	}
	
	@Ignore
	@Test
	public void testFindUnDeletedUnitbyGroupId(){
		int groupId = 252;
		int userId = 697344;
		List<CproUnit> list = unitDaoImpl.findUnDeletedUnitbyGroupId(groupId, userId);
//		Assert.assertThat(list.size(), org.hamcrest.Matchers.greaterThan(0));
		Assert.assertEquals(list.size(), 1);
	}
	
	@Ignore
	@Test
	@NotTransactional
	public void testGetAllUnitIdsByGroupId(){
		List<Integer> groupIds = new ArrayList<Integer>();
		groupIds.add(7);
		groupIds.add(138);
		groupIds.add(297);
		List<Long> list = unitDaoImpl.getAllUnitIdsByGroupId(groupIds);
//		Assert.assertThat(list.size(), org.hamcrest.Matchers.greaterThan(0));	
		Assert.assertEquals(list.size(), 28);
	}
	
	@Ignore
	@Test
	public void testFindUnitById(){
		int userId = 848896;
		long id = 11893L;
		Unit unit = unitDaoImpl.findUnitById(userId, id);
		Assert.assertEquals(unit.getMaterial().getAdtradeid().intValue(), 2504);
	}
	
	@Test
	@Rollback(false)
	public void testDeleteMater() {
	    DelMaterial delMaterial = new DelMaterial();
	    delMaterial.setUserId(5);
	    delMaterial.setMcId(202360631027L);
	    delMaterial.setMcVersionId(1);
	    unitDaoImpl.deleteMater(delMaterial);
	}
}
