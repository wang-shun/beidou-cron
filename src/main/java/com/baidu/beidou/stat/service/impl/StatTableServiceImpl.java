package com.baidu.beidou.stat.service.impl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprogroup.dao.CproGroupDao;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.stat.service.StatTableService;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

public class StatTableServiceImpl implements StatTableService {

	private static final int REPEATEGROUP_PAGE_SIZE = 1000;
	private static final Log log = LogFactory.getLog(StatTableServiceImpl.class);

	private String outputFileName;

	private UnitDao unitDao;
	private CproGroupDao cproGroupDao;
	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;

	/*
	 * 输出全流量投放的广告id
	 */
	public void outputAllSiteUnitId(Date date) {

		log.info("begin task: outputAllSiteUnitId");

		BufferedWriter bufferWriter = null;

		try {
			OutputStreamWriter op = new OutputStreamWriter(new FileOutputStream(this.outputFileName), "gbk");
			bufferWriter = new BufferedWriter(op);
			for (int sharding = 0; sharding < MultiDataSourceDaoImpl.shardingNum; sharding++) {
				outputBySharding(sharding, bufferWriter);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (bufferWriter != null) {
				try {
					bufferWriter.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		log.info("end task: outputAllSiteUnitId");
	}

	public void outputBySharding(int sharding, BufferedWriter bufferedWriter) throws IOException {

		Long allGroupSum = cproGroupDaoOnMultiDataSource.countAllGroupIdofEffPlan(sharding);

		int pageSize = allGroupSum.intValue() / REPEATEGROUP_PAGE_SIZE;

		log.info("task: outputAllSiteUnitId pagesize == " + pageSize);

		for (int i = 0; i <= pageSize; i++) {

			log.info("task id : " + i);
			// 获得分页的推广组个数
			List<Integer> groupIdList = cproGroupDaoOnMultiDataSource.findAllGroupIdofEffPlan(sharding, i, REPEATEGROUP_PAGE_SIZE);
			// 过滤设置全网投放的
			List<Integer> groupIdAllSiteList = cproGroupDaoOnMultiDataSource.filterGroupByAllSite(groupIdList);
			// 获得对应的创意IDs
			List<Long> unitIds = unitDao.getAllUnitIdsByGroupId(groupIdAllSiteList);

			for (Long unitId : unitIds) {
				bufferedWriter.write(unitId.toString());
				bufferedWriter.newLine();
			}
		}
	}

	public void setFulladName(String name) {
		this.setOutputFileName(name);
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public CproGroupDao getCproGroupDao() {
		return cproGroupDao;
	}

	public void setCproGroupDao(CproGroupDao cproGroupDao) {
		this.cproGroupDao = cproGroupDao;
	}

	public UnitDao getUnitDao() {
		return unitDao;
	}

	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}

}
