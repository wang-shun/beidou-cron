package com.baidu.beidou.account.service;

import java.util.Date;

public interface UserFundService {

	public void importClkData() throws Exception;
	public void createDailyLogTable(Date date) throws Exception;
	public void dropDailyLogTable(Date date) throws Exception;
	
	/**
	 * 每日定时转账
	 * @version 1.2.22
	 */
	public void moveFundPerDay();
	
	/**
	 * 每日定时转账后补充转账
	 * @param inputFileName 需要重新执行转账的userid及fund，两列用\t分隔
	 * @param outputFileName 转账的结果日志
	 * @version 1.1.61
	 */
	public void retryMoveFundPerDay(String inputFileName) throws Exception;
	
	/**
	 * 根据账户余额进行自动转账
	 * 下午03:20:10
	 */
	public void autoTransferFundPerMargin();
	public void  sendTransferResultMailAndSms(String resultFileName);
	
	/**
	 * reSendLimitedTransferReusltSmsMessage:补发送转账结果的短信提醒
	*/
	public void reSendLimitedTransferReusltSmsMessage();
	
	/*
	 * 根据传递的userid文件，查找userid对应的财务中心余额
	 * author zhangpingan
	 * inputFileName:用户id文件
	 * outputFileName：用户id及其余额(制表符分隔)
	 */
	public void retrieveBlanceByUserIds(String inputFileName, String outputFileName) throws Exception;
	
	/**
	 *修复用户转账数据
	 * @param inputFileName 需要重新执行转账的userid、fund（单位：分）、方向（0：凤巢->北斗, 1：北斗->凤巢），3列用\t分隔
	 * @author zhangpingan
	 * @param outputFileName 转账的结果日志
	 * @version 1.1.82
	 */
	public void recoveryUserFundTransfer(String inputFileName) throws Exception;
}
