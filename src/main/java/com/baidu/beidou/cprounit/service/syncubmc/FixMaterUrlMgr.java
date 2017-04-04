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
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;

/**
 * ClassName: FixMaterUrlMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料与ubmc中，查看ubmc是否存在该物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class FixMaterUrlMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(FixMaterUrlMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitSet unitImageMaterSet = null;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to fix all mater for wirelessurl in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
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
		log.info("fix all mater for wirelessurl in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitImageMaterSet.getTotal());
		
		unitImageMaterSet.closeFile();
		log.info("end to fix all mater for wirelessurl in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
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
									
									if (response == null) {
										log.error("ubmc get unit failed[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 1);
										continue;
									}
									
									if (!(response instanceof ResponseImageUnit
											|| response instanceof ResponseTextUnit
											|| response instanceof ResponseIconUnit)) {
										log.error("unit from ubmc is not text, icon, image[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 2);
									} else {
										RequestBaseMaterial request = null;
										
										if (response instanceof ResponseImageUnit) {
											ResponseImageUnit response1 = (ResponseImageUnit) response;
											
											// 如果db和ubmc中的移动点击url和移动显示url均一致，则无需更新
											if (compareWirelessUrl(unit.getWirelessShowUrl(), response1.getWirelessShowUrl())
													&& compareWirelessUrl(unit.getWirelessTargetUrl(), response1.getWirelessTargetUrl())) {
												continue;
											}
												
											request = new RequestImageUnitWithMediaId(unit.getMcId(), unit.getMcVersionId(), 
													response1.getWuliaoType(), response1.getTitle(), response1.getShowUrl(), 
													response1.getTargetUrl(), unit.getWirelessShowUrl(), 
													unit.getWirelessTargetUrl(), response1.getWidth(), 
													response1.getHeight(), response1.getFileSrc(), 
													response1.getFileSrcMd5(), response1.getAttribute(), 
													response1.getRefMcId(), response1.getDescInfo(), null);
										} else if (response instanceof ResponseIconUnit) {
											ResponseIconUnit response1 = (ResponseIconUnit) response;
											
											// 如果db和ubmc中的移动点击url和移动显示url均一致，则无需更新
											if (compareWirelessUrl(unit.getWirelessShowUrl(), response1.getWirelessShowUrl())
													&& compareWirelessUrl(unit.getWirelessTargetUrl(), response1.getWirelessTargetUrl())) {
												continue;
											}
												
											request = new RequestIconUnitWithMediaId(unit.getMcId(), unit.getMcVersionId(), 
													response1.getTitle(), response1.getDescription1(),
													response1.getDescription2(), response1.getShowUrl(), 
													response1.getTargetUrl(), unit.getWirelessShowUrl(), 
													unit.getWirelessTargetUrl(), response1.getWidth(), 
													response1.getHeight(), response1.getFileSrc(), 
													response1.getFileSrcMd5());
												
										} else if (response instanceof ResponseTextUnit) {
											ResponseTextUnit response1 = (ResponseTextUnit) response;
											
											// 如果db和ubmc中的移动点击url和移动显示url均一致，则无需更新
											if (compareWirelessUrl(unit.getWirelessShowUrl(), response1.getWirelessShowUrl())
													&& compareWirelessUrl(unit.getWirelessTargetUrl(), response1.getWirelessTargetUrl())) {
												continue;
											}
											
											request = new RequestTextUnit(unit.getMcId(), unit.getMcVersionId(), 
													response1.getTitle(), response1.getDescription1(),
													response1.getDescription2(), response1.getShowUrl(), 
													response1.getTargetUrl(), unit.getWirelessShowUrl(), 
													unit.getWirelessTargetUrl());
										}
										
										// 更新ubmc中的移动url信息
										List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
										requestUpdates.add(request);
										List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
										
										if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.size() != 1) {
											log.error("update wireless url for all unit from ubmc failed[mater_id=" + unit.getId()
													+ ", userId=" + unit.getUserId() 
													+ ", mcId=" + unit.getMcId()
													+ ", versionId=" + unit.getMcVersionId() + "]");
											logCheckPrinter.error(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + 3);
										} else {
											// 成功之后记录日志(invalid日志暂用于记录修复成功的创意信息)
											logCheckPrinter.invalid(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId());
										}
									}
								}
							}
						}
						
						logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=" + unitList.size() + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("fix all mater for wirelessurl in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("fix all mater for wirelessurl in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
			
			
			/**
			 * compareWirelessUrl: 比较db与ubmc中的移动链接是否不一致
			 * @version cpweb-567
			 * @author genglei01
			 * @date Jan 16, 2014
			 */
			private boolean compareWirelessUrl(String dbUrl, String ubmcUrl) {
				boolean isEmptyFlagForDbUrl = StringUtils.isEmpty(dbUrl);
				boolean isEmptyFlagForUbmcUrl = StringUtils.isEmpty(ubmcUrl);
				
				if (isEmptyFlagForDbUrl && isEmptyFlagForUbmcUrl) {
					return true;
				}
				
				if ((isEmptyFlagForDbUrl && !isEmptyFlagForUbmcUrl)
						|| (!isEmptyFlagForDbUrl && isEmptyFlagForUbmcUrl)) {
					return false;
				}
				
				if (dbUrl.equals(ubmcUrl)) {
					return true;
				} else {
					return false;
				}
			}
		};
	}
}
