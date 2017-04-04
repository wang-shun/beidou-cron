/**
 * 2009-4-27 下午03:11:35
 */
package com.baidu.beidou.unionsite.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.bo.BDSiteBo;
import com.baidu.beidou.unionsite.bo.IPCookieBo;
import com.baidu.beidou.unionsite.bo.QValue;
import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.bo.UnionSiteIndex;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.AdSizeDao;
import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.unionsite.dao.BDSiteStatOnAddbDao;
import com.baidu.beidou.unionsite.service.BDSiteStatService;
import com.baidu.beidou.unionsite.service.DomainComparator;
import com.baidu.beidou.unionsite.service.SiteScaleAlgorithm;
import com.baidu.beidou.unionsite.service.SiteStatUtil;
import com.baidu.beidou.unionsite.vo.SiteBDStatVo;
import com.baidu.beidou.unionsite.vo.SiteCmpLevelCalculateVo;
import com.baidu.beidou.unionsite.vo.SiteInfo4KeepInDB;
import com.baidu.beidou.unionsite.vo.UserSiteVO;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.dao.SequenceIdDaoOnXdb;
import com.baidu.beidou.util.file.IteratorObjectReader;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
/**
 * ClassName:BDSiteStatServiceImpl Function: TODO ADD FUNCTION
 * 
 * @author <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created 2010-12-23
 * @since TODO
 * @version $Id: Exp $
 */
public class BDSiteStatServiceImpl implements BDSiteStatService {
	private static final Log LOG = LogFactory.getLog(BDSiteStatServiceImpl.class);

	private static final Log TEST_LOG = LogFactory.getLog("test");

	private static final int MAX_SIZE = 1000; // 一次插入的最大个数
	private BDSiteStatDao siteDao = null;
	private BDSiteStatOnAddbDao siteOnAddbDao = null;
	private SequenceIdDaoOnXdb sequenceIdDaoOnXdb = null;
	private SiteScaleAlgorithm scaleAlgorithm = null;
	private static final int SCALE = 8;
	private static final double MAX_ABS = 0.00000001; // 绝对值小于该值时认为网站等级的F值相等
	private double standard_q1 = 50;
	private double standard_q2 = 50;

	// 5星级站点，为序列L前N*4%（向下去整，下面处理相同）个站点；
	// 4星级站点，为序列L前N*20%个站点去除5星级站点；对于N站点中余下的（即排除5星和4星的部分）：
	// F值降序排列前80%为3星级站点，后20%为2级站点。
	private static double FIFTH_SCALE_THRESHOLD = 0.04;
	private static double FOURTH_SCALE_THRESHOLD = 0.2;
	private static double THIRD_SCALE_THRESHOLD = 0.8;

	// 竞争非常激烈 rate_compete>=0.9
	// 竞争比较激烈 30% 30%*N
	// 竞争度一般 55% 25%*N
	// 竞争比较缓和 80% 25%*N
	// 竞争较少 100% 20%*N
	private static double RATE_COMPETE_THRESHOLD = 0.9; // rate_compete>=该值的站点的热度最高
	private static double SECOND_LEVEL_THRESHOLD = 0.3;
	private static double THIRD_LEVEL_THRESHOLD = 0.55;
	private static double FOURTH_LEVEL_THRESHOLD = 0.8;

	private AdSizeDao adSizeDao = null;

	private class UnionSiteAndDomain {
		// 保存获取一批同主域的联盟站点的返回值
		private String mainDomain;
		private UnionSiteIndex next;
	}

	/** 用户投放信息记录，从原来的BDSiteStatDaoImpl中迁移过来 */
	private class UserSiteInfo {
		int preUserid = 0;
		Set<Integer> preSiteSet = new HashSet<Integer>(10000);
		Set<Integer> preTradeSet = new HashSet<Integer>(100);
		boolean allSite = false;

		void clear(int userid) {
			preUserid = userid;
			preSiteSet = new HashSet<Integer>(10000);
			preTradeSet = new HashSet<Integer>(100);
			allSite = false;
		}
	}

	/**
	 * 计算，并存入数据库(包括基本的展现信息合并，等级，热度等计算)
	 * 
	 * @author zengyunfeng
	 * @param unionsiteFile
	 * @param unionSiteList
	 * @param qList
	 * @param siteStatList
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public SiteCmpLevelCalculateVo bdSiteStore(final String unionsiteFile, final String unionSiteListFile, final String qListFile, final String siteStatListFile, final Set<String> currentValidDomain) throws InternalException, IOException, ClassNotFoundException {
		LOG.info("Begin to bdSiteStore");

		// 文件遍历句柄
		IteratorObjectReader<UnionSiteIndex> unionSiteIterator = null;
		IteratorObjectReader<QValue> qIterator = null;
		IteratorObjectReader<SiteStatBo> siteStatIterator = null;
		IteratorObjectReader<IPCookieBo> siteIpCookieStatIterator = null;
		RandomAccessFile unionSiteReader = null;

		Map<String, SiteInfo4KeepInDB> currentValidSiteUrls = siteDao.findAllValidSiteUrlAndJointime();// 有效网站的当前加入时间
		Date now = new Date();// 当前时间

		// 把所有有效的站点设置为待处理
		siteDao.updateSiteStatusDealing();
		try {
			qIterator = new IteratorObjectReader<QValue>(qListFile);
			unionSiteIterator = new IteratorObjectReader<UnionSiteIndex>(unionSiteListFile);
			siteStatIterator = new IteratorObjectReader<SiteStatBo>(siteStatListFile);
			siteIpCookieStatIterator = new IteratorObjectReader<IPCookieBo>(siteStatListFile + SiteStatServiceImpl.ipcookieFileSuffix);

			unionSiteReader = new RandomAccessFile(unionsiteFile, "r");
			// 数据库插入过程中的临时数据结构
			final List<UnionSiteIndex> curDomainSites = new ArrayList<UnionSiteIndex>();
			final List<SiteStatBo> curSiteStats = new ArrayList<SiteStatBo>();
			// 声明为LinkedHashMap，保证后面使用curBDSite.values()时，安装插入的顺序取出值
			// @version 1.0.12
			final Map<String, BDSiteBo> curBDSite = new LinkedHashMap<String, BDSiteBo>();
			final List<SiteBDStatVo> curCmpBDStatList = new ArrayList<SiteBDStatVo>(MAX_SIZE + 10); // 用于记录当前的批次的站点id,用于计算cmp和scale
			SiteStatBo bdSiteStat = null;
			List<BDSiteBo> unStoredBDSiteList = new ArrayList<BDSiteBo>(MAX_SIZE + 10);
			List<SiteBDStatVo> uninitBDStatList = new ArrayList<SiteBDStatVo>(MAX_SIZE + 10);
			SiteStatBo nextSiteStat = null;
			QValue nextQValues = null;
			IPCookieBo nextIpCookie = null;
			UnionSiteAndDomain domainAndNext = new UnionSiteAndDomain();
			BDSiteBo bdSite = null;

			// Iterator<QValue> qIterator = qList.iterator();
			// Iterator<SiteStatBo> siteStatIterator = siteStatList.iterator();
			// Iterator<UnionSiteIndex> unionSiteIterator =
			// unionSiteList.iterator();

			// 存储返回的信息
			SiteCmpLevelCalculateVo result = new SiteCmpLevelCalculateVo();
			List<SiteBDStatVo> sitecmpList = new ArrayList<SiteBDStatVo>(10000); // 返回的网站id
			// list
			SiteBDStatVo sitecmpVo = null;
			result.setSiteList(sitecmpList);

			// 全库的最小值，最大值，用于记录等级
			long minRetrieveForCmp = Long.MAX_VALUE; // 用于等级的计算的最小值
			long minRetrieve = Long.MAX_VALUE;
			double minCtr2 = Double.MAX_VALUE;
			double minNum_ad_retrieve = Double.MAX_VALUE;
			int minNum_ip = Integer.MAX_VALUE;
			int minNum_cookie = Integer.MAX_VALUE;

			long maxRetrieveForCmp = 0; // 用于等级的计算的最大值
			long maxRetrieve = 0;
			double maxCtr2 = 0;
			double maxNum_ad_retrieve = 0;
			int maxNum_ip = 0;
			int maxNum_cookie = 0;

			int parentid = 0;
			boolean firstDomain = true;

			int maxSizeId = adSizeDao.getMaxSizeId();

			while ((nextSiteStat != null || siteStatIterator.hasNext()) && (domainAndNext.next != null || unionSiteIterator.hasNext())) {
				curBDSite.clear();
				// 取unionSiteList中一级域名相同的一批记录
				curDomainSites.clear();
				curSiteStats.clear();
				curCmpBDStatList.clear();
				firstDomain = true;
				domainAndNext = getSitesWithSameDomain(curDomainSites, unionSiteIterator, domainAndNext.next);
				// 没有取道新的域名，将全部处理完成
				if (domainAndNext == null || domainAndNext.mainDomain == null || curDomainSites.isEmpty()) {
					break;
				}
				nextSiteStat = getSiteStatWithSameDomain(curSiteStats, domainAndNext.mainDomain, siteStatIterator, nextSiteStat);
				// 改批域名没有对应的stat数据，该批域名不需要处理
				if (curSiteStats.isEmpty()) {
					continue;
				}

				// 过滤没有计费名的记录
				fileterNoCname(curDomainSites, curSiteStats);

				if (curDomainSites == null || curDomainSites.isEmpty()) {
					// 没有记录
					continue;
				}

				for (UnionSiteIndex site : curDomainSites) {
					bdSite = curBDSite.get(site.getDomain());
					if (bdSite == null) {
						bdSite = new BDSiteBo();
						// 计算统计数据
						bdSiteStat = mergeSiteStat(site, curSiteStats);
						if (bdSiteStat != null) { // 必须，因为前面的过滤只是判断计费名相同，并没有对二级域名判断相同，因此此处可以存在返回为null的情况
							int id = siteDao.findIdByUrl(site.getDomain());
							int score = 0;	//added by lvzichan,已在北斗库中的网站得分
							if (id < 1) {
								// 新增的站点ID
								id = sequenceIdDaoOnXdb.getUnionSiteidTypeId().intValue();
							} else {
								score = siteDao.findScoreById(id);
							}
							bdSite.setScore(score);
							if (firstDomain) {
								firstDomain = false;
								if (domainAndNext.mainDomain.equals(site.getDomain())) {
									// 为主域，除了主域外当前批次的域名的parentid=id，
									parentid = id;
								} else {
									parentid = 0;
								}
								bdSite.setParentid(0);
							} else {
								bdSite.setParentid(parentid);
							}
							bdSite.setSiteid(id);
							bdSite.setSite(site);
							QValue[] qValuePair = getQValue(site.getDomain(), qIterator, nextQValues);
							nextQValues = qValuePair[1];
							bdSite.setQValue(qValuePair[0]);

							IPCookieBo[] ipCookiePair = getIpCookie(site.getDomain(), siteIpCookieStatIterator, nextIpCookie);
							if (ipCookiePair != null && ipCookiePair[0] != null) {
								bdSiteStat.setUnique_ip(ipCookiePair[0].getUnique_ip());
								bdSiteStat.setUnique_cookie(ipCookiePair[0].getUnique_cookie());
							}
							nextIpCookie = ipCookiePair[1];
							bdSite.setStat(bdSiteStat);
							// 设置流量等级和尺寸流量等级
							bdSite.setThruputtype(SiteConstant.getSiteThruputType(bdSite.getStat().getRetrieve()));
							bdSite.setSizethruput(SiteConstant.getSizeThruputType(maxSizeId, bdSite.getStat().getSizeFlow()));
							curBDSite.put(site.getDomain(), bdSite);

							// 设置返回值
							sitecmpVo = new SiteBDStatVo();
							sitecmpVo.setSiteid(bdSite.getSiteid());
							sitecmpVo.setDomainFlag(bdSite.getSite().getDomainFlag());
							sitecmpVo.setParentid(bdSite.getParentid());
							if (bdSite.getQValue() == null) {
								sitecmpVo.setQ1(null);
								sitecmpVo.setQ2(null);
							} else {
								sitecmpVo.setQ1(bdSite.getQValue().getQ1());
								sitecmpVo.setQ2(bdSite.getQValue().getQ2());
							}

							sitecmpVo.setRetrieve(bdSite.getStat().getRetrieve());
							sitecmpVo.setUnique_cookie(bdSite.getStat().getUnique_cookie());
							sitecmpVo.setUnique_ip(bdSite.getStat().getUnique_ip());
							if (bdSiteStat.getAds() != 0) {
								sitecmpVo.setCtr2(bdSiteStat.getClicks() / (double) bdSiteStat.getAds());
							}
							if (bdSiteStat.getRetrieve() != 0) {
								sitecmpVo.setNum_ad_retrieve(bdSiteStat.getAds() / (double) bdSiteStat.getRetrieve());
							}
							curCmpBDStatList.add(sitecmpVo);

							// 判断最小值
							if (bdSite.getParentid() == 0) {
								if (minRetrieveForCmp > sitecmpVo.getRetrieve()) {
									minRetrieveForCmp = sitecmpVo.getRetrieve();
								}
								if (maxRetrieveForCmp < sitecmpVo.getRetrieve()) {
									maxRetrieveForCmp = sitecmpVo.getRetrieve();
								}
								if ((sitecmpVo.getRetrieve() > 0 && sitecmpVo.getCtr2() > 0 && sitecmpVo.getNum_ad_retrieve() > 0 && sitecmpVo.getUnique_ip() > 0 && sitecmpVo.getUnique_cookie() > 0)) {
									if (minRetrieve > sitecmpVo.getRetrieve()) {
										minRetrieve = sitecmpVo.getRetrieve();
									}
									if (minCtr2 > sitecmpVo.getCtr2()) {
										minCtr2 = sitecmpVo.getCtr2();
									}
									if (minNum_ad_retrieve > sitecmpVo.getNum_ad_retrieve()) {
										minNum_ad_retrieve = sitecmpVo.getNum_ad_retrieve();
									}
									if (minNum_ip > sitecmpVo.getUnique_ip()) {
										minNum_ip = sitecmpVo.getUnique_ip();
									}
									if (minNum_cookie > sitecmpVo.getUnique_cookie()) {
										minNum_cookie = sitecmpVo.getUnique_cookie();
									}
									if (maxRetrieve < sitecmpVo.getRetrieve()) {
										maxRetrieve = sitecmpVo.getRetrieve();
									}
									if (maxCtr2 < sitecmpVo.getCtr2()) {
										maxCtr2 = sitecmpVo.getCtr2();
									}
									if (maxNum_ad_retrieve < sitecmpVo.getNum_ad_retrieve()) {
										maxNum_ad_retrieve = sitecmpVo.getNum_ad_retrieve();
									}
									if (maxNum_ip < sitecmpVo.getUnique_ip()) {
										maxNum_ip = sitecmpVo.getUnique_ip();
									}
									if (maxNum_cookie < sitecmpVo.getUnique_cookie()) {
										maxNum_cookie = sitecmpVo.getUnique_cookie();
									}
								}
							}
						}
					} else {
						// 覆盖
						bdSite.setSite(site);
					}

					// -------------------------------->add by
					// liangshimu,20100916,Cpweb-168
					// 给BDSite添加加入时间，规则为：当前有效网站的加入时间不变，其他情况的加入时间为当前时间

					UnionSiteIndex index = bdSite.getSite();
					SiteInfo4KeepInDB info = index == null ? null : currentValidSiteUrls.get(index.getDomain());

					Date joinTime = (info == null || !info.isValid()) ? null : info.getJoinTime();
					if (joinTime == null) {
						joinTime = now;
					}
					bdSite.setJoinTime(joinTime);
					// <--------------------------------end
					// -------------------------------->add by
					// liangshimu,20101223,Cpweb-218
					// 网站是否当前已经失效，主要是看今天的统计文件中有没有该网站信息，有则没有失效，反之则失效
					bdSite.setCurrentValid(index != null && currentValidDomain.contains(index.getDomain()));
					// <--------------------------------end

					if (info != null) {
						bdSite.setSnapshot(info.getSnapshot() == null ? "" : info.getSnapshot());
					}
				}

				// 必须放在前面，防止先插入，然后更新nextid失败，
				// 如果先更新nextid，然后插入失败，只是会产生一些id的空洞
				// 用mysql的get_next_value()，在获取nextid的同时，已经将nextid加1了，此处不用在存储nextid了
				// siteOnAddbDao.restorNextid(nextId);

				sitecmpList.addAll(curCmpBDStatList);
				uninitBDStatList.addAll(curCmpBDStatList);
				unStoredBDSiteList.addAll(curBDSite.values());
				// 插入数据库，并把插入的数据从unStoredBDSiteList和uninitBDStatList中删除
				storePagedBDSite(unStoredBDSiteList, unionSiteReader, uninitBDStatList);
			}

			storeBDSite(unStoredBDSiteList, unionSiteReader, uninitBDStatList);

			// 计算等级,热度,为了节省内存单独更新数据库
			result.setMaxCtr2(maxCtr2);
			result.setMaxNum_ad_retrieve(maxNum_ad_retrieve);
			result.setMaxNum_cookie(maxNum_cookie);
			result.setMaxNum_ip(maxNum_ip);
			result.setMaxRetrieve(maxRetrieve);
			result.setMaxRetrieveForCmp(maxRetrieveForCmp);

			result.setMinCtr2(minCtr2);
			result.setMinNum_ad_retrieve(minNum_ad_retrieve);
			result.setMinNum_cookie(minNum_cookie);
			result.setMinNum_ip(minNum_ip);
			result.setMinRetrieve(minRetrieve);
			result.setMinRetrieveForCmp(minRetrieveForCmp);

			result.setSiteList(sitecmpList);

			// 把所有待处理的站点设置为失效，同时将当前失效(currentValid)也设置为1(失效)
			siteDao.updateSiteStatusInvalid();
			return result;
		} catch (FileNotFoundException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} catch (ClassNotFoundException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			if (unionSiteIterator != null) {
				try {
					unionSiteIterator.close();
				} catch (IOException e) {
				}
			}
			if (qIterator != null) {
				try {
					qIterator.close();
				} catch (IOException e) {
				}

			}
			if (siteStatIterator != null) {
				try {
					siteStatIterator.close();
				} catch (IOException e) {
				}
			}

			if (unionSiteReader != null) {
				try {
					unionSiteReader.close();
				} catch (IOException e) {
				}
			}
			LOG.info("End to bdSiteStore");
		}

	}

	/**
	 * 计算全库站点的等级和热度： cmplevel , ratecmp, scorecmp;
	 * 
	 * @author zengyunfeng
	 * @param siteid
	 */
	public void bdSiteCalculate(SiteCmpLevelCalculateVo siteListVo) {
		LOG.info("Begin to bdSiteCalculate");

		// 计算等级
		calculateScale(siteListVo.getSiteList(), siteListVo.getMinRetrieve(), siteListVo.getMinCtr2(), siteListVo.getMinNum_ad_retrieve(), siteListVo.getMinNum_ip(), siteListVo.getMinNum_cookie(), siteListVo.getMaxRetrieve(), siteListVo.getMaxCtr2(), siteListVo.getMaxNum_ad_retrieve(),
				siteListVo.getMaxNum_ip(), siteListVo.getMaxNum_cookie());

		// 计算热度
		LOG.info(Runtime.getRuntime().freeMemory());

		calculateCompete(siteListVo.getSiteList(), siteListVo.getMaxRetrieveForCmp(), siteListVo.getMinRetrieveForCmp());

		// 对有一级域名的二级域名站点设置等级和热度
		byte prescale = 0;
		byte precmplevel = 0;
		double preratecmp = 0;
		double prescorecmp = 0;
		for (int index = 0; index < siteListVo.getSiteList().size(); index++) {
			SiteBDStatVo site = siteListVo.getSiteList().get(index);
			if (site.getParentid() == 0) {
				prescale = site.getScale();
				precmplevel = site.getCmplevel();
				preratecmp = site.getRatecmp();
				prescorecmp = site.getScorecmp();
			} else {
				site.setScale(prescale);
				site.setCmplevel(precmplevel);
				site.setRatecmp(preratecmp);
				site.setScorecmp(prescorecmp);
			}
		}
		updateScaleAndCompete(siteListVo.getSiteList());

		// 测试使用中间数据
		debugOut(siteListVo);
		LOG.info("End to bdSiteCalculate");

	}

	// 测试使用中间数据
	private void debugOut(SiteCmpLevelCalculateVo siteListVo) {

		TEST_LOG.debug("maxCtr2=" + siteListVo.getMaxCtr2());
		TEST_LOG.debug("maxNum_ad_retrieve=" + siteListVo.getMaxNum_ad_retrieve());
		TEST_LOG.debug("maxNum_cookie=" + siteListVo.getMaxNum_cookie());
		TEST_LOG.debug("maxNum_ip=" + siteListVo.getMaxNum_ip());
		TEST_LOG.debug("maxRetrieve=" + siteListVo.getMaxRetrieve());
		TEST_LOG.debug("maxRetrieveForCmp=" + siteListVo.getMaxRetrieveForCmp());
		TEST_LOG.debug("minCtr2=" + String.valueOf(siteListVo.getMinCtr2()));
		TEST_LOG.debug("minNum_ad_retrieve=" + siteListVo.getMinNum_ad_retrieve());
		TEST_LOG.debug("minNum_cookie=" + siteListVo.getMinNum_cookie());
		TEST_LOG.debug("minNum_ip=" + siteListVo.getMinNum_ip());
		TEST_LOG.debug("minRetrieve=" + siteListVo.getMinRetrieve());
		TEST_LOG.debug("minRetrieveForCmp=" + siteListVo.getMinRetrieveForCmp());

		for (SiteBDStatVo bo : siteListVo.getSiteList()) {
			TEST_LOG.debug("[scale]" + bo.toString());
		}

	}

	/**
	 * 分批更新数据
	 * 
	 * @author zengyunfeng
	 * @param siteList
	 */
	private void updateScaleAndCompete(List<SiteBDStatVo> siteList) {
		LOG.info("Begin to updateScaleAndCompete");

		int size = siteList.size();
		for (int fromIndex = 0, toIndex = MAX_SIZE; fromIndex < size; fromIndex = toIndex) {
			toIndex = fromIndex + MAX_SIZE;
			if (toIndex > size) {
				toIndex = size;
			}
			siteDao.updateSiteScaleAndCmp(siteList.subList(fromIndex, toIndex));
		}
		LOG.info("Begin to updateScaleAndCompete");
	}

	/**
	 * 计算等级
	 * 
	 * @author zengyunfeng
	 * @param bdSiteList
	 * @param minRetrieve
	 * @param minCtr2
	 * @param minNum_ad_retrieve
	 * @param minNum_ip
	 * @param minNum_cookie
	 * @param maxRetrieve
	 * @param maxCtr2
	 * @param maxNum_ad_retrieve
	 * @param maxNum_ip
	 * @param maxNum_cookie
	 */
	private void calculateScale(final List<SiteBDStatVo> bdSiteList, long minRetrieve, double minCtr2, double minNum_ad_retrieve, int minNum_ip, int minNum_cookie, long maxRetrieve, double maxCtr2, double maxNum_ad_retrieve, int maxNum_ip, int maxNum_cookie) {
		LOG.info("Begin to calculateScale");

		List<SiteBDStatVo> scaleSiteList = new ArrayList<SiteBDStatVo>(10000);
		long disRetrieve = maxRetrieve - minRetrieve;
		double disCtr2 = maxCtr2 - minCtr2;
		double disNum_ad_retrieve = maxNum_ad_retrieve - minNum_ad_retrieve;
		int disNum_ip = maxNum_ip - minNum_ip;
		int disNum_cookie = maxNum_cookie - minNum_cookie;

		BigDecimal z1 = null;
		BigDecimal z2 = null;
		BigDecimal z3 = null;
		BigDecimal z4 = null;
		BigDecimal z5 = null;
		double F = 0;
		for (SiteBDStatVo site : bdSiteList) {
			if (site.getParentid() == 0) {
				if (site.getRetrieve() == 0 || site.getCtr2() == 0 || site.getNum_ad_retrieve() == 0 || site.getUnique_ip() == 0 || site.getUnique_cookie() == 0) {
					site.setScale(SiteConstant.SCALE[SiteConstant.SCALE.length - 1]);
					continue;
				}

				// zi=[xi-min(xLi)]/[max(xLi)-min(xLi)]
				if (disRetrieve == 0) {
					z1 = BigDecimal.ZERO.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				} else {
					z1 = BigDecimal.valueOf((site.getRetrieve() - minRetrieve) / (double) disRetrieve).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				}

				if (disCtr2 == 0) {
					z2 = BigDecimal.ZERO.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				} else {
					z2 = BigDecimal.valueOf((site.getCtr2() - minCtr2) / disCtr2).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				}

				if (disNum_ad_retrieve == 0) {
					z3 = BigDecimal.ZERO.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				} else {
					z3 = BigDecimal.valueOf((site.getNum_ad_retrieve() - minNum_ad_retrieve) / disNum_ad_retrieve).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				}

				if (disNum_ip == 0) {
					z4 = BigDecimal.ZERO.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				} else {
					z4 = BigDecimal.valueOf((site.getUnique_ip() - minNum_ip) / (double) disNum_ip).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				}

				if (disNum_cookie == 0) {
					z5 = BigDecimal.ZERO.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				} else {
					z5 = BigDecimal.valueOf((site.getUnique_cookie() - minNum_cookie) / (double) disNum_cookie).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
				}

				F = scaleAlgorithm.calculateF(z1.doubleValue(), z2.doubleValue(), z3.doubleValue(), z4.doubleValue(), z5.doubleValue());
				site.setF(F);
				scaleSiteList.add(site);
			}
		}

		// scaleSiteList对F值进行降序排列
		Collections.sort(scaleSiteList, new Comparator<SiteBDStatVo>() {

			public int compare(SiteBDStatVo o1, SiteBDStatVo o2) {
				double cmp = o1.getF() - o2.getF();
				if (cmp < 0) {
					return 1;
				} else if (cmp == 0) {
					return 0;
				} else {
					return -1;
				}
			}

		});
		// 5星级站点，为序列L前N*4%（向下去整，下面处理相同）个站点；
		// 4星级站点，为序列L前N*20%个站点去除5星级站点；对于N站点中余下的（即排除5星和4星的部分）：
		// F值降序排列前80%为3星级站点，后20%为2级站点。
		int size = scaleSiteList.size();
		int fiveIndex = (int) (size * FIFTH_SCALE_THRESHOLD);
		fiveIndex = getTailF(scaleSiteList, fiveIndex);
		int fourIndex = (int) (size * FOURTH_SCALE_THRESHOLD);
		fourIndex = getTailF(scaleSiteList, fourIndex);

		int threeIndex = fourIndex + (int) ((size - fourIndex) * THIRD_SCALE_THRESHOLD);
		threeIndex = getTailF(scaleSiteList, threeIndex);
		// 设置五星等级
		for (int index = 0; index < fiveIndex && index < size; index++) {
			if (scaleSiteList.get(index).getQ1() != null && scaleSiteList.get(index).getQ2() != null && (scaleSiteList.get(index).getQ1().floatValue() < standard_q1 || scaleSiteList.get(index).getQ2().floatValue() < standard_q2)) {
				scaleSiteList.get(index).setScale(SiteConstant.SCALE[SiteConstant.SCALE.length - 1]);
			} else {
				scaleSiteList.get(index).setScale(SiteConstant.SCALE[0]);
			}
		}

		// 设置四星等级
		for (int index = fiveIndex; index < fourIndex && index < size; index++) {
			if (scaleSiteList.get(index).getQ1() != null && scaleSiteList.get(index).getQ2() != null && (scaleSiteList.get(index).getQ1().floatValue() < standard_q1 || scaleSiteList.get(index).getQ2().floatValue() < standard_q2)) {
				scaleSiteList.get(index).setScale(SiteConstant.SCALE[SiteConstant.SCALE.length - 1]);
			} else {
				scaleSiteList.get(index).setScale(SiteConstant.SCALE[1]);
			}
		}

		// 设置三星等级
		for (int index = fourIndex; index < threeIndex && index < size; index++) {
			if (scaleSiteList.get(index).getQ1() != null && scaleSiteList.get(index).getQ2() != null && (scaleSiteList.get(index).getQ1().floatValue() < standard_q1 || scaleSiteList.get(index).getQ2().floatValue() < standard_q2)) {
				scaleSiteList.get(index).setScale(SiteConstant.SCALE[SiteConstant.SCALE.length - 1]);
			} else {
				scaleSiteList.get(index).setScale(SiteConstant.SCALE[2]);
			}
		}

		// 设置二星等级
		for (int index = threeIndex; index < size; index++) {
			scaleSiteList.get(index).setScale(SiteConstant.SCALE[SiteConstant.SCALE.length - 1]);
		}
		LOG.info("End to calculateScale");
	}

	private void updateTradeSiteList(Map<Integer, Set<Integer>> tradeSite, int siteid, int tradeid) {
		if (siteid == 0 || tradeid == 0) {
			return;
		}
		Set<Integer> trade = tradeSite.get(tradeid);
		if (trade == null) {
			trade = new HashSet<Integer>();
			tradeSite.put(tradeid, trade);
		}
		trade.add(siteid);
	}

	/**
	 * 计算热度
	 * 
	 * @author zengyunfeng
	 * @param bdSiteList
	 * @param maxRetrieve
	 */
	private void calculateCompete(final List<SiteBDStatVo> allbdSiteList, long maxRetrieve, long minRetrieve) {
		/**
		 * modify by liangshimu, 20100708：
		 * 在之前的设计中是因为考虑到统计一个站点被多少个用户选取了时要记录所有用户的ID，因此内存成为性能瓶颈，所以此处对所有的站点采用分批处理。
		 * 而在上一次优化过程已经不用记录所有的用户ID，只用记录用户个数就行了，因此以前担心的内存问题在此处不用考虑，所以不用分批统计。
		 * （原来统计一批时间大约3-4分钟，而需要分成20批，所以执行时间大约为60分钟，按一批来统计的话执行时间也就在几分钟左右）。
		 * 但同时考虑到当前从数据库中加载用户投放信息的记录条数已经达到18W条
		 * ，一条记录占用的内存大小大约为600B，所以当前18W使用的内存约100M，考虑到以后的业务扩展，
		 * 此处对从DB中加载域名用户信息时按批次来，一次最多加载50W条（做成参数，可配置）。
		 */
		LOG.info("Begin to calculateCompete");
		int usercnt = siteOnAddbDao.getAvailUserCount();
		Map<Integer, Integer> siteCompeteMap = new HashMap<Integer, Integer>((int) (allbdSiteList.size() / 0.9 + 1), 0.9f);

		// 获得分类对应的所有的站点，站点id对应的父站点id。
		// 分类id，对应为tradeSite的序号
		Map<Integer, Set<Integer>> tradeSite = new HashMap<Integer, Set<Integer>>(200, 0.9f);
		// 站点id为key,父站点id为value,如何没有父站点，这不存入map中
		Map<Integer, Integer> siteRelation = new HashMap<Integer, Integer>(1000, 0.9f);
		List<SiteBDStatVo> bdSiteList = new ArrayList<SiteBDStatVo>(10000);// 需要计算热度的站点

		double rate_compete;// 计算rate_compete
		double maxRate_compete = Double.MIN_VALUE;
		double minRate_compete = Double.MAX_VALUE;
		final UserSiteInfo preUserSiteInfo = new UserSiteInfo();// 存放前一个用户的投放信息

		// 设置分类和站点关系
		for (SiteBDStatVo site : allbdSiteList) {
			// 设置分类
			updateTradeSiteList(tradeSite, site.getSiteid(), site.getFirsttradeid());
			updateTradeSiteList(tradeSite, site.getSiteid(), site.getSecondtradeid());
			// 设置站点关系
			if (site.getParentid() == 0) {
				bdSiteList.add(site);
				siteCompeteMap.put(site.getSiteid(), Integer.valueOf(0));
			} else {
				siteRelation.put(site.getSiteid(), site.getParentid());
			}
		}
		LOG.info("End to init set and map, siteRelation.size=" + siteRelation.size() + "\ttradeSite.size=" + tradeSite.size() + "\tbdSiteList.size=" + bdSiteList.size() + "\tsiteCompeteMap.size=" + siteCompeteMap.size());

		List<UserSiteVO> list = siteOnAddbDao.statSiteUserVo();
		statSiteUser(siteCompeteMap, tradeSite, siteRelation, list, preUserSiteInfo);

		// 处理最后一个用户
		updateSiteUserCompete(siteCompeteMap, preUserSiteInfo, tradeSite, siteRelation);

		// 计算rate_compete
		for (SiteBDStatVo vo : bdSiteList) {
			Integer competeObject = siteCompeteMap.get(vo.getSiteid());
			int compete = 0;
			if (competeObject != null) {
				compete = competeObject.intValue();
			}
			rate_compete = compete / (double) usercnt;
			vo.setRatecmp(rate_compete);
			if (rate_compete > maxRate_compete) {
				maxRate_compete = rate_compete;
			}
			if (rate_compete < minRate_compete) {
				minRate_compete = rate_compete;
			}

			TEST_LOG.debug("[comepete]" + vo.getSiteid() + '\t' + compete + '\t' + rate_compete);

		}
		double maxRate_retrieve = 1;
		double maxRetrievePow = Math.pow(maxRetrieve, 0.5);
		double minRate_retrieve = Math.pow(minRetrieve, 0.5) / maxRetrievePow;
		double disRate_retrieve = maxRate_retrieve - minRate_retrieve;
		double z_rate_retrieve;
		double disRate_Compete = maxRate_compete - minRate_compete;
		double z_rate_compete;
		double rate_retrieve;
		double score_compete;
		double maxScore_comete = Double.MIN_VALUE;

		boolean retrieveSame = false;
		if (maxRetrieve == minRetrieve) {
			retrieveSame = true;
		}
		boolean rateCompeteSame = false;
		if (Math.abs(disRate_Compete) < 1E-10) {
			rateCompeteSame = true;
		}

		List<SiteBDStatVo> scoreSortList = new ArrayList<SiteBDStatVo>();
		for (int index = 0; index < bdSiteList.size(); index++) {

			rate_retrieve = Math.pow(bdSiteList.get(index).getRetrieve(), 0.5) / maxRetrievePow;
			// 没有设置score_compete
			// rate_retrieve标准化
			if (retrieveSame) {
				z_rate_retrieve = 1;
			} else {
				z_rate_retrieve = (rate_retrieve - minRate_retrieve) / disRate_retrieve;
			}
			// rate_compete标准化
			if (rateCompeteSame) {
				z_rate_compete = 1;
			} else {
				z_rate_compete = (bdSiteList.get(index).getRatecmp() - minRate_compete) / disRate_Compete;
			}
			if (z_rate_retrieve == 0) {
				score_compete = -1; // 标志为最大值，全部
			} else {
				score_compete = z_rate_compete / z_rate_retrieve;
			}
			bdSiteList.get(index).setScorecmp(score_compete);
			if (score_compete > maxScore_comete) {
				maxScore_comete = score_compete;
			}

			scoreSortList.add(bdSiteList.get(index));
		}
		// 对z_rate_retrieve为0的站点设置score_compete, rate_compete>=0.9的激烈度设为最高级
		int firstlevelCnt = 0;
		for (SiteBDStatVo site : bdSiteList) {
			if (site.getScorecmp() < 0) {
				site.setScorecmp(maxScore_comete);
			}
			if (site.getRatecmp() >= RATE_COMPETE_THRESHOLD) {
				site.setCmplevel(SiteConstant.COMPETE_LEVEL[0]);
				firstlevelCnt++;
			}
		}

		Collections.sort(scoreSortList, new Comparator<SiteBDStatVo>() {

			public int compare(SiteBDStatVo o1, SiteBDStatVo o2) {
				if (o1.getRatecmp() >= RATE_COMPETE_THRESHOLD || o2.getRatecmp() >= RATE_COMPETE_THRESHOLD) {
					// 只要有一个ratecmp>=RATE_COMPETE_THRESHOLD，就rate_compete desc,
					// sore_compete desc进行排序
					double cmp = o1.getRatecmp() - o2.getRatecmp();
					if (cmp < 0) {
						return 1;
					} else if (cmp == 0) {
						double cmp2 = o1.getScorecmp() - o2.getScorecmp();
						if (cmp2 < 0) {
							return 1;
						} else if (cmp2 == 0) {
							return 0;
						} else {
							return -1;
						}
					} else {
						return -1;
					}
				} else {
					// ratecmp<RATE_COMPETE_THRESHOLD，就sore_compete desc进行排序
					double cmp2 = o1.getScorecmp() - o2.getScorecmp();
					if (cmp2 < 0) {
						return 1;
					} else if (cmp2 == 0) {
						return 0;
					} else {
						return -1;
					}
				}
			}

		});

		int size = scoreSortList.size();

		// 竞争比较激烈 30% 30%*N
		// 竞争度一般 55% 25%*N
		// 竞争比较缓和 80% 25%*N
		// 竞争较少 100% 20%*N
		int N = size - firstlevelCnt;
		int secondIndex = firstlevelCnt + (int) (N * SECOND_LEVEL_THRESHOLD);
		if (secondIndex != firstlevelCnt) {
			// secondIndex = getTailScoreCompete(scoreSortList, secondIndex);
			for (int index = firstlevelCnt; index < secondIndex && index < size; index++) {
				scoreSortList.get(index).setCmplevel(SiteConstant.COMPETE_LEVEL[1]);
			}
		}

		int thirdIndex = firstlevelCnt + (int) (N * THIRD_LEVEL_THRESHOLD);
		if (thirdIndex != secondIndex) {
			// thirdIndex = getTailScoreCompete(scoreSortList, thirdIndex);
			for (int index = secondIndex; index < thirdIndex && index < size; index++) {
				scoreSortList.get(index).setCmplevel(SiteConstant.COMPETE_LEVEL[2]);
			}
		}

		int forthIndex = firstlevelCnt + (int) (N * FOURTH_LEVEL_THRESHOLD);
		if (forthIndex != thirdIndex) {
			// forthIndex = getTailScoreCompete(scoreSortList, forthIndex);
			for (int index = thirdIndex; index < forthIndex && index < size; index++) {
				scoreSortList.get(index).setCmplevel(SiteConstant.COMPETE_LEVEL[3]);
			}
		}

		for (int index = forthIndex; index < size; index++) {
			scoreSortList.get(index).setCmplevel(SiteConstant.COMPETE_LEVEL[4]);
		}
		LOG.info("End to calculateCompete");
	}

	/**
	 * 从start开始（包括start）后面第一个与start-1的F值不一样的序号, <br>
	 * 如果start<=0, 返回0
	 * 
	 * @author zengyunfeng
	 * @param list
	 * @param start
	 * @return
	 */
	private int getTailF(final List<SiteBDStatVo> list, final int start) {
		if (start <= 0) {
			return 0;
		}
		int index = start;
		int size = list.size();
		if (index > size) {
			index = size;
		}
		for (; index < size; index++) {
			if (Math.abs(list.get(index).getF() - list.get(start - 1).getF()) < MAX_ABS) {
				index++;
			} else {
				break;
			}
		}
		return index;
	}

	/**
	 * 更新bdSiteList中的整页信息
	 * 
	 * 有一级域名的二级域名的网站等级 如果bdSiteList>MAX_SIZE，则批量保存数据，
	 * 并且设置uninitBDStatList中的firsttradeid, secondtradeid
	 * 
	 * @author zengyunfeng
	 * @param bdSiteList
	 * @param unionsiteFile
	 * @param uninitBDStatList
	 * @param newPos
	 *            新加入的站点起始位置，对新加入的站点需要设置流量级别，尺寸流量级别，有一级域名的二级域名的网站等级
	 * @throws InternalException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void storePagedBDSite(final List<BDSiteBo> bdSiteList, RandomAccessFile unionSiteReader, final List<SiteBDStatVo> uninitBDStatList) throws InternalException, IOException, ClassNotFoundException {

		// 保存记录
		if (bdSiteList == null || bdSiteList.isEmpty()) {
			return;
		}
		if (uninitBDStatList == null || bdSiteList.isEmpty()) {
			return;
		}
		if (uninitBDStatList.size() != bdSiteList.size()) {
			throw new InternalException("bdSiteList.size() != unionsiteFile.size");
		}

		//
		int size = bdSiteList.size();
		List<BDSiteBo> deletedList = new ArrayList<BDSiteBo>(size);
		List<SiteBDStatVo> initedList = new ArrayList<SiteBDStatVo>(size);
		// 插入或更新N*MAX_SIZE的条记录，并设置firsttradeid和secondtradeid
		for (int fromIndex = 0, toIndex = MAX_SIZE; toIndex <= size;) {
			deletedList.addAll(bdSiteList.subList(fromIndex, toIndex));
			initedList.addAll(uninitBDStatList.subList(fromIndex, toIndex));
			siteDao.insertBDSite(bdSiteList.subList(fromIndex, toIndex), uninitBDStatList.subList(fromIndex, toIndex), unionSiteReader);
			fromIndex += MAX_SIZE;
			toIndex = fromIndex + MAX_SIZE;
		}
		bdSiteList.removeAll(deletedList);
		uninitBDStatList.removeAll(initedList);

	}

	/**
	 * 更新bdSiteList中的站点信息
	 * 
	 * @author zengyunfeng
	 * @param bdSiteList
	 * @param nextId
	 * @param maxSizeId
	 * @param uninitBDStatList
	 * @throws InternalException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void storeBDSite(final List<BDSiteBo> bdSiteList, RandomAccessFile unionSiteReader, final List<SiteBDStatVo> uninitBDStatList) throws InternalException, IOException, ClassNotFoundException {

		if (bdSiteList == null || bdSiteList.isEmpty()) {
			return;
		}
		if (uninitBDStatList == null || bdSiteList.isEmpty()) {
			return;
		}
		if (uninitBDStatList.size() != bdSiteList.size()) {
			throw new InternalException("siteTradeList.size() != bdsiteList.size");
		}

		int size = bdSiteList.size();
		for (int fromIndex = 0, toIndex = 0; fromIndex < size; fromIndex += MAX_SIZE) {
			if (fromIndex + MAX_SIZE > size) {
				toIndex = size;
			} else {
				toIndex = fromIndex + MAX_SIZE;
			}
			siteDao.insertBDSite(bdSiteList.subList(fromIndex, toIndex), uninitBDStatList.subList(fromIndex, toIndex), unionSiteReader);
		}

	}

	/**
	 * 合并站点的统计数据，如果为一级域名，则把所有的统计数据合并，如果为二级域名，则把所有计费名，域名相同的记录进行合并
	 * 
	 * @author zengyunfeng
	 * @param site
	 * @param siteStatList
	 *            与site主域相同的统计信息列表
	 * @return null:没有合并的记录
	 */
	private SiteStatBo mergeSiteStat(final UnionSiteIndex site, final List<SiteStatBo> siteStatList) {
		SiteStatBo result = null;
		if (site.getDomainFlag() == SiteConstant.MAINDOMAIN) {
			// 所有的二级域名相加
			for (SiteStatBo stat : siteStatList) {
				result = SiteStatUtil.mergeSiteStat(result, stat);
			}
		} else {
			// 计费名，域名相等的域名相加
			for (SiteStatBo stat : siteStatList) {
				if (stat.getDomain().equals(site.getDomain()) && stat.getCntn().equals(site.getCname())) {
					result = SiteStatUtil.mergeSiteStat(result, stat);
				}
			}
		}
		return result;
	}

	/**
	 * 过滤掉unionSiteList中计费名与siteStatList一个都不相同的记录
	 * 
	 * @author zengyunfeng
	 * @param unionSiteList
	 * @param siteStatList
	 */
	private void fileterNoCname(final List<UnionSiteIndex> unionSiteList, final List<SiteStatBo> siteStatList) {
		if ((unionSiteList == null) || unionSiteList.isEmpty()) {
			return;
		}
		if ((siteStatList == null) || siteStatList.isEmpty()) {
			return;
		}
		Set<String> cnames = new HashSet<String>();
		for (SiteStatBo stat : siteStatList) {
			cnames.add(stat.getCntn());
		}
		int size = unionSiteList.size();
		for (int index = 0; index < size; index++) {
			if (!cnames.contains(unionSiteList.get(index).getCname())) {
				unionSiteList.remove(index);
				index--;
				size--;
			}
		}
	}

	/**
	 * 取unionSiteList中一级域名相同的一批记录
	 * 
	 * @author zengyunfeng
	 * @param unionSiteList
	 * @param start
	 *            起始序号
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private UnionSiteAndDomain getSitesWithSameDomain(final List<UnionSiteIndex> result, final IteratorObjectReader<UnionSiteIndex> unionSiteList, final UnionSiteIndex nextsite) throws IOException, ClassNotFoundException {
		if (result == null) {
			return null;
		}
		if (nextsite == null && !unionSiteList.hasNext()) {
			// 一个站点也没有
			return null;
		}

		UnionSiteAndDomain domainAndNext = new UnionSiteAndDomain();
		UnionSiteIndex site = nextsite;
		String domain = null;
		if (site == null) {
			site = unionSiteList.next();
		}
		result.add(site);
		domain = site.getDomain();
		if (site.getDomainFlag() != SiteConstant.MAINDOMAIN) {
			domain = UrlParser.fetchMainDomain(domain);
		}
		domainAndNext.mainDomain = domain;
		while (unionSiteList.hasNext()) {
			site = unionSiteList.next();
			if (domain.equals(UrlParser.fetchMainDomain(site.getDomain()))) {
				result.add(site);
			} else {
				domainAndNext.next = site;
				break;
			}
		}
		return domainAndNext;
	}

	/**
	 * 取qvalue中域名相同的记录
	 * 
	 * @author zengyunfeng
	 * @param domain
	 * @param qList
	 * @param start
	 *            起始序号
	 * @return result[0]为域名相同的QValue, result[1]为下一个QValue
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private QValue[] getQValue(final String domain, final IteratorObjectReader<QValue> qList, final QValue nextValue) throws IOException, ClassNotFoundException {
		QValue[] result = new QValue[] { null, null };

		if (nextValue == null && (!qList.hasNext())) {
			return result;
		}
		QValue site = nextValue;
		if (site != null) {
			if (site.getDomain().equals(domain)) {
				result[0] = site;
				result[1] = null;
				return result;
			} else if (DomainComparator.domainCompare(site.getDomain(), domain) > 0) {
				// 比当前的主域大，不需要在向后查看
				result[0] = null;
				result[1] = site;
				return result;
			}
		}
		while (qList.hasNext()) {
			site = qList.next();
			if (site.getDomain().equals(domain)) {
				result[0] = site;
				result[1] = null;
				break;
			} else if (DomainComparator.domainCompare(site.getDomain(), domain) > 0) {
				// 比当前的主域大，不需要在向后查看
				result[0] = null;
				result[1] = site;
				break;
			}
		}

		return result;
	}

	/**
	 * 取ipcookie统计文件中域名相同的记录
	 * 
	 * @author zengyunfeng
	 * @param domain
	 * @param ipList
	 * @param start
	 *            起始序号
	 * @return result[0]为域名相同的QValue, result[1]为下一个QValue
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private IPCookieBo[] getIpCookie(final String domain, final IteratorObjectReader<IPCookieBo> ipList, final IPCookieBo nextValue) throws IOException, ClassNotFoundException {
		IPCookieBo[] result = new IPCookieBo[] { null, null };

		if (nextValue == null && (!ipList.hasNext())) {
			return result;
		}
		IPCookieBo site = nextValue;
		if (site != null) {
			if (site.getDomain().equals(domain)) {
				result[0] = site;
				result[1] = null;
				return result;
			} else if (DomainComparator.domainCompare(site.getDomain(), domain) > 0) {
				// 比当前的主域大，不需要在向后查看
				result[0] = null;
				result[1] = site;
				return result;
			}
		}
		while (ipList.hasNext()) {
			site = ipList.next();
			if (site.getDomain().equals(domain)) {
				result[0] = site;
				result[1] = null;
				break;
			} else if (DomainComparator.domainCompare(site.getDomain(), domain) > 0) {
				// 比当前的主域大，不需要在向后查看
				result[0] = null;
				result[1] = site;
				break;
			}
		}

		return result;
	}

	/**
	 * 取站点统计数据（7日平均）中一级域名相同的一批记录
	 * 
	 * @author zengyunfeng
	 * @param siteStatList
	 *            起始序号
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private SiteStatBo getSiteStatWithSameDomain(final List<SiteStatBo> result, final String domain, final IteratorObjectReader<SiteStatBo> siteStatList, final SiteStatBo nextSiteStat) throws IOException, ClassNotFoundException {
		if (result == null) {
			return null;
		}
		if (nextSiteStat == null && (!siteStatList.hasNext())) {
			return null;
		}
		SiteStatBo site = nextSiteStat;

		if (nextSiteStat != null) {
			if (domain.equals(UrlParser.fetchMainDomain(site.getDomain()))) {
				// 不能用site.getDomain().endwith(domain)，防止91.cn和hao91.cn或者edu.cn和nuaa.edu.cn不属于同一主域的情况
				result.add(site);
			} else if (DomainComparator.domainCompare(site.getDomain(), domain) > 0) {
				// 即不是同一个主域，又比当前的主域大，不需要在向后查看
				return site;
			}
		}

		while (siteStatList.hasNext()) {
			site = siteStatList.next();
			if (domain.equals(UrlParser.fetchMainDomain(site.getDomain()))) { // 主域一致，不能用site.domain.endwith(domain)判断
				result.add(site);
			} else if (DomainComparator.domainCompare(site.getDomain(), domain) > 0) {
				// 即不是同一个主域，又比当前的主域大，不需要在向后查看
				return site;
			}
		}

		return null;
	}

	/**
	 * 开始循环处理list
	 * 
	 * @param siteCompeteMap
	 *            SiteId->UserCount的映射
	 * @param tradeSite
	 *            SiteId -> TradeId的映射
	 * @param siteRelation
	 *            二级域名ID->父域名ID映射
	 * @param preUserSiteInfo
	 *            前一个User的投放信息
	 * @param list
	 *            分页查询出来的用户投放选择的站点信息
	 */
	public void statSiteUser(final Map<Integer, Integer> siteCompeteMap, final Map<Integer, Set<Integer>> tradeSite, final Map<Integer, Integer> siteRelation, List<UserSiteVO> list, final UserSiteInfo preUserSiteInfo) {
		long start = System.currentTimeMillis();
		for (int i = 0; i < list.size(); i++) {
			UserSiteVO rs = list.get(i);

			int userid = rs.userId;
			String tradeListStr = rs.siteTradeList;
			String siteListStr = rs.siteList;
			boolean allSite = rs.isallsite;
			String[] fields = null;
			int siteid = 0;
			int tradeid = 0;
			// 判断前一个用户和当前用户是否为同一个
			if (preUserSiteInfo.preUserid != userid) {
				// 更新前一个用户的站点竞争度
				updateSiteUserCompete(siteCompeteMap, preUserSiteInfo, tradeSite, siteRelation);
				// 清空前一个用户的站点信息
				preUserSiteInfo.clear(userid);
			}

			if (preUserSiteInfo.allSite) {
				continue;
			} else if (allSite) {
				preUserSiteInfo.allSite = true;
				continue;
			}

			// 更新preSiteSet
			if (!StringUtils.isEmpty(siteListStr)) {
				fields = siteListStr.split(SiteConstant.SITE_SEPERATOR_REX);
				for (String sitestr : fields) {
					if (StringUtils.isEmpty(sitestr)) {
						continue;
					}
					try {
						siteid = Integer.valueOf(sitestr);
						preUserSiteInfo.preSiteSet.add(siteid);
					} catch (NumberFormatException e) {
						LogUtils.error(LOG, e.getMessage(), e);
					}
				}
			}
			// 更新preTradeSet
			if (!StringUtils.isEmpty(tradeListStr)) {
				fields = tradeListStr.split(SiteConstant.SITE_SEPERATOR_REX);
				for (String tradestr : fields) {
					if (StringUtils.isEmpty(tradestr)) {
						continue;
					}
					try {
						tradeid = Integer.valueOf(tradestr);
						preUserSiteInfo.preTradeSet.add(tradeid);
					} catch (NumberFormatException e) {
						LogUtils.error(LOG, e.getMessage(), e);
					}
				}
			}
		}
		LOG.info("stat " + list.size() + " records using: " + (System.currentTimeMillis() - start) + "ms");
	}

	private void updateSiteUserCompete(final Map<Integer, Integer> siteCompeteMap, final UserSiteInfo preUserSiteInfo, final Map<Integer, Set<Integer>> tradeSite, final Map<Integer, Integer> siteRelation) {
		if (preUserSiteInfo == null || siteCompeteMap == null) {
			return;
		}
		if (preUserSiteInfo.allSite) {
			for (Entry<Integer, Integer> entry : siteCompeteMap.entrySet()) {
				entry.setValue(entry.getValue() + 1);
			}
		} else {
			uniqSite(tradeSite, preUserSiteInfo.preSiteSet, preUserSiteInfo.preTradeSet, siteRelation);
			updateSiteCompete(siteCompeteMap, preUserSiteInfo.preSiteSet, siteRelation);
		}
	}

	private void uniqSite(final Map<Integer, Set<Integer>> tradeSite, Set<Integer> siteSet, Set<Integer> tradeSet, final Map<Integer, Integer> siteRelation) {
		if (tradeSet == null || tradeSet.isEmpty()) {
			return;
		} else if (tradeSite == null || tradeSite.isEmpty()) {
			return;
		}
		if (siteSet == null) {
			return;
		}

		for (Integer tradeid : tradeSet) {
			Set<Integer> siteIdSet = tradeSite.get(tradeid);
			if (siteIdSet != null) {
				siteSet.addAll(siteIdSet);
			}
		}
		Iterator<Integer> it = siteSet.iterator();
		Integer parentSite = null;
		// 二级域名对应的一级域名已经在网站集合中，则进行删除该二级域名
		while (it.hasNext()) {
			parentSite = siteRelation.get(it.next());
			if (parentSite != null && siteSet.contains(parentSite)) {
				it.remove();
			}
		}

	}

	private void updateSiteCompete(final Map<Integer, Integer> siteCompeteMap, Set<Integer> siteSet, final Map<Integer, Integer> siteRelation) {
		if (siteCompeteMap == null) {
			return;
		}
		for (Integer site : siteSet) {
			Integer siteCompete = siteCompeteMap.get(site);
			if (siteCompete != null) {
				// 必须使用该判断，用户选择的某一些站点可能已经失效，因此不在siteCompeteMap内。
				siteCompete = siteCompete + 1;
				siteCompeteMap.put(site, siteCompete);
			} else if (siteRelation != null) {
				// 查看父站点是否在计算热度的集合中
				Integer parentSite = siteRelation.get(site);
				if (parentSite != null && parentSite != 0) {
					siteCompete = siteCompeteMap.get(parentSite);
					if (siteCompete != null) {
						siteCompete = siteCompete + 1;
						siteCompeteMap.put(parentSite, siteCompete);
					}
				}
			}

		}
	}

	public void setSiteDao(BDSiteStatDao siteDao) {
		this.siteDao = siteDao;
	}

	public void setScaleAlgorithm(SiteScaleAlgorithm scaleAlgorithm) {
		this.scaleAlgorithm = scaleAlgorithm;
	}

	public void setStandard_q1(double standard_q1) {
		this.standard_q1 = standard_q1;
	}

	public void setStandard_q2(double standard_q2) {
		this.standard_q2 = standard_q2;
	}

	public void setAdSizeDao(AdSizeDao adSizeDao) {
		this.adSizeDao = adSizeDao;
	}

	public void setSiteOnAddbDao(BDSiteStatOnAddbDao siteOnAddbDao) {
		this.siteOnAddbDao = siteOnAddbDao;
	}

	public void setSequenceIdDaoOnXdb(SequenceIdDaoOnXdb sequenceIdDaoOnXdb) {
		this.sequenceIdDaoOnXdb = sequenceIdDaoOnXdb;
	}

}
