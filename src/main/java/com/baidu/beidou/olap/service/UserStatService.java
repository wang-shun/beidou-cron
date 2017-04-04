package com.baidu.beidou.olap.service;

import java.util.Date;
import java.util.List;

import com.baidu.beidou.cache.bo.UserStatInfo;


public interface UserStatService {
	
	List<UserStatInfo> queryUsersData(int userId, Date from, Date to,  
    		int timeUnit);
}
