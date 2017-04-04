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

import com.baidu.beidou.cprounit.mcdriver.bean.response.TemplateDescJsonVo;
import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForAdmakerFixSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForAdmakerFixView;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.chuangyi.flash.decode.DecodeResult;
import com.baidu.chuangyi.flash.decode.FlashDecoder;
import com.baidu.gson.Gson;

/**
 * ClassName: RecompileAdmakerFlashMaterMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料，修复admaker有问题物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckAdmakerFlashVersionMaterMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckAdmakerFlashVersionMaterMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitForAdmakerFixSet unitAdmakerMaterSet = null;
	private Gson gson = new Gson();
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to check all unit mater for admaker in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitAdmakerMaterSet = new UnitForAdmakerFixSet(dbFileName);
		
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
		log.info("check all unit mater for admaker in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitAdmakerMaterSet.getTotal());
		
		unitAdmakerMaterSet.closeFile();
		log.info("end to check all unit mater for admaker in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UnitForAdmakerFixView> unitList = unitAdmakerMaterSet.getNextUnitMater(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=0], [checkNum=0]");
							break;
						}
						
						List<UnitForAdmakerFixView> units = new LinkedList<UnitForAdmakerFixView>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						int admakerNum = 0;
						int notAdmakerNum = 0;
						for (UnitForAdmakerFixView unit : unitList) {
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
										+ "\t" + 1);
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
									UnitForAdmakerFixView unit = units.get(index);
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
												+ "\t" + 2);
										continue;
									}
									
									if (!(response instanceof ResponseImageUnit)) {
										log.error("ubmc response is not flash[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 3);
										continue;
									}
									
									ResponseImageUnit response1 = (ResponseImageUnit)response;
									String fileSrc = response1.getFileSrc();
									
									if (StringUtils.isEmpty(fileSrc)) {
										log.error("fileSrc from ubmc is empty[mater_id=" + unit.getId()
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
									
									String attribute = response1.getAttribute();
									String refMcId = response1.getRefMcId();
									
									if (StringUtils.isNotEmpty(attribute) && StringUtils.isNotEmpty(refMcId)) {
										logCheckPrinter.invalid(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + unit.getState()
												+ "\t" + unit.getChaTime()
												+ "\t" + 1);
										admakerNum++;
										continue;
									}
									
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
												+ "\t" + 5);
										continue;
									}
									
									String xmlStr = ParseMC.extrateXml(data);
									if (StringUtils.isNotEmpty(xmlStr)) {
										logCheckPrinter.invalid(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + unit.getState()
												+ "\t" + unit.getChaTime()
												+ "\t" + 2);
										admakerNum++;
										continue;
									}
									
									boolean tpIdFlag = false;
									long tpId = ParseMC.getTpIdForSwf(data);
									if (tpId > 0) {
										tpIdFlag = true;
									}
									
									// tpId>0: 表明为admaker制作的物料，可以继续进行处理
									boolean descJsonFlag = false;
									FlashDecoder decoder = new FlashDecoder();
									DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
									if (decodeResult != null && decodeResult.getStatus() == 0) {
										String descJson = decodeResult.getMessage();
										TemplateDescJsonVo jsonVo = null;
										
										// 如果descJson为空，则说明该物料存在问题，打印日志
										if (StringUtils.isEmpty(descJson)
												|| (jsonVo = gson.fromJson(descJson, TemplateDescJsonVo.class)) == null) {
											log.error("descJson from admaker material is empty[mater_id=" + unit.getId()
													+ ", fileSrc=" + unit.getFileSrc()    
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
										descJsonFlag = true;
									}
									
									if (tpIdFlag) {
										admakerNum++;
										if (descJsonFlag) {
											logCheckPrinter.invalid(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + unit.getState()
													+ "\t" + unit.getChaTime()
													+ "\t" + 3);
										} else {
											logCheckPrinter.invalid(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + unit.getState()
													+ "\t" + unit.getChaTime()
													+ "\t" + 4);
										}
									} else {
										if (descJsonFlag) {
											logCheckPrinter.invalid(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + unit.getState()
													+ "\t" + unit.getChaTime()
													+ "\t" + 5);
											admakerNum++;
										} else {
											String drmcStr = ParseMC.parseDrmcFromSwf(data);
											if (StringUtils.isNotEmpty(drmcStr)) {
												logCheckPrinter.invalid(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + unit.getState()
														+ "\t" + unit.getChaTime()
														+ "\t" + 6);
												admakerNum++;
											} else {
												// 非admaker物料
												logCheckPrinter.invalid(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + unit.getState()
														+ "\t" + unit.getChaTime()
														+ "\t" + 0);
												notAdmakerNum++;
											}
										}
									}
								}
							}
						}
						logCheckPrinter.log("[INFO][unitList.size=" + unitList.size()
								+ "], [admakerNum=" + admakerNum + "], [notAdmakerNum=" + notAdmakerNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("check unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("check unit image mater failed dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
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
}
