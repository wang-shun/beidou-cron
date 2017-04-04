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

import com.baidu.beidou.auditmanager.dao.AuditHistoryDao;
import com.baidu.beidou.auditmanager.vo.AuditHistoryView;
import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.constant.SyncUbmcConstant;
import com.baidu.beidou.cprounit.service.syncubmc.vo.AuditHistoryUnitSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogPrinter;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.util.MD5;

/**
 * ClassName: SyncAuditHistoryMgr
 * Function: 同步beidou.auditcprounithistory中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncAuditHistoryMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(SyncAuditHistoryMgr.class);
	
	private LogPrinter logPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbSlice = 0;
	private AuditHistoryUnitSet historyUnitSet = null;
	
	private AuditHistoryDao auditHistoryDao = null;
	
	public void syncMater(int maxMaterNumSelect, PrintWriter errorWriter, PrintWriter logWriter, 
			String dbFileName, int maxThread, int dbSlice) {
		log.info("begin to sync history in dbSlice=" + dbSlice);
		
		logPrinter = new LogPrinter(errorWriter, logWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbSlice = dbSlice;
		
		historyUnitSet = new AuditHistoryUnitSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to sync history");
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
		log.info("sync history in dbSlice=" + dbSlice + ", use " + (time2 - time1) 
				+ " ms, unitTotalNum=" + historyUnitSet.getTotal());
		
		historyUnitSet.closeFile();
		log.info("end to sync history in  dbSlice=" + dbSlice);
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<AuditHistoryView> auditHistoryList = historyUnitSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(auditHistoryList)) {
							logPrinter.log("[INFO][dbSlice=" + dbSlice + "], [auditHistoryList.size=0], [syncNum=0]");
							break;
						}
						
						List<AuditHistoryView> auditHistorys = new LinkedList<AuditHistoryView>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (AuditHistoryView history : auditHistoryList) {
							RequestBaseMaterial request = null;
							
							Integer id = history.getId();
							Integer wuliaoType = history.getWuliaoType();
							String title = history.getTitle();
							String showUrl = history.getShowUrl();
							String targetUrl = history.getTargetUrl();
							String wirelessShowUrl = history.getWirelessShowUrl();
							String wirelessTargetUrl = history.getWirelessTargetUrl();
							
							if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
								String description1 = history.getDescription1();
								String description2 = history.getDescription2();
								
								request = new RequestTextUnit(null, null, title, description1, description2, 
										showUrl, targetUrl, wirelessShowUrl, wirelessTargetUrl);
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE
									|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
								Integer width = history.getWidth();
								Integer height = history.getHeight();
								
								String fileSrc = history.getFileSrc();
								if (StringUtils.isEmpty(fileSrc)) {
									log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
											+ "]fileSrc is null or empty, [history_id=" + id
											+ "], [fileSrc=" + history.getFileSrc()    
											+ "], [userId=" + history.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_AUDIT_HISTORY 
											+ "\t" + history.getId()
											+ "\t" + history.getUserId());
									continue;
								}
								
								String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
								byte[] data = this.getImage(url);
								if (null == data || data.length == 0){
									log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
											+ "]data from drmc is empty, [history_id=" + id
											+ "], [fileSrc=" + history.getFileSrc()    
											+ "], [userId=" + history.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_AUDIT_HISTORY 
											+ "\t" + history.getId()
											+ "\t" + history.getUserId());
									data = new byte[0];
								}
								String fileSrcMd5 = MD5.getMd5(data);
								
								request = new RequestImageUnitWithData(null, null, wuliaoType, title, showUrl, 
										targetUrl, wirelessShowUrl, wirelessTargetUrl, width, height, data, fileSrcMd5, "", "", "", null);
								
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
								String description1 = history.getDescription1();
								String description2 = history.getDescription2();
								Integer width = history.getWidth();
								Integer height = history.getHeight();
								
								String fileSrc = history.getFileSrc();
								if (StringUtils.isEmpty(fileSrc)) {
									log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
											+ "]fileSrc is null or empty, [history_id=" + id
											+ "], [fileSrc=" + history.getFileSrc()     
											+ "], [userId=" + history.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_AUDIT_HISTORY 
											+ "\t" + history.getId()
											+ "\t" + history.getUserId());
									continue;
								}
								
								String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
								byte[] data = this.getImage(url);
								if (null == data || data.length == 0){
									log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
											+ "]data from drmc is empty, [history_id=" + id
											+ "], [fileSrc=" + history.getFileSrc()    
											+ "], [userId=" + history.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_AUDIT_HISTORY 
											+ "\t" + history.getId()
											+ "\t" + history.getUserId());
									data = new byte[0];
								}
								String fileSrcMd5 = MD5.getMd5(data);
								
								request = new RequestIconUnitWithData(null, null, title, description1, description2, showUrl, 
										targetUrl, wirelessShowUrl, wirelessTargetUrl, width, height, data, fileSrcMd5);
							}
							
							requests.add(request);
							auditHistorys.add(history);
						}
						
						logPrinter.log("[INFO][dbSlice=" + dbSlice
								+ "], [auditHistorys.size=" + auditHistorys.size()
								+ "], [requests.size=" + requests.size() + "]");
						
						int syncNum = 0;
						if (CollectionUtils.isNotEmpty(requests)) {
							List<ResponseBaseMaterial> resultInsert = ubmcService.insert(requests);
							
							if (CollectionUtils.isEmpty(resultInsert) || resultInsert.size() != auditHistorys.size()) {
								log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
										+ "], resultInsert size[" + resultInsert.size()
										+ "] != request size[" + auditHistorys.size() + "]");
							} else {
								for (int index = 0; index < auditHistorys.size(); index++) {
									AuditHistoryView history = auditHistorys.get(index);
									ResponseBaseMaterial response = resultInsert.get(index);
									
									if (response != null && response.getMcId() > 0L) {
										auditHistoryDao.updateAuditHistory(history.getId(), response.getMcId(), 
												response.getVersionId(), history.getUserId());
										syncNum++;
									} else {
										log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
												+ "], ubmc insert history failed[id=" + history.getId()
												+ ", userId=" + history.getUserId() + "]");
										logPrinter.error(SyncUbmcConstant.ERROR_AUDIT_HISTORY 
												+ "\t" + history.getId()
												+ "\t" + history.getUserId());
									}
								}
							}
						}
						
						logPrinter.log("[INFO][dbSlice=" + dbSlice
								+ "], [auditHistoryList.size=" + auditHistoryList.size()
								+ "], [syncNum=" + syncNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("sync history in dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + maxMaterNumSelect);
					} catch (Exception e) {
						log.error("sync history failed in dbSlice=" + dbSlice, e);
					}
				} 
			}
			
			private byte[] getImage(String url) {
				if (!StringUtils.isEmpty(url)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = this.getFileByUrl(url);
						if (null == data || data.length == 0){
							log.error("[error=" + SyncUbmcConstant.ERROR_AUDIT_HISTORY
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

	public AuditHistoryDao getAuditHistoryDao() {
		return auditHistoryDao;
	}

	public void setAuditHistoryDao(AuditHistoryDao auditHistoryDao) {
		this.auditHistoryDao = auditHistoryDao;
	}
}
