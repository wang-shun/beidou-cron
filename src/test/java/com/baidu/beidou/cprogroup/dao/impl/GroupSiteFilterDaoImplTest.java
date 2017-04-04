/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.GroupSiteFilterDaoImplTest.java
 * 下午4:41:35 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprogroup.bo.GroupSiteFilter;

/**
 * 
 * @author kanghongwei
 */

@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class GroupSiteFilterDaoImplTest extends BaseMultiDataSourceTest {
	@Autowired
	private GroupSiteFilterDaoImpl groupSiteFilterDaoImpl;

	@Test
	@NotTransactional
	public void testFindByGroupId() {
		Integer groupId = Integer.MAX_VALUE;
		int userId = 480788;
		List<GroupSiteFilter> result = groupSiteFilterDaoImpl.findByGroupId(groupId, userId);
		Assert.assertEquals(result.size(), 0);
	}
}
