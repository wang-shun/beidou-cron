package com.baidu.beidou.unionsite.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;

@TransactionConfiguration(transactionManager = "capdbTransactionManager")
public class WM123AdTrade2SiteTradeServiceImplTest extends BaseMultiDataSourceTest {

	@Autowired
	public void setDataSource(@Qualifier("capdbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Autowired
	WM123AdTrade2SiteTradeServiceImpl service;
	
	// 测试需要有必要的原始文件srcGroupFilePath，最终产出广告行业和网站行业的对应文件
	@Test
	public void testComputeAdTrade2SiteTrade() {
		try {
			String srcGroupFilePath = "src\\test\\java\\com\\baidu\\beidou\\unionsite\\service\\impl\\group_adtrade_sitetradelist.txt";
			String adTrade2SiteTradeFilePath = "src\\test\\java\\com\\baidu\\beidou\\unionsite\\service\\impl\\adtrade_sitetrade_final.txt";
			service.computeAdTrade2SiteTrade(srcGroupFilePath, adTrade2SiteTradeFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}