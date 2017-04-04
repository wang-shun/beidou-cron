/**
 * beidou-cron-trunk#com.baidu.beidou.base.BaseMultiDataSourceTest.java
 * 上午11:22:32 created by kanghongwei
 */
package com.baidu.beidou.base;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.baidu.beidou.util.ThreadContext;

/**
 * 
 * @author kanghongwei
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public abstract class BaseMultiDataSourceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@BeforeClass
	public static void setup() {
		ThreadContext.putUserId(1);
	}

	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}
}
