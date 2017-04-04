package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.dao.WM123SiteStatOnCapDao;
import com.baidu.beidou.unionsite.service.WM123AdTrade2SiteTradeService;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author lvzichan
 * @since 2013-10-10
 */
public class WM123AdTrade2SiteTradeServiceImpl implements
		WM123AdTrade2SiteTradeService {

	private static final Log LOG = LogFactory
			.getLog(WM123AdTrade2SiteTradeServiceImpl.class);

	private static final String FILE_SEPARATOR = "\t";
	private WM123SiteStatOnCapDao wm123SiteStatOnCapDao;

	private Map<Integer, String> adTradeNameMap = new HashMap<Integer, String>(); // 一级广告主行业的id~name映射
	private Map<Integer, String> siteTradeNameMap = new HashMap<Integer, String>(); // 一级网站行业的id~name映射
	private Map<Integer, Integer> second2FirstSiteTradeMap = new HashMap<Integer, Integer>();// 一二级网站行业的映射关系,形如：<1,1>,<101,1>,<102,1>,<201,2>,……
	private Map<Integer, Map<Integer, Integer>> adTrade2SiteTradeMap = new HashMap<Integer, Map<Integer, Integer>>();

	/**
	 * 计算广告行业到网站行业的映射
	 * 
	 * @param srcGroupFilePath
	 *            待处理的源文件，字段如下： 
	 *            groupid:推广组id
	 *            userid:推广组所属用户id
	 *            adtradeid:用户所属的一级广告行业id
	 *            sitetradelist:推广组设置的投放行业列表
	 * 
	 * @param adTrade2SiteTradeFilePath
	 *            生成的广告行业~网站行业的对应关系文件，字段如下：
	 *            adtradeid：广告行业id
	 *            adtradename：广告行业name 
	 *            sitetradeid：网站行业id 
	 *            sitetradename：网站行业name
	 *            sitetradenum：选择该网站行业的次数 
	 *            sitetradepercent：选择该网站行业的百分比
	 * 
	 * 示例文件如： 
	 * 1 安全安保 1 音乐影视 	149 25% 
	 * 1 安全安保 2 休闲娱乐 	238 43% 
	 * 1 安全安保 3 游戏 	  	200 32%
	 * 
	 * @throws IOException
	 */
	public void computeAdTrade2SiteTrade(String srcGroupFilePath,
			String adTrade2SiteTradeFilePath) throws FileNotFoundException,
			IOException {
		LOG.info("In serviceImpl,Begin to computeAdTrade2SiteTrade");

		// 0.读取数据表beidoucode.adtrade和beidoucode.sitetrade，设置所有一级行业的id~name映射
		adTradeNameMap = wm123SiteStatOnCapDao.getFirstAdTradeMap();
		siteTradeNameMap = wm123SiteStatOnCapDao.getFirstSiteTradeMap();

		// 1.读取beidoucode.sitetrade表，查询所有一二级网站行业的映射关系，放入second2FirstSiteTradeMap中
		second2FirstSiteTradeMap = wm123SiteStatOnCapDao
				.getSecond2FirstSiteTradeMap();
		LOG.info("1.Success! Read from DB,init map");

		// 2.遍历源文件srcGroupFilePath，统计每个广告行业adTradeId，对应的一级网站行业的个数，放入adTrade2SiteTradeMap
		File srcGroupFile = new File(srcGroupFilePath);
		if (!srcGroupFile.exists()) {
			LOG.error("File not exist:" + srcGroupFilePath);
			throw new FileNotFoundException("File not exist:"
					+ srcGroupFilePath);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(srcGroupFile)));

		String line;
		while ((line = br.readLine()) != null) {
			try {
				String[] groupArray = line.split(FILE_SEPARATOR);
				if (groupArray.length != 4) {
					LOG.debug("In file '" + srcGroupFilePath
							+ "',this line doesn't have 4 items:" + line);
					continue;
				}
				Integer adTradeId = Integer.valueOf(groupArray[2]);
				String siteTradeList = groupArray[3];
				if (!adTradeNameMap.containsKey(adTradeId)
						|| StringUtil.isEmpty(siteTradeList)) {
					continue;
				}

				if (!adTrade2SiteTradeMap.containsKey(adTradeId)) {
					adTrade2SiteTradeMap.put(adTradeId,
							new HashMap<Integer, Integer>());
				}
				Map<Integer, Integer> siteTradeNumMap = adTrade2SiteTradeMap
						.get(adTradeId);
				// 处理这条数据adTradeId对应的siteTradelist
				Set<Integer> siteTradeIdSet = this
						.getFirstSiteTradeId(siteTradeList);
				for (Integer siteTradeId : siteTradeIdSet) {
					if (!siteTradeNumMap.containsKey(siteTradeId)) {
						siteTradeNumMap.put(siteTradeId, 1);
					} else {
						siteTradeNumMap.put(siteTradeId,
								siteTradeNumMap.get(siteTradeId) + 1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.debug("exception!line content:" + line);
			}
		}
		br.close();
		LOG.info("2.Success! Read file '" + srcGroupFilePath
				+ "',compute adTrade~siteTrade");

		// 3.将adTrade2SiteTradeMap中的数据写入文件adTrade2SiteTradeFilePath中
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(  
                new FileOutputStream(adTrade2SiteTradeFilePath), "GBK"));
		bw.write("adTradeId" + FILE_SEPARATOR + "adTradeName" + FILE_SEPARATOR
				+ "siteTradeId" + FILE_SEPARATOR + "siteTradeName" + FILE_SEPARATOR
				+ "siteTradeNum" + FILE_SEPARATOR + "percent");
		bw.newLine();
		// 遍历每一个广告行业
		for (Integer adTradeId : adTrade2SiteTradeMap.keySet()) {
			Map<Integer, Integer> siteTradeNumMap = adTrade2SiteTradeMap
					.get(adTradeId);
			int sum = 0;
			for (Integer siteTradeId : siteTradeNumMap.keySet()) {
				sum += siteTradeNumMap.get(siteTradeId);
			}
			if (sum == 0) {
				continue;
			}
			// 将一个广告行业对应的所有网站行业个数&百分比写入文件
			String adTradeName = adTradeNameMap.get(adTradeId);
			for (Integer siteTradeId : siteTradeNumMap.keySet()) {
				Integer siteTradeNum = siteTradeNumMap.get(siteTradeId);
				String siteTradeName = siteTradeNameMap.get(siteTradeId);
				float percent = (float) siteTradeNum / (float) sum * 100;
				bw.write(adTradeId + FILE_SEPARATOR + adTradeName
						+ FILE_SEPARATOR + siteTradeId + FILE_SEPARATOR
						+ siteTradeName + FILE_SEPARATOR + siteTradeNum
						+ FILE_SEPARATOR + String.format("%.2f", percent) + "%");
				bw.newLine();
			}
		}
		bw.flush();
		bw.close();
		LOG.info("3.Success! Write result to file '" + adTrade2SiteTradeFilePath
				+ "'");

		LOG.info("In serviceImpl,End to computeAdTrade2SiteTrade");
	}

	/**
	 * 取得每个推广组的sitetradelist对应的一级网站行业列表
	 * 
	 * @param siteTradeList
	 *            推广组的投放行业，如”6|9|401|402|702|“
	 * @return 对应的一级网站行业id集合，{6,9,4,7}
	 */
	private Set<Integer> getFirstSiteTradeId(String siteTradeList) {
		Set<Integer> firstSiteTradeIdList = new HashSet<Integer>();
		String[] siteTrades = siteTradeList.split("\\|");
		for (String siteTrade : siteTrades) {
			if (!StringUtil.isEmpty(siteTrade)) {
				try {
					Integer firstSiteTradeId = second2FirstSiteTradeMap
							.get(Integer.valueOf(siteTrade));
					if (firstSiteTradeId == null
							|| !siteTradeNameMap.containsKey(firstSiteTradeId)) {
						continue;
					}
					firstSiteTradeIdList.add(firstSiteTradeId);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return firstSiteTradeIdList;
	}

	public void setWm123SiteStatOnCapDao(
			WM123SiteStatOnCapDao wm123SiteStatOnCapDao) {
		this.wm123SiteStatOnCapDao = wm123SiteStatOnCapDao;
	}
}
