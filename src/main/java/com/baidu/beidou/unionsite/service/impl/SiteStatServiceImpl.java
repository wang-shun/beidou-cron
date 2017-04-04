/**
 * 2009-4-23 下午03:58:07
 */
package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.bo.IPCookieBo;
import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.SiteStatFileDao;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.unionsite.service.DomainComparator;
import com.baidu.beidou.unionsite.service.SiteStatService;
import com.baidu.beidou.unionsite.service.SiteStatUtil;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.file.IteratorObjectWriter;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteStatServiceImpl implements SiteStatService {

	private static final Log LOG = LogFactory.getLog(SiteStatServiceImpl.class);
	
	private static final Log statLogger = LogFactory.getLog("stat");
	
	/**
	 * 处理后的日统计二进制文件名前缀，全文件名为dayFilePrefix{index}, index=(0,
	 * MAX_STAT_FILE_SIZE-1)
	 */
	private String dayFilePrefix = "daysitestat.";
	private String avgSiteStatFile = "lastsevensitestat";
	private final static int MAX_STAT_FILE_SIZE = 7;
	private static final String charset = "GBK";
	private SiteStatFileDao statFileDao = null;
	public static final String ipcookieFileSuffix = ".ipcookie";
	public static final String flowFileSuffix = ".flow";
	public static final String filmFileSuffix = ".tiepian";

	private final Comparator<SiteStatBo> siteStatComparator = new Comparator<SiteStatBo>() {

		public int compare(SiteStatBo o1, SiteStatBo o2) {
			String tmpO1 = o1.getDomain();
			String tmpO2 = o2.getDomain();
			
			int result = DomainComparator.domainCompare(tmpO1, tmpO2);
			if (result == 0) {
				return o1.getCntn().compareTo(o2.getCntn());
			}
			return result;
		}

	};

	/**
	 * 对日统计数据进行校验，排序，然后存储
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 */
	public void sortDaySiteStat(String statFileName) throws InternalException,
			IOException {
		LogUtils.info(LOG, "start reading stat file.....");
		int[] range = statFileDao.getFileRange();
		if (range == null || range.length != 2) {
			LogUtils.fatal(LOG, "can't get stat file range");
			throw new InternalException("can't get stat file range");
		}

		if (range[0] >= MAX_STAT_FILE_SIZE) {
			range[1] = (range[1] + 1) % MAX_STAT_FILE_SIZE;
		} else {
			range[0]++;
		}
		// 新文件的序号
		int curIndex = (range[1] + range[0] - 1) % MAX_STAT_FILE_SIZE;

		LogUtils.info(LOG, "reading stat file: " + statFileName);
		long ms = System.currentTimeMillis();
		
		// 读原始文件，排序
		List<SiteStatBo> statList = readAndSortSiteStat(statFileName, SiteConstant.DISP_FIXED_FLAG);
		
		LogUtils.info(LOG, "end reading stat file: " + statFileName + ", size = " + statList.size());
		LogUtils.info(LOG, "reading stat file: " + statFileName + flowFileSuffix);
		
		// 读原始文件(悬浮)，排序 added by zhuqian @beidou1.2.24
		List<SiteStatBo> flowList = readAndSortSiteStat(statFileName + flowFileSuffix, SiteConstant.DISP_FLOW_FLAG);
		LogUtils.info(LOG, "end reading stat file: " + statFileName + flowFileSuffix + ", size = " + flowList.size());

		// 读原始文件(贴片)，排序 added by zhuqian @beidou1.2.33
		List<SiteStatBo> filmList = readAndSortSiteStat(statFileName + filmFileSuffix, SiteConstant.DISP_FILM_FLAG);
		LogUtils.info(LOG, "end reading stat file: " + statFileName + filmFileSuffix + ", size = " + filmList.size());
		
		//合并固定流量统计文件 && 悬浮流量统计文件 added by zhuqian @beidou1.2.24
		statList = mergeSiteStatList(statList, flowList);
		LogUtils.info(LOG, "end merging statList + flowList, totalSize = " + statList.size());
		
		//合并固定+悬浮流量统计文件 && 贴片流量统计文件 added by zhuqian @beidou1.2.33
		statList = mergeSiteStatList(statList, filmList);
		LogUtils.info(LOG, "end merging statList + flowList + filmList, totalSize = " + statList.size());
		
		LogUtils.info(LOG, "end reading stat file in [" + (System.currentTimeMillis() - ms) + "] ms");

		// 存储排好序的二进制文件
		// 现改由明文存储, mod by zhuqian @beidou1.2.33 
		FileOutputStream output = new FileOutputStream(dayFilePrefix + curIndex);
		try {
			statFileDao.persistentAll(output, statList);
		} catch (IOException e) {
			throw e;
		} finally {
			if(output != null){
				output.close();
			}
		}

		readAndStoreIPCookie(statFileName + ipcookieFileSuffix, dayFilePrefix
				+ curIndex + ipcookieFileSuffix);
		LogUtils.info(LOG, "end sort stat");
		// 更新数据库中的start和size
		statFileDao.storeFileRange(range[1], range[0]);
		LogUtils.info(LOG, "end store sorted stat file");
	}
	
	/**
	 * 读取老的文件修改成老的对象
	 * @author zengyunfeng
	 * @param file
	 * @throws InternalException 
	 * @throws IOException 
	 */
	public void transform(String statFileName) throws InternalException, IOException{
		LogUtils.info(LOG, "start reading stat file.....");
		int[] range = statFileDao.getFileRange();
		if (range == null || range.length != 2) {
			LogUtils.fatal(LOG, "can't get stat file range");
			throw new InternalException("can't get stat file range");
		}

		if (range[0] >= MAX_STAT_FILE_SIZE) {
			range[1] = (range[1] + 1) % MAX_STAT_FILE_SIZE;
		} else {
			range[0]++;
		}
		// 新文件的序号
		int curIndex = (range[1] + range[0] - 1) % MAX_STAT_FILE_SIZE;

		LogUtils.info(LOG, "reading stat file: " + statFileName);
		
		// 读原始文件，排序
		List<SiteStatBo> statList = readSiteStat(statFileName);
		
		LogUtils.info(LOG, "end reading stat file: " + statFileName + ", size = " + statList.size());
		
		// 存储排好序的二进制文件
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(dayFilePrefix + curIndex));
		try {
			statFileDao.persistentOldAll(output, statList);
		} catch (IOException e) {
			output.close();
			throw e;
		}

		readAndStoreIPCookie(statFileName + ipcookieFileSuffix, dayFilePrefix
				+ curIndex + ipcookieFileSuffix);
		LogUtils.info(LOG, "end sort stat");
		// 更新数据库中的start和size
		statFileDao.storeFileRange(range[1], range[0]);
		LogUtils.info(LOG, "end store sorted stat file");
	}
	
	/*
	 * added by zhuqian @beidou1.2.24
	 */
	private List<SiteStatBo> mergeSiteStatList(List<SiteStatBo> listA, List<SiteStatBo> listB){
		
		if(listA == null || listA.size() == 0){
			return listB;
		}
		if(listB == null || listB.size() == 0){
			return listA;
		}
				
		int s = 0;
		int f = 0;
		for(; s < listA.size() && f < listB.size();){
			
			SiteStatBo a = listA.get(s);
			SiteStatBo b = listB.get(f);
			
			if(siteStatComparator.compare(a, b) < 0){
				//用下一个a与b比较
				s++;
				
			}else if(siteStatComparator.compare(a, b) > 0){
				//将b加入A，取代a的位置，再用a与下一个b比较
				listA.add(s, b);
				s++; f++;
				
			}else{
				SiteStatBo combined = SiteStatUtil.mergeSiteStat(a, b);
				listA.set(s, combined);
				s++; f++;
				
			}			
		}
		
		//如果flowList中的记录没有全部添加到statList中，则将剩余数据全部追加到statList尾部
		if(f < listB.size()){
			
			listA.addAll(listB.subList(f, listB.size()));
			
		}

		return listA;
		
	}
	
	private void readAndStoreIPCookie(String fileName, String ouputfile)
			throws IOException {
		List<IPCookieBo> statList = readAndSortSiteIPCookie(fileName);
		// 存储排好序的二进制文件
		ObjectOutputStream output = new ObjectOutputStream(
				new FileOutputStream(ouputfile));
		try {
			statFileDao.persistentAllIPCookie(output, statList);
		} catch (IOException e) {
			output.close();
			throw e;
		}

	}

	private List<IPCookieBo> readAndSortSiteIPCookie(String fileName)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), charset));
		List<IPCookieBo> result = new ArrayList<IPCookieBo>(10000);
		try {
			do {
				try {
					IPCookieBo bo = statFileDao.readIPCookieRecord(reader);
					if (bo == null) {
						break;
					}
					result.add(bo);
				} catch (ErrorFormatException e) {
					LogUtils.error(LOG, e.getMessage(), e);
				}
			} while (true);

			Collections.sort(result, new Comparator<IPCookieBo>() {

				public int compare(IPCookieBo o1, IPCookieBo o2) {
					String tmpO1 = o1.getDomain();
					String tmpO2 = o2.getDomain();
					
					
					return DomainComparator.domainCompare(tmpO1, tmpO2);
				}

			});
			return result;

		} catch (IOException e) {
			throw e;
		} finally{
			reader.close();
		}

	}

	private List<SiteStatBo> readAndSortSiteStat(String fileName, int displayType)
			throws IOException {
		
		File statFile = new File(fileName);
		if (!statFile.exists()) {
			return new ArrayList<SiteStatBo>();
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), charset));
		List<SiteStatBo> result = new ArrayList<SiteStatBo>(10000);
		try {
			do {
				try {
					SiteStatBo bo = statFileDao.readRecord(reader, displayType);
					if (bo == null) {
						break;
					}
					result.add(bo);
				} catch (ErrorFormatException e) {
					statLogger.error(displayType + "\t" + e.getMessage());
					LogUtils.error(LOG, e.getMessage(), e);
				}
			} while (true);

			Collections.sort(result, siteStatComparator);
			return result;

		} catch (IOException e) {
			throw e;
		} finally{
			reader.close();
		}

	}
	
	private List<SiteStatBo> readSiteStat(String fileName)
		throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), charset));
		List<SiteStatBo> result = new ArrayList<SiteStatBo>(10000);
		try {
			do {
				try {
					SiteStatBo bo = statFileDao.readOldRecord(reader);
					if (bo == null) {
						break;
					}
					result.add(bo);
				} catch (ErrorFormatException e) {
					LogUtils.error(LOG, e.getMessage(), e);
				}
			} while (true);
		
			Collections.sort(result, siteStatComparator);
			return result;
		
		} catch (IOException e) {
			throw e;
		} finally{
			reader.close();
		}
		
	}
	
	

	/**
	 * 获得7天的平均统计数据并存储为平均数据文件
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void averageSiteStat() throws InternalException, IOException,
			ClassNotFoundException, ErrorFormatException {

		int[] range = statFileDao.getFileRange();
		if (range == null || range.length != 2) {
			LogUtils.fatal(LOG, "can't get stat file range");
			throw new InternalException("can't get stat file range");
		}
		if (range[0] <= 0 || range[0] > MAX_STAT_FILE_SIZE) {
			LogUtils.fatal(LOG, "error stat file range");
			throw new InternalException("can't get stat file range");
		}
		range[1] = range[1] % MAX_STAT_FILE_SIZE;

		averageSiteStatNoIp(range);
		averageSiteIpCookieStat(range);
	}

	/**
	 * 获得7天的平均统计数据并存储为平均数据文件
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void averageSiteStatNoIp(int[] range) throws InternalException,
			IOException, ErrorFormatException {
		LogUtils.info(LOG, "start average stat file.....");

		List<SiteStatBo> result = new ArrayList<SiteStatBo>(10000);
		BufferedReader[] inputs = new BufferedReader[range[0]]; // 日统计文件句柄
		SiteStatBo[] curStat = new SiteStatBo[range[0]]; // 当前读取的记录
		boolean[] moves = new boolean[range[0]]; // 日统计文件是否需要读取下一条记录，当前读取的是最小记录，则下次需要读下一条记录
		boolean allEnd = false; // 是否全部读取结束
		List<Integer> minSites = new ArrayList<Integer>(range[0]); // 最小记录的序号

		try {
			for (int i = 0; i < range[0]; i++) {
				
				inputs[i] = new BufferedReader(new InputStreamReader(new FileInputStream(
						dayFilePrefix + (range[1] + i) % MAX_STAT_FILE_SIZE), charset));
				moves[i] = true;
			}

			int comValue = 0; // 记录比较结果
			SiteStatBo avgStat = null; // 平均统计结果
			while (!allEnd) {
				// 读取日统计文件中一条记录,
				for (int i = 0; i < range[0]; i++) {
					if (moves[i]) {
						curStat[i] = statFileDao.next(inputs[i]);
					}
					moves[i] = false;
				}
				// 找出最小的记录
				allEnd = true;
				minSites.clear();
				minSites.add(-1);
				for (int i = 0; i < range[0]; i++) {
					if (curStat[i] != null) {
						if (minSites.get(0) < 0) { // 第一个有数据的记录
							comValue = 1;
						} else {
							comValue = siteStatComparator.compare(
									curStat[minSites.get(0)], curStat[i]);
						}

						if (comValue > 0) {
							minSites.clear();
							minSites.add(i);
						} else if (comValue == 0) {
							minSites.add(i);
						}
						allEnd = false; // 有最小值
					}
				}

				if (allEnd) { // 全部读完
					break;
				}

				avgStat = null;
				for (Integer minIndex : minSites) {
					// 对最小记录计算平均数，最小记录对应的文件需要读取下一条记录
					moves[minIndex] = true;
					// 统计数据相加
					avgStat = SiteStatUtil.mergeSiteStat(avgStat,
							curStat[minIndex]);
				}
				SiteStatUtil.averageSiteStat(avgStat, minSites.size());
				result.add(avgStat);
			}
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} catch (ErrorFormatException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			for (int i = 0; i < range[0]; i++) {
				if (inputs[i] != null) {
					try {
						inputs[i].close();
					} catch (IOException e) {
						LogUtils.fatal(LOG, e.getMessage(), e);
						// 此处不能throw异常，否则会引起前面的文件关闭异常，导致后面的文件没有关闭的情况。
						// 如果文件读取都没有异常，只是在关闭文件发生异常，则不影响后续的处理，因此可以不抛出异常。
						// 如果是打开或者读取发生异常，则会抛出异常。
					}
				}
			}
		}
		// 存储
		persistentAvgSiteStat(result);
		LogUtils.info(LOG, "end average stat file");

	}

	/**
	 * 获得7天的平均统计数据并存储为平均数据文件
	 * 
	 * @author zengyunfeng
	 * @throws InternalException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void averageSiteIpCookieStat(int[] range) throws InternalException,
			IOException, ClassNotFoundException {
		LogUtils.info(LOG, "start average ip stat file.....");

		List<IPCookieBo> result = new ArrayList<IPCookieBo>(10000);
		ObjectInputStream[] inputs = new ObjectInputStream[range[0]]; // 日统计文件句柄
		IPCookieBo[] curStat = new IPCookieBo[range[0]]; // 当前读取的记录
		boolean[] moves = new boolean[range[0]]; // 日统计文件是否需要读取下一条记录，当前读取的是最小记录，则下次需要读下一条记录
		boolean allEnd = false; // 是否全部读取结束
		List<Integer> minSites = new ArrayList<Integer>(range[0]); // 最小记录的序号

		try {
			for (int i = 0; i < range[0]; i++) {
				inputs[i] = new ObjectInputStream(new FileInputStream(
						dayFilePrefix + ((range[1] + i) % MAX_STAT_FILE_SIZE)
								+ ipcookieFileSuffix));
				moves[i] = true;
			}

			int comValue = 0; // 记录比较结果
			IPCookieBo avgStat = null; // 平均统计结果
			while (!allEnd) {
				// 读取日统计文件中一条记录,
				for (int i = 0; i < range[0]; i++) {
					if (moves[i]) {
						curStat[i] = statFileDao.nextIp(inputs[i]);
					}
					moves[i] = false;
				}
				// 找出最小的记录
				allEnd = true;
				minSites.clear();
				minSites.add(-1);
				for (int i = 0; i < range[0]; i++) {
					if (curStat[i] != null) {
						if (minSites.get(0) < 0) { // 第一个有数据的记录
							comValue = 1;
						} else {
							comValue = DomainComparator.domainCompare(
									curStat[minSites.get(0)].getDomain(), curStat[i].getDomain());
						}

						if (comValue > 0) {
							minSites.clear();
							minSites.add(i);
						} else if (comValue == 0) {
							minSites.add(i);
						}
						allEnd = false; // 有最小值
					}
				}

				if (allEnd) { // 全部读完
					break;
				}

				avgStat = null;
				for (Integer minIndex : minSites) {
					// 对最小记录计算平均数，最小记录对应的文件需要读取下一条记录
					moves[minIndex] = true;
					// 统计数据相加
					avgStat = SiteStatUtil.mergeSiteIpCookieStat(avgStat,
							curStat[minIndex]);
				}
				SiteStatUtil.averageSiteIpCookieStat(avgStat, minSites.size());
				result.add(avgStat);
			}
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} catch (ClassNotFoundException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			for (int i = 0; i < range[0]; i++) {
				if (inputs[i] != null) {
					try {
						inputs[i].close();
					} catch (IOException e) {
						LogUtils.fatal(LOG, e.getMessage(), e);
						// 此处不能throw异常，否则会引起前面的文件关闭异常，导致后面的文件没有关闭的情况。
						// 如果文件读取都没有异常，只是在关闭文件发生异常，则不影响后续的处理，因此可以不抛出异常。
						// 如果是打开或者读取发生异常，则会抛出异常。
					}
				}
			}
		}
		// 存储
		persistentAvgSiteIpCookieStat(result);
		LogUtils.info(LOG, "end average ip stat file");

	}

	/**
	 * 存储7日平均统计信息
	 * 
	 * @author zengyunfeng
	 * @param avgStat
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void persistentAvgSiteStat(final List<SiteStatBo> avgStat)
			throws FileNotFoundException, IOException {

		IteratorObjectWriter<SiteStatBo> output = null;
		try {
			output = new IteratorObjectWriter<SiteStatBo>(avgSiteStatFile);
			output.persistentCache(avgStat);
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			if (output != null) {
				output.close();
			}
		}

	}
	
	/**
	 * 存储7日平均统计信息，使用明文存储
	 * （由于后续bdSiteStore使用了IteratorObjectReader处理数据，故暂时没有使用）
	 * 
	 * @param avgStat
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void persistentAvgSiteStatAsText(final List<SiteStatBo> avgStat)
			throws FileNotFoundException, IOException {

		// 现改由明文存储, mod by zhuqian @beidou1.2.33 
		FileOutputStream output = new FileOutputStream(avgSiteStatFile);
		try {
			statFileDao.persistentAll(output, avgStat);
		} catch (IOException e) {
			throw e;
		} finally {
			if(output != null){
				output.close();
			}
		}

	}
	
	/**
	 * 存储7日平均统计信息
	 * 
	 * @author zengyunfeng
	 * @param avgStat
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void persistentAvgSiteIpCookieStat(final List<IPCookieBo> avgStat)
			throws FileNotFoundException, IOException {

		IteratorObjectWriter<IPCookieBo> output = null;
		try {
			output = new IteratorObjectWriter<IPCookieBo>(avgSiteStatFile+ipcookieFileSuffix);
			output.persistentCache(avgStat);
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			if (output != null) {
				output.close();
			}
		}

	}

	/**
	 * 读取7日平均统计数据信息
	 * 
	 * @author zengyunfeng
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public List<SiteStatBo> loadAvgSiteStat() throws IOException,
			ClassNotFoundException {
		ObjectInputStream indexInputStream = new ObjectInputStream(
				new FileInputStream(avgSiteStatFile));
		List<SiteStatBo> result;
		try {
			result = (List<SiteStatBo>) indexInputStream.readObject();
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} catch (ClassNotFoundException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			indexInputStream.close();
		}

		return result;
	}

	/**
	 * @param statFileDao
	 *            the statFileDao to set
	 */
	public void setStatFileDao(SiteStatFileDao statFileDao) {
		this.statFileDao = statFileDao;
	}

	/**
	 * @param dayFilePrefix
	 *            the dayFilePrefix to set
	 */
	public void setDayFilePrefix(String dayFilePrefix) {
		this.dayFilePrefix = dayFilePrefix;
	}

	public Set<String> getValidDomainFromCurrentDayFileAfterImported() {

		Set<String> set = new HashSet<String>();
		
		int[] range = statFileDao.getFileRange();
		if (range == null || range.length != 2) {
			LogUtils.fatal(LOG, "can't get stat file range");
			return null;
		}

		if (range[0] >= MAX_STAT_FILE_SIZE) {
			range[1] = (range[1] + 1) % MAX_STAT_FILE_SIZE;
		} else {
			range[0]++;
		}
		// 当天文件的序号，此处由于已经是更新后的值了，所以需要－2
		int curIndex = (range[1] + range[0] - 1 - 1) % MAX_STAT_FILE_SIZE;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(dayFilePrefix + curIndex), charset));
			SiteStatBo bo;
			while( (bo = statFileDao.next(br)) != null ) {
				set.add(bo.getDomain());
				set.add(UrlParser.fetchMainDomain(bo.getDomain()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
		return set;
	}

	/**
	 * @param avgSiteStatFile
	 *            the avgSiteStatFile to set
	 */
	public void setAvgSiteStatFile(String avgSiteStatFile) {
		this.avgSiteStatFile = avgSiteStatFile;
	}

	/**
	 * @return the avgSiteStatFile
	 */
	public String getAvgSiteStatFile() {
		return avgSiteStatFile;
	}
}
