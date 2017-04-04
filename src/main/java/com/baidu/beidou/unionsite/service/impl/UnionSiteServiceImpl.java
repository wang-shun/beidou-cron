/**
 * 2009-4-20 下午10:16:14
 */
package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.ErrorParameterException;
import com.baidu.beidou.unionsite.UnionSiteImporter;
import com.baidu.beidou.unionsite.bo.UnionSiteBo;
import com.baidu.beidou.unionsite.bo.UnionSiteIndex;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.UnionSiteFileDao;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.unionsite.service.DomainComparator;
import com.baidu.beidou.unionsite.service.UnionSiteService;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.file.IteratorObjectReader;
import com.baidu.beidou.util.file.IteratorObjectWriter;
import com.baidu.beidou.util.file.ObjectAccessUtil;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class UnionSiteServiceImpl implements UnionSiteService {
	private static final Log LOG = LogFactory
			.getLog(UnionSiteServiceImpl.class);

	/**
	 * 联盟站点的索引文件名
	 */
	private String indexFileName = "unionsiteindex.dat";

	/**
	 * 联盟站点的二进制文件名
	 */
	private String unionSiteFile = "unionsite.dat";

	private UnionSiteFileDao unionSiteFileDao = null;

	private static final String CHARSET = "GBK";

	/**
	 * 读取联盟的站点信息，存储为二进制的站点文件，并建立索引，进行排序
	 * 
	 * @author zengyunfeng
	 * @param filename
	 * @throws IOException
	 *             文件不存在或者读取异常
	 */
	public List<UnionSiteIndex> sortUnionSite(final String filename)
			throws IOException {
		// 读联盟数据，校验，建索引，存储，排序
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename), CHARSET));
		//删除文件，否则后续的写不会删除老的文件
		final File siteFile = new File(unionSiteFile);
		if(siteFile.exists()){
			siteFile.delete();
		}
		
		final RandomAccessFile siteAccess = new RandomAccessFile(unionSiteFile, "rw");
		final List<UnionSiteIndex> indexList = new ArrayList<UnionSiteIndex>(20000);
		UnionSiteIndex index = null;
		UnionSiteBo unionSiteBo = null;
		boolean ended = false; // 是否到文件末尾

		try {
			do {
				try {
					unionSiteBo = unionSiteFileDao.readRecord(reader);
					if (unionSiteBo == null) {
						ended = true;
					} else {
						index = new UnionSiteIndex();
						// 设置的索引，但是domainFlag并没有设置,domain设置的是为处理过的site值
						index.setStart(siteAccess.getFilePointer());
						int length = ObjectAccessUtil.writeObject(siteAccess,
								unionSiteBo);
						index.setDomain(unionSiteBo.getSiteUrl());
						index.setCname(unionSiteBo.getCname());
						index.setShowFlag(unionSiteBo.getShowFlag());
						index.setLength(length);

						indexList.add(index);
					}

				} catch (ErrorFormatException e) {
					LogUtils.error(LOG, e.getMessage(), e);
				}
			} while (!ended);
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LogUtils.fatal(LOG, e.getMessage(), e);
			}
			try {
				siteAccess.close();
			} catch (IOException e) {
				LogUtils.fatal(LOG, e.getMessage(), e);
			}

		}

		// 排序
		Collections.sort(indexList, new Comparator<UnionSiteIndex>() {

			public int compare(UnionSiteIndex o1, UnionSiteIndex o2) {
				String tmpO1 = o1.getDomain();
				String tmpO2 = o2.getDomain();
				
				int result = DomainComparator.domainCompare(tmpO1, tmpO2);
				if (result == 0) {
					//modify by liangshimu@cpweb-250，排序规则变成：域名->showFlag
					result = o1.getShowFlag() - o2.getShowFlag();
					if (result == 0) {
						return o1.getCname().compareTo(o2.getCname());
					}
				}
				return result;
			}

		});

		// 设置domainFlag,处理domain为二级域名
		formatSiteIndexList(indexList);

		return indexList;

	}

	/**
	 * //设置domainFlag并没有设置,处理domain
	 * 
	 * @author zengyunfeng
	 * @param list
	 */
	private void formatSiteIndexList(final List<UnionSiteIndex> list) {
		int size = list.size();
		String curMainDomain = null; // 当前的一级域名（在站点文件中实际存在）
		final Set<String> curSecondDomains = new HashSet<String>(10); // 当前主域下的的二级域名
		UnionSiteIndex site = null; // 当前的站点
		String mainDomain = null; // 当前站点对应的一级域名（站点文件中可能并不存在）
		String secondDomain = null; // 当前站点对应的二级域名（站点文件中可能并不存在）
		for (int index = 0; index < size; index++) {
			curMainDomain = null;
			curSecondDomains.clear();
			site = list.get(index);
			if (UrlParser.isIp(site.getDomain())) {
				// 过滤掉IP
				list.remove(index);
				index--;
				size--;
				LogUtils.warn(LOG, "ignore ip in union site file with domain='"
						+ site.getDomain() + "'");
				UnionSiteImporter.ignore++;
				continue;
			} else if (site.getDomain().contains(":")) {
				// 过滤掉包含端口号的域名
				list.remove(index);
				index--;
				size--;
				LogUtils.warn(LOG,
						"ignore site with port in union site file with domain='"
								+ site.getDomain() + "'");
				UnionSiteImporter.ignore++;
				continue;
			}
			mainDomain = UrlParser.fetchMainDomain(site.getDomain());

			int nextIndex = index + 1;
			for (String nextDomain = null; nextIndex < size; nextIndex++) {
				nextDomain = UrlParser.fetchMainDomain(list.get(nextIndex)
						.getDomain());
				if (!mainDomain.equals(nextDomain)) {
					break;
				}

			}
			// nextIndex指向下一个一级域名不一样的域名

			for (int i = index; i < nextIndex; i++) {
				site = list.get(i);
				if (site.getDomain().equals(mainDomain)) {
					// 当前为主域
					curMainDomain = site.getDomain();
					site.setDomainFlag(SiteConstant.MAINDOMAIN);
					continue;
				}
				secondDomain = UrlParser.fetchSecondDomain(site.getDomain());
				if (site.getDomain().equals(secondDomain)) {
					// 当前为二级域名
					curSecondDomains.add(secondDomain);
					site.setDomainFlag(SiteConstant.SECONDDOMAIN);
					continue;
				}

				// 当前为二级以下的域名
				if ((curMainDomain != null && site.getDomain().endsWith(
						"." + curMainDomain))
						|| curSecondDomains.contains(secondDomain)) {
					// 已经存在对应的一级域名或者二级域名，则舍弃
					list.remove(i);
					i--;
					nextIndex--;
					size--;
					LogUtils.warn(LOG,
							"ignore thirdDomain in union site file with domain='"
									+ site.getDomain() + "'");
					UnionSiteImporter.ignore++;
				} else { // 截断为二级域名
					site.setDomain(secondDomain);
					site.setDomainFlag(SiteConstant.SECONDDOMAIN);
				}

			}
			// 修正index
			index = nextIndex - 1;
		}
	}

	/**
	 * 存储联盟站点索引信息
	 * 
	 * @author zengyunfeng
	 * @param indexFileName
	 * @throws ErrorParameterException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void persistentSiteIndex(final List<UnionSiteIndex> indexes)
			throws ErrorParameterException, FileNotFoundException, IOException {
		IteratorObjectWriter<UnionSiteIndex> output = null;
		try {
			output = new IteratorObjectWriter<UnionSiteIndex>(indexFileName);
			output.persistentCache(indexes);
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
	 * 读取联盟站点索引信息
	 * 
	 * @author zengyunfeng
	 * @param indexFileName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	public List<UnionSiteIndex> loadSiteIndex() throws IOException,
			ClassNotFoundException {
		final ObjectInputStream indexInputStream = new ObjectInputStream(
				new FileInputStream(indexFileName));
		List<UnionSiteIndex> result;
		try {
			result = (List<UnionSiteIndex>) indexInputStream.readObject();
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
	 * @param indexFileName
	 *            the indexFileName to set
	 */
	public void setIndexFileName(String indexFileName) {
		this.indexFileName = indexFileName;
	}

	/**
	 * @param unionSiteFile
	 *            the unionSiteFile to set
	 */
	public void setUnionSiteFile(String unionSiteFile) {
		this.unionSiteFile = unionSiteFile;
	}

	/**
	 * @param unionSiteFileDao
	 *            the unionSiteFileDao to set
	 */
	public void setUnionSiteFileDao(UnionSiteFileDao unionSiteFileDao) {
		this.unionSiteFileDao = unionSiteFileDao;
	}

	/**
	 * @return the indexFileName
	 */
	public String getIndexFileName() {
		return indexFileName;
	}

	/**
	 * @return the unionSiteFile
	 */
	public String getUnionSiteFile() {
		return unionSiteFile;
	}

	public Set<String> getCurrentValidDomainFromIndexFile() {
		Set<String> set = new HashSet<String>();
		try {
			IteratorObjectReader<UnionSiteIndex> unionSiteIterator = new IteratorObjectReader<UnionSiteIndex>(indexFileName);
			while(unionSiteIterator.hasNext()) {
				UnionSiteIndex index = unionSiteIterator.next();
				set.add(index.getDomain());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}
	
	public static void main(String[] args) {
		List<Integer> l = new ArrayList<Integer>();
		l.add(0);
		l.add(1);
		l.add(0);
		Collections.sort(l, new Comparator<Integer>(){
	
			public int compare(Integer o1, Integer o2) {
				// TODO Auto-generated method stub
				return o1 - o2;
			}});
		
		System.out.println(l);

		List<UnionSiteIndex> list = new ArrayList<UnionSiteIndex>();
		UnionSiteIndex i = new UnionSiteIndex();
		i.setDomain("1");
		i.setCname("2");
		i.setShowFlag(0);
		list.add(i);
		i = new UnionSiteIndex();
		i.setDomain("1");
		i.setCname("2");
		i.setShowFlag(1);
		list.add(i);
		i = new UnionSiteIndex();
		i.setDomain("1");
		i.setCname("2");
		i.setShowFlag(0);
		list.add(i);

		// 排序
		Collections.sort(list, new Comparator<UnionSiteIndex>() {

			public int compare(UnionSiteIndex o1, UnionSiteIndex o2) {
				String tmpO1 = o1.getDomain();
				String tmpO2 = o2.getDomain();
				
				int result = DomainComparator.domainCompare(tmpO1, tmpO2);
				if (result == 0) {
					//modify by liangshimu@cpweb-250，排序规则变成：域名->showFlag
					result = o1.getShowFlag() - o2.getShowFlag();
					if (result == 0) {
						return o1.getCname().compareTo(o2.getCname());
					}
				}
				return result;
			}

		});
		
		System.out.println(list);
	}
}
