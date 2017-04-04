package com.baidu.beidou.unionsite.dao.impl;

import java.util.List;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.unionsite.vo.UserSiteVO;

@ContextConfiguration(locations = { "/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class BDSiteStatDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private BDSiteStatOnAddbDaoImpl bdSiteStatOnAddbDaoImpl;

	@Autowired
	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;

	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Test
	public void testStatSiteUserVo() {
		List<UserSiteVO> list = bdSiteStatOnAddbDaoImpl.statSiteUserVo();
		Assert.assertNotNull(list);
	}

	// @Test
	public void testFindGroupInfobyPlanId() {
		CproGroup g = cproGroupDaoOnMultiDataSource.findGroupInfoByGroupId(1);
		System.out.println(g == null);
	}
}
