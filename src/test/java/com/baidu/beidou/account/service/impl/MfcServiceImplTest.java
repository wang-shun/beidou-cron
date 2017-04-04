package com.baidu.beidou.account.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.account.service.MfcService;

@ContextConfiguration(locations = { "/applicationContext.xml", "classpath:/com/baidu/beidou/account/applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class MfcServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Test
	public void testGetUserProductBalance() {
		List<Integer> userIds = new ArrayList<Integer>();
		for (int i = 1; i < 100; i++) {
			userIds.add(i);
		}

		List<Integer> products = new ArrayList<Integer>();
		// products.add(2);
		products.add(1);
		MfcService mfcService = (MfcService) this.applicationContext.getBean("mfcService");
		double[][] result = mfcService.getUserProductBalance(userIds, products, 0);
		userIds = new ArrayList<Integer>();
		for (int i = 1; i < 101; i++) {
			userIds.add(i);
		}
		result = mfcService.getUserProductBalance(userIds, products, 0);
		userIds = new ArrayList<Integer>();
		for (int i = 1; i < 102; i++) {
			userIds.add(i);
		}
		result = mfcService.getUserProductBalance(userIds, products, 0);
		userIds = new ArrayList<Integer>();
		for (int i = 1; i < 201; i++) {
			userIds.add(i);
		}
		result = mfcService.getUserProductBalance(userIds, products, 0);
		userIds = new ArrayList<Integer>();
		for (int i = 1; i < 202; i++) {
			userIds.add(i);
		}
		result = mfcService.getUserProductBalance(userIds, products, 0);
		System.out.println(result);
		System.out.println(result.length);

		for (int i = 0; i < result.length; i++) {
			System.out.println();
			for (int j = 0; j < result[i].length; j++) {
				System.out.print(result[i][j]);
				System.out.print("|");
			}
		}

		System.out.println();
	}

	@Test
	public void testGetUserProductInvest() {
		List<Integer> userIds = new ArrayList<Integer>();
		userIds.add(76);
		userIds.add(7);
		userIds.add(23);

		List<Integer> products = new ArrayList<Integer>();
		products.add(2);

		MfcService mfcService = (MfcService) applicationContext.getBean("mfcService");
		double[][] result = mfcService.getUserProductInvest(userIds, products, 0);

		System.out.println(result);
		System.out.println(result.length);

		for (int i = 0; i < result.length; i++) {
			System.out.println();
			for (int j = 0; j < result[i].length; j++) {
				System.out.print(result[i][j]);
				System.out.print("|");
			}
		}

		System.out.println();
	}

	@Test
	public void autoProductTransfer(){
		Integer userId = 5;
		List<Integer> userIds = new ArrayList<Integer>();
		userIds.add(userId);
		List<Integer> products = new ArrayList<Integer>();
		products.add(5);
		
		MfcService mfcService = (MfcService) applicationContext.getBean("mfcService");
		int res = mfcService.autoProductTransfer(userId, 4, 5, 2.2);
		System.out.println("==================="+res);
		double[][] resd = mfcService.getUserProductBalance(userIds, products, 0);
		System.out.println("+++++++++++++++++++"+resd[0][0]);
	}
	
}
