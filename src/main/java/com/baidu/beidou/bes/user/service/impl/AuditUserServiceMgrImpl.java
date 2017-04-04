package com.baidu.beidou.bes.user.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.bes.user.dao.AuditUserDao;
import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;

public class AuditUserServiceMgrImpl implements AuditUserServiceMgr{
	private int maxCount = 100;
	
	private int insertDbInterval = 500;
	private AuditUserDao auditUserDao;
	@Override
	public List<AuditUserInfo> getAuditUserList(Integer company) {
		if (company == null) {
			return new ArrayList<AuditUserInfo>(0);
		}
		return auditUserDao.getAuditUserList(company);
	}


	@Override
	public void updateAuditStatus(List<AuditUserInfo> users,Integer company, Integer auditStatus) {
		if (CollectionUtils.isEmpty(users) || company == null || auditStatus == null) {
			return ;
		}
		auditUserDao.updateAuditStatus(users, company, auditStatus);
	}
	


	@Override
	public List<AuditUserInfo> getHasPushedUser(Integer company) {
		if (company == null) {
			return new ArrayList<AuditUserInfo>(0);
		}
		return auditUserDao.getHasPushedUser(company);
	}

	public AuditUserDao getAuditUserDao() {
		return auditUserDao;
	}

	public void setAuditUserDao(AuditUserDao auditUserDao) {
		this.auditUserDao = auditUserDao;
	}

	
	
	@Override
	public void insertAuditUser(List<AuditUserInfo> users) {
		if (CollectionUtils.isEmpty(users)) {
			return ;
		}
		int i = 0;
		List<AuditUserInfo> patchList = new ArrayList<AuditUserInfo>(maxCount);
		for (AuditUserInfo info : users) {
			patchList.add(info);
			i++;
			if (i < maxCount) {
				continue;
			} else {
				auditUserDao.insertAuditUser(patchList);
				i = 0;
				patchList.clear();
			} 
			try {
				TimeUnit.MILLISECONDS.sleep(insertDbInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (i != 0) {
			auditUserDao.insertAuditUser(patchList);
		}
		
	}

	@Override
	public void updateAuditUser(List<AuditUserInfo> users) {
		if (CollectionUtils.isEmpty(users)) {
			return ;
		}
		auditUserDao.updateAuditUser(users);
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public int getInsertDbInterval() {
		return insertDbInterval;
	}

	public void setInsertDbInterval(int insertDbInterval) {
		this.insertDbInterval = insertDbInterval;
	}

	@Override
	public void updateAuditStatus(Map<Integer, List<Integer>> failMap,Integer company, int auditStatus) {
		if (failMap == null || failMap.size() == 0
				|| company == null) {
			return ;
		}
		
		auditUserDao.updateAuditStatus(failMap, company, auditStatus);
	}

	@Override
	public void updateAuditPass(List<Integer> userIds,Integer company, Integer auditStatus) {
		if (CollectionUtils.isEmpty(userIds)
				|| company == null) {
			return ;
		}
		auditUserDao.updateAuditPass(userIds,company, auditStatus);
	}

	@Override
	public void updateAuditUnPass(Map<Integer, String> map, Integer company, Integer auditStatus) {
		if (map == null || map.size() == 0
				|| company == null) {
			return ;
		}
		auditUserDao.updateAuditUnPass(map,company, auditStatus);
	}
	
	
}
