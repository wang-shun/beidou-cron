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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.constant.CheckUbmcConstant;
import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;

/**
 * ClassName: CheckUnitImageMgr
 * Function: 校验beidou.cprounitmater[0-7]中图片物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckUnitImageMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckUnitImageMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitSet unitImageMaterSet = null;
	
	private UnitDao unitDao;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to check all unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitImageMaterSet = new UnitSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to work");
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
		log.info("check all unit image mater dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitImageMaterSet.getTotal());
		
		unitImageMaterSet.closeFile();
		log.info("end to check all unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UnitMaterView> unitList = unitImageMaterSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=0], [checkNum=0]");
							break;
						}
						
						List<UnitMaterView> units = new LinkedList<UnitMaterView>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (UnitMaterView unit : unitList) {
							RequestBaseMaterial request = null;
							
							Long mcId = unit.getMcId();
							Integer mcVersionId = unit.getMcVersionId();
							Integer wuliaoType = unit.getWuliaoType();
							
							if (mcId != null && mcId > 0 && (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE
									|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH
									|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON)) {
								request = new RequestLite(mcId, mcVersionId);
								requests.add(request);
								units.add(unit);
							} else {
								log.error("unit is not image or icon, or mcId is null[mater_id=" + unit.getId()
										+ ", userId=" + unit.getUserId() 
										+ ", mcId=" + unit.getMcId()
										+ ", versionId=" + unit.getMcVersionId() + "]");
								
								logCheckPrinter.error(unit.getId()
										+ "\t" + unit.getUserId()
										+ "\t" + unit.getMcId()
										+ "\t" + unit.getMcVersionId());
								continue;
							}
							
							String fileSrc = unit.getFileSrc();
							if (StringUtils.isEmpty(fileSrc)) {
								log.error("fileSrc is null or empty[mater_id=" + unit.getId()
										+ ", fileSrc=" + unit.getFileSrc()    
										+ ", userId=" + unit.getUserId() 
										+ ", mcId=" + unit.getMcId()
										+ ", versionId=" + unit.getMcVersionId() + "]");
								logCheckPrinter.error(unit.getId()
										+ "\t" + unit.getUserId()
										+ "\t" + unit.getMcId()
										+ "\t" + unit.getMcVersionId());
								continue;
							}
							
							String url = CproUnitConfig.DRMC_MATPREFIX + fileSrc;
							byte[] data = this.getImageFromDrmc(url);
							if (null == data){
								log.error("data from drmc is empty[mater_id=" + unit.getId()
										+ ", fileSrc=" + unit.getFileSrc()    
										+ ", userId=" + unit.getUserId() 
										+ ", mcId=" + unit.getMcId()
										+ ", versionId=" + unit.getMcVersionId() + "]");
								data = new byte[0];
							}
							unit.setData(data);
						}
						logCheckPrinter.log("[INFO][units.size=" + units.size()
								+ "], [requests.size=" + requests.size() + "]");
						
						if (CollectionUtils.isNotEmpty(requests)) {
							List<ResponseBaseMaterial> result = ubmcService.get(requests, false);
							
							if (CollectionUtils.isEmpty(result) || result.size() != units.size()) {
								log.error("resultInsert size[" + result.size()
										+ "] != request size[" + units.size() + "]");
							} else {
								for (int index = 0; index < units.size(); index++) {
									UnitMaterView unit = units.get(index);
									ResponseBaseMaterial response = result.get(index);
									
									if (response == null) {
										log.error("ubmc get unit failed[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId());
										continue;
									}
									
									try {
										
										if (response instanceof ResponseImageUnit) {
											ResponseImageUnit response1 = (ResponseImageUnit)response;
											
											byte[] data = this.getImageFromUbmc(response1.getFileSrc());
											if (null == data){
												log.error("data from ubmc is empty[mater_id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												data = new byte[0];
											}
											
											int checkValue = checkMater(response1, data, unit);
											if (checkValue != CheckUbmcConstant.CHECK_OK) {
												log.error("id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() 
														+ ", checkValue=" + checkValue);
												logCheckPrinter.invalid(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId());
											}
										} else if (response instanceof ResponseIconUnit) {
											ResponseIconUnit response1 = (ResponseIconUnit)response;
											
											byte[] data = ubmcService.getMediaData(response1.getFileSrc());
											if (null == data){
												log.error("data from ubmc is empty[mater_id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												data = new byte[0];
											}
											
											int checkValue = checkMater(response1, data, unit);
											if (checkValue != CheckUbmcConstant.CHECK_OK) {
												log.error("material is different between drmc and ubmc[id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() 
														+ ", checkValue=" + checkValue + "]");
												logCheckPrinter.invalid(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId());
											}
										} else {
											log.error("ubmc response is not image or icon[id=" + unit.getId()
													+ ", userId=" + unit.getUserId() 
													+ ", mcId=" + unit.getMcId()
													+ ", versionId=" + unit.getMcVersionId() + "]");
											logCheckPrinter.error(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId());
										}
									} catch(Exception e) {
										log.error("ubmc response is not text[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]", e);
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId());
									}
								}
							}
						}
						
						logCheckPrinter.log("[INFO][index=" + dbIndex
								+ "], [unitList.size=" + unitList.size() + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("check unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("check unit image mater failed dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
			
			private byte[] getImageFromDrmc(String url) {
				if (!StringUtils.isEmpty(url)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = this.getFileByUrl(url);
						if (null == data || data.length == 0){
							log.error("data from drmc is empty, url=" + url);
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
			
			private byte[] getImageFromUbmc(String fileSrc) {
				if (!StringUtils.isEmpty(fileSrc)) {
					for (int i = 0; i < 5; i++) {
						byte[] data = ubmcService.getMediaData(fileSrc);
						if (null == data){
							log.error("data from ubmc is empty, fileSrc=" + fileSrc);
						} else {
							return data;
						}
					}
				}
				return null;
			}
		};
	}
	
	private int checkMater(ResponseImageUnit response, byte[] responseData, UnitMaterView unit) {
		try {
			if (!checkString(response.getTitle(), unit.getTitle())) {
				return CheckUbmcConstant.CHECK_TITLE;
			}
			
			if (!checkString(response.getTargetUrl(), unit.getTargetUrl())) {
				return CheckUbmcConstant.CHECK_TARGETURL;
			}
			
			if (!checkString(response.getShowUrl(), unit.getShowUrl())) {
				return CheckUbmcConstant.CHECK_SHOWURL;
			}
			
			if (!checkString(response.getWirelessTargetUrl(), unit.getWirelessTargetUrl())) {
				return CheckUbmcConstant.CHECK_WIRELESS_TARGETURL;
			}
			
			if (!checkString(response.getWirelessShowUrl(), unit.getWirelessShowUrl())) {
				return CheckUbmcConstant.CHECK_WIRELESS_SHOWURL;
			}
			
			if (!checkInt(response.getWidth(), unit.getWidth())) {
				return CheckUbmcConstant.CHECK_WIDTH;
			}
			
			if (!checkInt(response.getHeight(), unit.getHeight())) {
				return CheckUbmcConstant.CHECK_HEIGHT;
			}
			
			if (!checkData(responseData, unit.getData())) {
				return CheckUbmcConstant.CHECK_IMAGE;
			}
		} catch(Exception e) {
			log.error("check text failed[id=" + unit.getId()
					+ ", userId=" + unit.getUserId() 
					+ ", mcId=" + unit.getMcId()
					+ ", versionId=" + unit.getMcVersionId() + "]", e);
			return CheckUbmcConstant.CHECK_FAIL;
		}
		
		return CheckUbmcConstant.CHECK_OK;
	}
	
	private int checkMater(ResponseIconUnit response, byte[] responseData, UnitMaterView unit) {
		try {
			if (!checkString(response.getTitle(), unit.getTitle())) {
				return CheckUbmcConstant.CHECK_TITLE;
			}
			
			if (!checkString(response.getDescription1(), unit.getDescription1())) {
				return CheckUbmcConstant.CHECK_DESC1;
			}
			
			if (!checkString(response.getDescription2(), unit.getDescription2())) {
				return CheckUbmcConstant.CHECK_DESC2;
			}
			
			if (!checkString(response.getTargetUrl(), unit.getTargetUrl())) {
				return CheckUbmcConstant.CHECK_TARGETURL;
			}
			
			if (!checkString(response.getShowUrl(), unit.getShowUrl())) {
				return CheckUbmcConstant.CHECK_SHOWURL;
			}
			
			if (!checkString(response.getWirelessTargetUrl(), unit.getWirelessTargetUrl())) {
				return CheckUbmcConstant.CHECK_WIRELESS_TARGETURL;
			}
			
			if (!checkString(response.getWirelessShowUrl(), unit.getWirelessShowUrl())) {
				return CheckUbmcConstant.CHECK_WIRELESS_SHOWURL;
			}
			
			if (!checkInt(response.getWidth(), unit.getWidth())) {
				return CheckUbmcConstant.CHECK_WIDTH;
			}
			
			if (!checkInt(response.getHeight(), unit.getHeight())) {
				return CheckUbmcConstant.CHECK_HEIGHT;
			}
			
			if (!checkData(responseData, unit.getData())) {
				return CheckUbmcConstant.CHECK_IMAGE;
			}
		} catch(Exception e) {
			log.error("check text failed[id=" + unit.getId()
					+ ", userId=" + unit.getUserId() 
					+ ", mcId=" + unit.getMcId()
					+ ", versionId=" + unit.getMcVersionId() + "]", e);
			return CheckUbmcConstant.CHECK_FAIL;
		}
		
		return CheckUbmcConstant.CHECK_OK;
	}
	
	private boolean checkData(byte[] data1, byte[] data2) {
		boolean data1Empty = ArrayUtils.isEmpty(data1);
		boolean data2Empty = ArrayUtils.isEmpty(data2);
		
		if (data1Empty && data2Empty) {
			return true;
		}
		
		if (!data1Empty && !data2Empty) {
			if (data1.length != data2.length) {
				return false;
			} else {
				for (int index = 0; index < data1.length; index++) {
					if (data1[index] != data2[index]) {
						return false;
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}
	
	private boolean checkInt(Integer int1, Integer int2) {
		if (int1 == null && int2 == null) {
			return true;
		}
		
		if (int1 != null && int2 != null) {
			if (int1.equals(int2)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean checkString(String str1, String str2) {
		boolean str1Empty = StringUtils.isEmpty(str1);
		boolean str2Empty = StringUtils.isEmpty(str2);
		
		if (str1Empty && str2Empty) {
			return true;
		}
		
		if (!str1Empty && !str2Empty) {
			if (str1.equalsIgnoreCase(str2)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
