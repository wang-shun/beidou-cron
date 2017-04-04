package com.baidu.beidou.user.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.salemanager.vo.SalerCustInfo;
import com.baidu.beidou.user.bo.User;

/**
 * @author hanxu03
 *
 */
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UserDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private UserDaoImpl userDaoImpl;
	
	@Test
	public void testFindUserBySFId(){
		int userId = 5;
		User user = userDaoImpl.findUserBySFId(userId);
		Assert.assertNotNull(user);
	}
	
	@Test
	public void testFindUsersBySFIds(){
		List<Integer> userId = new ArrayList<Integer>();
		userId.add(5);
		userId.add(18);
		userId.add(480787);
		Map<Integer, User> map = userDaoImpl.findUsersBySFIds(userId);
		Assert.assertEquals(map.size(), 2);
	}
	
	@Test
	@NotTransactional
	public void testCountAllUser(){
		Long cnt = userDaoImpl.countAllUser();
		Assert.assertThat(cnt.intValue(), org.hamcrest.Matchers.greaterThan(0));
//		Assert.assertEquals(cnt.longValue(), 1502331);
	}
	
	@Test
	@NotTransactional
	public void testFindAllCustInfo(){
		int[] excludeUstate = new int[] { 9 };
		int[] excludeShifenState = new int[] { 0,1,2,3,5,7 };
		Map<Integer, SalerCustInfo> map = userDaoImpl.findAllCustInfo(excludeUstate, excludeShifenState);
		Assert.assertThat(map.size(), org.hamcrest.Matchers.greaterThan(0));
//		Assert.assertEquals(map.size(), 178321);
	}
	
	@Test
	@NotTransactional
	public void testFindUserIdBySFState(){
		List<Integer> sfstateList = new ArrayList<Integer>();
		sfstateList.add(6);
		List<Integer> list = userDaoImpl.findUserIdBySFState(sfstateList);
		Assert.assertThat(list.size(),  org.hamcrest.Matchers.greaterThan(3));
//		Assert.assertEquals(list.size(), 28519);
	}
}
