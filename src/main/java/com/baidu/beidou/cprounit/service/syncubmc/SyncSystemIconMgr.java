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

import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.SyncUbmcConstant;
import com.baidu.beidou.cprounit.icon.bo.SystemIcon;
import com.baidu.beidou.cprounit.icon.dao.SystemIconDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.SysIconSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.util.MD5;

/**
 * ClassName: SyncSystemiconsMgr
 * Function: 同步beidouext.systemicons中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncSystemIconMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(SyncSystemIconMgr.class);
	
	private LogPrinter logPrinter = null;
	private int maxMaterNumSelect = 0;
	private SysIconSet sysIconSet = null;
	
	private SystemIconDao systemIconDao;
	
	public void syncMater(int maxMaterNumSelect, PrintWriter errorWriter, 
			PrintWriter logWriter, String dbFileName, int maxThread) {
		log.info("begin to sync sysicon");
		
		logPrinter = new LogPrinter(errorWriter, logWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		
		sysIconSet = new SysIconSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to sync sysicon");
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
		log.info("sync sysicon use " + (time2 - time1) 
				+ " ms, unitTotalNum=" + sysIconSet.getTotal());
		
		sysIconSet.closeFile();
		log.info("end to sync sysicon");
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<SystemIcon> systemIconList = sysIconSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(systemIconList)) {
							logPrinter.log("[INFO][systemIconList.size=0], [syncNum=0]");
							break;
						}
						
						List<SystemIcon> systemIcons = new LinkedList<SystemIcon>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (SystemIcon systemIcon : systemIconList) {
							Integer width = systemIcon.getWidth();
							Integer height = systemIcon.getHight();
							String fileSrc = systemIcon.getFileSrc();
							if (StringUtils.isEmpty(fileSrc)) {
								log.error("[error=" + SyncUbmcConstant.ERROR_SYSTEM_ICON
										+ "]fileSrc is null or empty, [system_icon_id=" + systemIcon.getId()
										+ "], [fileSrc=" + systemIcon.getFileSrc() + "]");
								logPrinter.error(SyncUbmcConstant.ERROR_SYSTEM_ICON 
										+ "\t" + systemIcon.getId() + "\t" + 0);
								continue;
							}
							
							String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
							byte[] data = this.getImage(url);
							if (null == data || data.length == 0){
								log.error("[error=" + SyncUbmcConstant.ERROR_SYSTEM_ICON
										+ "]data from drmc is empty, [system_icon_id=" + systemIcon.getId()
										+ "], [fileSrc=" + systemIcon.getFileSrc() + "]");
								logPrinter.error(SyncUbmcConstant.ERROR_SYSTEM_ICON 
										+ "\t" + systemIcon.getId() + "\t" + 0);
								data = new byte[0];
							}
							String fileSrcMd5 = MD5.getMd5(data);
							
							RequestIconMaterial request = new RequestIconMaterial(null, null, width, height, data, fileSrcMd5);
							requests.add(request);
							systemIcons.add(systemIcon);
						}
						
						logPrinter.log("[INFO][systemIcons.size=" + systemIcons.size()
								+ "], [requests.size=" + requests.size() + "]");
						
						int syncNum = 0;
						if (CollectionUtils.isNotEmpty(requests)) {
							List<ResponseBaseMaterial> resultInsert = ubmcService.insert(requests);
							
							if (CollectionUtils.isEmpty(resultInsert) || resultInsert.size() != systemIcons.size()) {
								log.error("[error=" + SyncUbmcConstant.ERROR_SYSTEM_ICON
										+ "], resultInsert size[" + resultInsert.size()
										+ "] != request size[" + systemIcons.size() + "]");
							} else {
								for (int index = 0; index < systemIcons.size(); index++) {
									SystemIcon systemIcon = systemIcons.get(index);
									ResponseBaseMaterial response = resultInsert.get(index);
									
									if (response != null && response.getMcId() > 0L) {
										systemIconDao.updateSystemIcon(systemIcon.getId(), response.getMcId());
										syncNum++;
									} else {
										log.error("[error=" + SyncUbmcConstant.ERROR_SYSTEM_ICON
												+ "], ubmc insert sysicon failed[system_icon_id=" 
												+ systemIcon.getId() + "]");
										logPrinter.error(SyncUbmcConstant.ERROR_SYSTEM_ICON 
												+ "\t" + systemIcon.getId() + "\t" + 0);
									}
								}
							}
						}
						
						logPrinter.log("[INFO][systemIconList.size=" + systemIconList.size()
								+ "], [syncNum=" + syncNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("sync sysicon use " + (time2 - time1) + " ms, unitNum=" + maxMaterNumSelect);
					} catch (Exception e) {
						log.error("sync sysicon failed", e);
					}
				} 
			}
			
			private byte[] getImage(String url) {
				if (!StringUtils.isEmpty(url)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = this.getFileByUrl(url);
						if (null == data || data.length == 0){
							log.error("[error=" + SyncUbmcConstant.ERROR_SYSTEM_ICON
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

	public SystemIconDao getSystemIconDao() {
		return systemIconDao;
	}

	public void setSystemIconDao(SystemIconDao systemIconDao) {
		this.systemIconDao = systemIconDao;
	}
}
