package com.baidu.beidou.unionsite.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.unionsite.service.WM123SiteCprodataService;

@ContextConfiguration(locations = { "/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "xdbTransactionManager")
public class WM123SiteCprodataServiceImplTest extends
		AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	public void setDataSource(
			@Qualifier("xdbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Autowired
	WM123SiteCprodataService service;
	
	// 测试需要有必要的原始文件，最终产出tqv_domain_savetodb文件，可用来比对写入数据库的内容是否正确
	@Test
	public void testSaveCprodata() {
		try {
			String domainCprodataFilePath = "src\\test\\java\\com\\baidu\\beidou\\unionsite\\service\\impl\\src_20131009";
			String saveToDbFilePath = "src\\test\\java\\com\\baidu\\beidou\\unionsite\\service\\impl\\dest_20131009";
			service.saveSiteCprodata(domainCprodataFilePath, saveToDbFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}