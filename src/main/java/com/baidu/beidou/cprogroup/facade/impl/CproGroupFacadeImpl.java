/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.facade.impl.CproGroupFacadeImpl.java
 * 下午6:47:02 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.facade.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.constant.WhiteType;
import com.baidu.beidou.cprogroup.facade.CproGroupFacade;
import com.baidu.beidou.cprogroup.service.CproGroupMgr;
import com.baidu.beidou.cprogroup.service.CproGroupMgrOnMultiDataSource;
import com.baidu.beidou.util.ThreadContext;

/**
 * 
 * @author kanghongwei
 */

public class CproGroupFacadeImpl implements CproGroupFacade {

	private static final Log log = LogFactory.getLog(CproGroupFacadeImpl.class);

	private CproGroupMgr cproGroupMgr;

	private CproGroupMgrOnMultiDataSource cproGroupMgrOnMultiDataSource;

	public void checkRepeateGroup() {
		cproGroupMgrOnMultiDataSource.checkRepeateGroup();
	}

	public void updateUserWhiteList(List<Integer> newUserList) {
		List<Integer> toResetUserList = cproGroupMgr.getNeedRestUserList(newUserList);
		cproGroupMgr.updateWhiteUsers(newUserList);

		List<Integer> baiduCommonTradeList = new ArrayList<Integer>();
		if (WhiteType.BAIDU_TRADE_COMMON_LIST != null) {
			baiduCommonTradeList.addAll(WhiteType.BAIDU_TRADE_COMMON_LIST);
		}
		List<Integer> whiteSites = cproGroupMgr.getBaiduWhiteSites(baiduCommonTradeList);
		List<Integer> whiteTrades = cproGroupMgr.getBaiduWhiteTrades(baiduCommonTradeList);

		for (Integer userId : toResetUserList) {
			ThreadContext.putUserId(userId);
			try {
				cproGroupMgr.resetUserWhiteConfig(userId, whiteSites, whiteTrades);
			} catch (Exception e) {
				log.error("Failed to resetUserWhiteConfig for userid=[" + userId + "]: " + e.getMessage(), e);
				continue;
			}
		}
	}

	public void adjustSiteTradeSystem(Map<Integer, Integer> siteMapping, Set<Integer> newFirstTradeList, Set<Integer> oldFirstTradeList) {
		if (siteMapping == null || newFirstTradeList == null || oldFirstTradeList == null) {
			return;
		}
		log.info("adjustSiteTradeSystem - START");
		List<Integer> cproGroupIdList = cproGroupMgrOnMultiDataSource.getCproGroupIdList();
		for (Integer groupId : cproGroupIdList) {
			CproGroup cproGroup = cproGroupMgrOnMultiDataSource.findGroupInfoByGroupId(groupId);
			if (cproGroup == null || cproGroup.getSiteTradeList() == null || cproGroup.getSiteTradeList().trim().length() == 0) {
				continue;
			}
			ThreadContext.putUserId(cproGroup.getUserId());
			cproGroupMgr.adjustSiteTradeSystem(cproGroup, siteMapping, newFirstTradeList, oldFirstTradeList);
		}
		log.info("adjustSiteTradeSystem - END");
	}

	public void setCproGroupMgr(CproGroupMgr cproGroupMgr) {
		this.cproGroupMgr = cproGroupMgr;
	}

	public void setCproGroupMgrOnMultiDataSource(CproGroupMgrOnMultiDataSource cproGroupMgrOnMultiDataSource) {
		this.cproGroupMgrOnMultiDataSource = cproGroupMgrOnMultiDataSource;
	}

}
