package com.baidu.beidou.aot.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.aot.bo.CproGroupStatInfo;
import com.baidu.beidou.aot.bo.CproPlanStatInfo;
import com.baidu.beidou.aot.bo.GroupAotInfo;
import com.baidu.beidou.aot.bo.PlanAotInfo;
import com.baidu.beidou.aot.bo.RegionCodeInfo;
import com.baidu.beidou.aot.bo.SiteAotInfo;
import com.baidu.beidou.aot.dao.CodeStatDao;
import com.baidu.beidou.aot.dao.CproGroupStatDao;
import com.baidu.beidou.aot.dao.CproGroupStatOnXdbDao;
import com.baidu.beidou.aot.dao.CproPlanOfflineStatDao;
import com.baidu.beidou.aot.dao.CproPlanStatDao;
import com.baidu.beidou.aot.dao.CproPlanStatOnXdbDao;
import com.baidu.beidou.aot.dao.UnionSiteStatDao;
import com.baidu.beidou.aot.entity.SiteStatVo;
import com.baidu.beidou.aot.service.AotMgr;
import com.baidu.beidou.cprogroup.constant.CproGroupConstant;

public class AotMgrImpl implements AotMgr {

	private static final Log log = LogFactory.getLog(AotMgrImpl.class);
	private static final int MAX_GROUP_PER_PAGE = Integer.MAX_VALUE;

	private static final int MAX_INSERT_PER_PAGE = 10000;

	private List<CproPlanStatInfo> cachedPlanStatInfos = new ArrayList<CproPlanStatInfo>(MAX_INSERT_PER_PAGE);
	private List<CproGroupStatInfo> cachedGroupStatInfos = new ArrayList<CproGroupStatInfo>(MAX_INSERT_PER_PAGE);

	private CproGroupStatDao cproGroupStatDao;
	private CproGroupStatOnXdbDao cproGroupStatOnXdbDao;
	private CproPlanStatDao cproPlanStatDao;
	private CproPlanOfflineStatDao cproPlanOfflineStatDao;
	private CproPlanStatOnXdbDao cproPlanStatOnXdbDao;
	private UnionSiteStatDao unionSiteStatDao;
	private CodeStatDao codeStatDao;

	private Map<Integer, SiteStatVo> siteStatMap = new HashMap<Integer, SiteStatVo>();
	private Map<Integer, SiteStatVo> siteTradeStatMap = new HashMap<Integer, SiteStatVo>();
	private Map<Integer, List<Integer>> tradeSiteMap = new HashMap<Integer, List<Integer>>();

	private Map<Integer, Integer> regCodeMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> firstRegMap = new HashMap<Integer, Integer>();

	private int firstRegCount = 0;
	private int secondRegCount = 0;

	private long siteFixedSrchs = 0;
	private long siteFlowSrchs = 0;

	private int siteAvgPrice = 0;

	/** 这里面的信息已经根据planId排过序了 * */
	private List<PlanAotInfo> cproPlanAotInfo;
	/** 这里面的信息已经根据groupId排过序了 * */
	private List<GroupAotInfo> cproGroupAotInfo;

	public void importDBInfo() {
		log.info("开始加载推广计划信息");
		// 获取Plan的凌晨基准信息，由于Plan个数较少，信息也较小，因此不分页
		loadPlanInfo();
		log.info("开始加载推广组信息");
		// 获取Group的凌晨基准信息（仅包含price信息），这里也不分页
		loadGroupPriceInfo();
		log.info("开始加载站点信息");
		// 加载全部站点信息，并按照规则得到相应的数据结构，由于site数目较小，也没有分页
		loadSiteInfo();
		log.info("开始加载Code信息");
		// 加载全部Code信息，并按照规则得到相应的数据结构，没有分页
		loadCodeInfo();
		log.info("开始计算推广计划信息");
		// 计算Plan的相关信息并存储
		executePlanStat();
		log.info("开始计算推广组信息");
		// 计算Group的相关信息并存储，这里进行了分页
		executeGroupStat();
		log.info("importAotDB结束");
	}

	private void loadCodeInfo() {
		List<RegionCodeInfo> regInfo = codeStatDao.findAllRegionCodeInfo();
		if (CollectionUtils.isNotEmpty(regInfo)) {
			for (RegionCodeInfo info : regInfo) {
				if (info.getSecondregid() == 0) {
					// 一级地域
					firstRegCount++;
					regCodeMap.put(info.getFirstregid(), 0);
					Integer i = firstRegMap.get(info.getFirstregid());
					if (i == null) {
						firstRegMap.put(info.getFirstregid(), 0);
					}
				} else {
					// 二级地域
					regCodeMap.put(info.getSecondregid(), info.getFirstregid());
					secondRegCount++;
					Integer i = firstRegMap.get(info.getFirstregid());
					if (i == null) {
						firstRegMap.put(info.getFirstregid(), 1);
					} else {
						firstRegMap.put(info.getFirstregid(), i + 1);
					}
				}
			}
		}
	}

	private void executePlanStat() {
		if (CollectionUtils.isNotEmpty(cproPlanAotInfo)) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date to = calendar.getTime();
			calendar.add(Calendar.DATE, -1);
			Date from = calendar.getTime();
			for (PlanAotInfo info : cproPlanAotInfo) {
				// 首先计算其在线时长，这个很简单，看scheme里面有多少个1就行了
				CproPlanStatInfo stat = new CproPlanStatInfo();
				// 需要一个Calendar对象，就不复用上面那个了
				Calendar instance = Calendar.getInstance();
				int validtime = 0;
				int onlinetime = 0;
				int tmp = info.getYesterdaySchema();
				for (int i = 0; i < 24; i++) {
					if ((tmp & 1) == 1) {
						validtime++;
					}
					tmp >>= 1;
				}
				/**
				 * 以下算法只是目前在用，以后应该会结合online与offline，把它做得更完善些
				 */
				if (info.getBudgetOver() == 0) {
					// 如果在昨天最后时间（也就是该脚本运行的时间），推广计划没有超预算
					// 则认为在线时间与有效时间相等
					onlinetime = validtime;
				} else {
					// 取得昨天最晚下线时间
					Date lastOfftime = cproPlanOfflineStatDao.findLastOfftimeByDate(info.getPlanId(), from, to, info.getUserId());
					if (lastOfftime == null) {
						// 如果昨天没有下过线（其实理论上这个if不应该进来）
						// 则认为在线时间与有效时间相等
						onlinetime = validtime;
					} else {
						instance.setTime(lastOfftime);
						int hour = instance.get(Calendar.HOUR_OF_DAY);
						int scheme = info.getYesterdaySchema();
						for (int i = 0; i < 24; i++) {
							if ((scheme & 1) == 1) {
								onlinetime++;
							}
							if (i >= hour)
								break;
							scheme >>= 1;
						}
					}
				}
				stat.setBudgetover(info.getBudgetOver());
				stat.setPlanid(info.getPlanId());
				stat.setOnlinetime(onlinetime);
				stat.setValidtime(validtime);
				queueAndSavePlanStatInfo(stat);
			}
		}
		if (cachedPlanStatInfos.size() > 0) {
			cproPlanStatOnXdbDao.saveCproPlanStatInfo(cachedPlanStatInfos);
			cachedPlanStatInfos.clear();
		}
	}

	private void queueAndSavePlanStatInfo(CproPlanStatInfo info) {
		cachedPlanStatInfos.add(info);
		if (cachedPlanStatInfos.size() >= MAX_INSERT_PER_PAGE) {
			cproPlanStatOnXdbDao.saveCproPlanStatInfo(cachedPlanStatInfos);
			cachedPlanStatInfos.clear();
		}
	}

	private void queueAndSaveGroupStatInfo(CproGroupStatInfo info) {
		cachedGroupStatInfos.add(info);
		if (cachedGroupStatInfos.size() >= MAX_INSERT_PER_PAGE) {
			cproGroupStatOnXdbDao.saveGroupStatInfo(cachedGroupStatInfos);
			cachedGroupStatInfos.clear();
		}
	}

	private void executeGroupStat() {
		// 开启拉链
		int totalIndex = 0;
		int currentPage = 0;
		while (totalIndex < cproGroupAotInfo.size()) {
			// 这个batch里面的元素也已经根据groupId排过序了
			log.info("分页处理推广组信息：第" + currentPage + "组");
			List<GroupAotInfo> batch = cproGroupStatDao.findGroupAotInfoByPage(currentPage, MAX_GROUP_PER_PAGE);
			currentPage++;
			int innerIndex = 0;
			if (CollectionUtils.isEmpty(batch)) {
				// 已经查找不到了，这里应该是一个warning
				log.warn("外层还没有中止，但是cprogroupinfo已经没有数据了");
				break;
			}
			while (innerIndex < batch.size() && totalIndex < cproGroupAotInfo.size()) {
				GroupAotInfo in = batch.get(innerIndex);
				GroupAotInfo out = cproGroupAotInfo.get(totalIndex);
				if (in.getGroupId() > out.getGroupId()) {
					log.warn("当前cprogroupinfo表中缺失了:" + out.getGroupId());
					totalIndex++;
				} else if (in.getGroupId() == out.getGroupId()) {
					in.setPrice(out.getPrice());
					executeGroupStat(in);
					totalIndex++;
					innerIndex++;
				} else {
					log.warn("当前cprogroupinfo表中多出了:" + in.getGroupId());
					innerIndex++;
				}
			}
		}
		if (cachedGroupStatInfos.size() > 0) {
			cproGroupStatOnXdbDao.saveGroupStatInfo(cachedGroupStatInfos);
			cachedGroupStatInfos.clear();
		}
	}

	private void executeGroupStat(GroupAotInfo info) {
		CproGroupStatInfo statInfo = new CproGroupStatInfo();
		statInfo.setGroupid(info.getGroupId());
		// 地域信息这块，我信任数据库中没有脏数据，因此为了提高性能，没再对second进行merge，而是直接加和
		if (info.getIsallregion() == CproGroupConstant.GROUP_ALLREGION) {
			statInfo.setFirstregcount(this.firstRegCount);
			statInfo.setSecondregcount(this.secondRegCount);
		} else {
			int firRegC = 0;
			int secRegC = 0;
			String reglist = info.getReglist();
			if (reglist == null || reglist.length() == 0) {
				log.warn("推广组" + info.getGroupId() + "不是全地域投放，却没有reglist信息");
			} else {
				String[] array = reglist.split("\\|");
				if (array != null) {
					for (String s : array) {
						if (s == null || s.length() == 0)
							continue;
						try {
							Integer i = Integer.valueOf(s);
							Integer t = regCodeMap.get(i);
							if (t == null) {
								continue;
							}
							if (t == 0) {
								firRegC++;
								Integer sec = this.firstRegMap.get(i);
								if (sec != null) {
									secRegC += sec.intValue();
								}
							} else {
								secRegC++;
							}
						} catch (Exception e) {
						}
					}
				}
			}
			statInfo.setFirstregcount(firRegC);
			statInfo.setSecondregcount(secRegC);
		}
		// SiteTrade这块，也相信DB中没有脏数据，因此统计时也仅进行加合，没有进行merge
		statInfo.setLastprice(info.getPrice());
		if (info.getIsallsite() == CproGroupConstant.GROUP_ALLSITE) {
			statInfo.setSitefixedsrchs(this.siteFixedSrchs);
			statInfo.setSiteflowsrchs(this.siteFlowSrchs);
			statInfo.setSiteavgprice(this.siteAvgPrice);
		} else {
			long flows = 0;
			long fixed = 0;
			long clks = 0;
			long cost = 0;
			Set<Integer> excludeSiteList = new HashSet<Integer>();
			if (info.getSitetradelist() != null) {
				String[] list = info.getSitetradelist().split("\\|");
				if (list != null) {
					for (String s : list) {
						if (s == null || s.length() == 0)
							continue;
						try {
							Integer i = Integer.valueOf(s);
							if (i == null)
								continue;
							SiteStatVo vo = siteTradeStatMap.get(i);
							if (vo == null)
								continue;
							flows += vo.getSiteflowsrchs();
							fixed += vo.getSitefixedsrchs();
							clks += vo.getClks();
							cost += vo.getCost();
							excludeSiteList.addAll(tradeSiteMap.get(i));
						} catch (Exception e) {
						}
					}
				}
			}
			if (info.getSitelist() != null) {
				String[] list = info.getSitelist().split("\\|");
				if (list != null) {
					for (String s : list) {
						if (s == null || s.length() == 0)
							continue;
						try {
							Integer i = Integer.valueOf(s);
							if (i == null)
								continue;
							SiteStatVo vo = siteStatMap.get(i);
							if (vo == null)
								continue;
							if (excludeSiteList.contains(i)) {
								// 这个site在trade已经统计过了，不再去计算了
								continue;
							}
							flows += vo.getSiteflowsrchs();
							fixed += vo.getSitefixedsrchs();
							clks += vo.getClks();
							cost += vo.getCost();
						} catch (Exception e) {
						}
					}
				}
			}
			statInfo.setSitefixedsrchs(fixed);
			statInfo.setSiteflowsrchs(flows);
			int avg = clks == 0 ? 0 : (int) Math.ceil((double) cost / (double) clks);
			statInfo.setSiteavgprice(avg);
		}
		// 将这个数据存储至数据库中
		queueAndSaveGroupStatInfo(statInfo);
	}

	private void loadPlanInfo() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		cproPlanAotInfo = cproPlanStatDao.findAllPlanInfo(calendar.get(Calendar.DAY_OF_WEEK));
	}

	private void loadGroupPriceInfo() {
		cproGroupAotInfo = cproGroupStatDao.findAllGroupAotInfoOnlyPrice();
	}

	private void loadSiteInfo() {
		List<SiteAotInfo> siteInfos = unionSiteStatDao.findAllSiteAotInfo();
		if (CollectionUtils.isNotEmpty(siteInfos)) {
			long totalClks = 0;
			long totalCost = 0;
			for (SiteAotInfo info : siteInfos) {
				totalClks += info.getClks();
				totalCost += info.getCost();
				// 处理site统计信息
				SiteStatVo siteStat = new SiteStatVo();
				siteStat.setSrchs(info.getSrchs());
				siteStat.setClks(info.getClks());
				siteStat.setCost(info.getCost());
				siteStat.setSiteflowsrchs(info.getSiteflowsrchs());
				siteStat.setSitefixedsrchs(info.getSitefixedsrchs());
				siteStatMap.put(info.getSiteId(), siteStat);
				// 处理firstTrade统计信息
				SiteStatVo firstTradeStat = siteTradeStatMap.get(info.getFirstTradeId());
				if (firstTradeStat == null) {
					firstTradeStat = new SiteStatVo();
					siteTradeStatMap.put(info.getFirstTradeId(), firstTradeStat);
				}
				firstTradeStat.setSrchs(firstTradeStat.getSrchs() + info.getSrchs());
				firstTradeStat.setClks(firstTradeStat.getClks() + info.getClks());
				firstTradeStat.setCost(firstTradeStat.getCost() + info.getCost());
				firstTradeStat.setSitefixedsrchs(firstTradeStat.getSitefixedsrchs() + info.getSitefixedsrchs());
				firstTradeStat.setSiteflowsrchs(firstTradeStat.getSiteflowsrchs() + info.getSiteflowsrchs());
				// 处理secondTrade统计信息
				SiteStatVo secondTradeStat = siteTradeStatMap.get(info.getSecondTradeId());
				if (secondTradeStat == null) {
					secondTradeStat = new SiteStatVo();
					siteTradeStatMap.put(info.getSecondTradeId(), secondTradeStat);
				}
				secondTradeStat.setSrchs(secondTradeStat.getSrchs() + info.getSrchs());
				secondTradeStat.setClks(secondTradeStat.getClks() + info.getClks());
				secondTradeStat.setCost(secondTradeStat.getCost() + info.getCost());
				secondTradeStat.setSitefixedsrchs(secondTradeStat.getSitefixedsrchs() + info.getSitefixedsrchs());
				secondTradeStat.setSiteflowsrchs(secondTradeStat.getSiteflowsrchs() + info.getSiteflowsrchs());
				// 处理TradeMap信息
				List<Integer> firstTradeSiteList = tradeSiteMap.get(info.getFirstTradeId());
				if (firstTradeSiteList == null) {
					firstTradeSiteList = new ArrayList<Integer>();
					tradeSiteMap.put(info.getFirstTradeId(), firstTradeSiteList);
				}
				firstTradeSiteList.add(info.getSiteId());
				List<Integer> secondTradeSiteList = tradeSiteMap.get(info.getSecondTradeId());
				if (secondTradeSiteList == null) {
					secondTradeSiteList = new ArrayList<Integer>();
					tradeSiteMap.put(info.getSecondTradeId(), secondTradeSiteList);
				}
				secondTradeSiteList.add(info.getSiteId());
				// 处理总srchs信息
				siteFixedSrchs += info.getSitefixedsrchs();
				siteFlowSrchs += info.getSiteflowsrchs();
			}
			this.siteAvgPrice = totalClks == 0 ? 0 : (int) Math.ceil((double) totalCost / (double) totalClks);
		}
	}

	public CproGroupStatDao getCproGroupStatDao() {
		return cproGroupStatDao;
	}

	public void setCproGroupStatDao(CproGroupStatDao cproGroupStatDao) {
		this.cproGroupStatDao = cproGroupStatDao;
	}

	public CproPlanStatDao getCproPlanStatDao() {
		return cproPlanStatDao;
	}

	public void setCproPlanStatDao(CproPlanStatDao cproPlanStatDao) {
		this.cproPlanStatDao = cproPlanStatDao;
	}

	public UnionSiteStatDao getUnionSiteStatDao() {
		return unionSiteStatDao;
	}

	public void setUnionSiteStatDao(UnionSiteStatDao unionSiteStatDao) {
		this.unionSiteStatDao = unionSiteStatDao;
	}

	public CodeStatDao getCodeStatDao() {
		return codeStatDao;
	}

	public void setCodeStatDao(CodeStatDao codeStatDao) {
		this.codeStatDao = codeStatDao;
	}

	public void setCproGroupStatOnXdbDao(CproGroupStatOnXdbDao cproGroupStatOnXdbDao) {
		this.cproGroupStatOnXdbDao = cproGroupStatOnXdbDao;
	}

	public void setCproPlanStatOnXdbDao(CproPlanStatOnXdbDao cproPlanStatOnXdbDao) {
		this.cproPlanStatOnXdbDao = cproPlanStatOnXdbDao;
	}

	public CproPlanOfflineStatDao getCproPlanOfflineStatDao() {
		return cproPlanOfflineStatDao;
	}

	public void setCproPlanOfflineStatDao(CproPlanOfflineStatDao cproPlanOfflineStatDao) {
		this.cproPlanOfflineStatDao = cproPlanOfflineStatDao;
	}

}
