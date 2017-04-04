/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.GroupIpFilterDaoImplTest.java
 * 下午7:58:59 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprogroup.bo.GroupIpFilter;

/**
 * 
 * @author kanghongwei
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class GroupIpFilterDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private GroupIpFilterDaoImpl groupIpFilterDaoImpl;

	@Test
	public void testFindByGroupId() {
		Integer groupId = Integer.MAX_VALUE;
		int userId = 3;
		List<GroupIpFilter> result = groupIpFilterDaoImpl.findByGroupId(groupId, userId);
		Assert.assertEquals(result.size(), 0);
	}

}
