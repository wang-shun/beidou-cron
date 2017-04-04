package com.baidu.beidou.aot.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.aot.dao.QtkrSpecialTradeDao;
import com.baidu.beidou.aot.service.AotQtMgr;
import com.baidu.beidou.cprogroup.bo.CproKeyword;
import com.baidu.beidou.cprogroup.constant.CproGroupConstant;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprogroup.dao.CproGroupOnCapDao;
import com.baidu.beidou.cprogroup.dao.CproKeywordDao;
import com.baidu.beidou.cprogroup.dao.KrRecycleDao;
import com.baidu.beidou.cprogroup.dao.QTBlacklistDao;
import com.baidu.beidou.cprounit.icon.bo.AdTradeInfo;
import com.baidu.beidou.cprounit.icon.dao.AdConfigDao;
import com.baidu.beidou.user.constant.UserConstant;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-30
 */
public class AotQtMgrImpl implements AotQtMgr {

	private static final Log log = LogFactory.getLog(AotQtMgrImpl.class);

	public static final int QT_WORD_TAG_BLACK = 0; // 黑名单词TAG
	public static final int QT_WORD_TAG_GREY = 1; // 灰名单词TAG

	private UserDao userDao;
	private AdConfigDao adConfigDao;
	private UserInfoMgr userInfoMgr;
	private KrRecycleDao krRecycleDao;
	private QTBlacklistDao qtBlackListDao;
	private CproKeywordDao cproKeywordDao;
	private CproGroupOnCapDao cproGroupOnCapDao;
	private QtkrSpecialTradeDao qtkrSpecialTradeDao;
	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;

	/** groupId 和 userId 的对应关系， key是groupId，value是userId **/
	private Map<Integer, Integer> groupIdUserIdMap = new HashMap<Integer, Integer>();

	/** 用户级别关键词黑名单，key是userId，value是对应的关键词黑名单列表 **/
	private Map<Integer, Set<Long>> userBlackWordIdMap = new HashMap<Integer, Set<Long>>();

	/** 全局关键词黑名单 **/
	private Set<Long> globalBlackWordIdSet = new HashSet<Long>();

	/** 全局关键词灰名单（受限） **/
	private Set<Long> globalGreyWordIdSet = new HashSet<Long>();

	/** 大客户的userId **/
	private Set<Integer> heavyUserIdSet = new HashSet<Integer>();

	/** 有效客户的userId **/
	private Set<Integer> validUserIdSet = new HashSet<Integer>();

	/** 用户行业对应关系缓存，key是二级行业，value是一级行业 **/
	private Map<Integer, Integer> tradeIdMap = new HashMap<Integer, Integer>();

	/** 优化建议3中，需要单独处理的二级行业 **/
	private Set<Integer> qtkrSpecialTradeIdSet = new HashSet<Integer>();

	public void importQtkrWord(String inputGroupkrWordFile, String outputGroupkrWordFile, float relativityLimit, int minQtkrCnt) {

		loadGlobalBlackGrayWord();

		// 处理后的待过滤地域名称map<id,name>
		Map<Integer, String> regionMap = cproGroupOnCapDao.getRegIdNameMap();

		// 得到的Map中，key为一级地域或二级地域id，value为一级地域id；当key为一级地域id时，value和key相同
		Map<Integer, Integer> regRelationMap = cproGroupOnCapDao.getRegRelationMap();

		// 根据regRelationMap获取一级二级地域对应的Map，key为一级地域，value是该一级地域对应的所有二级地域
		Map<Integer, Set<Integer>> regFirstSecondRelationMap = getFirstSecondRegRalationMap(regRelationMap);

		BufferedReader br = null;
		BufferedWriter bw = null;

		long t1 = System.currentTimeMillis();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inputGroupkrWordFile), "GBK"));
			// br = new BufferedReader(new FileReader(inputGroupkrWordFile));
			bw = new BufferedWriter(new FileWriter(outputGroupkrWordFile));

			Map<Integer, Set<Integer>> groupIdQtkrWordSetMap = new HashMap<Integer, Set<Integer>>();
			Map<Integer, Set<Integer>> userIdGroupIdSetMap = new HashMap<Integer, Set<Integer>>();

			String line = null;
			while ((line = br.readLine()) != null) {

				// line 的格式为 groupId \t [wordid:name:value, wordid:name:value,
				// …]
				line=line.trim();

				try {
					// 判断格式是否正确
					int groupId = Integer.parseInt(line.substring(0, line.indexOf("\t")).trim());
					if (groupId <= 0) {
						log.warn("groupId(" + groupId + ") is <=0, continue");
						continue;
					}

					// 对于每个group，获得regionMap的副本
					Map<Integer, String> regionMapBak = new HashMap<Integer, String>();
					regionMapBak.putAll(regionMap);

					if ((line.indexOf("]") - line.indexOf("[")) <= 1) {
						log.warn("has no word info for group=" + groupId);
						continue;
					}

					String lastStr = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
					if (lastStr == null || lastStr.trim() == "") {
						log.warn("has no word info for group=" + groupId);
						continue;
					}
					String[] wordArr = lastStr.split(",");
					if (ArrayUtils.isEmpty(wordArr)) {
						log.warn("do not get qtkr words for group " + groupId);
						continue;
					}

					// 取得文件中的推荐词，并过滤低于阈值的记录
					Map<Integer, String> qtkrWordidNameMap = new HashMap<Integer, String>();
					for (String wordStr : wordArr) {
						int pos = wordStr.indexOf(":");
						int wordid = Integer.parseInt(wordStr.substring(0, pos).trim());// 取wordId
						int pos2 = wordStr.lastIndexOf(":");
						String name = wordStr.substring(pos + 1, pos2).trim();// 取name
						float wordRelativity = Float.parseFloat(wordStr.substring(pos2 + 1).trim());

						if (wordRelativity < relativityLimit) {
							continue;
						}

						qtkrWordidNameMap.put(wordid, name);
					}

					/**
					 * 对推荐词进行过滤
					 */
					Integer userId = getUserIdByGroupId(groupId);
					if (userId == null) {
						log.warn("do not get userid for group " + groupId);
						continue;
					}

					// 过滤全局黑名单词
					for (Long globalblackwordid : globalBlackWordIdSet) {
						qtkrWordidNameMap.remove(globalblackwordid.intValue());
					}

					// 过滤用户级别黑名单词
					Set<Long> blackWordidSet = getBlackWordByUserId(userId);
					for (Long userblackwordid : blackWordidSet) {
						qtkrWordidNameMap.remove(userblackwordid.intValue());
					}

					// 过滤非推广地域词
					List<Integer> regionlist = cproGroupDaoOnMultiDataSource.getRegionList(groupId, userId);
					int isallregiontag = cproGroupDaoOnMultiDataSource.getIsallregionTag(groupId, userId);

					// 过滤回收站词 add by qianlei
					List<Long> recycleWordids = krRecycleDao.getUserRecycleWordIds(userId);
					for (Long recycleWordid : recycleWordids) {
						qtkrWordidNameMap.remove(recycleWordid.intValue());
					}

					// 判断是否需要过滤非推广地域词
					if (regionIsLimit(isallregiontag, regionlist, regionMapBak.keySet(), regFirstSecondRelationMap.keySet())) {

						for (Integer regid : regionlist) {
							if (regFirstSecondRelationMap.containsKey(regid.intValue())) {// regid是一级地域
								// 获取一级地域对应的二级地域
								Set<Integer> secondRegIdSet = regFirstSecondRelationMap.get(regid);
								for (Integer secondRegId : secondRegIdSet) {
									regionMapBak.remove(secondRegId);
								}
							}
							// 无论是一级地域还是二级地域都要删除regid
							regionMapBak.remove(regid);
						}
						Map<Integer, String> delMap = new HashMap<Integer, String>();
						for (Map.Entry<Integer, String> entry : qtkrWordidNameMap.entrySet()) {
							for (Map.Entry<Integer, String> regionentry : regionMapBak.entrySet()) {
								if (entry.getValue().contains(regionentry.getValue())) {
									delMap.put(entry.getKey(), entry.getValue());
									break;
								}
							}
						}
						for (Map.Entry<Integer, String> entry : delMap.entrySet()) {
							qtkrWordidNameMap.remove(entry.getKey());
						}

					}

					// 将QT推广组的推荐词保存起来，以后批量写入
					groupIdQtkrWordSetMap.put(groupId, qtkrWordidNameMap.keySet());
					Set<Integer> groupIdSet = userIdGroupIdSetMap.get(userId);
					if (groupIdSet == null) {
						groupIdSet = new HashSet<Integer>();
						userIdGroupIdSetMap.put(userId, groupIdSet);
					}
					groupIdSet.add(groupId);
				} catch (NumberFormatException e) {
					log.warn("Failed to parse line data", e);
				}
			}

			/**
			 * 将推荐的关键词写入文件
			 */
			int batchNum = 10;

			/** 1. 循环userId <--> groupIdSet **/
			for (Map.Entry<Integer, Set<Integer>> entry : userIdGroupIdSetMap.entrySet()) {

				int userId = entry.getKey();
				Set<Integer> groupIdSet = entry.getValue();

				try {
					List<Integer> tmpGroupIdList = new ArrayList<Integer>();

					/** 2. 循环groupIdSet **/
					for (Integer groupId : groupIdSet) {
						tmpGroupIdList.add(groupId);

						if (tmpGroupIdList.size() == batchNum) {
							writeQtkrWord(tmpGroupIdList, userId, groupIdQtkrWordSetMap, minQtkrCnt, bw);
							tmpGroupIdList.clear();
						}
					}

					if (tmpGroupIdList.size() > 0) {
						writeQtkrWord(tmpGroupIdList, userId, groupIdQtkrWordSetMap, minQtkrCnt, bw);
						tmpGroupIdList.clear();
					}

				} catch (Exception e) {
					log.warn("Failed to write qtkrword for user " + userId, e);
					continue;
				}
			}
		} catch (Exception e) {
			log.error("Fail to import qtkr words", e);
			System.exit(1);
		} finally {
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		long t2 = System.currentTimeMillis();
		log.info("cost time " + (t2 - t1));
	}

	private void writeQtkrWord(List<Integer> tmpGroupIdList, int userId, Map<Integer, Set<Integer>> groupIdQtkrWordSetMap, int minQtkrCnt, BufferedWriter bw) throws Exception {

		List<CproKeyword> qtKeyWordList = cproKeywordDao.findByGroupIds(tmpGroupIdList, userId);
		Set<Integer> tmpWordIdSet = new HashSet<Integer>();
		Set<Integer> recordGroupIdSet = new HashSet<Integer>();
		int oldGroupId = 0;

		/** 循环groupIdSet查询出来的keyword **/
		for (CproKeyword keyword : qtKeyWordList) {

			Integer gid = keyword.getGroupId();
			recordGroupIdSet.add(gid);

			if (oldGroupId == 0) {
				oldGroupId = gid;
			}

			if (!gid.equals(oldGroupId)) {

				Set<Integer> qtkrWordidSet = groupIdQtkrWordSetMap.get(oldGroupId);
				qtkrWordidSet.removeAll(tmpWordIdSet);

				if (qtkrWordidSet.size() > minQtkrCnt) {
					for (Integer wordId : qtkrWordidSet) {
						bw.write(String.valueOf(oldGroupId));
						bw.write("\t");
						bw.write(String.valueOf(wordId));
						bw.newLine();
					}
				}

				tmpWordIdSet.clear();
			}

			oldGroupId = gid;
			tmpWordIdSet.add(keyword.getWordId().intValue());
		}

		// 对于循环到最后，没有写入文件的groupId
		if (tmpWordIdSet.size() > 0) {
			Set<Integer> qtkrWordidSet = groupIdQtkrWordSetMap.get(oldGroupId);
			qtkrWordidSet.removeAll(tmpWordIdSet);

			if (qtkrWordidSet.size() > minQtkrCnt) {
				for (Integer wordId : qtkrWordidSet) {
					bw.write(String.valueOf(oldGroupId));
					bw.write("\t");
					bw.write(String.valueOf(wordId));
					bw.newLine();
				}
			}
		}

		// 对于没有已购词的QT推广组
		tmpGroupIdList.removeAll(recordGroupIdSet);
		if (tmpGroupIdList.size() > 0) {
			for (Integer gid : tmpGroupIdList) {
				Set<Integer> qtkrWordidSet = groupIdQtkrWordSetMap.get(gid);
				if (qtkrWordidSet.size() > minQtkrCnt) {
					for (Integer wordId : qtkrWordidSet) {
						bw.write(String.valueOf(gid));
						bw.write("\t");
						bw.write(String.valueOf(wordId));
						bw.newLine();
					}
				}
			}
		}
	}

	/**
	 * 根据groupid查询userid。
	 * 为了减少查询次数，当缓存里没有groupid和userid的对应关系时，查询此userid对应的所有groupid缓存起来
	 * 
	 * @param groupId
	 * @return
	 */
	private Integer getUserIdByGroupId(int groupId) {
		Integer userId = groupIdUserIdMap.get(groupId);
		if (userId != null) {
			return userId;
		}

		List<Integer> tmpGroupIdList = new ArrayList<Integer>();
		tmpGroupIdList.add(groupId);

		List<Integer> userIdList = cproGroupDaoOnMultiDataSource.findUserIdByGroupIds(tmpGroupIdList);
		if (userIdList.size() < 1) {
			return null;
		}

		userId = userIdList.get(0);
		List<Integer> groupIdList = cproGroupDaoOnMultiDataSource.findGroupIdsByUserId(userId);

		if (CollectionUtils.isNotEmpty(groupIdList)) {
			for (Integer gid : groupIdList) {
				groupIdUserIdMap.put(gid, userId);
			}
		}

		return userId;
	}

	/**
	 * 缓存全局的黑名单关键词
	 */
	private void loadGlobalBlackGrayWord() {
		Map<Long, Integer> tmpBlackWordMap = qtBlackListDao.findQTBlackList();

		if (MapUtils.isNotEmpty(tmpBlackWordMap)) {
			for (Entry<Long, Integer> entry : tmpBlackWordMap.entrySet()) {
				if (new Integer(QT_WORD_TAG_BLACK).equals(entry.getValue())) {
					globalBlackWordIdSet.add(entry.getKey());
				} else if (new Integer(QT_WORD_TAG_GREY).equals(entry.getValue())) {
					globalGreyWordIdSet.add(entry.getKey());
				}
			}
		}
	}

	/**
	 * 缓存用户级别的黑名单关键词
	 * 
	 * @param userId
	 * @return
	 */
	private Set<Long> getBlackWordByUserId(int userId) {

		Set<Long> wordidSet = userBlackWordIdMap.get(userId);
		if (wordidSet != null) {
			return wordidSet;
		}

		wordidSet = new HashSet<Long>();
		userBlackWordIdMap.put(userId, wordidSet);

		List<Long> blackWordList = qtBlackListDao.findBlackListByUser(userId);
		if (CollectionUtils.isNotEmpty(blackWordList)) {
			wordidSet.addAll(blackWordList);
		}

		return wordidSet;
	}

	public void importQtWordNum(String userTradeFile, String groupQtWordNumFile, int batchGroupPerUser) {

		loadGlobalBlackGrayWord();
		loadHeavyUserId();
		loadValidUserId();
		loadUserTrade();
		loadQtkrSpecialTradeId();

		BufferedReader br = null;
		BufferedWriter bw = null;

		try {
			br = new BufferedReader(new FileReader(userTradeFile));
			bw = new BufferedWriter(new FileWriter(groupQtWordNumFile));

			String line = null;
			int cnt = 0;

			while ((line = br.readLine()) != null) {

				// line 的格式为 userId \t tradeId
				line=line.trim();
				cnt++;

				try {
					// 判断格式是否正确
					String[] idArr = line.split("\t");
					if (ArrayUtils.isEmpty(idArr) || idArr.length < 2) {
						log.warn("do not get user's tradeId: " + line);
						continue;
					}

					int userId = Integer.parseInt(idArr[0]);
					int tradeId = Integer.parseInt(idArr[1]);

					// 不考虑非有效客户
					if (!validUserIdSet.contains(userId)) {
						continue;
					}

					// 不考虑大客户
					if (heavyUserIdSet.contains(userId)) {
						continue;
					}

					// 如果用户没有行业或行业为"其他"，则跳过
					if (tradeId == 0 || String.valueOf(tradeId).indexOf("99") >= 0) {
						continue;
					}

					// 如果用户的二级行业ID不用特殊处理，则用一级行业ID来代替；否则用二级行业ID处理
					if (!qtkrSpecialTradeIdSet.contains(tradeId)) {
						Integer parentTradeId = tradeIdMap.get(tradeId);
						if (parentTradeId != null) {
							tradeId = parentTradeId;
						}
					}

					/**
					 * 取得用户下的所有QT推广组，并循环处理
					 */
					List<Integer> groupIdList = cproGroupDaoOnMultiDataSource.findGroupIdsByUserIdAndTargettype(userId, CproGroupConstant.GROUP_TARGET_TYPE_QT);

					if (groupIdList.size() == 0) {
						continue;
					}
					log.info("### [cnt, userId, groupId's size] - [" + cnt + ", " + userId + ", " + groupIdList.size() + "]");

					long t1 = System.currentTimeMillis();
					int groupIdCnt = groupIdList.size();
					for (int i = 0; i < groupIdCnt;) {

						// 每次从DB中获取batchGroupPerUser个推广组的QT词
						int j = i + batchGroupPerUser;
						if (j > groupIdCnt) {
							j = groupIdCnt;
						}

						List<Integer> tmpGidList = new ArrayList<Integer>();
						for (int pos = i; pos < j; pos++) {
							tmpGidList.add(groupIdList.get(pos));
						}

						Map<Integer, Set<Integer>> groupIdWordIdSetMap = new HashMap<Integer, Set<Integer>>();
						List<CproKeyword> qtKeyWordList = cproKeywordDao.findByGroupIds(tmpGidList, userId);
						for (CproKeyword word : qtKeyWordList) {
							Integer groupId = word.getGroupId();
							Set<Integer> wordIdSet = groupIdWordIdSetMap.get(groupId);
							if (wordIdSet == null) {
								wordIdSet = new HashSet<Integer>();
								groupIdWordIdSetMap.put(groupId, wordIdSet);
							}

							wordIdSet.add(word.getWordId().intValue());
						}

						// 轮询 batchGroupPerUser 个推广组，进行关键词过滤
						for (Integer groupId : tmpGidList) {
							Set<Integer> wordIdSet = groupIdWordIdSetMap.get(groupId);

							int wordCnt = 0;
							if (wordIdSet != null) {
								// 过滤全局黑名单词
								for (Long globalblackwordid : globalBlackWordIdSet) {
									if (globalblackwordid != null) {
										wordIdSet.remove(globalblackwordid.intValue());
									}
								}

								// 过滤全局灰名单词（受限）
								for (Long globalgreywordid : globalGreyWordIdSet) {
									if (globalgreywordid != null) {
										wordIdSet.remove(globalgreywordid.intValue());
									}
								}

								// 过滤用户级别的黑名单词
								Set<Long> blackWordidSet = getBlackWordByUserId(userId);
								for (Long blackwordid : blackWordidSet) {
									if (blackwordid != null) {
										wordIdSet.remove(blackwordid.intValue());
									}
								}

								wordCnt = wordIdSet.size();
							}

							// 将最后剩下的关键词词写入文件
							bw.write(String.valueOf(tradeId));
							bw.write("\t");
							bw.write(String.valueOf(userId));
							bw.write("\t");
							bw.write(String.valueOf(groupId));
							bw.write("\t");
							bw.write(String.valueOf(wordCnt));
							bw.newLine();

							// bw.flush();
						}

						// 下标增加batchGroupPerUser
						i = j;
					}

					long t2 = System.currentTimeMillis();
					log.info("### [userId, costTime] - [" + userId + ", " + (t2 - t1) + "]");
				} catch (Exception e) {
					log.warn("Failed to parse line data", e);
				}
			}
		} catch (Exception e) {
			log.error("Fail to compute qt group words num", e);
			System.exit(1);
		} finally {
			try {
				if (bw != null) {
					bw.flush();
					bw.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.exit(0);
	}

	/**
	 * 获取大客户的userId
	 */
	private void loadHeavyUserId() {
		int[] userIdArr = userInfoMgr.getAllHeavyCustomers();
		if (userIdArr != null && userIdArr.length > 0) {
			heavyUserIdSet.addAll(Arrays.asList(ArrayUtils.toObject(userIdArr)));
		}
	}

	/**
	 * 获取有效客户的userId
	 */
	private void loadValidUserId() {
		List<Integer> sfstateList = new ArrayList<Integer>();
		sfstateList.add(UserConstant.SHIFEN_STATE_NORMAL);
		validUserIdSet.addAll(userDao.findUserIdBySFState(sfstateList));
	}

	/**
	 * 获取用户行业的分类
	 */
	private void loadUserTrade() {
		List<AdTradeInfo> tradeList = adConfigDao.findAdTrade();
		if (CollectionUtils.isNotEmpty(tradeList)) {
			for (AdTradeInfo tradeInfo : tradeList) {
				tradeIdMap.put(tradeInfo.getTradeid(), tradeInfo.getParentid());
			}
		}
	}

	/**
	 * 获取一级二级地域对应的Map，key为一级地域，value是该一级地域对应的所有二级地域
	 */
	private Map<Integer, Set<Integer>> getFirstSecondRegRalationMap(Map<Integer, Integer> regRelationMap) {
		Map<Integer, Set<Integer>> resultMap = new HashMap<Integer, Set<Integer>>();

		for (Map.Entry<Integer, Integer> entry : regRelationMap.entrySet()) {
			int key = entry.getKey().intValue();
			int value = entry.getValue().intValue();

			// 如果resultMap里面不存在此一级地域，则new一个新的
			if (!resultMap.containsKey(value)) {
				resultMap.put(value, new HashSet<Integer>());
			}

			// 如果key和value相等，则为一级地域;否则为二级地域，并将其添加到对应的一级地域下包含的二级地域集合里
			if (key != value) {
				resultMap.get(value).add(key);
			}
		}
		return resultMap;
	}

	/**
	 * 判断group地域设置是否有限制（不限标志位置位|reglist仅为全国|reglist仅为其他|reglist仅为全国和其他）
	 * 是则不进行地域过滤，反之需要过滤
	 */
	private boolean regionIsLimit(int isallregiontag, List<Integer> reglist, Set<Integer> regionMapBakSet, Set<Integer> allFirstRegIncludingOthersSet) {
		// 全地域投放则无需过滤
		if (isallregiontag == 1) {
			return false;
		}

		// 获得全国的一级地域idlist
		List<Integer> nationwide = new ArrayList<Integer>();
		// 获得其他地域
		List<Integer> otherReg = new ArrayList<Integer>();

		for (Integer firstRegId : allFirstRegIncludingOthersSet) {
			// regionMapBakSet中包含的一级地域，添加到全国一级地域idlist中
			if (regionMapBakSet.contains(firstRegId)) {
				nationwide.add(firstRegId);
			} else {// 否则是其他地域
				otherReg.add(firstRegId);
			}
		}

		// 仅为全国
		if (isallregiontag == 0 && (reglist.size() == nationwide.size() && reglist.containsAll(nationwide))) {
			return false;
		}

		// 仅为其他
		if (isallregiontag == 0 && (reglist.size() == otherReg.size() && reglist.containsAll(otherReg))) {
			return false;
		}

		// 仅为全国和其他
		if (isallregiontag == 0 && (reglist.size() == allFirstRegIncludingOthersSet.size() && reglist.containsAll(allFirstRegIncludingOthersSet))) {
			return false;
		}

		return true;
	}

	/**
	 * 获取优化建议3中需要单独处理的用户二级行业
	 */
	private void loadQtkrSpecialTradeId() {
		qtkrSpecialTradeIdSet.addAll(qtkrSpecialTradeDao.findQtkrSpecialTradeId());
	}

	public QTBlacklistDao getQtBlackListDao() {
		return qtBlackListDao;
	}

	public void setQtBlackListDao(QTBlacklistDao qtBlackListDao) {
		this.qtBlackListDao = qtBlackListDao;
	}

	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public AdConfigDao getAdConfigDao() {
		return adConfigDao;
	}

	public void setAdConfigDao(AdConfigDao adConfigDao) {
		this.adConfigDao = adConfigDao;
	}

	public QtkrSpecialTradeDao getQtkrSpecialTradeDao() {
		return qtkrSpecialTradeDao;
	}

	public void setQtkrSpecialTradeDao(QtkrSpecialTradeDao qtkrSpecialTradeDao) {
		this.qtkrSpecialTradeDao = qtkrSpecialTradeDao;
	}

	public CproKeywordDao getCproKeywordDao() {
		return cproKeywordDao;
	}

	public void setCproKeywordDao(CproKeywordDao cproKeywordDao) {
		this.cproKeywordDao = cproKeywordDao;
	}

	public KrRecycleDao getKrRecycleDao() {
		return krRecycleDao;
	}

	public void setKrRecycleDao(KrRecycleDao krRecycleDao) {
		this.krRecycleDao = krRecycleDao;
	}

	public void setCproGroupOnCapDao(CproGroupOnCapDao cproGroupOnCapDao) {
		this.cproGroupOnCapDao = cproGroupOnCapDao;
	}

	public void setCproGroupDaoOnMultiDataSource(CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource) {
		this.cproGroupDaoOnMultiDataSource = cproGroupDaoOnMultiDataSource;
	}

}
