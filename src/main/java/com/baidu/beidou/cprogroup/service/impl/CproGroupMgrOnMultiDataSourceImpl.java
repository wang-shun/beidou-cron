/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.service.impl.CproGroupMgrOnMultiDataSourceImpl.java
 * 下午12:03:37 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cprogroup.bo.CproGroupMoreInfo;
import com.baidu.beidou.cprogroup.bo.CproKeyword;
import com.baidu.beidou.cprogroup.bo.GroupIpFilter;
import com.baidu.beidou.cprogroup.bo.GroupSiteFilter;
import com.baidu.beidou.cprogroup.bo.GroupSitePrice;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprogroup.dao.CproKeywordDao;
import com.baidu.beidou.cprogroup.dao.GroupIpFilterDao;
import com.baidu.beidou.cprogroup.dao.GroupSiteFilterDao;
import com.baidu.beidou.cprogroup.dao.GroupSitePriceDao;
import com.baidu.beidou.cprogroup.service.CproGroupMgrOnMultiDataSource;
import com.baidu.beidou.cprogroup.vo.RepeateGroupStatResult;
import com.baidu.beidou.cproplan.bo.CproPlan;
import com.baidu.beidou.cproplan.service.CproPlanMgr;
import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.CproUnitMgr;
import com.baidu.beidou.indexgrade.bo.SimpleGroup;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.freemarker.FreeMarkerTemplateHandler;
import com.baidu.beidou.util.freemarker.TemplateHandler;

/**
 * 
 * @author kanghongwei
 */

public class CproGroupMgrOnMultiDataSourceImpl implements CproGroupMgrOnMultiDataSource {

	private static final Log log = LogFactory.getLog(CproGroupMgrOnMultiDataSourceImpl.class);

	private static final String FILE_SEPERATOR = "|";
	private static final String SITEPRICE_FILE_SEPERATOR = "****";
	private static final String REPEATEGROUP_FILE_PRE = "repeate_group.dat.";

	private UserDao userDao;
	private CproKeywordDao cproKeywordDao;
	private GroupIpFilterDao groupIpFilterDao;
	private GroupSitePriceDao groupSitePriceDao;
	private GroupSiteFilterDao groupSiteFilterDao;

	private CproPlanMgr cproPlanMgr;
	private CproUnitMgr cproUnitMgr;

	// 导出重复推广组检查文件的存放位置
	private String repeateGroupOutputFilePath;
	private String repeateGroupOutputFileMailFrom;
	private String repeateGroupOutputFileMailTo;

	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;

	public void checkRepeateGroup() {
		log.info("begin task: checkRepeateGroup");

		BufferedWriter bw = null;

		Date yesterDay = DateUtils.getNextDay(new Date(), Long.valueOf(-1));

		String writeFileName = this.repeateGroupOutputFilePath + REPEATEGROUP_FILE_PRE + DateUtils.formatDate(yesterDay, "yyyyMMdd");

		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeFileName), "GBK"));

			List<RepeateGroupStatResult> stateResultList = new ArrayList<RepeateGroupStatResult>();

			List<List<CproGroupMoreInfo>> groupMoreInfoList = new ArrayList<List<CproGroupMoreInfo>>();

			StringBuilder builder = new StringBuilder();

			// 返回推广计划ID，推广计划名称，用户ID
			List<CproPlan> planInfoList = cproPlanMgr.findPlanInfoorderbyUserId();

			for (CproPlan planInfo : planInfoList) {

				// 清空容器
				stateResultList.clear();

				groupMoreInfoList = this.filterbyGroup(planInfo.getPlanId(), planInfo.getUserId());

				// 第一遍过滤结果为空，推出
				if (groupMoreInfoList.size() == 0) {
					continue;
				}

				// 获取用户信息
				User user = userDao.findUserBySFId(planInfo.getUserId());

				if (user == null) {
					continue;
				}

				Integer userId = user.getUserid();
				String userName = user.getUsername();
				// 根据主题词过滤
				groupMoreInfoList = this.filterbyKeyword(groupMoreInfoList, userId);
				if (groupMoreInfoList.size() == 0) {
					continue;
				}

				// 根据ip过滤设置
				groupMoreInfoList = this.filterbyIpFilter(groupMoreInfoList, planInfo.getUserId());
				if (groupMoreInfoList.size() == 0) {
					continue;
				}

				// 根据网站过滤设置
				groupMoreInfoList = this.filterbySiteFilter(groupMoreInfoList, planInfo.getUserId());
				if (groupMoreInfoList.size() == 0) {
					continue;
				}
				// 根据分网站价格过滤设置
				groupMoreInfoList = this.filterbySitePrice(groupMoreInfoList, planInfo.getUserId());
				if (groupMoreInfoList.size() == 0) {
					continue;
				}

				// 根据创意过滤设置
				groupMoreInfoList = this.filterbyUnitInfo(groupMoreInfoList, userId);
				if (groupMoreInfoList.size() == 0) {
					continue;
				}

				// 组织信息,写入文件
				for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoList) {
					RepeateGroupStatResult repeateGroupStatResult = new RepeateGroupStatResult();
					repeateGroupStatResult.setUserId(planInfo.getUserId());
					repeateGroupStatResult.setUserName(userName);
					repeateGroupStatResult.setPlanName(planInfo.getPlanName());
					repeateGroupStatResult.setRepeateGroupNum(subGroupMoreInfoLis.size());

					builder.setLength(0);
					for (CproGroupMoreInfo groupMoreInfo : subGroupMoreInfoLis) {
						builder.append(groupMoreInfo.getGroupName());
						builder.append(',');
					}
					if (builder.length() > 0) {
						builder.deleteCharAt(builder.length() - 1);
					}

					repeateGroupStatResult.setRepeateGroupNames(builder.toString());

					stateResultList.add(repeateGroupStatResult);
				}

				// 将一个推广计划的推广组重复信息记录到文件中
				this.writeRepeateGroupInfo(stateResultList, bw);
			}

		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		// 读取文件，发邮件
		List<RepeateGroupStatResult> repeateGroupStatResultList = this.readRepeateGroupInfo(writeFileName);

		this.sendRepeateGroupInfoMail(repeateGroupStatResultList, yesterDay);

		log.info("end task: checkRepeateGroup");
	}

	public CproGroup findGroupInfoByGroupId(Integer groupId) {
		if (groupId == null) {
			return null;
		}
		return cproGroupDaoOnMultiDataSource.findGroupInfoByGroupId(groupId);
	}

	public List<Integer> getCproGroupIdList() {
		return cproGroupDaoOnMultiDataSource.getAllCproGroupIds();
	}

	/**
	 * 将重复信息写入文件
	 * 
	 * @param stateResultList
	 * @param bw
	 */
	private void writeRepeateGroupInfo(List<RepeateGroupStatResult> stateResultList, BufferedWriter bw) {

		if (bw == null) {
			return;
		}

		try {
			for (RepeateGroupStatResult repeateGroupStatResult : stateResultList) {
				String line = repeateGroupStatResult.getUserId() + "\t" + repeateGroupStatResult.getUserName() + "\t" + repeateGroupStatResult.getPlanName() + "\t" + repeateGroupStatResult.getRepeateGroupNum() + "\t" + repeateGroupStatResult.getRepeateGroupNames();
				bw.append(line);
				bw.newLine();
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 读文件，生成数据结构，准备发送邮件
	 * 
	 * @param fileName
	 */
	private List<RepeateGroupStatResult> readRepeateGroupInfo(String fileName) {

		List<RepeateGroupStatResult> repeateGroupInfoList = new ArrayList<RepeateGroupStatResult>();

		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "GBK"));
			String line = null;
			while ((line = br.readLine()) != null) {

				String[] domain = line.split("\t");
				if (domain.length != 5) {
					log.error("colum num is error :" + line);
					continue;
				}

				RepeateGroupStatResult repeateGroupStatResult = new RepeateGroupStatResult();
				repeateGroupStatResult.setUserId(Integer.valueOf(domain[0]));
				repeateGroupStatResult.setUserName(domain[1]);
				repeateGroupStatResult.setPlanName(domain[2]);
				repeateGroupStatResult.setRepeateGroupNum(Integer.valueOf(domain[3]));
				repeateGroupStatResult.setRepeateGroupNames(domain[4]);

				repeateGroupInfoList.add(repeateGroupStatResult);
			}

		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		return repeateGroupInfoList;
	}

	/**
	 * 将检查的重复结果发送邮件
	 * 
	 * @param repeateGroupStatResultList
	 * @param yesterDay
	 */
	@SuppressWarnings("unchecked")
	private void sendRepeateGroupInfoMail(List<RepeateGroupStatResult> repeateGroupStatResultList, Date yesterDay) {

		if (repeateGroupStatResultList.size() == 0) {
			log.info("repeate number is 0, don't send email");
			return;
		}

		String template = "com/baidu/beidou/cprogroup/template/repeateGroupMail.ftl";

		String mailTitle = "[监控][beidou]配置相同的推广组-" + DateUtils.formatDate(yesterDay, "yyyy年MM月dd日");

		@SuppressWarnings("rawtypes")
		Map repeateInfoMap = new HashMap(1);
		repeateInfoMap.put("repeateInfos", repeateGroupStatResultList);

		try {
			TemplateHandler handler = new FreeMarkerTemplateHandler();
			String mailTxt = handler.applyTemplate(template, repeateInfoMap);
			MailUtils.sendHtmlMail(this.repeateGroupOutputFileMailFrom, this.repeateGroupOutputFileMailTo, mailTitle, mailTxt);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 过滤推广组
	 * 
	 * @param planId
	 * @param userId
	 */
	private List<List<CproGroupMoreInfo>> filterbyGroup(final Integer planId, int userId) {

		List<List<CproGroupMoreInfo>> groupInfoListFiltered = new ArrayList<List<CproGroupMoreInfo>>();

		Map<CproGroupMoreInfo, List<CproGroupMoreInfo>> groupMoreInfoMap = new HashMap<CproGroupMoreInfo, List<CproGroupMoreInfo>>();

		List<CproGroupMoreInfo> groupList = this.findEffectGroupInfoMorebyPlanId(planId, userId);

		// 放置到map中
		for (CproGroupMoreInfo groupMoreInfo : groupList) {
			if (groupMoreInfoMap.get(groupMoreInfo) == null) {
				List<CproGroupMoreInfo> subGroupMoreInfoList = new ArrayList<CproGroupMoreInfo>();
				subGroupMoreInfoList.add(groupMoreInfo);
				groupMoreInfoMap.put(groupMoreInfo, subGroupMoreInfoList);
			} else {
				List<CproGroupMoreInfo> subGroupMoreInfoList = groupMoreInfoMap.get(groupMoreInfo);
				subGroupMoreInfoList.add(groupMoreInfo);
			}
		}

		// 将重复个数超过1个的推广组过滤出来
		for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoMap.values()) {
			if (subGroupMoreInfoLis.size() > 1) {
				groupInfoListFiltered.add(subGroupMoreInfoLis);
			}
		}

		return groupInfoListFiltered;
	}

	private List<CproGroupMoreInfo> findEffectGroupInfoMorebyPlanId(final Integer planId, int userId) {
		if (planId == null) {
			return new ArrayList<CproGroupMoreInfo>(0);
		}
		return cproGroupDaoOnMultiDataSource.findEffectGroupInfoMorebyPlanId(planId, userId);
	}

	/**
	 * 根据主题词进行过滤
	 * 
	 * @param groupInfoList
	 * @param userId
	 */
	private List<List<CproGroupMoreInfo>> filterbyKeyword(List<List<CproGroupMoreInfo>> groupInfoList, Integer userId) {

		List<List<CproGroupMoreInfo>> groupInfoListFiltered = new ArrayList<List<CproGroupMoreInfo>>();

		Map<List<Long>, List<CproGroupMoreInfo>> groupMoreInfoMap = new HashMap<List<Long>, List<CproGroupMoreInfo>>();
		List<Long> wordIdList = new ArrayList<Long>();

		for (List<CproGroupMoreInfo> subGroupInfoList : groupInfoList) {

			// 清空
			groupMoreInfoMap.clear();

			for (CproGroupMoreInfo groupMoreInfo : subGroupInfoList) {

				List<CproKeyword> keyWordList = cproKeywordDao.getCproKeywordsByGroup(groupMoreInfo.getGroupId(), userId);

				wordIdList.clear();
				for (CproKeyword cproKeyword : keyWordList) {
					wordIdList.add(cproKeyword.getWordId());
				}
				// 排序
				Collections.sort(wordIdList);

				if (groupMoreInfoMap.get(wordIdList) == null) {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = new ArrayList<CproGroupMoreInfo>();
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
					groupMoreInfoMap.put(wordIdList, subGroupMoreInfoFilteredList);
				} else {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = groupMoreInfoMap.get(wordIdList);
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
				}
			}

			// 将重复个数超过1个的推广组过滤出来
			for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoMap.values()) {
				if (subGroupMoreInfoLis.size() > 1) {
					groupInfoListFiltered.add(subGroupMoreInfoLis);
				}
			}
		}
		return groupInfoListFiltered;
	}

	/**
	 * 根据ip过滤设置进行过滤
	 * 
	 * @param groupInfoList
	 * @param userId
	 * @return
	 */
	private List<List<CproGroupMoreInfo>> filterbyIpFilter(List<List<CproGroupMoreInfo>> groupInfoList, int userId) {

		List<List<CproGroupMoreInfo>> groupInfoListFiltered = new ArrayList<List<CproGroupMoreInfo>>();

		Map<List<String>, List<CproGroupMoreInfo>> groupMoreInfoMap = new HashMap<List<String>, List<CproGroupMoreInfo>>();

		List<String> ipFilterList = new ArrayList<String>();

		for (List<CproGroupMoreInfo> subGroupInfoList : groupInfoList) {

			// 清空
			groupMoreInfoMap.clear();

			for (CproGroupMoreInfo groupMoreInfo : subGroupInfoList) {

				List<GroupIpFilter> ipFilterObjList = groupIpFilterDao.findByGroupId(groupMoreInfo.getGroupId(), userId);

				ipFilterList.clear();
				for (GroupIpFilter ipFilterObj : ipFilterObjList) {
					ipFilterList.add(ipFilterObj.getIp());
				}

				// 排序
				Collections.sort(ipFilterList);

				if (groupMoreInfoMap.get(ipFilterList) == null) {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = new ArrayList<CproGroupMoreInfo>();
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
					groupMoreInfoMap.put(ipFilterList, subGroupMoreInfoFilteredList);
				} else {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = groupMoreInfoMap.get(ipFilterList);
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
				}
			}

			// 将重复个数超过1个的推广组过滤出来
			for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoMap.values()) {
				if (subGroupMoreInfoLis.size() > 1) {
					groupInfoListFiltered.add(subGroupMoreInfoLis);
				}
			}

		}

		return groupInfoListFiltered;
	}

	/**
	 * 网站过滤进行过滤
	 * 
	 * @param groupInfoList
	 * @param userId
	 */
	private List<List<CproGroupMoreInfo>> filterbySiteFilter(List<List<CproGroupMoreInfo>> groupInfoList, int userId) {

		List<List<CproGroupMoreInfo>> groupInfoListFiltered = new ArrayList<List<CproGroupMoreInfo>>();

		Map<List<String>, List<CproGroupMoreInfo>> groupMoreInfoMap = new HashMap<List<String>, List<CproGroupMoreInfo>>();

		List<String> siteFilterList = new ArrayList<String>();

		for (List<CproGroupMoreInfo> subGroupInfoList : groupInfoList) {

			// 清空
			groupMoreInfoMap.clear();

			for (CproGroupMoreInfo groupMoreInfo : subGroupInfoList) {

				List<GroupSiteFilter> siteFilterObjList = groupSiteFilterDao.findByGroupId(groupMoreInfo.getGroupId(), userId);

				siteFilterList.clear();

				for (GroupSiteFilter siteFilterObj : siteFilterObjList) {
					siteFilterList.add(siteFilterObj.getSite());
				}

				// 排序
				Collections.sort(siteFilterList);

				if (groupMoreInfoMap.get(siteFilterList) == null) {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = new ArrayList<CproGroupMoreInfo>();
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
					groupMoreInfoMap.put(siteFilterList, subGroupMoreInfoFilteredList);
				} else {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = groupMoreInfoMap.get(siteFilterList);
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
				}
			}

			// 将重复个数超过1个的推广组过滤出来
			for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoMap.values()) {
				if (subGroupMoreInfoLis.size() > 1) {
					groupInfoListFiltered.add(subGroupMoreInfoLis);
				}
			}

		}

		return groupInfoListFiltered;
	}

	/**
	 * 分网站价格进行过滤
	 * 
	 * @param groupInfoList
	 * @param userId
	 */
	private List<List<CproGroupMoreInfo>> filterbySitePrice(List<List<CproGroupMoreInfo>> groupInfoList, int userId) {

		List<List<CproGroupMoreInfo>> groupInfoListFiltered = new ArrayList<List<CproGroupMoreInfo>>();

		Map<List<String>, List<CproGroupMoreInfo>> groupMoreInfoMap = new HashMap<List<String>, List<CproGroupMoreInfo>>();

		List<String> sitePriceList = new ArrayList<String>();

		StringBuilder builder = new StringBuilder();

		for (List<CproGroupMoreInfo> subGroupInfoList : groupInfoList) {

			// 清空
			groupMoreInfoMap.clear();

			for (CproGroupMoreInfo groupMoreInfo : subGroupInfoList) {

				List<GroupSitePrice> sitePriceObjList = groupSitePriceDao.findByGroupId(groupMoreInfo.getGroupId(), userId);

				sitePriceList.clear();

				for (GroupSitePrice sitePriceObj : sitePriceObjList) {

					builder.setLength(0);

					builder.append(sitePriceObj.getSiteid());
					builder.append(SITEPRICE_FILE_SEPERATOR);
					builder.append(sitePriceObj.getPrice());
					builder.append(SITEPRICE_FILE_SEPERATOR);
					builder.append(sitePriceObj.getTargeturl());

					sitePriceList.add(builder.toString());
				}

				// 排序
				Collections.sort(sitePriceList);

				if (groupMoreInfoMap.get(sitePriceList) == null) {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = new ArrayList<CproGroupMoreInfo>();
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
					groupMoreInfoMap.put(sitePriceList, subGroupMoreInfoFilteredList);
				} else {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = groupMoreInfoMap.get(sitePriceList);
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
				}
			}

			// 将重复个数超过1个的推广组过滤出来
			for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoMap.values()) {
				if (subGroupMoreInfoLis.size() > 1) {
					groupInfoListFiltered.add(subGroupMoreInfoLis);
				}
			}

		}

		return groupInfoListFiltered;
	}

	/**
	 * 根据创意信息进行过滤
	 * 
	 * @param groupInfoList
	 * @param userId
	 */
	private List<List<CproGroupMoreInfo>> filterbyUnitInfo(List<List<CproGroupMoreInfo>> groupInfoList, Integer userId) {

		List<List<CproGroupMoreInfo>> groupInfoListFiltered = new ArrayList<List<CproGroupMoreInfo>>();

		Map<List<String>, List<CproGroupMoreInfo>> groupMoreInfoMap = new HashMap<List<String>, List<CproGroupMoreInfo>>();

		List<String> unitInfoList = new ArrayList<String>();

		StringBuilder builder = new StringBuilder();

		for (List<CproGroupMoreInfo> subGroupInfoList : groupInfoList) {

			// 清空
			groupMoreInfoMap.clear();

			for (CproGroupMoreInfo groupMoreInfo : subGroupInfoList) {

				List<CproUnit> unitObjList = cproUnitMgr.findUnDeletedUnitbyGroupId(groupMoreInfo.getGroupId(), userId);

				unitInfoList.clear();

				for (CproUnit unitObj : unitObjList) {

					builder.setLength(0);

					builder.append(unitObj.getWuliaoType());
					builder.append(FILE_SEPERATOR);
					builder.append(unitObj.getTitle());
					builder.append(FILE_SEPERATOR);

					if ((unitObj.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL) || (unitObj.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON)) {
						builder.append(unitObj.getDescription1());
					} else {
						builder.append("");
					}

					builder.append(FILE_SEPERATOR);

					if ((unitObj.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL) || (unitObj.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON)) {
						builder.append(unitObj.getDescription2());
					} else {
						builder.append("");
					}

					unitInfoList.add(builder.toString());
				}

				// 排序
				Collections.sort(unitInfoList);

				if (groupMoreInfoMap.get(unitInfoList) == null) {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = new ArrayList<CproGroupMoreInfo>();
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
					groupMoreInfoMap.put(unitInfoList, subGroupMoreInfoFilteredList);
				} else {
					List<CproGroupMoreInfo> subGroupMoreInfoFilteredList = groupMoreInfoMap.get(unitInfoList);
					subGroupMoreInfoFilteredList.add(groupMoreInfo);
				}
			}

			// 将重复个数超过1个的推广组过滤出来
			for (List<CproGroupMoreInfo> subGroupMoreInfoLis : groupMoreInfoMap.values()) {
				if (subGroupMoreInfoLis.size() > 1) {
					groupInfoListFiltered.add(subGroupMoreInfoLis);
				}
			}

		}
		return groupInfoListFiltered;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setCproKeywordDao(CproKeywordDao cproKeywordDao) {
		this.cproKeywordDao = cproKeywordDao;
	}

	public void setGroupIpFilterDao(GroupIpFilterDao groupIpFilterDao) {
		this.groupIpFilterDao = groupIpFilterDao;
	}

	public void setGroupSitePriceDao(GroupSitePriceDao groupSitePriceDao) {
		this.groupSitePriceDao = groupSitePriceDao;
	}

	public void setGroupSiteFilterDao(GroupSiteFilterDao groupSiteFilterDao) {
		this.groupSiteFilterDao = groupSiteFilterDao;
	}

	public void setCproPlanMgr(CproPlanMgr cproPlanMgr) {
		this.cproPlanMgr = cproPlanMgr;
	}

	public void setCproUnitMgr(CproUnitMgr cproUnitMgr) {
		this.cproUnitMgr = cproUnitMgr;
	}

	public void setRepeateGroupOutputFilePath(String repeateGroupOutputFilePath) {
		this.repeateGroupOutputFilePath = repeateGroupOutputFilePath;
	}

	public void setRepeateGroupOutputFileMailFrom(String repeateGroupOutputFileMailFrom) {
		this.repeateGroupOutputFileMailFrom = repeateGroupOutputFileMailFrom;
	}

	public void setRepeateGroupOutputFileMailTo(String repeateGroupOutputFileMailTo) {
		this.repeateGroupOutputFileMailTo = repeateGroupOutputFileMailTo;
	}

	public void setCproGroupDaoOnMultiDataSource(CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource) {
		this.cproGroupDaoOnMultiDataSource = cproGroupDaoOnMultiDataSource;
	}

    @Override
    public List<SimpleGroup> getAllGroupIdByPrice(int price) {
        return cproGroupDaoOnMultiDataSource.getAllGroupIdByPrice(price);
    }

    public int countByGroupId(Integer groupId, Integer userId) {
        return cproKeywordDao.countByGroupId(groupId, userId);
    }

    public List<Long> getCproKeywordIdsByGroup(Integer groupId, Integer userId) {
        return cproKeywordDao.getCproKeywordIdsByGroup(groupId, userId);
    }

    public List<Integer> filterGroupIdByKeywordCount(List<Integer> groupIds, Integer userId, int countLimit) {
        return cproKeywordDao.filterGroupIdByKeywordCount(groupIds, userId, countLimit);
    }
}
