package com.baidu.beidou.auditmanager.service;

import java.util.List;

public interface ResultSetCallBack<T> {

	/**
	 * 得到db查询结果之后的处理
	 * @return
	 */
	int dealWithResultSet(List<T> resultList);
	
}
