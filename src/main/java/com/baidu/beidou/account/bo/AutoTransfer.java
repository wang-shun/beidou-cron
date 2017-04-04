package com.baidu.beidou.account.bo;

import java.util.Date;

/*
 * `userid` int(11) NOT NULL,
    `fund` int(10) default NULL COMMENT '转账数额 单位：分',
    `is_success` tinyint(3) NOT NULL COMMENT '自动转账结果，1：失败；0：成功',
    `rcv_time` datetime default NULL COMMENT '完成自动转账的时间',
     @author zhangpingan
 */
public class AutoTransfer {
	private Integer userid;
	private Integer fund;
	private Integer is_success;
	private Date rcvTime;
	
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public Integer getFund() {
		return fund;
	}
	public void setFund(Integer fund) {
		this.fund = fund;
	}
	public Integer getIs_success() {
		return is_success;
	}
	public void setIs_success(Integer is_success) {
		this.is_success = is_success;
	}
	public Date getRcvTime() {
		return rcvTime;
	}
	public void setRcvTime(Date rcvTime) {
		this.rcvTime = rcvTime;
	}

}
