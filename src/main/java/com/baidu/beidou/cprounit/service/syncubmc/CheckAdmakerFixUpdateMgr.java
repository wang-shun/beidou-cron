package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.TemplateDescJsonVo;
import com.baidu.beidou.cprounit.mcdriver.constant.AmConstant;
import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.AmService;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForAdmakerFixSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForAdmakerFixView;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseAdmakerMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.chuangyi.flash.decode.DecodeResult;
import com.baidu.chuangyi.flash.decode.FlashDecoder;
import com.baidu.gson.Gson;

/**
 * ClassName: CheckAdmakerFixUpdateMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料，修复admaker有问题物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckAdmakerFixUpdateMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckAdmakerFixUpdateMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitForAdmakerFixSet unitAdmakerMaterSet = null;
	private Gson gson = new Gson();
	
	private AmService amService;
	
	private UnitDao unitDao;
	
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
						
						int bugMaterialNum = 0;
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
									
									long tpId = ParseMC.getTpIdForSwf(data);
									if (tpId > 0) {
										// tpId>0: 表明为admaker制作的物料，可以继续进行处理
										FlashDecoder decoder = new FlashDecoder();
										DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
										if (decodeResult != null && decodeResult.getStatus() == 0) {
											String fileSrcMd5 = null;
											
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
											
											if (this.isAdmakerDrmcMater(jsonVo)) {
												continue;
											}
											
											GrantResult grantResult = amService.grantAuthority(descJson, tpId);
											
											if (grantResult.getStatusCode() == AmConstant.AM_GRANT_STATUS_RETRY_FAIL) {
												log.error("admaker grantAuthority retry three or more times failed[mater_id=" + unit.getId()
														+ ", fileSrc=" + unit.getFileSrc()    
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() 
														+ ", tpId=" + tpId
														+ ", descJson=" + descJson + "]");
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 7);
												continue;
											} else if (grantResult.getStatusCode() == AmConstant.AM_GRANT_STATUS_FAIL) {
												log.error("result from admaker grantAuthority is failed[mater_id=" + unit.getId()
														+ ", fileSrc=" + unit.getFileSrc()    
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() 
														+ ", tpId=" + tpId
														+ ", descJson=" + descJson + "]");
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 8);
												continue;
											} else if (grantResult.getStatusCode() == AmConstant.AM_GRANT_STATUS_OK) {
												Long mcId = grantResult.getMcId();
												Integer versionId = grantResult.getVersionId();
												
												// 构造请求
												List<RequestBaseMaterial> requestFixs = new ArrayList<RequestBaseMaterial>();
												RequestLite requestFix = new RequestLite(mcId, versionId);
												requestFixs.add(requestFix);
												// 获取图片存放在ubmc的物料信息，主要用于得到图片media所位于的占位符
												List<ResponseBaseMaterial> unitInfoFixList = ubmcService.get(requestFixs, false);
												if (CollectionUtils.isEmpty(unitInfoFixList) 
														|| unitInfoFixList.get(0) == null
														|| !(unitInfoFixList.get(0) instanceof ResponseAdmakerMaterial)) {
													log.error("result from admaker grantAuthority is null[mater_id=" + unit.getId()
															+ ", fileSrc=" + unit.getFileSrc()    
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() 
															+ ", tpId=" + tpId
															+ ", descJson=" + descJson + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 9);
													continue;
												}
												
												ResponseAdmakerMaterial responseFix = (ResponseAdmakerMaterial)unitInfoFixList.get(0);
												fileSrcMd5 = responseFix.getFileSrcMd5();
												RequestBaseMaterial requestUpdate = new RequestImageUnitWithMediaId(unit.getMcId(), 
														unit.getMcVersionId(), response1.getWuliaoType(), response1.getTitle(), 
														response1.getShowUrl(), response1.getTargetUrl(), response1.getWirelessShowUrl(), 
														response1.getWirelessTargetUrl(), response1.getWidth(), 
														response1.getHeight(), responseFix.getFileSrc(), responseFix.getFileSrcMd5(), 
														responseFix.getAttribute(), responseFix.getRefMcId(), responseFix.getDescInfo(), null);
												
												List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
												requestUpdates.add(requestUpdate);
												List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
												
												if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.size() != 1) {
													log.error("update attribute/md5/refMcId of image or flash from ubmc failed[mater_id=" 
															+ unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 10);
												} else {
													logCheckPrinter.invalid(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId());
													bugMaterialNum++;
													
													if (StringUtils.isNotEmpty(fileSrcMd5)) {
														unitDao.updateUnitMd5(dbIndex, unit.getId(), fileSrcMd5, 
																unit.getChaTime(), unit.getUserId());
													}
												}
												
											} else {
												log.error("admaker grantAuthority failed[mater_id=" + unit.getId()
														+ ", fileSrc=" + unit.getFileSrc()    
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() 
														+ ", tpId=" + tpId
														+ ", descJson=" + descJson + "]");
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 11);
											}
										}
									}
								}
							}
						}
						logCheckPrinter.log("[INFO][unitList.size=" + unitList.size()
								+ "], [bugMaterialNum=" + bugMaterialNum + "]");
						
						long time2 = System.currentTimeMillis();
						log.info("check unit image mater dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("check unit image mater failed dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
					}
				} 
			}
			
			private boolean isAdmakerDrmcMater(TemplateDescJsonVo jsonVo) {
				boolean result = true;
				
				List<Map<String, String>> elements = jsonVo.getElements();
				if (elements == null || elements.isEmpty()) {
					log.error("descJson of material from admaker has no elements");
					return true;
				}
				
				for (Map<String, String> map : elements) {
					if (map.containsKey("src")) {
						String src = map.get("src");
						if (StringUtils.isNotEmpty(src) && src.indexOf("drmcmm.baidu.com/media") < 0) {
							return false;
						}
					}
				}
				
				return result;
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

	public AmService getAmService() {
		return amService;
	}

	public void setAmService(AmService amService) {
		this.amService = amService;
	}

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
