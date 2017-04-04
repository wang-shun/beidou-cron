package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;

/**
 * ClassName: CheckUnitImageMgr
 * Function: 校验beidou.cprounitmater[0-7]中图片物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckImageUpdateMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckImageUpdateMgr.class);
	
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
							Long mcId = unit.getMcId();
							Integer mcVersionId = unit.getMcVersionId();
							RequestBaseMaterial request = new RequestLite(mcId, mcVersionId);
							requests.add(request);
							units.add(unit);
							
						}
						logCheckPrinter.log("[INFO][units.size=" + units.size()
								+ "], [requests.size=" + requests.size() + "]");
						
						List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
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
											
											if (response1.getRefMcId() == null || response1.getDescInfo() == null) {
												RequestImageUnitWithMediaId request = new RequestImageUnitWithMediaId(response1.getMcId(), 
														response1.getVersionId(), response1.getWuliaoType(), response1.getTitle(), 
														response1.getShowUrl(), response1.getTargetUrl(), 
														response1.getWirelessShowUrl(),	response1.getWirelessTargetUrl(), 
														response1.getWidth(), response1.getHeight(), 
														response1.getFileSrc(), response1.getFileSrcMd5(), "", "", "", null);
												requestUpdates.add(request);
												logCheckPrinter.invalid(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId());
											}
											
										} else {
											log.error("ubmc response is not image[id=" + unit.getId()
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
								
								ubmcService.update(requestUpdates);
								logCheckPrinter.log("[INFO][index=" + dbIndex
										+ "], [requestUpdates.size=" + requestUpdates.size() + "]");
							}
						}
						
						long time2 = System.currentTimeMillis();
						log.info("check unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, updateUnitNum=" + requestUpdates.size());
					} catch (Exception e) {
						log.error("check unit image mater failed dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
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
