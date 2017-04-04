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

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.constant.SyncUbmcConstant;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.util.MD5;

/**
 * ClassName: SyncUnitMgr
 * Function: 同步beidou.cprounitmater[0-7]中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncUnitMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(SyncUnitMgr.class);
	
	private LogPrinter logPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitSet unitMaterSet = null;
	
	private UnitDao unitDao;
	
	public void syncMater(int maxMaterNumSelect, PrintWriter errorWriter, PrintWriter logWriter, 
			String dbFileName, int maxThread, int dbIndex, int dbSlice) {
		
		log.info("begin to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logPrinter = new LogPrinter(errorWriter, logWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitMaterSet = new UnitSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to sync unit");
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
		log.info("sync unit in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitMaterSet.getTotal());
		
		unitMaterSet.closeFile();
		log.info("end to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UnitMaterView> unitList = unitMaterSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							logPrinter.log("[INFO][dbIndex=" + dbSlice + ", index=" 
									+ dbIndex + "], [unitList.size=0], [syncNum=0]");
							break;
						}
						
						List<UnitMaterView> unitInserts = new LinkedList<UnitMaterView>();
						List<RequestBaseMaterial> requestInserts = new LinkedList<RequestBaseMaterial>();
						List<UnitMaterView> unitUpdates = new LinkedList<UnitMaterView>();
						List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
						
						for (UnitMaterView unit : unitList) {
							RequestBaseMaterial request = null;
							
							Long unitId = unit.getId();
							Long mcId = unit.getMcId();
							Integer mcVersionId = unit.getMcVersionId();
							Integer wuliaoType = unit.getWuliaoType();
							String title = unit.getTitle();
							String showUrl = unit.getShowUrl();
							String targetUrl = unit.getTargetUrl();
							String wirelessShowUrl = unit.getWirelessShowUrl();
							String wirelessTargetUrl = unit.getWirelessTargetUrl();
							
							if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
								String description1 = unit.getDescription1();
								String description2 = unit.getDescription2();
								
								request = new RequestTextUnit(mcId, mcVersionId, title, description1, description2, 
										showUrl, targetUrl, wirelessShowUrl, wirelessTargetUrl);
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE
									|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
								Integer width = unit.getWidth();
								Integer height = unit.getHeight();
								
								String fileSrc = unit.getFileSrc();
								if (StringUtils.isEmpty(fileSrc)) {
									log.error("[error=" + SyncUbmcConstant.ERROR_MATER
											+ "]fileSrc is null or empty, [unit_id=" + unitId
											+ "], [fileSrc=" + unit.getFileSrc()    
											+ "], [userId=" + unit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_MATER 
											+ "\t" + unit.getId()
											+ "\t" + unit.getUserId());
									continue;
								}
								
								String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
								byte[] data = this.getImage(url);
								if (null == data || data.length == 0){
									log.error("[error=" + SyncUbmcConstant.ERROR_MATER
											+ "]data from drmc is empty, [unit_id=" + unitId
											+ "], [fileSrc=" + unit.getFileSrc()    
											+ "], [userId=" + unit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_MATER 
											+ "\t" + unit.getId()
											+ "\t" + unit.getUserId());
									data = new byte[0];
								}
								String fileSrcMd5 = MD5.getMd5(data);
								unit.setFileSrcMd5(fileSrcMd5);
								
								request = new RequestImageUnitWithData(mcId, mcVersionId, wuliaoType, title, showUrl, 
										targetUrl, wirelessShowUrl, wirelessTargetUrl, width, height, data, fileSrcMd5, "", "", "", null);
								
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
								String description1 = unit.getDescription1();
								String description2 = unit.getDescription2();
								Integer width = unit.getWidth();
								Integer height = unit.getHeight();
								
								String fileSrc = unit.getFileSrc();
								if (StringUtils.isEmpty(fileSrc)) {
									log.error("[error=" + SyncUbmcConstant.ERROR_MATER
											+ "]fileSrc is null or empty, [unit_id=" + unitId
											+ "], [fileSrc=" + unit.getFileSrc()     
											+ "], [userId=" + unit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_MATER 
											+ "\t" + unit.getId()
											+ "\t" + unit.getUserId());
									continue;
								}
								
								String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
								byte[] data = this.getImage(url);
								if (null == data || data.length == 0){
									log.error("[error=" + SyncUbmcConstant.ERROR_MATER
											+ "]data from drmc is empty, [unit_id=" + unitId
											+ "], [fileSrc=" + unit.getFileSrc()    
											+ "], [userId=" + unit.getUserId() + "]");
									logPrinter.error(SyncUbmcConstant.ERROR_MATER 
											+ "\t" + unit.getId()
											+ "\t" + unit.getUserId());
									data = new byte[0];
								}
								String fileSrcMd5 = MD5.getMd5(data);
								unit.setFileSrcMd5(fileSrcMd5);
								
								request = new RequestIconUnitWithData(mcId, mcVersionId, title, description1, description2, showUrl, 
										targetUrl, wirelessShowUrl, wirelessTargetUrl, width, height, data, fileSrcMd5);
							}
							
							if (mcId == null || mcId == 0) {
								request.setMcId(null);
								requestInserts.add(request);
								unitInserts.add(unit);
							} else {
								requestUpdates.add(request);
								unitUpdates.add(unit);
							}
						}
						logPrinter.log("[INFO][dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ "], [unitInserts.size=" + unitInserts.size()
								+ "], [unitUpdates.size=" + unitUpdates.size()
								+ "], [requestInserts.size=" + requestInserts.size()
								+ "], [requestUpdates.size=" + requestUpdates.size() + "]");
						
						int syncNum = 0;
						if (CollectionUtils.isNotEmpty(requestInserts)) {
							List<ResponseBaseMaterial> resultInsert = ubmcService.insert(requestInserts);
							
							if (CollectionUtils.isEmpty(resultInsert) || resultInsert.size() != unitInserts.size()) {
								log.error("[error=" + SyncUbmcConstant.ERROR_MATER
										+ "], resultInsert size[" + resultInsert.size()
										+ "] != request size[" + unitInserts.size() + "]");
							} else {
								for (int index = 0; index < unitInserts.size(); index++) {
									UnitMaterView unit = unitInserts.get(index);
									ResponseBaseMaterial response = resultInsert.get(index);
									
									if (response != null && response.getMcId() > 0L) {
										unitDao.updateUnit(dbIndex, unit.getId(), response.getMcId(), 
												response.getVersionId(), unit.getUserId());
										unitDao.updateUnitSyncFlag(dbIndex, unit.getId(), unit.getFileSrcMd5(), unit.getChaTime(), unit.getUserId());
										syncNum++;
									} else {
										log.error("[error=" + SyncUbmcConstant.ERROR_MATER
												+ "], ubmc insert unit failed[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() + "]");
										logPrinter.error(SyncUbmcConstant.ERROR_MATER 
												+ "\t" + unit.getId()
												+ "\t" + unit.getUserId());
									}
								}
							}
						}
						
						if (CollectionUtils.isNotEmpty(requestUpdates)) {
							List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
							
							if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.size() != unitUpdates.size()) {
								log.error("[error=" + SyncUbmcConstant.ERROR_MATER
										+ "], resultUpdate size[" + resultUpdate.size()
										+ "] != request size[" + unitUpdates.size() + "]");
							} else {
								for (int index = 0; index < unitUpdates.size(); index++) {
									ResponseBaseMaterial response = resultUpdate.get(index);
									UnitMaterView unit = unitUpdates.get(index);
									if (response != null && response.getMcId().equals(unit.getMcId())) {
										unitDao.updateUnitSyncFlag(dbIndex, unit.getId(), unit.getFileSrcMd5(), unit.getChaTime(), unit.getUserId());
										syncNum++;
									} else {
										log.error("[error=" + SyncUbmcConstant.ERROR_MATER
												+ "], ubmc update unit failed[id=" + unit.getId()
												+ ", userId=" + unit.getUserId()
												+ ", mcId=" + unit.getMcId() + "]");
										logPrinter.error(SyncUbmcConstant.ERROR_MATER 
												+ "\t" + unit.getId()
												+ "\t" + unit.getUserId());
									}
								}
							}
						}
						
						logPrinter.log("[INFO][dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ "], [unitList.size=" + unitList.size()
								+ "], [syncNum=" + syncNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + maxMaterNumSelect);
					} catch (Exception e) {
						log.error("sync unit failed in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
			
			private byte[] getImage(String url) {
				if (!StringUtils.isEmpty(url)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = this.getFileByUrl(url);
						if (null == data || data.length == 0){
							log.error("[error=" + SyncUbmcConstant.ERROR_MATER
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

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
