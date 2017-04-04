package com.baidu.beidou.unionsite.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.unionsite.service.WM123SiteScoreService;

@ContextConfiguration(locations = { "/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "xdbTransactionManager")
public class WM123SiteScoreServiceImplTest extends
		AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	public void setDataSource(
			@Qualifier("xdbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	// 测试需要有必要的原始文件，最终产出site_score文件，可用来比对写入数据库的内容是否正确
	@Test
	public void testRefreshScoreService() {
		WM123SiteScoreService service = (WM123SiteScoreService) this.applicationContext
				.getBean("wm123SiteScoreService");
		try {
			service.refreshSiteScore();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//测试WM123SiteScoreServiceImpl中的私有方法
	@Test
	public void testGetScoreImpact() throws Exception
	{
		WM123SiteScoreServiceImpl serviceImpl = (WM123SiteScoreServiceImpl) this.applicationContext
				.getBean("wm123SiteScoreService");
		Method m = serviceImpl.getClass().getDeclaredMethod("getScoreImpact", new Class[]{long.class,float.class});
		m.setAccessible(true);
		Object result = m.invoke(serviceImpl, new Object[]{(long)1,(float)1.1});
		m.setAccessible(false);
		Assert.assertEquals(Float.parseFloat(result.toString()), (float)2.5);
	}
}