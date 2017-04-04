package com.baidu.beidou.account.dao;

import java.util.Date;

public interface UserPerFundDAO{
	public void createLogTable(Date date) throws Exception;
	public void dropLogTable(Date date)throws Exception;
}
