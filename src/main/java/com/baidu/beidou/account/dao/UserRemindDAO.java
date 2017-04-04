package com.baidu.beidou.account.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.account.bo.UserRemind;
import com.baidu.beidou.account.constant.RemindConstant;

public interface UserRemindDAO {
	public List<UserRemind> findRemindRecByUser(Integer userId);
	/**
	 * findRemindRecByType:根据提醒类型查找提醒记录
	 *
	 * @param type 提醒类型，当前一般1、2
	 * @return      提醒列表
	 * @since Cpweb-166 提醒升级 
	 * @see RemindConstant#REMIND_TYPE_ACCOUNT
	 * @see RemindConstant#REMIND_TYPE_TRANSFER
	*/
	public Map<Integer, UserRemind> findRemindRecByType(Integer type);
	/**
	 * findRemindRecByUserAndType:根据userId和type查找对应的提醒记录
	 *
	 * @param userId 用户ID，非空
	 * @param type 提醒类型，当前一般1、2
	 * @return      提醒列表
	 * @since Cpweb-166 提醒升级 
	 * @see RemindConstant#REMIND_TYPE_ACCOUNT
	 * @see RemindConstant#REMIND_TYPE_TRANSFER
	*/
	public List<UserRemind> findRemindRecByUserAndType(Integer userId, Integer type);
}
