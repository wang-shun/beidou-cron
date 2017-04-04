package com.baidu.beidou.unionsite.dao.impl;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;

/**
 * @author lvzichan
 * @since 2013-10-10
 */
@TransactionConfiguration(transactionManager = "capdbTransactionManager")
public class WM123SiteStatOnCapDaoImplTest extends BaseMultiDataSourceTest {

	@Autowired
	private WM123SiteStatOnCapDaoImpl wm123SiteStatOnCapDaoImpl;

	public void setDataSource(@Qualifier("capdbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}
	
	@Test
	@NotTransactional
	public void testGetFirstAdTradeMap() {
		Map<Integer, String> firstAdTradeMap = wm123SiteStatOnCapDaoImpl.getFirstAdTradeMap();
		for (Integer adTradeId : firstAdTradeMap.keySet()) {
			System.out.println(adTradeId + "," + firstAdTradeMap.get(adTradeId));
		}
		System.out.println(firstAdTradeMap.size());
	}
	
	@Test
	@NotTransactional
	public void testGetFirstSiteTradeMap() {
		Map<Integer, String> firstSiteTradeMap = wm123SiteStatOnCapDaoImpl.getFirstSiteTradeMap();
		for (Integer siteTradeId : firstSiteTradeMap.keySet()) {
			System.out.println(siteTradeId + "," + firstSiteTradeMap.get(siteTradeId));
		}
		System.out.println(firstSiteTradeMap.size());
	}
	
	@Test
	@NotTransactional
	public void testGetSiteTradeMap() {
		Map<Integer, Integer> second2firstSiteTradeMap = wm123SiteStatOnCapDaoImpl.getSecond2FirstSiteTradeMap();
		for (Integer siteTradeId : second2firstSiteTradeMap.keySet()) {
			System.out.println(siteTradeId + "," + second2firstSiteTradeMap.get(siteTradeId));
		}
		System.out.println(second2firstSiteTradeMap.size());
	}
}