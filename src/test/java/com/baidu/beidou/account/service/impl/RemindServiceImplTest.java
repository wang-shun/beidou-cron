package com.baidu.beidou.account.service.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.account.bo.UserRemind;
import com.baidu.beidou.account.service.RemindService;
import com.baidu.beidou.account.vo.TransferResult;

@ContextConfiguration(locations = { "/applicationContext.xml", "classpath:/com/baidu/beidou/account/applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" })
@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class RemindServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	public static final String email = "liangshimu@baidu.com";
	public static final String mobile = "15210198954";

	@Autowired
	public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Test
	public void testMessageReplace() {
		RemindServiceImpl rsi = new RemindServiceImpl();
		String ori = rsi.getSuccessSmsMessage();
		Map<String, String> replacer = new LinkedHashMap<String, String>();
		replacer.put("linkman", "小木全");
		replacer.put("username", "victor");
		replacer.put("balance", "123");
		String message = rsi.messageReplace(ori, replacer);
		System.out.println(message);
	}

	@Test
	public void testGetUserRemindByType() {
		RemindService service = (RemindService) this.applicationContext.getBean("remindService");
		Assert.assertEquals(1468, service.findAllTransferUserRemind().size());

	}

	@Test
	public void testSendSuccessMessage() throws Exception {
		RemindService service = (RemindService) this.applicationContext.getBean("remindService");
		// | 4382 | 3 | 2 | liangshimu@baidu.com | |
		// | 4383 | 5 | 2 | liangshimu@baidu.com | 15210198954 |
		// | 4384 | 7 | 2 | | 15210198954 |
		UserRemind userRemind = new UserRemind();
		userRemind.setEmail(email);
		userRemind.setRemindId(4382);
		userRemind.setUserId(3);
		userRemind.setRemindType(2);
		TransferResult vo = new TransferResult();
		vo.setFund(1000.1);
		vo.setTime(new Date());
		service.sendSuccessMessage(userRemind, vo);
		userRemind.setRemindId(4383);
		userRemind.setUserId(5);
		userRemind.setMobile(mobile);
		service.sendFailMessage(userRemind, vo);
		userRemind.setUserId(7);
		userRemind.setEmail("");
		service.sendFailMessage(userRemind, vo);

	}

}
