package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
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

import com.baidu.beidou.cache.util.DateUtils;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.TemplateDescJsonVo;
import com.baidu.beidou.cprounit.mcdriver.constant.AmConstant;
import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.RecompileCreativeService;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForRecompileTargettedSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForRecompileTargettedView;
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
 * ClassName: RecompileAdmakerFlashMaterMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料，修复admaker有问题物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class RecompileTargetMaterMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(RecompileTargetMaterMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitForRecompileTargettedSet unitTarggettedSet = null;
	private Gson gson = new Gson();
	
	private RecompileCreativeService recompileCreativeService;
	
	private UnitDao unitDao;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to recompile targetted unit image mater in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.maxMaterNumSelect = maxMaterNumSelect / maxThread;
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitTarggettedSet = new UnitForRecompileTargettedSet(dbFileName);
		
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
		log.info("recompile targetted unit image mater in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitTarggettedSet.getTotal());
		
		unitTarggettedSet.closeFile();
		log.info("end to recompile targetted unit image mater in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	protected Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (true) {
					try {
						long time1 = System.currentTimeMillis();
						List<UnitForRecompileTargettedView> unitList = unitTarggettedSet.getNextUnitMater(maxMaterNumSelect);
						
						if (CollectionUtils.isEmpty(unitList)) {
							break;
						}
						
						List<UnitForRecompileTargettedView> units = new LinkedList<UnitForRecompileTargettedView>();
						List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
						
						for (UnitForRecompileTargettedView unit : unitList) {
							Date chaTime = unitDao.findUnitChaTimeById(unit.getUserId(), unit.getId());
							if (null == chaTime){
								log.error("chatime is null from db[mater_id=" + unit.getId()
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
							unit.setChaTime(chaTime);
							
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
						
						if (CollectionUtils.isNotEmpty(requests)) {
							List<ResponseBaseMaterial> result = ubmcService.get(requests, false);
							
							if (CollectionUtils.isEmpty(result) || result.size() != units.size()) {
								log.error("resultInsert size[" + result.size()
										+ "] != request size[" + units.size() + "]");
							} else {
								for (int index = 0; index < units.size(); index++) {
									UnitForRecompileTargettedView unit = units.get(index);
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
									
									boolean xmlFlag = false;
									boolean tpIdFlag = false;
									boolean descJsonFlag = false;
									boolean drmcUrlFlag = false;
									boolean btnErrorFlag = false;
									
									// 分析xml标记
									String xmlMeta = ParseMC.extrateXml(data);
									if (StringUtils.isNotEmpty(xmlMeta)) {
										xmlFlag = true;
									}
									
									// 分析tpId标记
									Long tpId = null;
									int tpIdInt = ParseMC.getTpIdForSwf(data);
									if (tpIdInt > 0) {
										tpIdFlag = true;
										tpId = Long.valueOf(tpIdInt);
									} else {
										// 没有tpId时，使用tpId为null，调用admaker接口将赋予默认值
										tpIdFlag = false;
										tpId = null;
									}
									
									GrantResult grantResult = null;
									if (xmlFlag) {
										grantResult = recompileCreativeService.grantAuthorityForXmlMeta(xmlMeta, tpId);
									} else {
										// 分析descJson标记
										// tpId>0: 表明为admaker制作的物料，可以继续进行处理
										FlashDecoder decoder = new FlashDecoder();
										DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
										if (decodeResult != null && decodeResult.getStatus() == 0) {
											String descJson = decodeResult.getMessage();
											TemplateDescJsonVo jsonVo = null;
											
											// 如果descJson为空，则说明该物料存在问题，打印日志
											if (StringUtils.isEmpty(descJson)
													|| (jsonVo = gson.fromJson(descJson, TemplateDescJsonVo.class)) == null) {
												log.error("descJson from admaker material is empty[mater_id=" + unit.getId()
														+ ", userId=" + unit.getUserId() 
														+ ", mcId=" + unit.getMcId()
														+ ", versionId=" + unit.getMcVersionId() + "]");
												logCheckPrinter.error(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + 6);
												continue;
											} else {
												descJsonFlag = true;
												btnErrorFlag = this.isErrorBtnExist(jsonVo);
												
												if (StringUtils.isEmpty(response1.getAttribute())
														|| StringUtils.isEmpty(response1.getRefMcId())
														|| btnErrorFlag) {
													grantResult = recompileCreativeService.grantAuthority(descJson, tpId);
												}
											}
										}
										String drmcStr = ParseMC.parseDrmcFromSwf(data);
										if (StringUtils.isNotEmpty(drmcStr)) {
											drmcUrlFlag = true;
										}
									}
									
									if (grantResult != null) {
										if (grantResult.getStatusCode() == AmConstant.AM_GRANT_STATUS_RETRY_FAIL) {
											log.error("admaker grantAuthority retry three or more times failed[mater_id=" + unit.getId()
													+ ", userId=" + unit.getUserId() 
													+ ", mcId=" + unit.getMcId()
													+ ", versionId=" + unit.getMcVersionId() + "]");
											logCheckPrinter.error(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + 8);
											continue;
										} else if (grantResult.getStatusCode() == AmConstant.AM_GRANT_STATUS_FAIL) {
											log.error("result from admaker grantAuthority is failed[mater_id=" + unit.getId()
													+ ", userId=" + unit.getUserId() 
													+ ", mcId=" + unit.getMcId()
													+ ", versionId=" + unit.getMcVersionId() + "]");
											logCheckPrinter.error(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + 9);
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
											
											ResponseAdmakerMaterial responseFix = (ResponseAdmakerMaterial)unitInfoFixList.get(0);
											String fileSrcMd5 = responseFix.getFileSrcMd5();
											if (StringUtils.isNotEmpty(fileSrcMd5)) {
												// 考虑到ctr预估团队正在使用md5值，大批量的更新该字段，会对其实验模型造成一定影响，此次就不更新数据库中的md5
												// 这样，ubmc中的md5值，将会是最准确的；后续，通知ctr团队迁移使用ubmc中的md5值
//												int res = unitDao.updateUnitMd5(dbIndex, unit.getId(), fileSrcMd5, 
//														unit.getChaTime(), unit.getUserId());
												
												Date chaTime = unitDao.findUnitChaTimeById(unit.getUserId(), unit.getId());
												if (chaTime == null) {
													log.error("unit not exist, and chatime is null[mater_id=" + unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 11);
													continue;
												}
												
												if (chaTime.after(unit.getChaTime())) {
													log.error("unit has bean modified[mater_id=" 
															+ unit.getId()
															+ ", userId=" + unit.getUserId() 
															+ ", mcId=" + unit.getMcId()
															+ ", versionId=" + unit.getMcVersionId() + "]");
													logCheckPrinter.error(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + 12);
													continue;
												} else {
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
																+ "\t" + 13);
														continue;
													} else {
														logCheckPrinter.invalid(unit.getId()
																+ "\t" + unit.getUserId()
																+ "\t" + unit.getMcId()
																+ "\t" + unit.getMcVersionId());
													}
												}
											}
										} else {
											log.error("admaker grantAuthority failed[mater_id=" + unit.getId()
													+ ", userId=" + unit.getUserId() 
													+ ", mcId=" + unit.getMcId()
													+ ", versionId=" + unit.getMcVersionId() + "]");
											logCheckPrinter.error(unit.getId()
													+ "\t" + unit.getUserId()
													+ "\t" + unit.getMcId()
													+ "\t" + unit.getMcVersionId()
													+ "\t" + 14);
											continue;
										}
									}
									
									// 记录创意的标记
									logCheckPrinter.log(unit.getId()
											+ "\t" + unit.getUserId()
											+ "\t" + unit.getMcId()
											+ "\t" + unit.getMcVersionId()
											+ "\t" + DateUtils.getDateStr(unit.getChaTime())
											+ "\t" + xmlFlag
											+ "\t" + tpIdFlag
											+ "\t" + descJsonFlag
											+ "\t" + drmcUrlFlag
											+ "\t" + btnErrorFlag);
								}
							}
						}
						
						long time2 = System.currentTimeMillis();
						log.info("recompile targetted unit image mater in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
								+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
					} catch (Exception e) {
						log.error("recompile targetted unit image mater failed in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
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
			
			/**
			 * 判断descJson中，是否存在有问题的btn元素。<br>
			 * 规则：包含btn元素，其中graph_src属性含有drmc链接。
			 * @param jsonStr descJson
			 * @return 
			 */
			public boolean isErrorBtnExist(TemplateDescJsonVo descJson) {
				for (Map<String, String> eachElement : descJson.getElements()) {
					// 是否为按钮元素类型
					String elementType = eachElement.get("type");
					
					if ("btn".equals(elementType)) {
						String graphSrc = eachElement.get("graph_src");
						// 含有graph_src，并且为drmc url
						if (StringUtils.contains(graphSrc, "drmcmm.baidu.com")) {
							return true;
						}
					}
				}
				return false;
		 	}
			
		};
	}

	public RecompileCreativeService getRecompileCreativeService() {
		return recompileCreativeService;
	}

	public void setRecompileCreativeService(
			RecompileCreativeService recompileCreativeService) {
		this.recompileCreativeService = recompileCreativeService;
	}

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
