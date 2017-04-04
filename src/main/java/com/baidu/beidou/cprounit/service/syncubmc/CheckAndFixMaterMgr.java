package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.UnitBeanUtils;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitMaterCheckSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitMaterCheckView;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestSmartUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseSmartUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;

/**
 * ClassName: CheckUnitAllMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料，查看ubmc是否存在该物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckAndFixMaterMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckAndFixMaterMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitMaterCheckSet unitMaterSet = null;
	
	private UnitDao unitDao;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.maxMaterNumSelect = maxMaterNumSelect;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitMaterSet = new UnitMaterCheckSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to work");
		ExecutorService pool = Executors.newFixedThreadPool(maxThread);
		long time1 = System.currentTimeMillis();
		for (int times = 0; times < maxThread; times++) {
			Runnable worker = this.createCheckTask();
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
		log.info("check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitMaterSet.getTotal());
		
		unitMaterSet.closeFile();
		log.info("end to check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createCheckTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UnitMaterCheckView> unitList = unitMaterSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=0], [checkNum=0]");
							break;
						}
						
						List<UnitMaterCheckView> units = new LinkedList<UnitMaterCheckView>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (UnitMaterCheckView unit : unitList) {
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
									UnitMaterCheckView unit = units.get(index);
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
									
									if (!(response instanceof ResponseImageUnit
											|| response instanceof ResponseTextUnit
											|| response instanceof ResponseIconUnit
											|| response instanceof ResponseSmartUnit)) {
										log.error("unit from ubmc is not text, icon, image, flash, or smart[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 3);
									} else {
										
										int compareCode = UnitBeanUtils.compareMaterialFromDbToUbmc(unit, response);
										if (compareCode == 0) {
											continue;
										} else {
											logCheckPrinter.invalid(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + unit.getWuliaoType()
													+ "\t" + unit.getState()
													+ "\t" + compareCode);
										}
									}
								}
							}
						}
						
						logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=" + unitList.size() + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("[TASK]check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("check the material of db and ubmc failed dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
		};
	}
	
	public void fixMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to check all unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.maxMaterNumSelect = maxMaterNumSelect;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitMaterSet = new UnitMaterCheckSet(dbFileName);
		
		// 多线程处理
		log.info("begin to create " + maxThread + " threads to work");
		ExecutorService pool = Executors.newFixedThreadPool(maxThread);
		long time1 = System.currentTimeMillis();
		for (int times = 0; times < maxThread; times++) {
			Runnable worker = this.createFixTask();
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
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitMaterSet.getTotal());
		
		unitMaterSet.closeFile();
		log.info("end to check all unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createFixTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UnitMaterCheckView> unitList = unitMaterSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=0], [checkNum=0]");
							break;
						}
						
						List<UnitMaterCheckView> units = new LinkedList<UnitMaterCheckView>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (UnitMaterCheckView unit : unitList) {
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
									UnitMaterCheckView unit = units.get(index);
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
									
									if (!(response instanceof ResponseImageUnit
											|| response instanceof ResponseTextUnit
											|| response instanceof ResponseIconUnit
											|| response instanceof ResponseSmartUnit)) {
										log.error("unit from ubmc is not text, icon, image, flash, or smart[id=" + unit.getId()
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										logCheckPrinter.error(unit.getId()
												+ "\t" + unit.getUserId()
												+ "\t" + unit.getMcId()
												+ "\t" + unit.getMcVersionId()
												+ "\t" + 3);
									} else {
										
										int compareCode = UnitBeanUtils.compareMaterialFromDbToUbmc(unit, response);
										if (compareCode == 0) {
											continue;
										} else {
											boolean ubmcFix = false;
											boolean dbFix = false;
											int width = 0;
											int height = 0;
											String fileSrcMd5 = null;
											RequestBaseMaterial request = null;
											
											if (unit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
												// 文字物料，以数据库数据为主进行修复
												request = new RequestTextUnit(unit.getMcId(), unit.getMcVersionId(), unit.getTitle(), 
														unit.getDescription1(),	unit.getDescription2(), unit.getShowUrl(), 
														unit.getTargetUrl(), unit.getWirelessShowUrl(), unit.getWirelessTargetUrl());
												
											} else if (unit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_PICTURE
													|| unit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_FLASH) {
												// 图片创意，类型不一致的数量很少，程序可以不做处理，走线下通知运营人员操作
												if (compareCode != 1) {
													ResponseImageUnit response1 = (ResponseImageUnit) response;
													request = new RequestImageUnitWithMediaId(unit.getMcId(), unit.getMcVersionId(), 
															unit.getWuliaoType(), unit.getTitle(), unit.getShowUrl(), unit.getTargetUrl(), 
															unit.getWirelessShowUrl(), unit.getWirelessTargetUrl(), 
															response1.getWidth(), response1.getHeight(), response1.getFileSrc(), 
															response1.getFileSrcMd5(), response1.getAttribute(), response1.getRefMcId(), 
															response1.getDescInfo(), null);
													
													// 如果是尺寸发生了变化，需要更新db中的width/height/file_src_md5
													if ((compareCode & 8) == 8) {
														width = response1.getWidth();
														height = response1.getHeight();
														fileSrcMd5 = response1.getFileSrcMd5();
													}
												}
											} else if (unit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
												// 图文物料，以数据库数据为主进行修复
												List<Long> iconIds = unitDao.findIconIdByUnitId(unit.getId(), unit.getUserId());
												if (CollectionUtils.isEmpty(iconIds) || iconIds.get(0) == null) {
													log.error("unit icon id not exist[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 4);
												} else {
													Long iconId = iconIds.get(0);
													
													// 构造获取图标请求
													List<RequestBaseMaterial> requestIcons = new ArrayList<RequestBaseMaterial>();
													RequestLite requestIcon = new RequestLite(iconId, 1);
													requestIcons.add(requestIcon);
													List<ResponseBaseMaterial> resultIcons = ubmcService.get(requestIcons, false);
													if (CollectionUtils.isEmpty(result) || resultIcons.size() != 1
															|| !(resultIcons.get(0) instanceof ResponseIconMaterial)) {
														log.error("unit icon from umbc not exist or material from ubmc not icon[mater_id=" + unit.getId()
																+ ", userId=" + unit.getUserId() 
																+ ", mcId=" + unit.getMcId()
																+ ", versionId=" + unit.getMcVersionId() + "]");
														
														logCheckPrinter.error(unit.getId()
																+ "\t" + unit.getUserId()
																+ "\t" + unit.getMcId()
																+ "\t" + unit.getMcVersionId()
																+ "\t" + 5);
													} else {
														ResponseIconMaterial iconMaterial = (ResponseIconMaterial) resultIcons.get(0);
														request = new RequestIconUnitWithMediaId(unit.getMcId(), unit.getMcVersionId(), 
																unit.getTitle(), unit.getDescription1(),	unit.getDescription2(), unit.getShowUrl(), 
																unit.getTargetUrl(), unit.getWirelessShowUrl(), unit.getWirelessTargetUrl(), 
																unit.getWidth(), unit.getHeight(), iconMaterial.getFileSrc(), iconMaterial.getFileSrcMd5());
													}
												}
												
											} else if (unit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_SMART_IDEA) {
												// 智能物料，以数据库数据为主进行修复
												if (response instanceof ResponseSmartUnit) {
													ResponseSmartUnit response1 = (ResponseSmartUnit)response;
													request = new RequestSmartUnit(unit.getMcId(), unit.getMcVersionId(), unit.getWuliaoType(), 
															unit.getShowUrl(), unit.getTargetUrl(), unit.getWirelessShowUrl(), unit.getWirelessTargetUrl(), 
															unit.getWidth(), unit.getHeight(), response1.getTemplateId());
												} else {
													log.error("smart unit from ubmc is invalid[id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 5);
												}
											}
											
											if (request != null) {
												List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
												requestUpdates.add(request);
												List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
												if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.size() != 1) {
													log.error("update material from ubmc failed[mater_id=" 
															+ unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 6);
												} else {
													ubmcFix = true;
													
													// 如果是尺寸发生了变化，需要更新db中的width/height/file_src_md5
													if ((compareCode & 8) == 8 && (width != 0 && height !=0 && fileSrcMd5 != null)) {
														unitDao.updateUnitSizeAndMd5(dbIndex, unit.getId(), width, height, 
																fileSrcMd5, unit.getChaTime(), unit.getUserId());
														dbFix = true;
													}
												}
											}
											
											logCheckPrinter.invalid(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + unit.getWuliaoType()
													+ "\t" + response.getWuliaoType()
													+ "\t" + unit.getState()
													+ "\t" + compareCode
													+ "\t" + ubmcFix
													+ "\t" + dbFix);
										}
									}
								}
							}
						}
						
						logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=" + unitList.size() + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("[TASK]check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("check the material of db and ubmc failed dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
		};
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
