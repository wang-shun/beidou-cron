package com.baidu.beidou.account.dao;

public interface AutoTransferDAO {
	/**
	 * 设置成功标记位
	 * @param userId
	 * @param fund
	 */
	public void setSuccessFlag(Integer userId, Integer fund);
    
	/**
	 * 设置失败标记位
	 * @param userId
	 * @param fund
	 */
	public void setFailFlag(Integer userId, Integer fund) ;
}
