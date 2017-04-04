package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.dao.WM123SiteStatDao;
import com.baidu.beidou.unionsite.service.WM123SiteCprodataService;
import com.baidu.beidou.unionsite.vo.WM123SiteCprodataVo;

/**
 * @author lvzichan
 * @since 2013-10-10
 */
public class WM123SiteCprodataServiceImpl implements WM123SiteCprodataService {

	private static final Log LOG = LogFactory
			.getLog(WM123SiteCprodataServiceImpl.class);

	private static final String FILE_SEPARATOR = "\t";// 文件字段分隔符
	private static final Integer MAX_SIZE = 1000;// 一次插入数据库的最大记录条数
	private WM123SiteStatDao wm123SiteStatDao;

	/**
	 * 读取上游数据文件domainCprodataFilePath，将站点推广数据入库
	 * domainCprodataFilePath文件格式如下：
	 * domain	站点域名
	 * click	天点击数
	 * uv	点击独立访客数
	 * ctr	点击率（百分比）
	 * cpm	千次展现成本
	 * hour:click	每个小时的点击次数，可有0~24个
	 * 字段分割：\t
	 */
	public void saveSiteCprodata(String domainCprodataFilePath,
			String saveToDbFilePath) throws FileNotFoundException, IOException {

		// 0.初始化数据库中所有site的url~id映射
		Map<String, Integer> siteUrl2IdMap = wm123SiteStatDao
				.getAllSiteUrl2IdMapping();

		// 1.读取文件domainCprodataFilePath，得到在北斗数据库表中的站点推广数据
		List<WM123SiteCprodataVo> cprodataVoList = new ArrayList<WM123SiteCprodataVo>();
		File domainFile = new File(domainCprodataFilePath);
		if (!domainFile.exists()) {
			LOG.error("File not exist:" + domainCprodataFilePath);
			throw new FileNotFoundException("File not exist:"
					+ domainCprodataFilePath);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(domainFile)));

		// 文件数据的日期，从domainCprodataFilePath文件名截取最后的日期后缀
		String dateStr = domainCprodataFilePath
				.substring(domainCprodataFilePath.lastIndexOf('_') + 1);
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyyMMdd").parse(dateStr);
		} catch (ParseException e1) {
			e1.printStackTrace();
			throw new IOException("File name '" + domainCprodataFilePath
					+ "' invalid, not end with _yyyymmdd");
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day - 30);
		Date date30Ago = c.getTime();
		String date30AgoStr = new SimpleDateFormat("yyyyMMdd")
				.format(date30Ago);

		String line;
		int totalLineNum = 0;
		while ((line = br.readLine()) != null) {
			totalLineNum++;
			try {
				String[] srcDomainLine = line.split(FILE_SEPARATOR);
				if (srcDomainLine.length < 5) {
					LOG.debug("Find one illegal line:" + line);
					continue;
				}
				String domain = srcDomainLine[0];
				if (!siteUrl2IdMap.containsKey(domain)) {// 不在表beidouext.unionsite中的domain，忽略不理
					continue;
				}
				WM123SiteCprodataVo cprodataVo = new WM123SiteCprodataVo();
				cprodataVo.setSiteUrl(domain);
				cprodataVo.setSiteId(siteUrl2IdMap.get(domain));
				cprodataVo.setInsertDate(dateStr);
				cprodataVo.setClick(Integer.parseInt(srcDomainLine[1]));
				cprodataVo.setUv(Integer.parseInt(srcDomainLine[2]));
				cprodataVo.setCtr(Float.valueOf(srcDomainLine[3]));
				cprodataVo.setCpm(Float.parseFloat(srcDomainLine[4]));
				// 遍历srcDomainLine之后的字段，取得每个小时的点击数，拼接hourClickStr
				String[] hourClickNum = new String[24];
				for (int i = 0; i < 24; i++) {
					hourClickNum[i] = "-";
				}
				for (int i = 5; i < srcDomainLine.length; i++) {
					String[] oneHourClick = srcDomainLine[i].split(":");
					Integer hour = Integer.parseInt(oneHourClick[0]);
					hourClickNum[hour] = oneHourClick[1];
				}

				String hourClickStr = hourClickNum[0];
				for (int i = 1; i < 24; i++) {
					hourClickStr += "|" + hourClickNum[i];
				}
				cprodataVo.setHourClick(hourClickStr);
				cprodataVoList.add(cprodataVo);
			} catch (Exception e) {
				LOG.debug("Find one illegal line:" + line + "\n Exception: "
						+ e.getMessage());
			}
		}
		br.close();
		LOG.info("1.Success! Read file '" + domainCprodataFilePath
				+ "',total lines:" + totalLineNum);

		// 2.将cprodataVoList数据分批入库
		wm123SiteStatDao.delSiteCprodataByDate(date30AgoStr);
		int size = cprodataVoList.size();
		for (int fromIndex = 0, toIndex = MAX_SIZE; fromIndex < size; fromIndex = toIndex) {
			toIndex = fromIndex + MAX_SIZE;
			if (toIndex > size) {
				toIndex = size;
			}
			wm123SiteStatDao.saveSiteCprodata(cprodataVoList.subList(fromIndex,
					toIndex));
		}
		LOG.info("2.Success! Write to DB records: " + cprodataVoList.size());

		// 3.将cprodataVoList写入文件saveToDbFilePath，只是为了验证数据库写的是否正确，非必须
		try {
			File saveToDbFile = new File(saveToDbFilePath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(saveToDbFile));
			bw.write("siteUrl" + FILE_SEPARATOR + "siteId" + FILE_SEPARATOR
					+ "date" + FILE_SEPARATOR + "cpm" + FILE_SEPARATOR + "ctr"
					+ FILE_SEPARATOR + "uv" + FILE_SEPARATOR + "click"
					+ FILE_SEPARATOR + "hourClick");
			bw.newLine();
			for (WM123SiteCprodataVo cprodataVo : cprodataVoList) {
				bw.write(cprodataVo.toStringInFile());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			LOG.error("3.Fail! Write to file:" + saveToDbFilePath);
		}
		LOG.info("3.Success! Write to file:" + saveToDbFilePath);
	}

	public void setWm123SiteStatDao(WM123SiteStatDao wm123SiteStatDao) {
		this.wm123SiteStatDao = wm123SiteStatDao;
	}
}
