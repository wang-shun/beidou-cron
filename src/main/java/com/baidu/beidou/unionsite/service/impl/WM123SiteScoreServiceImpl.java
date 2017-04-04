package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.unionsite.dao.WM123SiteStatDao;
import com.baidu.beidou.unionsite.service.WM123SiteScoreService;
import com.baidu.beidou.unionsite.vo.WM123SiteScoreVo;

/**
 * @author lvzichan
 * @since 2013-08-01
 */
public class WM123SiteScoreServiceImpl implements WM123SiteScoreService {

	private static final Log LOG = LogFactory
			.getLog(WM123SiteScoreServiceImpl.class);

	private static final String FILE_SEPARATOR = "\t";
	private static final int MAX_SIZE = 1000; // 数据库中一次插入的最大个数
	private BDSiteStatDao siteStatDao;
	private WM123SiteStatDao wm123SiteStatDao;

	// 计算四个得分的相关阈值，与文件wm123-sitescore-refresher.properties中的配置对应
	private float score1ImpactPs1;
	private float score1ImpactPs2;
	private long score1ImpactAlexa1;
	private long score1ImpactAlexa2;
	private float score1ImpactRank1;
	private float score1ImpactRank2;
	private float score1ImpactRank3;

	private long score2TrafficPv1;
	private long score2TrafficPv2;
	private long score2TrafficPv3;
	private long score2TrafficUv1;
	private long score2TrafficUv2;
	private long score2TrafficUv3;
	private float score2TrafficRank1;
	private float score2TrafficRank2;
	private float score2TrafficRank3;
	private float score2TrafficRank4;

	private float score3ObviousRate;
	private int score3ObviousArea;
	private float score3ObviousScreen;
	private float score3ObviousIt;
	private float score3ObviousRank1;
	private float score3ObviousRank2;

	private float score4QualityRank1;
	private float score4QualityRank2;
	
	// 源数据文件
	/**
	 * 站点粒度的文件
	 * 路径：/home/work/beidou-cron/data/wm123RefreshSiteScore/domain_info
	 * 字段：
	 * domain 站点名;
	 * alexa Alexa排名;
	 * dps ps排名;
	 * 1startype 1星类型(>=0为一星);
	 * pv 推广位pv;
	 * uv 推广位uv;
	 * 字段分割：\t
	 */
	private String domainFileName;
	
	/**
	 * 站点-推广位粒度的文件
	 * 路径：/home/work/beidou-cron/data/wm123RefreshSiteScore/domain_tu_info【.1~14】
	 * 字段：
	 * domain 站点名
	 * tu 推广位
	 * it 可视时间
	 * area 推广位面积
	 * first_second_screen 首屏+二屏概率
	 * adspv 推广位流量
	 * 字段分割：\t
	 */
	private String domainTuFileName;
	private int domainTuFileNum; // 14
	
	/**
	 * 最终计算出来的在北斗库中的站点得分
	 * 路径：/home/work/beidou-cron/data/wm123RefreshSiteScore/domain_score
	 * 字段：siteId，siteUrl，scoreImpact，scoreTraffic，scoreObvious，scoreQuality，scoreTotal
	 * 字段分割：\t
	 */
	private String domainScoreFileName;

	/**
	 * main function 读取上游数据文件domainFile和domainTuFile，根据得分规则计算各个网站的指标得分，并入库
	 */
	public void refreshSiteScore() throws FileNotFoundException, IOException {
		LOG.info("Begin to refreshSiteScore");

		// 0.初始化数据库中所有site的url~id映射
		Map<String, Integer> siteUrl2IdMap = wm123SiteStatDao
				.getAllSiteUrl2IdMapping();

		// 1.读取文件domain_info，计算三个指标评分score1 score2 score4
		Map<String, WM123SiteScoreVo> siteScoreMap = new HashMap<String, WM123SiteScoreVo>();
		setAllScore3(siteUrl2IdMap, siteScoreMap);
		LOG.info("1.Success!Read file '"
				+ domainFileName
				+ "',compute scoreImpact & scoreTraffic & scoreQuality, and put into map");

		// 2.读取文件domain_tu_info.1~14，计算所有站点的醒目度得分，更新map中的值
		if (setAllScoreObvious(siteScoreMap)) {
			LOG.info("2.Success!Read file '" + domainTuFileName
					+ "',set scoreObvious in map");
		} else {
			LOG.error("Fail!Read file '" + domainTuFileName + "',1~"
					+ domainTuFileNum + ",no one file exist!");
			throw new FileNotFoundException("File not exist:"
					+ domainTuFileName + ".1~" + domainTuFileNum);
		}

		// 3.计算map中站点的总得分，更新到数据库中
		computeAndSaveScore(siteScoreMap);
		LOG.info("3.Success!Compute scoreTotal and update into db,total records num is:"
				+ siteScoreMap.size());

		// 4.将计算出来的最终得分写入文件domainScoreFile，只是为了验证数据库写的是否正确，非必须
		try {
			File domainScoreFile = new File(domainScoreFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					domainScoreFile));
			for (WM123SiteScoreVo siteScore : siteScoreMap.values()) {
				bw.write(siteScore.getSiteId() + FILE_SEPARATOR
						+ siteScore.getSiteUrl() + FILE_SEPARATOR
						+ siteScore.getScoreImpact() + FILE_SEPARATOR
						+ siteScore.getScoreTraffic() + FILE_SEPARATOR
						+ siteScore.getScoreObvious() + FILE_SEPARATOR
						+ siteScore.getScoreQuality() + FILE_SEPARATOR
						+ siteScore.getScoreTotal());
				bw.newLine();
			}
			bw.flush();
			bw.close();
			LOG.info("4.Success!Write siteScoreMap to file '"
					+ domainScoreFileName + "'");
		} catch (Exception e) {
			LOG.info("4.Fail!Write siteScoreMap to file '"
					+ domainScoreFileName + "' catch exception:"
					+ e.getMessage());
		}

		LOG.info("End to refreshSiteScore");
	}

	/**
	 * 读取文件domain_info,计算三个指标评分score1 score2 score4,放入siteScoreMap中
	 * 
	 * @param siteUrl2IdMap
	 *            数据库unionsite中siteUrl~siteId的映射
	 * @param siteScoreMap
	 *            空map
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void setAllScore3(Map<String, Integer> siteUrl2IdMap,
			Map<String, WM123SiteScoreVo> siteScoreMap) throws IOException,
			FileNotFoundException {
		LOG.info("Begin to setAllScore3");

		File domainFile = new File(domainFileName);
		if (!domainFile.exists()) {
			LOG.error("File not exist:" + domainFileName);
			throw new FileNotFoundException("File not exist:" + domainFileName);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(domainFile)));

		String line;
		int i = 0;
		while ((line = br.readLine()) != null) {
			i++;
			try {
				String[] srcDomainLine = line.split(FILE_SEPARATOR);
				if (srcDomainLine.length != 6) {
					LOG.debug("In file '" + domainFileName
							+ "',this line doesn't have 6 items:" + line);
					continue;
				}
				String domain = srcDomainLine[0];
				if (!siteUrl2IdMap.containsKey(domain)) {// 不在表beidouext.unionsite中的domain，忽略不理
					continue;
				}
				WM123SiteScoreVo siteScoreVo = new WM123SiteScoreVo();
				siteScoreVo.setSiteUrl(domain);
				siteScoreVo.setSiteId(siteUrl2IdMap.get(domain));
				siteScoreVo.setScoreImpact(getScoreImpact(
						Long.valueOf(srcDomainLine[1]),
						Float.valueOf(srcDomainLine[2])));
				siteScoreVo.setScoreTraffic(getScoreTraffic(
						Long.valueOf(srcDomainLine[4]),
						Long.valueOf(srcDomainLine[5])));
				siteScoreVo.setScoreQuality(getScoreQuality(Integer
						.valueOf(srcDomainLine[3])));
				siteScoreMap.put(domain, siteScoreVo);
			} catch (Exception e) {
				LOG.debug("In file '" + domainFileName
						+ "',find one illegal line:" + line);
			}

		}
		LOG.info("File '" + domainFileName + "' line nums:" + i
				+ ";domains in db(finally put in map):" + siteScoreMap.size());
		br.close();

		LOG.info("End to setAllScore3");
	}

	/**
	 * 读取文件domain_tu_info.1~14,根据可视时间、推广位面积、首屏+二屏概率判断醒目推广位，计算所有站点的醒目度得分，更新map中的值
	 * 
	 * @param siteScoreMap
	 *            已经设置了三个指标得分的map
	 * @return boolean 如果成功处理了至少一个文件返回true，否则返回false
	 */
	private boolean setAllScoreObvious(
			Map<String, WM123SiteScoreVo> siteScoreMap) throws IOException,
			FileNotFoundException {
		LOG.info("Begin to setAllScoreObvious");

		boolean hasAtLeastOneFile = false; // 是否成功处理了至少一个文件
		for (int i = 1; i <= domainTuFileNum; i++) {
			File domainTuFile = new File(domainTuFileName + "." + i);
			if (!domainTuFile.exists()) {
				LOG.info("File not exist:" + domainTuFileName + "." + i);
				continue;
			}

			LOG.info("Read file:" + domainTuFileName + "." + i);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(domainTuFile)));

			String line = br.readLine();
			String lastDomain = null; // 上一行的domain
			String curDomain = null; // 当前行的domain
			List<String> sameDomainLines = new ArrayList<String>();// domain相同的数据行
			if (line == null) {
				continue;
			}
			sameDomainLines.add(line);
			lastDomain = line.substring(0, line.indexOf(FILE_SEPARATOR));

			while ((line = br.readLine()) != null) {
				curDomain = line.substring(0, line.indexOf(FILE_SEPARATOR));
				if (curDomain.equals(lastDomain)) {
					sameDomainLines.add(line);
				} else {
					WM123SiteScoreVo siteScore = siteScoreMap.get(lastDomain);
					if (siteScore != null) { // 若lastDomain在map中不存在，则直接忽略
						// 计算lastDomain对应的scoreObvious，与map中已有值进行对比，如果更高，则更新map中的值
						float scoreObvious = getScoreObvious(sameDomainLines);
						if (scoreObvious > siteScore.getScoreObvious()) {
							siteScore.setScoreObvious(scoreObvious);
						}
					}

					// 重置sameDomainLines和lastDomain
					sameDomainLines.clear();
					sameDomainLines.add(line);
					lastDomain = curDomain;
				}
			}
			// 计算最后一个lastDomain的scoreObvious
			WM123SiteScoreVo siteScore = siteScoreMap.get(lastDomain);
			if (siteScore != null) { // 若lastDomain在map中不存在，则直接忽略
				float scoreObvious = getScoreObvious(sameDomainLines);
				if (scoreObvious > siteScore.getScoreObvious()) {
					siteScore.setScoreObvious(scoreObvious);
				}
			}
			br.close();

			hasAtLeastOneFile = true;
		}
		LOG.info("End to setAllScoreObvious");

		return hasAtLeastOneFile;
	}

	/**
	 * 计算map中站点的总得分，更新到数据库中
	 * 
	 * @param siteScoreMap
	 *            已经计算完4个指标得分的map
	 */
	private void computeAndSaveScore(Map<String, WM123SiteScoreVo> siteScoreMap) {
		LOG.info("Begin to computeAndSaveScore");

		List<WM123SiteScoreVo> refreshScoreList = new ArrayList<WM123SiteScoreVo>();

		// 遍历map，计算站点总得分
		for (WM123SiteScoreVo siteScore : siteScoreMap.values()) {
			siteScore.setScoreTotal(getScoreTotal(siteScore));
			refreshScoreList.add(siteScore);
		}

		// 将网站得分数据分批更新到数据库表中
		int size = refreshScoreList.size();
		for (int fromIndex = 0, toIndex = MAX_SIZE; fromIndex < size; fromIndex = toIndex) {
			toIndex = fromIndex + MAX_SIZE;
			if (toIndex > size) {
				toIndex = size;
			}
			siteStatDao.updateSiteScore(refreshScoreList.subList(fromIndex,
					toIndex));
		}

		LOG.info("End to computeAndSaveScore");
	}

	/**
	 * 根据alexa排名以及ps排名，计算网站影响力得分
	 * 
	 * @param alexa
	 * @param ps
	 * @return
	 */
	private float getScoreImpact(long alexa, float ps) {
		if (alexa == -1 && ps == -1) { // 两个指标都没取到值，得分为0
			return 0;
		} else if (ps >= score1ImpactPs1
				|| (alexa >= 0 && alexa <= score1ImpactAlexa1)) {
			return score1ImpactRank1;
		} else if ((ps >= score1ImpactPs2 && ps < score1ImpactPs1)
				|| (alexa > score1ImpactAlexa1 && alexa <= score1ImpactAlexa2)) {
			return score1ImpactRank2;
		} else {
			return score1ImpactRank3;
		}
	}

	/**
	 * 根据网站pv和uv，计算网盟流量得分
	 * 
	 * @param pv
	 * @param uv
	 * @return
	 */
	private float getScoreTraffic(long pv, long uv) {
		if (pv == -1 && uv == -1) { // 两个指标都没取到值，得分为0
			return 0;
		} else if (pv >= score2TrafficPv1 || uv >= score2TrafficUv1) {
			return score2TrafficRank1;
		} else if ((pv >= score2TrafficPv2 && pv < score2TrafficPv1)
				|| (uv >= score2TrafficUv2 && uv < score2TrafficUv1)) {
			return score2TrafficRank2;
		} else if ((pv >= score2TrafficPv3 && pv < score2TrafficPv2)
				|| (uv >= score2TrafficUv3 && uv < score2TrafficUv2)) {
			return score2TrafficRank3;
		} else {
			return score2TrafficRank4;
		}
	}

	/**
	 * 根据站点是否一星级，计算站点流量质量得分
	 * 
	 * @param starType
	 *            是否一星站点，-1代表不是一星站点
	 * @return
	 */
	private float getScoreQuality(int starType) {
		if (starType == -1) { // 不是一星站点
			return score4QualityRank1;
		} else {
			return score4QualityRank2;
		}
	}

	/**
	 * 根据同一个domain的多个tu数据，计算domain的scoreObvious
	 * 
	 * @param sameDomainLines
	 *            同一个domain的多条tu数据
	 * @return 该domain的醒目度得分
	 */
	private float getScoreObvious(List<String> sameDomainLines) {
		long totalPv = 0;
		long obviousPv = 0;

		for (String line : sameDomainLines) {
			try {
				String[] lineArray = line.split(FILE_SEPARATOR);
				totalPv += Long.valueOf(lineArray[5]);
				if (Float.valueOf(lineArray[2]) >= score3ObviousIt
						&& Integer.valueOf(lineArray[3]) >= score3ObviousArea
						&& Float.valueOf(lineArray[4]) >= score3ObviousScreen) {
					obviousPv += Long.valueOf(lineArray[5]);
				}
			} catch (Exception e) {
				LOG.debug("Find one illegal line:" + line);
			}
		}
		if (totalPv == 0) {
			return score3ObviousRank2;
		}
		if ((float) obviousPv / (float) totalPv >= score3ObviousRate) {
			return score3ObviousRank1;
		} else {
			return score3ObviousRank2;
		}
	}

	/**
	 * 根据网站的四个指标得分，计算总得分
	 * 
	 * @param siteScore
	 *            已经设置了四个指标得分的vo对象
	 * @return 网站总得分
	 */
	private int getScoreTotal(WM123SiteScoreVo siteScore) {
		float scoreImpact = siteScore.getScoreImpact();
		float scoreTraffic = siteScore.getScoreTraffic();
		float scoreObvious = siteScore.getScoreObvious();
		float scoreQuality = siteScore.getScoreQuality();

		// 四项都无得分，直接返回0
		if (scoreImpact == 0 && scoreTraffic == 0 && scoreObvious == 0
				&& scoreQuality == 0) {
			return 0;
		}

		// 将个别无得分的项，以2分计
		if (scoreImpact == 0) {
			scoreImpact = 2;
		}
		if (scoreTraffic == 0) {
			scoreTraffic = 2;
		}
		if (scoreObvious == 0) {
			scoreObvious = 2;
		}
		if (scoreQuality == 0) {
			scoreQuality = 2;
		}

		// 计算最终总得分
		float totalScore = scoreImpact + scoreTraffic + scoreObvious
				+ scoreQuality;
		if (totalScore < 7) {
			return 6;
		} else if (totalScore == 7) {
			return 7;
		} else if (totalScore < 8) {
			return 8;
		} else if (totalScore < 9) {
			return 9;
		} else {
			return 10;
		}
	}

	public void setSiteStatDao(BDSiteStatDao siteStatDao) {
		this.siteStatDao = siteStatDao;
	}

	public void setWm123SiteStatDao(WM123SiteStatDao wm123SiteStatDao) {
		this.wm123SiteStatDao = wm123SiteStatDao;
	}

	public void setScore1ImpactPs1(float score1ImpactPs1) {
		this.score1ImpactPs1 = score1ImpactPs1;
	}

	public void setScore1ImpactPs2(float score1ImpactPs2) {
		this.score1ImpactPs2 = score1ImpactPs2;
	}

	public void setScore1ImpactAlexa1(long score1ImpactAlexa1) {
		this.score1ImpactAlexa1 = score1ImpactAlexa1;
	}

	public void setScore1ImpactAlexa2(long score1ImpactAlexa2) {
		this.score1ImpactAlexa2 = score1ImpactAlexa2;
	}

	public void setScore1ImpactRank1(float score1ImpactRank1) {
		this.score1ImpactRank1 = score1ImpactRank1;
	}

	public void setScore1ImpactRank2(float score1ImpactRank2) {
		this.score1ImpactRank2 = score1ImpactRank2;
	}

	public void setScore1ImpactRank3(float score1ImpactRank3) {
		this.score1ImpactRank3 = score1ImpactRank3;
	}

	public void setScore2TrafficPv1(long score2TrafficPv1) {
		this.score2TrafficPv1 = score2TrafficPv1;
	}

	public void setScore2TrafficPv2(long score2TrafficPv2) {
		this.score2TrafficPv2 = score2TrafficPv2;
	}

	public void setScore2TrafficPv3(long score2TrafficPv3) {
		this.score2TrafficPv3 = score2TrafficPv3;
	}

	public void setScore2TrafficUv1(long score2TrafficUv1) {
		this.score2TrafficUv1 = score2TrafficUv1;
	}

	public void setScore2TrafficUv2(long score2TrafficUv2) {
		this.score2TrafficUv2 = score2TrafficUv2;
	}

	public void setScore2TrafficUv3(long score2TrafficUv3) {
		this.score2TrafficUv3 = score2TrafficUv3;
	}

	public void setScore2TrafficRank1(float score2TrafficRank1) {
		this.score2TrafficRank1 = score2TrafficRank1;
	}

	public void setScore2TrafficRank2(float score2TrafficRank2) {
		this.score2TrafficRank2 = score2TrafficRank2;
	}

	public void setScore2TrafficRank3(float score2TrafficRank3) {
		this.score2TrafficRank3 = score2TrafficRank3;
	}

	public void setScore2TrafficRank4(float score2TrafficRank4) {
		this.score2TrafficRank4 = score2TrafficRank4;
	}

	public void setScore3ObviousRate(float score3ObviousRate) {
		this.score3ObviousRate = score3ObviousRate;
	}

	public void setScore3ObviousArea(int score3ObviousArea) {
		this.score3ObviousArea = score3ObviousArea;
	}

	public void setScore3ObviousScreen(float score3ObviousScreen) {
		this.score3ObviousScreen = score3ObviousScreen;
	}

	public void setScore3ObviousIt(float score3ObviousIt) {
		this.score3ObviousIt = score3ObviousIt;
	}

	public void setScore3ObviousRank1(float score3ObviousRank1) {
		this.score3ObviousRank1 = score3ObviousRank1;
	}

	public void setScore3ObviousRank2(float score3ObviousRank2) {
		this.score3ObviousRank2 = score3ObviousRank2;
	}

	public void setScore4QualityRank1(float score4QualityRank1) {
		this.score4QualityRank1 = score4QualityRank1;
	}

	public void setScore4QualityRank2(float score4QualityRank2) {
		this.score4QualityRank2 = score4QualityRank2;
	}

	public void setDomainFileName(String domainFileName) {
		this.domainFileName = domainFileName;
	}

	public void setDomainTuFileName(String domainTuFileName) {
		this.domainTuFileName = domainTuFileName;
	}

	public void setDomainTuFileNum(int domainTuFileNum) {
		this.domainTuFileNum = domainTuFileNum;
	}

	public void setDomainScoreFileName(String domainScoreFileName) {
		this.domainScoreFileName = domainScoreFileName;
	}

}
