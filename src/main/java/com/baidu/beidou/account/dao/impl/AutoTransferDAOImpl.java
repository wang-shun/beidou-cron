package com.baidu.beidou.account.dao.impl;

import com.baidu.beidou.account.dao.AutoTransferDAO;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author zhangpingan
 * 
 */
public class AutoTransferDAOImpl extends GenericDaoImpl implements AutoTransferDAO {

	/**
	 * 设置成功标记位
	 * 
	 * @param userId
	 * @param fund
	 */
	public void setSuccessFlag(Integer userId, Integer fund) {
		String sql = "update beidouext.autotransfer set is_success=0 ,fund=?, rcv_time=now() where userid=?;";
		super.executeBySql(sql, new Object[] { fund, userId });
	}

	/**
	 * 设置失败标记位
	 * 
	 * @param userId
	 * @param fund
	 */
	public void setFailFlag(Integer userId, Integer fund) {
		String sql = "update beidouext.autotransfer set is_success=1 ,fund=?,  rcv_time=now() where userid=?;";
		super.executeBySql(sql, new Object[] { fund, userId });
	}
}
