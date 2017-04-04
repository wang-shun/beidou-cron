package com.baidu.beidou.account.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.account.bo.UserRemind;
import com.baidu.beidou.account.vo.TransferResult;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.vo.UserEmailInfo;

public interface RemindService {
	public List<UserRemind> findAllUserRemindById(Integer userId);
	
	/**
	 * findAllTransferUserRemind:查找所有转账提醒的设置
	 *
	 * @return      所有设置了转账提醒的记录
	 * @since Cpweb-166 账户提醒升级
	*/
	public Map<Integer, UserRemind> findAllTransferUserRemind();

	/**
	 * sendSuccessMessage:发成功信息
	*/
	void sendSuccessMessage(UserRemind userRemind, TransferResult result) throws Exception;
	/**
	 * sendFailMessage:发失败信息
	*/
	void sendFailMessage(UserRemind userRemind, TransferResult result) throws Exception;
	/**
	 * sendSuccessSmsMessage:发送成功短信
	 *
	 * @param user 用户
	 * @param ui 用户帐户信息
	 * @param balance 帐户余额
	 * @param mobile 手机
	 * @throws UnknownHostException
	 * @throws IOException      
	 * @since 
	*/
	public void sendSuccessSmsMessage(User user, UserEmailInfo ui, 
			Date date, String mobile) throws UnknownHostException, IOException;
	
	/**
	 * batchSendDailyLimitedSmsMessage:批量发送受限时间内的短信
	 * @param filePrefix文件前缀
	*/
	public void batchSendDailyLimitedSmsMessage(String filePrefix);
	

	/**
	 * batchSendHourlyLimitedSmsMessage:批量发送受限时间内的短信
	 * @param filePrefix文件前缀
	*/
	public void batchSendHourlyLimitedSmsMessage(String filePrefix);
}
