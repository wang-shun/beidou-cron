/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.CproGroupDaoImplTest.java
 * 下午11:48:15 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.bo.CproGroupMoreInfo;

/**
 * 
 * @author kanghongwei
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class CproGroupDaoOnMultiDataSourceImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private CproGroupDaoOnMultiDataSourceImpl cproGroupDaoOnMultiDataSourceImpl;

	@Test
	public void testFindGroupInfobyUserId() {
		Integer userId = Integer.MAX_VALUE;
		List<CproGroup> result = cproGroupDaoOnMultiDataSourceImpl.findGroupInfobyUserId(userId);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	public void testFindEffectGroupInfoMorebyPlanId() {
		Integer planId = Integer.MAX_VALUE;
		int userId = 480788;
		List<CproGroupMoreInfo> result = cproGroupDaoOnMultiDataSourceImpl.findEffectGroupInfoMorebyPlanId(planId, userId);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	@NotTransactional
	public void testGetAllCproGroupIds() {
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.getAllCproGroupIds();
		Assert.assertThat(result.size(), org.hamcrest.Matchers.greaterThan(0));
	}

	@Test
	@NotTransactional
	public void testfindGroupInfoByGroupId() {
		int groupId = Integer.MAX_VALUE;
		CproGroup cproGroup = cproGroupDaoOnMultiDataSourceImpl.findGroupInfoByGroupId(groupId);
		Assert.assertNull(cproGroup);
	}

	@Test
	public void testFindGroupNameByGroupId() {
		Integer groupId = Integer.MAX_VALUE;
		int userId = 3;
		String groupName = cproGroupDaoOnMultiDataSourceImpl.findGroupNameByGroupId(groupId, userId);
		Assert.assertNull(groupName);
	}

	@Test
	@NotTransactional
	public void testFilterGroupByAllSite() {
		List<Integer> groupIds = new ArrayList<Integer>();
		groupIds.add(Integer.MAX_VALUE);
		groupIds.add(Integer.MAX_VALUE - 1);
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.filterGroupByAllSite(groupIds);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	@NotTransactional
	public void testCountAllGroupIdofEffPlan() {
		int slice = 1;
		long count = cproGroupDaoOnMultiDataSourceImpl.countAllGroupIdofEffPlan(slice);
		Assert.assertThat((int) count, org.hamcrest.Matchers.greaterThan(0));
	}

	@Test
	@NotTransactional
	public void testFindAllGroupIdofEffPlan() {
		int currPage = 0;
		int pageSize = 3;
		int sharding = 1;
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.findAllGroupIdofEffPlan(sharding, currPage, pageSize);
		Assert.assertEquals(result.size(), pageSize);
	}

	@Test
	@NotTransactional
	public void testFindGroupByRegId() {
		int regId = Integer.MAX_VALUE;
		List<Map<String, Object>> result = cproGroupDaoOnMultiDataSourceImpl.findGroupByRegId(regId);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	public void testFindGroupIdsByUserId() {
		int userId = 3;
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.findGroupIdsByUserId(userId);
		Assert.assertThat(result.size(), org.hamcrest.Matchers.greaterThan(0));
	}

	@Test
	@NotTransactional
	public void testFindUserIdByGroupIds() {
		List<Integer> groupIds = new ArrayList<Integer>();
		groupIds.add(Integer.MAX_VALUE);
		groupIds.add(Integer.MAX_VALUE - 1);
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.findUserIdByGroupIds(groupIds);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	public void testFindGroupIdsByUserIdAndTargettype() {
		int userId = 3;
		int targetType = Integer.MAX_VALUE;
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.findGroupIdsByUserIdAndTargettype(userId, targetType);
		System.out.println(result.size());
	}

	@Test
	public void testGetRegionList() {
		int groupId = Integer.MAX_VALUE;
		int userId = 3;
		List<Integer> result = cproGroupDaoOnMultiDataSourceImpl.getRegionList(groupId, userId);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	public void testGetIsallregionTag() {
		int groupId = Integer.MAX_VALUE;
		int userId = 3;
		int result = cproGroupDaoOnMultiDataSourceImpl.getIsallregionTag(groupId, userId);
		Assert.assertEquals(result, 1);
	}
}
