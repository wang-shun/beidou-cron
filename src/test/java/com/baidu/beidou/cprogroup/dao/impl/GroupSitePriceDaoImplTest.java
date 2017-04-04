/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.GroupSitePriceDaoImplTest.java
 * 下午1:37:55 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprogroup.bo.GroupSitePrice;

/**
 * 
 * @author kanghongwei
 */

@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class GroupSitePriceDaoImplTest extends BaseMultiDataSourceTest {
	@Autowired
	private GroupSitePriceDaoImpl groupSitePriceDao;

	@Test
	public void testFindByGroupId() {
		Integer groupId = Integer.MAX_VALUE;
		int userId = 480788;
		List<GroupSitePrice> result = groupSitePriceDao.findByGroupId(groupId, userId);
		Assert.assertEquals(result.size(), 0);
	}

}
