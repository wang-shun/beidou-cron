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
import com.baidu.beidou.cprounit.icon.bo.UserUploadIcon;
import com.baidu.beidou.cprounit.icon.dao.UserUploadIconDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UserIconSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.util.MD5;

/**
 * ClassName: SyncUserIconMgr
 * Function: 同步beidouext.useruploadicons
 *
 * @author genglei
 * @version cpweb-567
 * @date May 14, 2013
 */
public class SyncUserUploadIconMgr extends SyncBaseMgr {

	private static final Log log = LogFactory.getLog(SyncUserUploadIconMgr.class);
	
	private LogPrinter logPrinter = null;
	private int maxMaterNumSelect = 0;
	private UserIconSet userIconSet = null;
	
	private UserUploadIconDao userUploadIconDao = null;
	
	public void syncMater(int maxMaterNumSelect, PrintWriter errorWriter, 
			PrintWriter logWriter, String dbFileName, int maxThread) {
		log.info("begin to sync usericon");
		
		logPrinter = new LogPrinter(errorWriter, logWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		
		userIconSet = new UserIconSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to sync usericon");
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
		log.info("sync usericon use " + (time2 - time1) 
				+ " ms, unitTotalNum=" + userIconSet.getTotal());
		
		userIconSet.closeFile();
		log.info("end to sync usericon");
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UserUploadIcon> userIconList = userIconSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(userIconList)) {
							logPrinter.log("[INFO][userIconList.size=0], [syncNum=0]");
							break;
						}
						
						List<UserUploadIcon> userIcons = new LinkedList<UserUploadIcon>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (UserUploadIcon userIcon : userIconList) {
							Integer width = userIcon.getWidth();
							Integer height = userIcon.getHight();
							String fileSrc = userIcon.getFileSrc();
							if (StringUtils.isEmpty(fileSrc)) {
								log.error("[error=" + SyncUbmcConstant.ERROR_USER_UPLOAD_ICON
										+ "]fileSrc is null or empty, [user_icon_id=" + userIcon.getId()
										+ "], [fileSrc=" + userIcon.getFileSrc() + "]");
								logPrinter.error(SyncUbmcConstant.ERROR_USER_UPLOAD_ICON 
										+ "\t" + userIcon.getId() 
										+ "\t" + userIcon.getUserId());
								continue;
							}
							
							String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
							byte[] data = this.getImage(url);
							if (null == data || data.length == 0){
								log.error("[error=" + SyncUbmcConstant.ERROR_USER_UPLOAD_ICON
										+ "]data from drmc is empty, [user_icon_id=" + userIcon.getId()
										+ "], [fileSrc=" + userIcon.getFileSrc() + "]");
								logPrinter.error(SyncUbmcConstant.ERROR_USER_UPLOAD_ICON 
										+ "\t" + userIcon.getId() 
										+ "\t" + userIcon.getUserId());
								data = new byte[0];
							}
							String fileSrcMd5 = MD5.getMd5(data);
							
							RequestIconMaterial request = new RequestIconMaterial(null, null, width, height, data, fileSrcMd5);
							requests.add(request);
							userIcons.add(userIcon);
						}
						
						logPrinter.log("[INFO][userIcons.size=" + userIcons.size()
								+ "], [requests.size=" + requests.size() + "]");
						
						int syncNum = 0;
						if (CollectionUtils.isNotEmpty(requests)) {
							List<ResponseBaseMaterial> resultInsert = ubmcService.insert(requests);
							
							if (CollectionUtils.isEmpty(resultInsert) || resultInsert.size() != userIcons.size()) {
								log.error("[error=" + SyncUbmcConstant.ERROR_USER_UPLOAD_ICON
										+ "], resultInsert size[" + resultInsert.size()
										+ "] != request size[" + userIcons.size() + "]");
							} else {
								for (int index = 0; index < userIcons.size(); index++) {
									UserUploadIcon userIcon = userIcons.get(index);
									ResponseBaseMaterial response = resultInsert.get(index);
									
									if (response != null && response.getMcId() > 0L) {
										userUploadIconDao.updateUserUploadIcon(userIcon.getId(), response.getMcId());
										syncNum++;
									} else {
										log.error("[error=" + SyncUbmcConstant.ERROR_USER_UPLOAD_ICON
												+ "], ubmc insert sysicon failed[user_icon_id=" 
												+ userIcon.getId() + "]");
										logPrinter.error(SyncUbmcConstant.ERROR_USER_UPLOAD_ICON 
												+ "\t" + userIcon.getId() 
												+ "\t" + userIcon.getUserId());
									}
								}
							}
						}
						
						logPrinter.log("[INFO][userIconList.size=" + userIconList.size()
								+ "], [syncNum=" + syncNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("sync usericon use " + (time2 - time1) + " ms, unitNum=" + maxMaterNumSelect);
					} catch (Exception e) {
						log.error("sync usericon failed", e);
					}
				} 
			}
			
			private byte[] getImage(String url) {
				if (!StringUtils.isEmpty(url)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = this.getFileByUrl(url);
						if (null == data || data.length == 0){
							log.error("[error=" + SyncUbmcConstant.ERROR_USER_UPLOAD_ICON
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

	public UserUploadIconDao getUserUploadIconDao() {
		return userUploadIconDao;
	}

	public void setUserUploadIconDao(UserUploadIconDao userUploadIconDao) {
		this.userUploadIconDao = userUploadIconDao;
	}
}
