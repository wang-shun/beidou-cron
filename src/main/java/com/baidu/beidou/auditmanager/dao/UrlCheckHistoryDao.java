package com.baidu.beidou.auditmanager.dao;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnitForMail;

public interface UrlCheckHistoryDao {
	public void insertUrlCheckHistory(List<UrlUnit> urlUnitList, int type);
	
	public List<UrlUnitForMail> getUrlCheckHistory(Date startTime, 
			Date endTime, Integer type);
}
