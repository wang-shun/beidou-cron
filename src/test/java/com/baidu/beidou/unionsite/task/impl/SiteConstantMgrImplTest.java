/**
 * 2009-4-22 上午01:38:13
 */
package com.baidu.beidou.unionsite.task.impl;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.unionsite.UnionSiteImporter;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.BDSiteStatOnAddbDao;
import com.baidu.beidou.unionsite.service.impl.SiteConstantMgrImpl;
import com.baidu.beidou.util.LogUtils;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
@ContextConfiguration(locations = { "/applicationContext.xml" })
@TransactionConfiguration(transactionManager="addbTransactionManager")
public class SiteConstantMgrImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final Log LOG = LogFactory.getLog(UnionSiteImporter.class);

	@Autowired
	private SiteConstantMgrImpl service = null;

	@Autowired
	private BDSiteStatOnAddbDao bdSiteStatOnAddbDao;

	@Autowired
	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	/**
	 * Test method for
	 * {@link com.baidu.beidou.unionsite.task.impl.SiteImportTaskImpl#importUnionSite()}
	 * .
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	// @Test
	public void testImportUnionSite() throws IOException, ClassNotFoundException {
		LogUtils.info(LOG, "start to import union site data....");
		service.loadConfFile();
		int i = SiteConstant.BLACK_SITE.size();
		System.out.println(i);
		LogUtils.info(LOG, "end to import union site data.");

	}

	@Test
	public void test() {
		service.loadConfFile();
		List<Integer> result = bdSiteStatOnAddbDao.findAllSiteUser();
		System.out.println(result.size());
	}

}
