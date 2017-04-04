package com.baidu.beidou.account.dao;

import java.util.List;

import com.baidu.beidou.account.bo.RemindType;
import com.baidu.beidou.util.dao.GenericDao;

public interface RemindTypeDAO {
	public List<RemindType> findAllType();
}
