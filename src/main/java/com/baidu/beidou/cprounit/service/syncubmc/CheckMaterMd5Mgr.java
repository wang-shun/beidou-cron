package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;
import com.baidu.beidou.util.MD5;

/**
 * ClassName: CheckUnitAllMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料，查看ubmc是否存在该物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckMaterMd5Mgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckMaterMd5Mgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitSet unitImageMaterSet = null;
	
	private UnitDao unitDao;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to check all mater for md5 in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
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
		log.info("check all mater for md5 in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitImageMaterSet.getTotal());
		
		unitImageMaterSet.closeFile();
		log.info("end to check all mater for md5 in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
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
							
							if (mcId != null && mcId > 0) {
								request = new RequestLite(mcId, mcVersionId);
								requests.add(request);
								units.add(unit);
							} else {
								log.error("mcId is null[mater_id=" + unit.getId()
										+ ", userId=" + unit.getUserId() 
										+ ", mcId=" + unit.getMcId()
										+ ", versionId=" + unit.getMcVersionId() + "]");
								
								logCheckPrinter.error(unit.getId()
										+ "\t" + unit.getUserId()
										+ "\t" + unit.getMcId()
										+ "\t" + unit.getMcVersionId()
										+ "\t" + 0);
								continue;
							}
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
									String dbFileSrcMd5 = unit.getFileSrcMd5();
									
									if (response == null) {
										log.error("ubmc get unit failed[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 0);
										continue;
									}
									
									if (!(response instanceof ResponseImageUnit
											|| response instanceof ResponseTextUnit
											|| response instanceof ResponseIconUnit)) {
										log.error("unit from ubmc is not text, icon, image, or flash[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 1);
									} else {
										RequestBaseMaterial request = null;
										
										String fileSrcMd5 = null;
										String fileSrc = null;
										if (response instanceof ResponseImageUnit) {
											ResponseImageUnit response1 = (ResponseImageUnit) response;
											fileSrcMd5 = response1.getFileSrcMd5();
											fileSrc = response1.getFileSrc();
											
											if (StringUtils.isEmpty(fileSrc)) {
												log.error("fileSrc is null or empty for image, or flash[id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 2);
												continue;
											}
											
											if (StringUtils.isEmpty(fileSrcMd5)) {
												log.error("md5 is null or empty for image, or flash[id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 3);
												
												byte[] data = this.getImageFromUbmc(fileSrc);
												if (null == data){
													log.error("data from ubmc is empty[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 4);
													continue;
												}
												
												fileSrcMd5 = MD5.getMd5(data);
												if (StringUtils.isEmpty(fileSrcMd5)){
													log.error("md5 generated by the util of MD5 is empty[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 5);
													continue;
												}
												
												request = new RequestImageUnitWithMediaId(unit.getMcId(), unit.getMcVersionId(), 
														response1.getWuliaoType(), response1.getTitle(), response1.getShowUrl(), 
														response1.getTargetUrl(), response1.getWirelessShowUrl(), 
														response1.getWirelessTargetUrl(), response1.getWidth(), 
														response1.getHeight(), fileSrc, fileSrcMd5, response1.getAttribute(), 
														response1.getRefMcId(), response1.getDescInfo(), null);
												
												List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
												requestUpdates.add(request);
												List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
												
												if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.size() != 1) {
													log.error("update md5 of image or flash from ubmc failed[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
												}
											}
										} else if (response instanceof ResponseIconUnit) {
											ResponseIconUnit response1 = (ResponseIconUnit) response;
											fileSrcMd5 = response1.getFileSrcMd5();
											fileSrc = response1.getFileSrc();
											
											if (StringUtils.isEmpty(fileSrc)) {
												log.error("fileSrc is null or empty for icon[id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 6);
												continue;
											}
											
											if (StringUtils.isEmpty(fileSrcMd5)) {
												log.error("md5 is null or empty for icon[id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 7);
												
												byte[] data = this.getImageFromUbmc(fileSrc);
												if (null == data){
													log.error("data from ubmc is empty[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 8);
													continue;
												}
												
												fileSrcMd5 = MD5.getMd5(data);
												if (StringUtils.isEmpty(fileSrcMd5)){
													log.error("md5 generated by the util of MD5 is empty[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 9);
													continue;
												}
												
												request = new RequestIconUnitWithMediaId(unit.getMcId(), unit.getMcVersionId(), 
														response1.getTitle(), response1.getDescription1(),
														response1.getDescription2(), response1.getShowUrl(), 
														response1.getTargetUrl(), response1.getWirelessShowUrl(), 
														response1.getWirelessTargetUrl(), response1.getWidth(), 
														response1.getHeight(), fileSrc, fileSrcMd5);
												
												List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
												requestUpdates.add(request);
												List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
												
												if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.size() != 1) {
													log.error("update md5 of icon from ubmc failed[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
												}
											}
										}
										
										if (StringUtils.isEmpty(fileSrcMd5)) {
											log.error("md5 is null or empty for mater[id=" + unit.getId()
													+ ", userId=" + unit.getUserId() 
													+ ", mcId=" + unit.getMcId()
													+ ", versionId=" + unit.getMcVersionId() + "]");
											
											logCheckPrinter.error(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + 10);
											continue;
										}
										
										// 如果db中md5为null，或者与ubmc中存储md5不一致，则更新db
										if (StringUtils.isEmpty(dbFileSrcMd5)
												|| !(dbFileSrcMd5.equalsIgnoreCase(fileSrcMd5))) {
											unitDao.updateUnitMd5(dbIndex, unit.getId(), fileSrcMd5, 
													unit.getChaTime(), unit.getUserId());
											logCheckPrinter.invalid(unit.getId() + "\t" + unit.getUserId() 
													+ "\t" + dbFileSrcMd5 + "\t" + fileSrcMd5);
										}
									}
								}
							}
						}
						
						logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=" + unitList.size() + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("check all mater for md5 in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("check all mater for md5 failed in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
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

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
