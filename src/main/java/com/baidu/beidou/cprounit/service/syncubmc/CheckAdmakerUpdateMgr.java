package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
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

import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.AmDataService;
import com.baidu.beidou.cprounit.service.CproUnitWriteMgr;
import com.baidu.beidou.cprounit.service.DrmcService;
import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial2;
import com.baidu.beidou.cprounit.service.bo.response.ResponseImageMaterial;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForAdmakerFixSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitForAdmakerFixView;
import com.baidu.beidou.util.ThreadContext;
import com.baidu.chuangyi.flash.decode.DecodeResult;
import com.baidu.chuangyi.flash.decode.FlashDecoder;

/**
 * ClassName: CheckUnitAllMgr
 * Function: 校验beidou.cprounitmater[0-7]中所有物料，查看ubmc是否存在该物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckAdmakerUpdateMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckAdmakerUpdateMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int maxMaterNumSelect = 0;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitForAdmakerFixSet unitAdmakerMaterSet = null;
	
	private DrmcService drmcService;
	private AmDataService amDataService;
	private CproUnitWriteMgr unitWriteMgr;
	
	private String errorAdmakerUrl;
	private String errorUbmcTmpUrl;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to check all unit flash mater for admaker in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
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
		log.info("check all unit flash mater for admaker in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitAdmakerMaterSet.getTotal());
		
		unitAdmakerMaterSet.closeFile();
		log.info("end to check all unit flash mater for admaker in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
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
						
						int bugMaterialNum = 0;
						for (UnitForAdmakerFixView unit : unitList) {
							String dbFileSrc = unit.getFileSrc();
							if (StringUtils.isEmpty(dbFileSrc)) {
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
							
							String dbDrmcUrl = CproUnitConfig.DRMC_MATPREFIX + dbFileSrc;
							byte[] data = this.getImageFromDrmc(dbDrmcUrl);
//							byte[] data = this.getImageData("drmc-ubmc.swf");
							if (null == data){
								log.error("data from drmc is empty[mater_id=" + unit.getId()
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
									
									// 如果包含ubmc临时url或者admaker限次url，则认为该物料为有问题物料，需要进行修复
									boolean bugFlag = false;
									int ubmcUrlIndex = descJson.indexOf(errorUbmcTmpUrl);
									int admakerUrlIndex = descJson.indexOf(errorAdmakerUrl);
									if (ubmcUrlIndex >= 0) {
										log.error("descJson from admaker material has ubmc temp url[mater_id=" + unit.getId()
												+ ", fileSrc=" + unit.getFileSrc()    
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										bugFlag = true;
										bugMaterialNum++;
									} else if (admakerUrlIndex >= 0) {
										log.error("descJson from admaker material has admaker time url[mater_id=" + unit.getId()
												+ ", fileSrc=" + unit.getFileSrc()    
												+ ", userId=" + unit.getUserId() 
												+ ", mcId=" + unit.getMcId()
												+ ", versionId=" + unit.getMcVersionId() + "]");
										
										bugFlag = true;
										bugMaterialNum++;
									}
									
									if (bugFlag) {
										String drmcUrl = amDataService.downloadDrmcMaterial(descJson, tpId);
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
										
										Long unitId = unit.getId();
										Integer userId = unit.getUserId();
										Integer state = unit.getState();
										Date chaTime = unit.getChaTime();
										String title = unit.getTitle();
										String showUrl = unit.getShowUrl();
										String targetUrl = unit.getTargetUrl();
										String wirelessShowUrl = unit.getWirelessShowUrl();
										String wirelessTargetUrl = unit.getWirelessTargetUrl();
										Integer width = unit.getWidth();
										Integer height = unit.getHeight();
										
										List<BeidouMaterialBase> ads = new ArrayList<BeidouMaterialBase>();
										RequestImageMaterial2 flash = new RequestImageMaterial2(title, showUrl, targetUrl,
												drmcUrl, width, height, wirelessShowUrl, wirelessTargetUrl);
										ads.add(flash);
										List<BeidouMaterialBase> drmcResult = drmcService.tmpInsertBatch(ads, false);
										
										Long wid = 0L;
										Long fwid = 0L;
										String fileSrc = null;
										if (CollectionUtils.isNotEmpty(drmcResult) && (drmcResult.get(0) != null)
												&& drmcResult.get(0) instanceof ResponseImageMaterial) {
											// 设置userId至context中
											ThreadContext.putUserId(userId);
											
											ResponseImageMaterial adFromDrmc = (ResponseImageMaterial)drmcResult.get(0);
											wid = adFromDrmc.getMcid();
											fileSrc = adFromDrmc.getFileSrc();
											
											// 如果创意状态为有效或者暂停，需要将物料激活至正式桶
											if (state == CproUnitConstant.UNIT_STATE_NORMAL
													|| state == CproUnitConstant.UNIT_STATE_PAUSE) {
												long[] tmpids = new long[1];
												long[] fmids = new long[1];
												tmpids[0] = wid;
												fmids[0] = 0;
												int size = tmpids.length;
												BeidouMaterialBase[] activeResult = drmcService.tmpActiveBatch(tmpids, ArrayUtils.subarray(fmids, 0, size), true, false);
												
												if (!ArrayUtils.isEmpty(activeResult) && activeResult[0] != null
														&& activeResult[0] instanceof ResponseImageMaterial) {
													ResponseImageMaterial response = (ResponseImageMaterial) activeResult[0];
													fileSrc = response.getFileSrc();
													fwid = response.getMcid();
													
													unitWriteMgr.updateUnitInfo(userId, unitId, fileSrc, fwid, chaTime, state);
													logCheckPrinter.invalid(unit.getId()
															+ "\t" + unit.getUserId()
															+ "\t" + unit.getMcId()
															+ "\t" + unit.getMcVersionId()
															+ "\t" + unit.getState());
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
												}
											} else {
												unitWriteMgr.updateUnitInfo(userId, unitId, fileSrc, wid, chaTime, state);
												logCheckPrinter.invalid(unit.getId()
														+ "\t" + unit.getUserId()
														+ "\t" + unit.getMcId()
														+ "\t" + unit.getMcVersionId()
														+ "\t" + unit.getState());
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
			
			private byte[] getImageData(String fileName) {
				byte[] data = null;
				fileName = getImageFilePath(fileName);
				FileInputStream input = null;
				File tmpFile = new File(fileName);
				try {
					long fileSize = tmpFile.length();
					data = new byte[(int) fileSize];
					input = new FileInputStream(tmpFile);
					input.read(data);
					input.close();
				} catch (IOException e) {
					System.out.println("image data not found");
					data = null;
				}
				
				return data;
			}
			
			private String getImageFilePath(String fileName) {
				return new File("").getAbsolutePath() + "\\src\\test\\java\\com\\baidu\\beidou\\cprounit\\images\\" + fileName;
			}
		};
	}
	public DrmcService getDrmcService() {
		return drmcService;
	}

	public void setDrmcService(DrmcService drmcService) {
		this.drmcService = drmcService;
	}

	public AmDataService getAmDataService() {
		return amDataService;
	}

	public void setAmDataService(AmDataService amDataService) {
		this.amDataService = amDataService;
	}

	public CproUnitWriteMgr getUnitWriteMgr() {
		return unitWriteMgr;
	}

	public void setUnitWriteMgr(CproUnitWriteMgr unitWriteMgr) {
		this.unitWriteMgr = unitWriteMgr;
	}

	public String getErrorAdmakerUrl() {
		return errorAdmakerUrl;
	}

	public void setErrorAdmakerUrl(String errorAdmakerUrl) {
		this.errorAdmakerUrl = errorAdmakerUrl;
	}

	public String getErrorUbmcTmpUrl() {
		return errorUbmcTmpUrl;
	}

	public void setErrorUbmcTmpUrl(String errorUbmcTmpUrl) {
		this.errorUbmcTmpUrl = errorUbmcTmpUrl;
	}
}
