/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.unionsite.dao.BDSiteStatOnAddbDao;
import com.baidu.beidou.unionsite.dao.WhiteUrlFileDao;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.unionsite.service.DomainComparator;
import com.baidu.beidou.unionsite.service.WhiteUrlMgr;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.TestLogUtils;
import com.baidu.beidou.util.UrlParser;
import com.baidu.beidou.util.dao.SequenceIdDaoOnXdb;

/**
 * ClassName:WhiteUrlMgrImpl Function: TODO ADD FUNCTION
 * 
 * @author <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version 1.0.51
 * @since TODO
 * @Date 2010 2010-5-20 下午10:33:03
 * 
 * @see
 */

public class WhiteUrlMgrImpl implements WhiteUrlMgr {

	private static final Log LOG = LogFactory.getLog(WhiteUrlMgrImpl.class);
	private BDSiteStatDao bdSiteDao = null;
	private BDSiteStatOnAddbDao siteOnAddbDao = null;
	
	private WhiteUrlFileDao whiteUrlDao = null;
	private SequenceIdDaoOnXdb sequenceIdDaoOnXdb = null;
	private String charset = "gbk";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.beidou.unionsite.service.WhiteUrlMgr#getWhiteList(java.lang
	 * .String)
	 */
	public List<WhiteUrl> getWhiteList(String file) throws IOException {
		if (StringUtils.isEmpty(file)) {
			return new ArrayList<WhiteUrl>(0);
		}
		List<WhiteUrl> whiteUrlList = new ArrayList<WhiteUrl>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), charset));
		WhiteUrl record = null;
		try {
			do {
				try {
					record = whiteUrlDao.readRecord(reader);
					if (record == null) {
						break;
					}
					String domain = UrlParser.getMainDomain(record.getUrl());
					if (domain == null) {
						LOG.error("无法获取" + record.getUrl() + "的主域");
						continue;
					}
					whiteUrlList.add(record);
				} catch (ErrorFormatException e) {
					LOG.error(e.getMessage(), e);
				}
			} while (true);
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LogUtils.fatal(LOG, e.getMessage(), e);
			}
		}

		// 对白名单进行排序
		Collections.sort(whiteUrlList, new Comparator<WhiteUrl>() {

			public int compare(WhiteUrl o1, WhiteUrl o2) {
				return DomainComparator.urlCompare(o1.getUrl(), o2.getUrl());
			}
		});

		// 去除存在包含关系的主域
		for (int index = whiteUrlList.size() - 1; index > 0; index--) {
			if (DomainComparator.urlContain(whiteUrlList.get(index - 1)
					.getUrl(), whiteUrlList.get(index).getUrl())) {
				// 前一个包含后一个url, 则删除前一个url;
				whiteUrlList.remove(index - 1);
			}
		}

		// 获取所有的主域id
		Map<String, Integer> domainId = bdSiteDao.getAllMainDomainId();
		int dbSiteId = sequenceIdDaoOnXdb.getUnionSiteidTypeId().intValue();
		int nextSiteId = dbSiteId;
		Integer siteId = null;
		String domain = null;
		List<WhiteUrl> toInsertDomain = new ArrayList<WhiteUrl>();
		for (WhiteUrl whiteUrl : whiteUrlList) {
			domain = UrlParser.getMainDomain(whiteUrl.getUrl());
			siteId = domainId.get(domain);
			if (siteId == null) {
				siteId = nextSiteId;
				nextSiteId++;
				domainId.put(domain, siteId);
				// 待插入unionsite,占位siteid
				WhiteUrl insertDomain = new WhiteUrl();
				insertDomain.setSiteid(siteId);
				insertDomain.setUrl(domain);
				toInsertDomain.add(insertDomain);
			}
			whiteUrl.setSiteid(siteId);
		}

		if (nextSiteId > dbSiteId) {
			// 保存数据库
			// 用mysql的get_next_value()，在获取nextid的同时，已经将nextid加1了，此处不用在存储nextid了
			// siteOnAddbDao.restorNextid(nextSiteId);
			int batchSize = 1000;
			int toIndex = 0;
			int size = toInsertDomain.size();
			for (int index = 0; index < size; index += batchSize) {
				toIndex = index + batchSize;
				if (toIndex > size) {
					toIndex = size;
				}
				bdSiteDao.insertInvalidDomainSite(toInsertDomain.subList(index,
						toIndex));
			}
		}
		
		//测试日志
		if(TestLogUtils.isDebugEnabled()){
			for(WhiteUrl whiteUrl: whiteUrlList){
				TestLogUtils.testInfo(whiteUrl);
			}
		}
		return whiteUrlList;
	}

	public BDSiteStatDao getBdSiteDao() {
		return bdSiteDao;
	}

	public void setBdSiteDao(BDSiteStatDao bdSiteDao) {
		this.bdSiteDao = bdSiteDao;
	}

	public WhiteUrlFileDao getWhiteUrlDao() {
		return whiteUrlDao;
	}

	public void setWhiteUrlDao(WhiteUrlFileDao whiteUrlDao) {
		this.whiteUrlDao = whiteUrlDao;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setSiteOnAddbDao(BDSiteStatOnAddbDao siteOnAddbDao) {
		this.siteOnAddbDao = siteOnAddbDao;
	}

	public void setSequenceIdDaoOnXdb(SequenceIdDaoOnXdb sequenceIdDaoOnXdb) {
		this.sequenceIdDaoOnXdb = sequenceIdDaoOnXdb;
	}

}
