package com.baidu.beidou.auditmanager.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnitForMail;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlResult;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

public interface UrlJumpCheckMgr {
	
	public long getStartPoint(String configFileName);
	
	public void setStartPoint(String configFileName, long startPoint);
	
	public UrlUnit instantUrlRefuse(long unitId, BmqUrlResult bmqUrlResult);
	
	public UrlUnit patrolUrlRefuse(long unitId, int userId, BmqUrlResult bmqUrlResult);
	
	public void insertUrlCheckHistory(UrlUnit urlUnit, int type);
	
	public void insertUrlCheckHistory(List<UrlUnit> urlUnitList, int type);

	public PartitionStrategy getStrategy();
	
	public void loadRefuseReasonMap();
	
	/**
	 * findValidUrlList: 轮巡满足条件的url，去除掉大客户
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-24
	 */
	public List<UrlCheckUnit> findValidUrlList(int tableIndex);
	
	public Map<Integer, List<UrlUnitForMail>> getUrlCheckHistory(Date startTime, 
			Date endTime, Integer type);
	
	public void sendMail(Integer userId, List<UrlUnitForMail> auditRecordList) throws Exception;
}
