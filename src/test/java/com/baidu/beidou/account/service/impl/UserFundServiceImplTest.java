package com.baidu.beidou.account.service.impl;

import static org.junit.Assert.fail;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.account.service.UserFundService;

@ContextConfiguration(locations = { "/applicationContext.xml", "classpath:/com/baidu/beidou/account/applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UserFundServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	public static final Log log = LogFactory.getLog(UserFundServiceImplTest.class);

	@Autowired
	UserFundService userFundService;

	@Autowired
	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void testAutoTransferFundPerMargin() {
		fail("Not yet implemented");
	}

	@Test
	public void testReSendLimitedTransferReusltSmsMessage() {
		userFundService.reSendLimitedTransferReusltSmsMessage();
	}

	@Test
	public void testSendTransferResultMail() {
	}

	@Test
	public void testTransferResultMailAndSms() {
		userFundService.sendTransferResultMailAndSms("a");
	}
}
