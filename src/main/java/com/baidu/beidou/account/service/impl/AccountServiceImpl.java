package com.baidu.beidou.account.service.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import com.baidu.beidou.account.dao.SfClickDAO;
import com.baidu.beidou.account.service.AccountService;

/**
 * @author zhuqian
 *
 */
public class AccountServiceImpl implements AccountService {
	
	private static final Log LOG = LogFactory.getLog(AccountServiceImpl.class);
	
	private SfClickDAO sfClickDao;
	
	/**
	 * @return the sfClickDao
	 */
	public SfClickDAO getSfClickDao() {
		return sfClickDao;
	}

	/**
	 * @param sfClickDao the sfClickDao to set
	 */
	public void setSfClickDao(SfClickDAO sfClickDao) {
		this.sfClickDao = sfClickDao;
	}


	/*
	 * (non-Javadoc)
	 * @see com.baidu.beidou.account.service.AccountService#getTotalExpenseByUserDate(int, java.util.Date)
	 */
	public double getTotalExpenseByUserDate(final int userid, final Date targetDate) {
		try{
			double expense = sfClickDao.getTotalExpenseByUserDate(userid, targetDate);
			return new Long(Math.round(expense * 100)).intValue();
		}catch (BadSqlGrammarException e){
			//如果clk表不存在，说明计费端没有当日的任何消费记录，可以直接返回
			LOG.warn("getTodaysExpenseByUser(): " + e.getCause().getMessage() + ", return 0 for user["+userid+"]");
			return 0;
		}
	}


	
	
	
}
