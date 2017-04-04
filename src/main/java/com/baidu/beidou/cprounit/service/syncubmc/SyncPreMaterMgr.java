package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.PreMater;
import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.constant.SyncUbmcConstant;
import com.baidu.beidou.cprounit.dao.PreMaterDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.PreUnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.util.MD5;

/**
 * ClassName: SyncPreMaterMgr
 * Function: 同步beidou.precprounitmater[0-7]中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncPreMaterMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(SyncPreMaterMgr.class);
	
	private LogPrinter logPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private PreUnitSet preUnitSet = null;
	
	private PreMaterDao preMaterDao;
	
	public void syncMater(int maxMaterNumSelect, PrintWriter errorWriter, PrintWriter logWriter, 
			String dbFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("begin to sync preunit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		
		logPrinter = new LogPrinter(errorWriter, logWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		preUnitSet = new PreUnitSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to sync preunit");
		ExecutorService pool = Executors.newFixedThreadPool(maxThread);
		long time1 = System.currentTimeMillis();
		for (int times = 0; times < maxThread; times++) {
			Runnable worker = this.createTask();
			pool.execute(worker);
		}
		
		pool.shutdown();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("An error has occurred while the pool is executing...");
		} catch (Exception e) {
			log.error("An error has occurred while the pool is executing...");
		}
		long time2 = System.currentTimeMillis();
		log.info("sync preunit in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + preUnitSet.getTotal());
		
		preUnitSet.closeFile();
		log.info("end to sync preunit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<PreMater> preUnitList = preUnitSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(preUnitList)) {
							logPrinter.log("[INFO][dbIndex=" + dbSlice + ", index=" 
									+ dbIndex + "], [preUnitList.size=0], [syncNum=0]");
							break;
						}
						
						List<PreMater> preUnits = new LinkedList<PreMater>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (PreMater preUnit : preUnitList) {
							RequestBaseMaterial request = null;
							
							Long unitId = preUnit.getId();
							Integer wuliaoType = preUnit.getWuliaoType();
							String title = preUnit.getTitle();
							String showUrl = preUnit.getShowUrl();
							String targetUrl = preUnit.getTargetUrl();
							String wirelessShowUrl = preUnit.getWirelessShowUrl();
							String wirelessTargetUrl = preUnit.getWirelessTargetUrl();
							
							if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
								String description1 = preUnit.getDescription1();
								String description2 = preUnit.getDescription2();
								
								request = new RequestTextUnit(null, null, title, description1, description2, 
										showUrl, targetUrl, wirelessShowUrl, wirelessTargetUrl);
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE
									|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
								Integer width = preUnit.getWidth();
								Integer height = preUnit.getHeight();
								
								String fileSrc = preUnit.getFileSrc();
								if (StringUtils.isEmpty(fileSrc)) {
									log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
											+ "]fileSrc is null or empty, [preunit_id=" + unitId
											+ "], [fileSrc=" + preUnit.getFileSrc()    
											+ "], [userId=" + preUnit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_PRE_MATER 
											+ "\t" + preUnit.getId()
											+ "\t" + preUnit.getUserId());
									continue;
								}
								
								String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
								byte[] data = this.getImage(url);
								if (null == data || data.length == 0){
									log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
											+ "]data from drmc is empty, [preunit_id=" + unitId
											+ "], [fileSrc=" + preUnit.getFileSrc()    
											+ "], [userId=" + preUnit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_PRE_MATER 
											+ "\t" + preUnit.getId()
											+ "\t" + preUnit.getUserId());
									data = new byte[0];
								}
								String fileSrcMd5 = MD5.getMd5(data);
								
								request = new RequestImageUnitWithData(null, null, wuliaoType, title, showUrl, 
										targetUrl, wirelessShowUrl, wirelessTargetUrl, width, height, data, fileSrcMd5, "", "", "", null);
								
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
								String description1 = preUnit.getDescription1();
								String description2 = preUnit.getDescription2();
								Integer width = preUnit.getWidth();
								Integer height = preUnit.getHeight();
								
								String fileSrc = preUnit.getFileSrc();
								if (StringUtils.isEmpty(fileSrc)) {
									log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
											+ "]fileSrc is null or empty, [preunit_id=" + unitId
											+ "], [fileSrc=" + preUnit.getFileSrc()     
											+ "], [userId=" + preUnit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_PRE_MATER 
											+ "\t" + preUnit.getId()
											+ "\t" + preUnit.getUserId());
									continue;
								}
								
								String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
								byte[] data = this.getImage(url);
								if (null == data || data.length == 0){
									log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
											+ "]data from drmc is empty, [preunit_id=" + unitId
											+ "], [fileSrc=" + preUnit.getFileSrc()    
											+ "], [userId=" + preUnit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_PRE_MATER 
											+ "\t" + preUnit.getId()
											+ "\t" + preUnit.getUserId());
									data = new byte[0];
								}
								String fileSrcMd5 = MD5.getMd5(data);
								
								request = new RequestIconUnitWithData(null, null, title, description1, description2, showUrl, 
										targetUrl, wirelessShowUrl, wirelessTargetUrl, width, height, data, fileSrcMd5);
							}
							
							requests.add(request);
							preUnits.add(preUnit);
						}
						
						logPrinter.log("[INFO][dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ "], [preUnits.size=" + preUnits.size()
								+ "], [requests.size=" + requests.size() + "]");
						
						int syncNum = 0;
						if (CollectionUtils.isNotEmpty(requests)) {
							List<ResponseBaseMaterial> resultInsert = ubmcService.insert(requests);
							
							if (CollectionUtils.isEmpty(resultInsert) || resultInsert.size() != preUnits.size()) {
								log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
										+ "], resultInsert size[" + resultInsert.size()
										+ "] != request size[" + preUnits.size() + "]");
							} else {
								for (int index = 0; index < preUnits.size(); index++) {
									PreMater preUnit = preUnits.get(index);
									ResponseBaseMaterial response = resultInsert.get(index);
									
									if (response != null && response.getMcId() > 0L) {
										preMaterDao.updatePreMater(dbIndex, preUnit.getId(), response.getMcId(), 
												response.getVersionId(), preUnit.getUserId());
										syncNum++;
									} else {
										log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
												+ "], ubmc insert preunit failed[id=" + preUnit.getId()
												+ ", userId=" + preUnit.getUserId() + "]");
										logPrinter.error(SyncUbmcConstant.ERROR_PRE_MATER 
												+ "\t" + preUnit.getId()
												+ "\t" + preUnit.getUserId());
									}
								}
							}
						}
						
						logPrinter.log("[INFO][dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ "], [preUnitList.size=" + preUnitList.size()
								+ "], [syncNum=" + syncNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("sync preunit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + maxMaterNumSelect);
					} catch (Exception e) {
						log.error("sync preunit failed in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
			
			private byte[] getImage(String url) {
				if (!StringUtils.isEmpty(url)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = this.getFileByUrl(url);
						if (null == data || data.length == 0){
							log.error("[error=" + SyncUbmcConstant.ERROR_PRE_MATER
									+ "]data from drmc is empty, url=" + url);
						} else {
							return data;
						}
					}
				}
				return null;
			}
			
			/**
			 * getFileByUrl: 根据url获取远程文件
			 * @version cpweb-567
			 * @author genglei01
			 * @date May 30, 2013
			 */
			private byte[] getFileByUrl(String url) {
				// http client
				HttpClient httpClient = new HttpClient();
				httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(2000);       // 连接建立时间
				httpClient.getHttpConnectionManager().getParams().setSoTimeout(5000);			   // 数据读取时间
				httpClient.getParams().setConnectionManagerTimeout(2000);                           // 搜索连接时间
				
				// get method
				GetMethod getMethod = new GetMethod(url);
				getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
				
				try {
					int statusCode = httpClient.executeMethod(getMethod);
					if (statusCode == HttpStatus.SC_OK) {
						return getMethod.getResponseBody();
					} else {
						throw new IOException("wrong status code: " + statusCode + " for url:" + url);
					}
				} catch (HttpException e) {
					log.info(e.getMessage(), e);
				} catch (IOException e) {
					log.info(e.getMessage(), e);
				} finally {
					getMethod.releaseConnection();
				}
				
				return null;
			}
		};
	}

	public PreMaterDao getPreMaterDao() {
		return preMaterDao;
	}

	public void setPreMaterDao(PreMaterDao preMaterDao) {
		this.preMaterDao = preMaterDao;
	}
}
