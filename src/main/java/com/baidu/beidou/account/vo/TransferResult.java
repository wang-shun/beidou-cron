package com.baidu.beidou.account.vo;

import java.util.Date;

import com.baidu.beidou.account.bo.UserRemind;

/**
 * ClassName:TransferResult Function: 用于方便表示一个转账结果
 * 
 * @author <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created 2010-8-30
 * @since Cpweb-166 提醒升级
 * @version $Id: Exp $
 */
public class TransferResult {
	/** 发生时间差 */
	private Date time;
	/** 用户ID */
	private int userId;
	/** 转账数 */
	private double fund;
	/** 方向 */
	private int direction = 0;
	/**
	 * 是否成功，0表示成功，为了扩展方便， 此处用int不用boolean，因此以后失败原因可能很多
	 */
	private int success = 0;
	
	private UserRemind userRemind;

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public double getFund() {
		return fund;
	}

	public void setFund(double fund) {
		this.fund = fund;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public UserRemind getUserRemind() {
		return userRemind;
	}

	public void setUserRemind(UserRemind userRemind) {
		this.userRemind = userRemind;
	}

}
