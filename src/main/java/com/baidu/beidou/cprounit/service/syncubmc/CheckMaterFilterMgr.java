package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.UnitBeanUtils;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitSet;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestSmartUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
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
public class CheckMaterFilterMgr extends SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(CheckMaterFilterMgr.class);
	
	private LogCheckPrinter logCheckPrinter = null;
	private int dbIndex = 0;
	private int dbSlice = 0;
	private UnitSet unitImageMaterSet = null;
	
	private UnitDao unitDao;
	
	public void checkMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, 
			PrintWriter logWriter, PrintWriter invalidWriter, String dbFileName, 
			int dbIndex, int dbSlice) {
		
		log.info("begin to filter material special char in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
		this.dbIndex = dbIndex;
		this.dbSlice = dbSlice;
		
		unitImageMaterSet = new UnitSet(dbFileName);
		
		// 多线程处理
		log.info("begin to work");
		long time1 = System.currentTimeMillis();
		
		this.filterChar();
		
		long time2 = System.currentTimeMillis();
		log.info("filter material special char in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice
				+ ", use " + (time2 - time1) + " ms, unitTotalNum=" + unitImageMaterSet.getTotal());
		
		unitImageMaterSet.closeFile();
		log.info("end to filter material special char in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
	}
	
	private void filterChar() {
		try {
			long time1 = System.currentTimeMillis();
			List<UnitMaterView> unitList = unitDao.findToFilterUnit(dbIndex, dbSlice);
			
			if (CollectionUtils.isEmpty(unitList)) {
				logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=0], [checkNum=0]");
				return;
			}
			
			int specialNum = 0;
			int updateNum = 0;
			for (UnitMaterView unit : unitList) {
				if (!UnitBeanUtils.hasSpecialChar(unit)) {
					// 如果不包含特殊字符，则不作处理
					continue;
				}
				
				List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
				RequestBaseMaterial request = null;
				Long mcId = unit.getMcId();
				Integer mcVersionId = unit.getMcVersionId();
				
				if (mcId != null && mcId > 0) {
					request = new RequestLite(mcId, mcVersionId);
					requests.add(request);
					specialNum++;
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
				
				List<ResponseBaseMaterial> result = ubmcService.get(requests, false);
				if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
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
				
				ResponseBaseMaterial response = result.get(0);
				if (!(response instanceof ResponseSmartUnit)) {
					log.error("unit from ubmc is not smart[id=" + unit.getId()
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
				
				// 过滤特殊字符
				UnitBeanUtils.filterSpecialChar(unit);
				
				List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
				if (response instanceof ResponseSmartUnit) {
					ResponseSmartUnit response1 = (ResponseSmartUnit) response;
					
					request = new RequestSmartUnit(unit.getMcId(), unit.getMcVersionId(), 
							response1.getWuliaoType(), unit.getShowUrl(), 
							unit.getTargetUrl(), unit.getWirelessShowUrl(), unit.getWirelessTargetUrl(), 
							response1.getWidth(), response1.getHeight(), response1.getTemplateId());
					requestUpdates.add(request);
				}
				
				List<UnitMaterView> latestUnitList = unitDao.findUnitWithSpecialChar(dbIndex, unit.getId(), unit.getUserId());
				if (CollectionUtils.isEmpty(latestUnitList) 
						|| latestUnitList.get(0) == null) {
					log.error("unit not exist[mater_id=" + unit.getId()
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
				
				UnitMaterView latestUnit = latestUnitList.get(0);
				if (latestUnit.getChaTime().after(unit.getChaTime())) {
					log.error("unit has bean modified[mater_id=" + unit.getId()
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
				
				List<ResponseBaseMaterial> resultUpdate = ubmcService.update(requestUpdates);
				if (CollectionUtils.isEmpty(resultUpdate) || resultUpdate.get(0) == null) {
					log.error("update material from ubmc failed[mater_id=" + unit.getId()
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
				
				unitDao.updateUnitFilterSpecialChar(dbIndex, unit);
				logCheckPrinter.invalid(unit.getId() + "\t" + unit.getUserId() 
						+ "\t" + unit.getMcId() + "\t" + unit.getMcVersionId());
				updateNum++;
			}
			
			logCheckPrinter.log("[INFO]index=" + dbIndex + ", unitList.size=" + unitList.size() 
					+ ", specialNum=" + specialNum + ", updateNum=" + updateNum);
			
			long time2 = System.currentTimeMillis();
			log.info("filter material special char in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice
					+ ", use " + (time2 - time1) + " ms, unitNum=" + unitList.size());
		} catch (Exception e) {
			log.error("filter material special char failed in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice, e);
		}
	}

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}
}
