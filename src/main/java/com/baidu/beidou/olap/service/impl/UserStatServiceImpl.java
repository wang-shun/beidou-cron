package com.baidu.beidou.olap.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baidu.beidou.cache.bo.UserStatInfo;
import com.baidu.beidou.olap.service.UserStatService;
import com.baidu.unbiz.olap.driver.bo.ReportRequest;
import com.baidu.unbiz.olap.driver.bo.ReportRequestBuilder;
import com.baidu.unbiz.olap.obj.DateTriple;
import com.baidu.unbiz.olap.service.AbstractOlapService;


@Service
public class UserStatServiceImpl extends AbstractOlapService implements
		UserStatService {
	@Value("${olap.start.year}")
	protected int startYear=2008; 
	
	@Value("${olap.start.month}")
	protected int startMonth=11; 
	
	@Value("${olap.start.day}")
	protected int startDay=13;
	
	/**
	 * OlapEngine表起始可查时间三元组
	 */
	private final DateTriple dt = new DateTriple(startYear, startMonth, startDay);
	// 数据入doris年月日，之前的数据不提供查询
	@Override
	protected DateTriple getStartDate() {
		return dt;
	}
	
	@Override
	public List<UserStatInfo> queryUsersData(int userId, Date from, Date to,
			 int timeUnit) {
		ReportRequest<UserStatInfo> rr = new ReportRequestBuilder<UserStatInfo>() {}
				.setUserId(userId)
				.setFrom(from)
				.setTo(to)
				.setTimeUnit(timeUnit)
				.build();

		return super.getStorageData(rr);
	}

}
