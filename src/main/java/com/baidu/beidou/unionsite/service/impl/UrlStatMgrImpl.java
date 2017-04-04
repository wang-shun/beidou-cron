/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.UrlStat;
import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.unionsite.dao.UrlStatFileDao;
import com.baidu.beidou.unionsite.service.DomainComparator;
import com.baidu.beidou.unionsite.service.UrlStatMgr;
import com.baidu.beidou.unionsite.vo.SiteSize;
import com.baidu.beidou.unionsite.vo.SiteTotalStat;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.string.StringUtil;

/**
 * ClassName:UrlStatMgrImpl Function: TODO ADD FUNCTION
 * 
 * @author <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version
 * @since TODO
 * @Date 2010 2010-5-23 上午01:01:13
 * 
 * @see
 */

public class UrlStatMgrImpl implements UrlStatMgr {

	private static final Log LOG = LogFactory.getLog(UrlStatMgrImpl.class);
	private static final String URL_SIGN_TABLE = "siteurl";
	private static final String URL_STAT_TABLE = "siteurlstat";
	private static final String SITE_STAT_TABLE = "mainsitesize";
	private static final String OUTPUT_CHARSET= "utf8";
	private UrlStatFileDao urlFileDao = null;
	private BDSiteStatDao bdSiteDao = null;

	private int whiteUrlTop = 100;
	private int validUrlTop = 100;
	private int minCountOfSize=20;//每一个尺寸的最小展现次数,url的每个尺寸的展现量如果小于该数值,则抛弃该尺寸，目的是避免作弊行为
	private int filterThreshold = 200;//当分尺寸流量最大值大于该值时进行20和1%流量过滤
	private int minSrchsForLargeThruput = 20;//最大流量大于filterThreshold时，分尺寸流量必须大于该值；
	private double minSrchsRateForLargeThruput = 0.01;//最大流量大于filterThreshold时，分尺寸流量/max的值必须大于该值；
    

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.beidou.unionsite.service.UrlStatMgr#generateUrlStatFile(java
	 * .util.List, java.lang.String, int, java.lang.String)
	 */
	public void generateUrlStatFile(final List<WhiteUrl> whiteUrlList,
			final String urlFile, final int tableCnt, final String outputPath) throws IOException {
		if(whiteUrlList == null){
			LOG.fatal("参数为null!");
			return ;
		}
		if(urlFile == null || urlFile.length() == 0){
			LOG.fatal("url统计文件为空!");
			return ;
		}
		if(outputPath == null || outputPath.length()==0){
			LOG.fatal("文件路径为空!");
			return ;
		}
		if(tableCnt<1){
			LOG.fatal("拆表个数非正数");
			return ;
		}
		if(outputPath == null || outputPath.length()==0){
			LOG.fatal("文件路径为空!");
			return ;
		}
		
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader( new FileInputStream(urlFile), "gbk"));
		
		BufferedWriter[] urlSignWriters = new BufferedWriter[tableCnt];
		BufferedWriter[] urlStatWriters = new BufferedWriter[tableCnt];
		BufferedWriter siteStatWriter = null;
		
		//获得有效的主域id(包括没有主域的二级域名)
		final Map<String, Integer> validSiteIdMap = bdSiteDao.findAllValidDomainId();
		final List<UrlStat>  urlStatList = new ArrayList<UrlStat>(2000);
		final Map<SiteSize, List<Integer>> unionSiteUrlStat = new HashMap<SiteSize, List<Integer>>(100);		
		final Map<SiteSize, List<Integer>> whiteSiteUrlStat = new HashMap<SiteSize, List<Integer>>(100);
		final Map<Integer, SiteTotalStat> siteTotalStatMap = new HashMap<Integer, SiteTotalStat>();

		UrlStat curUrlStat = null;
		String preDomain = null;	//前一个主域
		String curDomain = null;	//当前url的主域
		String secDomain = null;	//当前url的二级域名
		String site = null;
		Integer siteId = null;
		boolean inValidDomain = false;
		boolean inWhiteUrl = false;
		WhiteUrl whiteUrl = new WhiteUrl();
		int preWhite = 0;
		
		try {
			//初始化输出文件
			for(int index=0; index<tableCnt; index++){
				urlSignWriters[index] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath+File.separatorChar+URL_SIGN_TABLE+index), OUTPUT_CHARSET));
				urlStatWriters[index] = new BufferedWriter(new FileWriter(outputPath+File.separatorChar+URL_STAT_TABLE+index));
			}
			siteStatWriter = new BufferedWriter(new FileWriter(outputPath+File.separatorChar+SITE_STAT_TABLE));
			
			UrlStat nextUrlStat = null;
			for (curUrlStat = urlFileDao.readRecord(reader,minCountOfSize); 
			        curUrlStat == null || curUrlStat.getUrl() == null; ) {
			    curUrlStat = urlFileDao.readRecord(reader,minCountOfSize);
			}
			
			for(nextUrlStat = urlFileDao.readRecord(reader,minCountOfSize); 
			        curUrlStat != null;  
			        nextUrlStat = urlFileDao.readRecord(reader,minCountOfSize)){
			    if( nextUrlStat != null && null == nextUrlStat.getUrl()) {
				    continue;
				}
				if (nextUrlStat != null && curUrlStat.getUrl().equals(nextUrlStat.getUrl())) {
    			    
			       //如果两个Url相同，则进行信息合并
					int mergeDisplaytype = nextUrlStat.getDisplaytype()|curUrlStat.getDisplaytype();
					if(mergeDisplaytype>=SiteConstant.DISP_FULL_FLAG){
						LOG.warn("合并相同url后的展现类型异常,前类型:"+nextUrlStat.getDisplaytype()+",后类型:"+curUrlStat.getDisplaytype());
						continue;
					}
					curUrlStat.setDisplaytype(mergeDisplaytype);
					int mergeSupporttype=nextUrlStat.getSupporttype()|curUrlStat.getSupporttype();
					if(mergeSupporttype>SiteConstant.WL_FULL_SUPPORT){
						LOG.warn("合并相同url后的支持类型异常,前类型:"+nextUrlStat.getSupporttype()+",后类型:"+curUrlStat.getSupporttype());
						continue;
					}
					curUrlStat.setSupporttype(mergeSupporttype);
					curUrlStat.setSrchs(curUrlStat.getSrchs() + nextUrlStat.getSrchs());
					Map<Integer, Integer> preSize=curUrlStat.getSize();
					for(Entry<Integer, Integer> entry : nextUrlStat.getSize().entrySet()){
					    Integer key = entry.getKey();
					    Integer value = preSize.get(key);
					    if (value == null) {
					        value = 0;
					    }
					    value += entry.getValue();
						preSize.put(key, value);
					}
                    continue;
				} else {
                    //begin..., add by liangshimu@cpweb417, 2012-02-28, 在输出之前进行一次过滤
                    int maxThruput = 0;//当前Url所有尺寸中的最大流量值
                    for (Integer thruput : curUrlStat.getSize().values()) {
                        if (thruput > maxThruput) {
                            maxThruput = thruput;
                        }
                    }
                    
                    if (maxThruput >= filterThreshold) {
                    
                        Set<Integer> set = curUrlStat.getSize().keySet();  
                        for (Iterator<Integer> iterator = set.iterator(); iterator.hasNext();) {
                            Integer key = iterator.next();
                            double value = curUrlStat.getSize().get(key) * 1.0;
                            if (value <= minSrchsForLargeThruput
                                    || (value / maxThruput) <= minSrchsRateForLargeThruput) {
                                //不满足流量要求的尺寸过滤掉
//                                curUrlStat.getSize().remove(key);
                                iterator.remove();
                            }
                        }
                    }
                    //end... 
				}
				
				site = UrlParser.parseUrl(curUrlStat.getUrl());
				if(UrlParser.isIp(site)){
					curDomain = site;
					secDomain = null;
				}else{
					curDomain = UrlParser.fetchMainDomain(site);
					secDomain = UrlParser.fetchSecondDomain(site);
				}
				
				//判断当前的主域是否和前一个一致
				if(preDomain == null){
					preDomain = curDomain;
				}else if(!preDomain.equals(curDomain)){
					//当前为新的主域,保存上一个主域
					outputResult(urlStatList, siteTotalStatMap, urlSignWriters, urlStatWriters, siteStatWriter, tableCnt);
					urlStatList.clear();
					siteTotalStatMap.clear();
					unionSiteUrlStat.clear();
					whiteSiteUrlStat.clear();
					preDomain = curDomain;
				}
				
				//对当前主域内的内容，判断是否需要加入url库
				siteId = validSiteIdMap.get(curDomain);
				if(siteId == null){
					siteId = validSiteIdMap.get(secDomain);
					if(siteId == null){
						//不在whitelist中
						inValidDomain = false;
					}else{
						inValidDomain = true;
					}
				}else{
					inValidDomain = true;
				}
				whiteUrl.setUrl(curUrlStat.getUrl());
				//index of the search key, if it is contained in the list; 
				//otherwise, (-(insertion point) - 1). 
				//The insertion point is defined as the point at which the key would be inserted into the list: 
				//the index of the first element greater than the key, or list.size(), 
				int position = Collections.binarySearch(whiteUrlList, whiteUrl, new Comparator<WhiteUrl>() {

					public int compare(WhiteUrl o1, WhiteUrl o2) {
						return DomainComparator.urlCompare(o1.getUrl(), o2.getUrl());
					}
				});
				if(position>=0){
					inWhiteUrl = true;
					preWhite = position;
				}else{
					preWhite = -position - 2;//比当前url小的最大whiteUrl
					if(preWhite<0 ){
						inWhiteUrl = false;
					}else{
						if(DomainComparator.urlContain(whiteUrlList.get(preWhite).getUrl(), curUrlStat.getUrl())){
							inWhiteUrl = true;
						}else{
							inWhiteUrl = false;
						}
					}
				}
				if(inValidDomain && (!inWhiteUrl || whiteUrlList.get(preWhite).isWhite())){
					//validDomain
					curUrlStat.setSiteid(siteId);
					addUrlDb(curUrlStat, urlStatList, unionSiteUrlStat, siteTotalStatMap, validUrlTop);
				}else if(!inValidDomain && inWhiteUrl && whiteUrlList.get(preWhite).isWhite()){
					//whiteDomain
					curUrlStat.setSiteid(whiteUrlList.get(preWhite).getSiteid());
					addUrlDb(curUrlStat, urlStatList, whiteSiteUrlStat, siteTotalStatMap, whiteUrlTop);
				}
				
				//把下一个Urlstat赋值给当前
				curUrlStat = nextUrlStat;
			}
			//最后一个主域进行
			outputResult(urlStatList, siteTotalStatMap, urlSignWriters, urlStatWriters, siteStatWriter, tableCnt);
		} catch (IOException e) {
			LOG.fatal(e.getMessage(), e);
			throw e;
		} finally {
			for(int index=0; index<tableCnt; index++){
				if(urlSignWriters[index] != null){
					urlSignWriters[index].close();
				}
				if(urlStatWriters[index] != null){
					urlStatWriters[index].close();
				}
			}
			if(siteStatWriter != null){
				siteStatWriter.close();
			}
			
			reader.close();
		}
		LOG.info(urlFileDao.getStatCounter());

	}

	/**
	 * 
	 * addUrlDb: 一条url加入url库中，如果加入，则返回true，否则返回false
	 *
	 * @param urlStat
	 * @param urlStatList
	 * @param siteUrlStat	对应的白名单原因加入，还是union站点全库加入
	 * @param siteTotalStatMap
	 * @param topCnt	同一个分组加入的个数
	 * @return      
	 * @since 1.0.51
	 */
	private boolean addUrlDb(final UrlStat urlStat,
			final List<UrlStat> urlStatList,
			final Map<SiteSize, List<Integer>> siteUrlStat,
			final Map<Integer, SiteTotalStat> siteTotalStatMap, final int topCnt) {
		SiteSize siteSize = null;
		int result = -1;
		for (Integer size : urlStat.getSize().keySet()) {
			siteSize = new SiteSize();
			siteSize.setSiteid(urlStat.getSiteid());
			siteSize.setSize(size);
			result = addUrlSize(urlStat, result, urlStatList, siteUrlStat, siteTotalStatMap, topCnt, siteSize);
		}
		if ((urlStat.getSupporttype() & SiteConstant.WL_TEXT_FLAG) > 0) {
			//文字，需要单独作为sizeid=0的分组
			siteSize = new SiteSize();
			siteSize.setSiteid(urlStat.getSiteid());
			siteSize.setSize(0);
			result = addUrlSize(urlStat, result, urlStatList, siteUrlStat, siteTotalStatMap, topCnt, siteSize);
		}
		return result >= 0;
	}
	
	
	/**
	 * 
	 * addUrlSize: 判断一个url的一个尺寸是否需要加入url库（分组的前100个）中，如果需要，则返回加入的在当前主域中的序号
	 *
	 * @param urlStat
	 * @param index
	 * @param urlStatList
	 * @param siteUrlStat
	 * @param siteTotalStatMap
	 * @param topCnt
	 * @param siteSize
	 * @return      
	 * @since 1.0.51
	 */
	private int addUrlSize(final UrlStat urlStat, final int index,
			final List<UrlStat> urlStatList,
			final Map<SiteSize, List<Integer>> siteUrlStat,
			final Map<Integer, SiteTotalStat> siteTotalStatMap, final int topCnt, final SiteSize siteSize) {
		int result = index; 
		List<Integer> list = siteUrlStat.get(siteSize);
		if (list == null || list.size() < topCnt) {
			if (result < 0) {
				result = urlStatList.size();
				urlStatList.add(urlStat);
			}
			if(list == null){
				list = new ArrayList<Integer>(topCnt);
				siteUrlStat.put(siteSize, list);
				SiteTotalStat siteTotalStat = siteTotalStatMap.get(urlStat
						.getSiteid());
				if (siteTotalStat == null) {
					siteTotalStat = new SiteTotalStat();
					siteTotalStatMap
							.put(urlStat.getSiteid(), siteTotalStat);
				}
				
				//
				if(siteSize.getSize() != 0){
					siteTotalStat.getSize().add(siteSize.getSize());
				}
				siteTotalStat.setDisplayType(siteTotalStat.getDisplayType()|urlStat.getDisplaytype());
				siteTotalStat.setSupporttype(siteTotalStat.getSupporttype()|urlStat.getSupporttype());
			}
			list.add(result);
			
		}
		return result;
	}

	/**
	 * 
	 * outputResult: 把同一个主域下的记录存入文件中
	 *
	 * @param urlStatList
	 * @param siteTotalStatMap
	 * @param urlSignWriters
	 * @param urlStatWriters
	 * @param siteStatWriter
	 * @param tableCnt
	 * @throws IOException      
	 * @since 1.0.51
	 */
	private void outputResult(final List<UrlStat> urlStatList,
			final Map<Integer, SiteTotalStat> siteTotalStatMap,
			final BufferedWriter[] urlSignWriters,
			final BufferedWriter[] urlStatWriters,
			final BufferedWriter siteStatWriter, final int tableCnt)
			throws IOException {
		BufferedWriter curUrlSignWriter = null;
		BufferedWriter curUrlStatWriter = null;
		int partition = 0;
		
		if (CollectionUtils.isEmpty(urlStatList)) {
		    return;
		}

		for (UrlStat stat : urlStatList) {
		    
			partition = getPartition(stat.getSiteid(), tableCnt);
			curUrlSignWriter = urlSignWriters[partition];
			curUrlStatWriter = urlStatWriters[partition];
			// url签名
			String md5 = StringUtil.getMd5(stat.getUrl());
			BigInteger sign1 = new BigInteger(md5.substring(0, 16), 16); // 前16个字符，高64位
			BigInteger sign2 = new BigInteger(md5.substring(16), 16); // 低64位
			curUrlSignWriter.write(sign1.toString());
			curUrlSignWriter.write('\t');
			curUrlSignWriter.write(sign2.toString());
			curUrlSignWriter.write('\t');
			curUrlSignWriter.write(stat.getUrl());
			curUrlSignWriter.write('\n');

			curUrlStatWriter.write(String.valueOf(stat.getSiteid()));
			curUrlStatWriter.write('\t');
			curUrlStatWriter.write(sign1.toString());
			curUrlStatWriter.write('\t');
			curUrlStatWriter.write(sign2.toString());
			curUrlStatWriter.write('\t');
			curUrlStatWriter.write(String.valueOf(stat.getDisplaytype()));
			curUrlStatWriter.write('\t');
			curUrlStatWriter.write(String.valueOf(stat.getSupporttype()));
			curUrlStatWriter.write('\t');
			curUrlStatWriter.write(String.valueOf(stat.getSrchs()));
			curUrlStatWriter.write('\t');
			writeSize(curUrlStatWriter, stat.getSize().keySet());
			curUrlStatWriter.write('\n');
		}

		for (Entry<Integer, SiteTotalStat> entry : siteTotalStatMap.entrySet()) {
			siteStatWriter.write(String.valueOf(entry.getKey()));
			siteStatWriter.write('\t');
			siteStatWriter.write(String.valueOf(entry.getValue()
					.getDisplayType()));
			siteStatWriter.write('\t');
			siteStatWriter.write(String.valueOf(entry.getValue()
					.getSupporttype()));
			siteStatWriter.write('\t');
			writeSize(siteStatWriter, entry.getValue().getSize());
			siteStatWriter.write('\n');
		}
		
	}

	/**
	 * 
	 * getPartition: 获取分区的id
	 *
	 * @param keyId 判断分区的key
	 * @param tableCnt
	 * @return      
	 * @since 1.0.51
	 */
	private int getPartition(final int keyId, final int tableCnt) {
		return keyId % tableCnt;
	}
	
	/**
	 * 
	 * writeSize: 尺寸字段写入文件
	 *
	 * @param sizeWriter
	 * @param sizes
	 * @throws IOException      
	 * @since 1.0.51
	 */
	private void writeSize(final BufferedWriter sizeWriter,
			Collection<Integer> sizes) throws IOException {
		if (sizeWriter == null) {
			return;
		}
		StringBuilder size = new StringBuilder();
		if (sizes != null && !sizes.isEmpty()) {
			for (Integer s : sizes) {
				size.append(s).append(SiteConstant.SIZETHRUPUTSPLITER);
			}
			size.delete(size.length()
					- SiteConstant.SIZETHRUPUTSPLITER.length(), size.length());
		}
		sizeWriter.write(size.toString());
	}

	/**
	 * urlFileDao
	 * 
	 * @return the urlFileDao
	 */

	public UrlStatFileDao getUrlFileDao() {
		return urlFileDao;
	}

	/**
	 * urlFileDao
	 * 
	 * @param urlFileDao
	 *            the urlFileDao to set
	 */

	public void setUrlFileDao(UrlStatFileDao urlFileDao) {
		this.urlFileDao = urlFileDao;
	}

	/**
	 * bdSiteDao
	 * 
	 * @return the bdSiteDao
	 */

	public BDSiteStatDao getBdSiteDao() {
		return bdSiteDao;
	}

	/**
	 * bdSiteDao
	 * 
	 * @param bdSiteDao
	 *            the bdSiteDao to set
	 */

	public void setBdSiteDao(BDSiteStatDao bdSiteDao) {
		this.bdSiteDao = bdSiteDao;
	}

	/**
	 * whiteUrlTop
	 * 
	 * @return the whiteUrlTop
	 */

	public int getWhiteUrlTop() {
		return whiteUrlTop;
	}

	/**
	 * whiteUrlTop
	 * 
	 * @param whiteUrlTop
	 *            the whiteUrlTop to set
	 */

	public void setWhiteUrlTop(int whiteUrlTop) {
		this.whiteUrlTop = whiteUrlTop;
	}

	/**
	 * validUrlTop
	 * 
	 * @return the validUrlTop
	 */

	public int getValidUrlTop() {
		return validUrlTop;
	}

	/**
	 * validUrlTop
	 * 
	 * @param validUrlTop
	 *            the validUrlTop to set
	 */

	public void setValidUrlTop(int validUrlTop) {
		this.validUrlTop = validUrlTop;
	}
	
	public int getMinCountOfSize() {
		return minCountOfSize;
	}

	public void setMinCountOfSize(int minCountOfSize) {
		this.minCountOfSize = minCountOfSize;
	}

}
