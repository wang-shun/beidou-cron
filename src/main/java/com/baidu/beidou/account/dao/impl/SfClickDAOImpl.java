package com.baidu.beidou.account.dao.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.baidu.beidou.account.dao.SfClickDAO;
import com.baidu.beidou.stat.util.dao.BaseDAOSupport;

/**
 * @author zhuqian
 * 
 */
public class SfClickDAOImpl extends BaseDAOSupport implements SfClickDAO {

	public double getTodaysExpenseByUser(int userid) {
		return this.getTotalExpense(userid, new Date());
	}

	public double getTotalExpenseByUserDate(int userid, Date targetDate) {
		if (targetDate == null) {
			return 0;
		}
		return this.getTotalExpense(userid, targetDate);
	}

	private double getTotalExpense(int userid, Date targetDate) {

		String tableName = CLK_TABLE_NAME_BASE + CLK_TABLE_NAME_DF.format(targetDate);

		StringBuilder sql = new StringBuilder();
		sql.append(" select sum(bid) expense from ").append(tableName).append(" where userid = ? ");

		List result = super.findByCondition(sql.toString(), new Object[] { userid });
		if (!CollectionUtils.isEmpty(result)) {
			BigDecimal expense = (BigDecimal) ((Map) result.get(0)).get("expense");
			if (expense == null) {
				return 0;
			} else {
				return expense.doubleValue();
			}

		}

		return 0;
	}

}
