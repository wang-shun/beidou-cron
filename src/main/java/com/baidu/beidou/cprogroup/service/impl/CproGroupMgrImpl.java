package com.baidu.beidou.cprogroup.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.dao.CproGroupDao;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprogroup.dao.CproGroupOnXdbDao;
import com.baidu.beidou.cprogroup.dao.WhiteListDao;
import com.baidu.beidou.cprogroup.service.CproGroupMgr;
import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zhangpeng
 * @version 1.0.0
 * 
 *          refactor by kanghongwei since 2012-10-30
 */
public class CproGroupMgrImpl implements CproGroupMgr {

	private static final Log log = LogFactory.getLog(CproGroupMgrImpl.class);

	private WhiteListDao whiteListDao;
	private CproGroupDao cproGroupDao;

	private CproGroupOnXdbDao cproGroupOnXdbDao;
	private BDSiteStatDao bdSiteStatDao;
	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;

	public void adjustSiteTradeSystem(CproGroup cproGroup, Map<Integer, Integer> siteMapping, Set<Integer> newFirstTradeList, Set<Integer> oldFirstTradeList) {
		String oldSiteTradeList = cproGroup.getSiteTradeList();
		Set<Integer> oldTradeList = splitIds(oldSiteTradeList);
		Set<Integer> newTradeList = new HashSet<Integer>();

		// 如果是所有的一级行业，则替换成新的一级行业
		if (oldTradeList.size() == oldFirstTradeList.size() && oldTradeList.containsAll(oldFirstTradeList)) {
			newTradeList.addAll(newFirstTradeList);
		}
		// 否则，通过mapping的方式替换
		else {
			for (Integer id : oldTradeList) {
				Integer newId = siteMapping.get(id);
				if (newId != null && newId > 0) {
					newTradeList.add(newId);
				}
			}
		}
		String newSiteTradeList = packageIds(newTradeList);
		cproGroupDao.updateGroupInfoSiteTradeList(cproGroup.getGroupId(), newSiteTradeList);
	}

	private Set<Integer> splitIds(String ids) {
		if (ids == null) {
			return new HashSet<Integer>(0);
		}

		String[] items = ids.split("\\|");

		Set<Integer> result = new HashSet<Integer>();
		for (String item : items) {
			try {
				Integer id = new Integer(item);
				result.add(id);
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		return result;
	}

	private String packageIds(Set<Integer> idSet) {
		if (idSet == null || idSet.size() == 0) {
			return null;
		}
		// 先对ID进行排序
		List<Integer> idList = Arrays.asList(idSet.toArray(new Integer[idSet.size()]));
		Collections.sort(idList);

		StringBuilder sb = new StringBuilder();
		for (Integer id : idList) {
			sb.append(id.toString()).append("|");
		}
		return sb.toString();
	}

	public void updateWhiteUsers(List<Integer> newUserList) {
		whiteListDao.updateWhiteUsers(newUserList);
	}

	public List<Integer> getBaiduWhiteSites(List<Integer> baiduCommonTradeList) {
		List<Integer> baiduCommonSiteList = bdSiteStatDao.findSiteIdByBaiduCommonTrade(baiduCommonTradeList);
		List<Integer> whiteSites = whiteListDao.findAllWhiteSites();
		whiteSites.removeAll(baiduCommonSiteList);
		return whiteSites;
	}

	public List<Integer> getBaiduWhiteTrades(List<Integer> baiduCommonTradeList) {
		List<Integer> whiteTrades = whiteListDao.findAllWhiteTrades();
		whiteTrades.removeAll(baiduCommonTradeList);
		return whiteTrades;
	}

	public List<Integer> getNeedRestUserList(List<Integer> newUserList) {
		List<Integer> toResetUserList = new ArrayList<Integer>(0);
		List<Integer> oldUserList = whiteListDao.findAllWhiteUsers();

		if (CollectionUtils.isEmpty(newUserList)) {
			toResetUserList = oldUserList;
		} else if (!CollectionUtils.isEmpty(oldUserList)) {
			oldUserList.removeAll(newUserList);
			toResetUserList.addAll(oldUserList);
		}
		return toResetUserList;
	}

	public void resetUserWhiteConfig(Integer userId, List<Integer> whiteSites, List<Integer> whiteTrades) {

		List<CproGroup> groupList = cproGroupDaoOnMultiDataSource.findGroupInfobyUserId(userId);
		if (CollectionUtils.isEmpty(groupList)) {
			return;
		}

		for (CproGroup group : groupList) {

			boolean changed = false;

			String oldObj = group.toString();
			String siteList = group.getSiteList();
			String siteTradeList = group.getSiteTradeList();

			List<Integer> siteIds = new ArrayList<Integer>();
			List<Integer> siteTradeIds = new ArrayList<Integer>();

			// 如果
			if (!StringUtils.isEmpty(siteList)) {
				siteIds = StringUtil.splitIntToList(siteList, "|");
				int size = siteIds.size();

				siteIds.removeAll(whiteSites);

				if (siteIds.size() < size) {
					changed = true;
					// 最后一位有分隔符
					group.setSiteList(StringUtil.join("|", siteIds) + "|");
				}
			}

			if (!StringUtils.isEmpty(siteTradeList)) {
				siteTradeIds = StringUtil.splitIntToList(siteTradeList, "|");
				int size = siteTradeIds.size();

				siteTradeIds.removeAll(whiteTrades);

				if (siteTradeIds.size() < size) {
					changed = true;
					// 最后一位有分隔符
					group.setSiteTradeList(StringUtil.join("|", siteTradeIds) + "|");
				}
			}
			if (changed) {
				try {
					// 更新网站、行业信息
					cproGroupDao.updateCproGroup(group);
					// 更新sitesum
					int newSiteSum = cproGroupOnXdbDao.calculateSiteSum(group.getGroupId(), siteIds, siteTradeIds);
					cproGroupDao.updateGroupSiteSum(group.getGroupId(), newSiteSum);

					log.info("System reset white config for user[" + userId + "]: oldObj=[" + oldObj + "], newObj=[" + group + ",sitesum=" + newSiteSum + "]");

				} catch (Exception e) {
					log.error("System failed to update for user[" + userId + "]: oldObj=[" + oldObj + "], failedObj=[" + group + "]");
				}
			}
		}
	}

	public void setWhiteListDao(WhiteListDao whiteListDao) {
		this.whiteListDao = whiteListDao;
	}

	public void setCproGroupDao(CproGroupDao cproGroupDao) {
		this.cproGroupDao = cproGroupDao;
	}

	public void setCproGroupOnXdbDao(CproGroupOnXdbDao cproGroupOnXdbDao) {
		this.cproGroupOnXdbDao = cproGroupOnXdbDao;
	}

	public void setBdSiteStatDao(BDSiteStatDao bdSiteStatDao) {
		this.bdSiteStatDao = bdSiteStatDao;
	}

	public void setCproGroupDaoOnMultiDataSource(CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource) {
		this.cproGroupDaoOnMultiDataSource = cproGroupDaoOnMultiDataSource;
	}

}