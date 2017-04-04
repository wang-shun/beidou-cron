package com.baidu.beidou.unionsite.dao.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.unionsite.vo.UserSiteVO;

/**
 * @author hanxu03
 * 
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class BDSiteStatOnAddbDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private BDSiteStatOnAddbDaoImpl bDSiteStatOnAddbDaoImpl;

	@Test
	@NotTransactional
	public void testGetAvailUserCount() {
		int cnt = bDSiteStatOnAddbDaoImpl.getAvailUserCount();
		Assert.assertThat(cnt, org.hamcrest.Matchers.greaterThan(0));
		System.out.println(cnt);
	}

	@Test
	@NotTransactional
	public void testStatSiteUserVo() {
		List<UserSiteVO> list = bDSiteStatOnAddbDaoImpl.statSiteUserVo();
		Assert.assertThat(list.size(), org.hamcrest.Matchers.greaterThan(0));
		System.out.println(list.size());
		for (UserSiteVO vo : list.subList(0, 5)) {
			System.out.println(vo.getUserId());
			System.out.println(vo.isallsite);
			System.out.println(vo.getSiteTradeList());
			System.out.println(vo.getSiteList());
		}
	}

}
