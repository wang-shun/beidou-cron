/**
 * 2009-4-22 上午04:12:15
 */
package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.QValue;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.service.DomainComparator;
import com.baidu.beidou.unionsite.service.QValueService;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.file.IteratorObjectWriter;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class QValueServiceImpl implements QValueService {

	private static final Log LOG = LogFactory.getLog(QValueServiceImpl.class);
	private String qcacheFileName = "q_";
	private static final String QFILEFORMAT = "yyyyMMddhhmm";
	private static final String CHARSET = "GBK";
	private static final String FIELD_SPLITER = "\t";

	/**
	 * 获得Q值的二进制配置文件，如果配置文件发生变化，则排序，过滤，处理，然后以二进制文件存储，如果文件没有发生变化，则直接返回上次保存的二进制文件
	 * 
	 * @author zengyunfeng
	 * @param q_main
	 * @param q_site
	 * @param recompute
	 *            是否需要重新载入Q值文件
	 * @return 返回Q值二进制文件名
	 * @throws IOException
	 */
	public String loadQValue(String q_main, String q_site, boolean recompute)
			throws IOException {
		File q_main_file = new File(q_main);
		if (!q_main_file.isFile()) {
			throw new FileNotFoundException(q_main
					+ " does not exist or is not a file");
		}
		File q__site_file = new File(q_site);
		if (!q__site_file.isFile()) {
			throw new FileNotFoundException(q_site
					+ " does not exist or is not a file");
		}
		Date maindate = new Date(q_main_file.lastModified());
		Date sitedate = new Date(q__site_file.lastModified());

		DateFormat format = new SimpleDateFormat(QFILEFORMAT);
		String curQCacheFile = null;
		// 取修改时间最近的修改时间判断
		if (maindate.compareTo(sitedate) < 0) {
			curQCacheFile = qcacheFileName + format.format(sitedate);
		} else {
			curQCacheFile = qcacheFileName + format.format(maindate);
		}

		File qcacheFile = new File(curQCacheFile);
		if (!recompute && qcacheFile.isFile()) {
			// 存在对应的缓存文件，直接读取。
			return curQCacheFile;
		}
		List<QValue> qList = loadQvalueConfig(q_main, true);
		List<QValue> qsiteList = loadQvalueConfig(q_site, false);
		qList.addAll(qsiteList);
		sortAndValidQSort(qList);
		persistentCache(qList, curQCacheFile);
		return curQCacheFile;
	}

	/**
	 * 读取Q值的配置文件
	 * 
	 * @author zengyunfeng
	 * @param qfilename
	 * @return
	 * @throws IOException
	 */
	private List<QValue> loadQvalueConfig(String qfilename, boolean isDomain)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(qfilename), CHARSET));
		List<QValue> qList = new ArrayList<QValue>(20000);
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if ("".equals(line.trim())) {
					continue;
				}
				QValue qValue = parseQValue(line, isDomain);
				if (qValue != null) {
					qList.add(qValue);
				}
			}
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			reader.close();
		}
		return qList;
	}

	/**
	 * 对QValue列进行排序，并过滤掉其中重复的，重复的取最小值
	 * 
	 * @author zengyunfeng
	 * @param qList
	 */
	private void sortAndValidQSort(final List<QValue> qList) {
		/**
		 * 按照domain,DomainFlag进行排序
		 */
		Collections.sort(qList, new Comparator<QValue>() {

			public int compare(QValue o1, QValue o2) {
				String tmpO1 = o1.getDomain();
				String tmpO2 = o2.getDomain();
				
				
				int result = DomainComparator.domainCompare(tmpO1, tmpO2);
				if (result == 0) {
					return o1.getDomainFlag() - o2.getDomainFlag();
				}
				return result;
			}

		});

		int size = qList.size();
		String secondDomain = null;
		String mainDomain = null;
		float minQ1 = Float.MAX_VALUE;
		float minQ2 = Float.MAX_VALUE;
		boolean hasSecondDomain = false;
		for (int index = 0; index < size; index++) {
			hasSecondDomain = false;
			secondDomain = UrlParser.fetchSecondDomain(qList.get(index)
					.getDomain());
			if (secondDomain == null) {
				qList.get(index).setDomainFlag(SiteConstant.MAINDOMAIN);
				// 删除重复的一级域名
				mainDomain = UrlParser.fetchMainDomain(qList.get(index)
						.getDomain());
				if (mainDomain == null) {
					continue;
				}
				index++;
				for (String curFirst = null; index < size;) {
					curFirst = UrlParser.fetchMainDomain(qList.get(index)
							.getDomain());
					if (mainDomain.equals(curFirst)
							&& curFirst.equals(qList.get(index).getDomain())) {
						// 删除重复的
						qList.remove(index);
						size--;
					} else {
						break;
					}
				}
				index--;
				continue;
			} else if (secondDomain.equals(qList.get(index).getDomain())) {
				hasSecondDomain = true;
			}
			minQ1 = qList.get(index).getQ1();
			minQ2 = qList.get(index).getQ2();
			index++;
			for (String curSecond = null; index < size;) {
				curSecond = UrlParser.fetchSecondDomain(qList.get(index)
						.getDomain());
				if (curSecond != null && secondDomain.equals(curSecond)) {
					// 相同二级域名的二级或者三级域名
					if (!hasSecondDomain) {
						if (minQ1 > qList.get(index).getQ1()) {
							minQ1 = qList.get(index).getQ1();
						}
						if (minQ2 > qList.get(index).getQ2()) {
							minQ2 = qList.get(index).getQ2();
						}
					}
					qList.remove(index);
					size--;
				} else {
					break;
				}
			}
			index--;
			if (!hasSecondDomain) {
				qList.get(index).setDomain(secondDomain);
				qList.get(index).setQ1(minQ1);
				qList.get(index).setQ2(minQ2);
			}
			qList.get(index).setDomainFlag(SiteConstant.SECONDDOMAIN);
		}

	}

	/**
	 * 列名 数据类型和说明 站点/主域 字符串类型，长度256以内的变长字符串 质量度得分1 浮点数 质量度得分2
	 * 浮点数（注：质量度得分1和2分别是以不同的方式对分维度指标加权汇总的） 质量度类型 整型，1表示站点，2表示主域 该站点质量度覆盖的分指标类型
	 * 整型，在beidou使用过程中可不关注
	 * 
	 * @author zengyunfeng
	 * @param line
	 * @return
	 */
	private QValue parseQValue(String line, boolean isDomain) {
		if (line == null) {
			return null;
		}
		String[] fields = line.split(FIELD_SPLITER);
		if (fields.length < 3) {
			LogUtils.error(LOG, "error format of q_"
					+ (isDomain ? "main" : "site") + " file with record='"
					+ line + "'");
			return null;
		} else if (fields.length != 5) {
			LogUtils.warn(LOG, "error format of q_"
					+ (isDomain ? "main" : "site") + " file with record='"
					+ line + "'");
		}
		QValue result = new QValue();
		String curFiled = fields[0].trim();
		if (UrlParser.isIp(curFiled)) {
			LogUtils.warn(LOG, "error format of q_"
					+ (isDomain ? "main" : "site") + " file with record='"
					+ line + "'");
			return null;
		} else if (curFiled.contains(":")) {
			LogUtils.warn(LOG, "error format of q_"
					+ (isDomain ? "main" : "site") + " file with record='"
					+ line + "'");
			return null;
		}

		if (isDomain) {
			result.setDomain(curFiled);
			result.setDomainFlag(SiteConstant.MAINDOMAIN);
		} else {
			String secondDomain = UrlParser.fetchSecondDomain(curFiled);
			result.setDomainFlag(SiteConstant.SECONDDOMAIN);
			if (secondDomain == null) {
				// 为一级域名
				result.setDomain(curFiled);
			} else {
				result.setDomain(secondDomain);
			}

		}

		curFiled = fields[1].trim();
		try {
			float nField = Float.parseFloat(curFiled);
			result.setQ1(nField);
			curFiled = fields[2].trim();
			nField = Float.parseFloat(curFiled);
			result.setQ2(nField);
		} catch (NumberFormatException e) {
			LogUtils.warn(LOG, "error format of q_"
					+ (isDomain ? "main" : "site") + " file with record='"
					+ line + "'");
			return null;
		}
		return result;
	}

	private void persistentCache(final List<QValue> list, final String cacheFile)
			throws FileNotFoundException, IOException {
		IteratorObjectWriter<QValue> output = null;
		try {
			output = new IteratorObjectWriter<QValue>(cacheFile);
			output.persistentCache(list);
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
	 * @param cacheFileName
	 *            the qcacheFileName to set
	 */
	public void setQcacheFileName(String cacheFileName) {
		qcacheFileName = cacheFileName;
	}

	/**
	 * @return the qcacheFileName
	 */
	public String getQcacheFileName() {
		return qcacheFileName;
	}

}
