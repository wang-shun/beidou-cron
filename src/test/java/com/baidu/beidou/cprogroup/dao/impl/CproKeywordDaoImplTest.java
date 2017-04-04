/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.CproKeywordDaoImplTest.java
 * 下午8:27:38 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprogroup.bo.CproKeyword;

/**
 * 
 * @author kanghongwei
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class CproKeywordDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private CproKeywordDaoImpl cproKeywordDaoImpl;

	@Test
	public void testGetCproKeywordsByGroup() {
		Integer groupId = Integer.MAX_VALUE;
		Integer userId = 3;
		@SuppressWarnings("deprecation")
		List<CproKeyword> result = cproKeywordDaoImpl.getCproKeywordsByGroup(groupId, userId);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	public void testFindByGroupIds() {
		List<Integer> groupIdList = new ArrayList<Integer>();
		groupIdList.add(Integer.MAX_VALUE);
		groupIdList.add(Integer.MAX_VALUE - 1);
		Integer userId = 3;
		List<CproKeyword> result = cproKeywordDaoImpl.findByGroupIds(groupIdList, userId);
		Assert.assertEquals(result.size(), 0);
	}

}
