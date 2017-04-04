/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.impl.CodeStatDaoImplTest.java
 * 下午5:24:34 created by kanghongwei
 */
package com.baidu.beidou.aot.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.aot.bo.GroupAotInfo;
import com.baidu.beidou.base.BaseMultiDataSourceTest;

/**
 * 
 * @author kanghongwei
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class CproGroupStatDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private CproGroupStatDaoImpl cproGroupStatDaoImpl;

	@Test
	@NotTransactional
	public void testFindGroupAotInfoByPage() {
		int curPage = 0;
		int pageSize = 10;
		List<GroupAotInfo> pagedList = cproGroupStatDaoImpl.findGroupAotInfoByPage(curPage, pageSize);
		Assert.assertEquals(pagedList.size(), pageSize);
	}

	@Test
	@NotTransactional
	public void testFindAllGroupAotInfoOnlyPrice() {
		List<GroupAotInfo> result = cproGroupStatDaoImpl.findAllGroupAotInfoOnlyPrice();
		Assert.assertNotNull(result);
	}

}
