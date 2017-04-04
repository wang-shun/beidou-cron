package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.AmService;
import com.baidu.beidou.cprounit.service.CproUnitWriteMgr;
import com.baidu.beidou.cprounit.service.DrmcService;
import com.baidu.beidou.cprounit.service.IconService;
import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial2;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralMaterial;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralWithIconMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseImageMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseLiteralMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseLiteralWithIconMaterial;
import com.baidu.beidou.cprounit.service.syncubmc.vo.DrmcUnitSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitDrmcMaterView;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.util.ThreadContext;
import com.baidu.chuangyi.flash.decode.DecodeResult;
import com.baidu.chuangyi.flash.decode.FlashDecoder;

/**
 * ClassName: SyncUnitMgr
 * Function: 同步beidou.cprounitmater[0-7]中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncDrmcUnitMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(SyncDrmcUnitMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private DrmcUnitSet unitMaterSet = null;
	
	private DrmcService drmcService;
	private IconService iconService;
	private AmService amService;
	private CproUnitWriteMgr unitWriteMgr;
	
	public void syncMater(int maxMaterNumSelect, PrintWriter errorWriter, PrintWriter logWriter, 
			PrintWriter invalidWriter, String dbFileName, int maxThread, int dbIndex, int dbSlice) {
		
		log.info("begin to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitMaterSet = new DrmcUnitSet(dbFileName);
		
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
						List<UnitDrmcMaterView> unitList = unitMaterSet.getNextList(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							logCheckPrinter.log("[INFO][dbIndex=" + dbSlice + ", index=" 
									+ dbIndex + "], [unitList.size=0], [syncNum=0]");
							break;
						}
						
						int syncNum = 0;
						for (UnitDrmcMaterView unit : unitList) {
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
								
								RequestLiteralMaterial literal = new RequestLiteralMaterial(title, showUrl, 
										targetUrl, description1, description2, wirelessShowUrl, wirelessTargetUrl);
								if (this.syncDrmcMater(unit, literal)) {
									syncNum++;
								}
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE
									|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
								Integer width = unit.getWidth();
								Integer height = unit.getHeight();
								
								List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
								RequestBaseMaterial request = new RequestLite(mcId, mcVersionId);
								requests.add(request);
								List<ResponseBaseMaterial> result = ubmcService.get(requests, false);
								
								ResponseBaseMaterial response = null;
								if (CollectionUtils.isEmpty(result) || result.size() != 1 
										|| (response = result.get(0)) == null) {
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
								
								if (!(response instanceof ResponseImageUnit)) {
									log.error("ubmc unit is not image or flash unit [id=" + unit.getId()
											+ ", userId=" + unit.getUserId() 
											+ ", mcId=" + unit.getMcId()
											+ ", versionId=" + unit.getMcVersionId() + "]");
									
									logCheckPrinter.error(unit.getId()
											+ "\t" + unit.getUserId()
											+ "\t" + unit.getMcId()
											+ "\t" + unit.getMcVersionId());
									continue;
								}
								
								ResponseImageUnit response1 = (ResponseImageUnit)response;
								
								byte[] data = this.getImageFromUbmc(response1.getFileSrc());
								if (null == data){
									log.error("data from ubmc is empty[mater_id=" + unit.getId()
											+ ", userId=" + unit.getUserId() 
											+ ", mcId=" + unit.getMcId()
											+ ", versionId=" + unit.getMcVersionId() + "]");
									logCheckPrinter.error(unit.getId()
											+ "\t" + unit.getUserId()
											+ "\t" + unit.getMcId()
											+ "\t" + unit.getMcVersionId());
									continue;
								}
								
								if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
									int tpId = ParseMC.getTpIdForSwf(data);
									if (tpId > 0) {
										// tpId>0: 表明为admaker制作的物料，可以继续进行处理
										FlashDecoder decoder = new FlashDecoder();
										DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
										if (decodeResult != null && decodeResult.getStatus() == 0) {
											String descJson = decodeResult.getMessage();
											
											// 如果descJson为空，则说明该物料存在问题，打印日志
											if (StringUtils.isEmpty(descJson)) {
												log.error("descJson from admaker material is empty[mater_id=" + unit.getId()
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
											
											String drmcUrl = amService.downloadDrmcMaterial(descJson, tpId);
											if (StringUtils.isEmpty(drmcUrl)) {
												log.error("new fileSrc generated by admaker is null or empty[mater_id=" + unit.getId()
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
											
											RequestImageMaterial2 image = new RequestImageMaterial2(title, showUrl, targetUrl, 
													drmcUrl, width, height, wirelessShowUrl, wirelessTargetUrl);
											if (this.syncDrmcMater(unit, image)) {
												syncNum++;
											}
											continue;
										}
									}
								}
								
								RequestImageMaterial image = new RequestImageMaterial(title, showUrl, targetUrl, 
										data, width, height, wirelessShowUrl, wirelessTargetUrl);
								if (this.syncDrmcMater(unit, image)) {
									syncNum++;
								}
							} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
								String description1 = unit.getDescription1();
								String description2 = unit.getDescription2();
								Integer width = unit.getWidth();
								Integer height = unit.getHeight();
								
								List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
								RequestBaseMaterial request = new RequestLite(mcId, mcVersionId);
								requests.add(request);
								List<ResponseBaseMaterial> result = ubmcService.get(requests, false);
								
								ResponseBaseMaterial response = null;
								if (CollectionUtils.isEmpty(result) || result.size() != 1 
										|| (response = result.get(0)) == null) {
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
								
								if (!(response instanceof ResponseIconUnit)) {
									log.error("ubmc unit is not icon unit [id=" + unit.getId()
											+ ", userId=" + unit.getUserId() 
											+ ", mcId=" + unit.getMcId()
											+ ", versionId=" + unit.getMcVersionId() + "]");
									
									logCheckPrinter.error(unit.getId()
											+ "\t" + unit.getUserId()
											+ "\t" + unit.getMcId()
											+ "\t" + unit.getMcVersionId());
									continue;
								}
								
								ResponseIconUnit response1 = (ResponseIconUnit)response;
								byte[] data = this.getImageFromUbmc(response1.getFileSrc());
								if (null == data){
									log.error("data from ubmc is empty[mater_id=" + unit.getId()
											+ ", userId=" + unit.getUserId() 
											+ ", mcId=" + unit.getMcId()
											+ ", versionId=" + unit.getMcVersionId() + "]");
									logCheckPrinter.error(unit.getId()
											+ "\t" + unit.getUserId()
											+ "\t" + unit.getMcId()
											+ "\t" + unit.getMcVersionId());
									continue;
								}
								
								RequestLiteralWithIconMaterial literalWithIcon = new RequestLiteralWithIconMaterial(title, 
										showUrl, targetUrl, description1, description2, data, 
										width, height, wirelessShowUrl, wirelessTargetUrl);
								if (this.syncDrmcMater(unit, literalWithIcon)) {
									syncNum++;
								}
							}
						}
						logCheckPrinter.log("[INFO][dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
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
			
			private boolean syncDrmcMater(UnitDrmcMaterView unit, BeidouMaterialBase ad) {
				Long unitId = unit.getId();
				Integer userId = unit.getUserId();
				Date chaTime = unit.getChaTime();
				Integer state = unit.getState();
				
				List<BeidouMaterialBase> ads = new ArrayList<BeidouMaterialBase>();
				ads.add(ad);
				
				boolean result = false;
				List<BeidouMaterialBase> drmcResult = drmcService.tmpInsertBatch(ads, false);
				
				Long wid = 0L;
				Long fwid = 0L;
				String fileSrc = null;
				if (CollectionUtils.isNotEmpty(drmcResult) && (drmcResult.get(0) != null)) {
					// 设置userId至context中
					ThreadContext.putUserId(userId);
					
					if (drmcResult.get(0) instanceof ResponseImageMaterial) {
						ResponseImageMaterial adFromDrmc = (ResponseImageMaterial)drmcResult.get(0);
						wid = adFromDrmc.getMcid();
						fileSrc = adFromDrmc.getFileSrc();
					} else if (drmcResult.get(0) instanceof ResponseLiteralWithIconMaterial) {
						ResponseLiteralWithIconMaterial adFromDrmc = (ResponseLiteralWithIconMaterial)drmcResult.get(0);
						wid = adFromDrmc.getMcid();
						fileSrc = adFromDrmc.getFileSrc();
					} else if (drmcResult.get(0) instanceof ResponseLiteralMaterial) {
						ResponseLiteralMaterial adFromDrmc = (ResponseLiteralMaterial)drmcResult.get(0);
						wid = adFromDrmc.getMcid();
						fileSrc = null;
					} else {
						log.error("drmc insert material failed, result type is invalid[mater_id=" + unit.getId()
								+ ", fileSrc=" + unit.getFileSrc()    
								+ ", userId=" + unit.getUserId() 
								+ ", mcId=" + unit.getMcId()
								+ ", versionId=" + unit.getMcVersionId() + "]");
						logCheckPrinter.error(unit.getId()
								+ "\t" + unit.getUserId()
								+ "\t" + unit.getMcId()
								+ "\t" + unit.getMcVersionId()
								+ "\t" + unit.getState());
						result = false;
						return result;
					}
					
					// 如果创意状态为有效或者暂停，需要将物料激活至正式桶
					if (state == CproUnitConstant.UNIT_STATE_NORMAL
							|| state == CproUnitConstant.UNIT_STATE_PAUSE) {
						long[] tmpids = new long[1];
						long[] fmids = new long[1];
						tmpids[0] = wid;
						fmids[0] = 0;
						int size = tmpids.length;
						BeidouMaterialBase[] activeResult = drmcService.tmpActiveBatch(tmpids, ArrayUtils.subarray(fmids, 0, size), true, false);
						
						if (!ArrayUtils.isEmpty(activeResult) && activeResult[0] != null) {
							
							if (activeResult[0] instanceof ResponseImageMaterial) {
								ResponseImageMaterial adFromDrmc = (ResponseImageMaterial)activeResult[0];
								fwid = adFromDrmc.getMcid();
								fileSrc = adFromDrmc.getFileSrc();
							} else if (activeResult[0] instanceof ResponseLiteralWithIconMaterial) {
								ResponseLiteralWithIconMaterial adFromDrmc = (ResponseLiteralWithIconMaterial)activeResult[0];
								fwid = adFromDrmc.getMcid();
								fileSrc = adFromDrmc.getFileSrc();
							} else if (activeResult[0] instanceof ResponseLiteralMaterial) {
								ResponseLiteralMaterial adFromDrmc = (ResponseLiteralMaterial)activeResult[0];
								fwid = adFromDrmc.getMcid();
								fileSrc = null;
							} else {
								log.error("drmc active tmpmaterial failed, result type is invalid[mater_id=" + unit.getId()
										+ ", fileSrc=" + unit.getFileSrc()    
										+ ", userId=" + unit.getUserId() 
										+ ", mcId=" + unit.getMcId()
										+ ", versionId=" + unit.getMcVersionId() + "]");
								logCheckPrinter.error(unit.getId()
										+ "\t" + unit.getUserId()
										+ "\t" + unit.getMcId()
										+ "\t" + unit.getMcVersionId()
										+ "\t" + unit.getState());
								result = false;
								return result;
							}
							
							unitWriteMgr.updateUnitForUbmcToDrmc(userId, unitId, fileSrc, fwid, chaTime, state);
							logCheckPrinter.invalid(unit.getId()
									+ "\t" + unit.getUserId()
									+ "\t" + unit.getMcId()
									+ "\t" + unit.getMcVersionId()
									+ "\t" + unit.getState());
							result = true;
						} else {
							log.error("drmc active tmpmaterial failed[mater_id=" + unit.getId()
									+ ", fileSrc=" + unit.getFileSrc()    
									+ ", userId=" + unit.getUserId() 
									+ ", mcId=" + unit.getMcId()
									+ ", versionId=" + unit.getMcVersionId() + "]");
							logCheckPrinter.error(unit.getId()
									+ "\t" + unit.getUserId()
									+ "\t" + unit.getMcId()
									+ "\t" + unit.getMcVersionId()
									+ "\t" + unit.getState());
							result = false;
						}
					} else {
						unitWriteMgr.updateUnitForUbmcToDrmc(userId, unitId, fileSrc, wid, chaTime, state);
						logCheckPrinter.invalid(unit.getId()
								+ "\t" + unit.getUserId()
								+ "\t" + unit.getMcId()
								+ "\t" + unit.getMcVersionId()
								+ "\t" + unit.getState());
						result = true;
					}
				} else {
					log.error("drmc insert material failed[mater_id=" + unit.getId()
							+ ", fileSrc=" + unit.getFileSrc()    
							+ ", userId=" + unit.getUserId() 
							+ ", mcId=" + unit.getMcId()
							+ ", versionId=" + unit.getMcVersionId() + "]");
					logCheckPrinter.error(unit.getId()
							+ "\t" + unit.getUserId()
							+ "\t" + unit.getMcId()
							+ "\t" + unit.getMcVersionId());
					result = false;
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

	public DrmcService getDrmcService() {
		return drmcService;
	}

	public void setDrmcService(DrmcService drmcService) {
		this.drmcService = drmcService;
	}

	public AmService getAmService() {
		return amService;
	}

	public void setAmService(AmService amService) {
		this.amService = amService;
	}

	public CproUnitWriteMgr getUnitWriteMgr() {
		return unitWriteMgr;
	}

	public void setUnitWriteMgr(CproUnitWriteMgr unitWriteMgr) {
		this.unitWriteMgr = unitWriteMgr;
	}

	public IconService getIconService() {
		return iconService;
	}

	public void setIconService(IconService iconService) {
		this.iconService = iconService;
	}
}
