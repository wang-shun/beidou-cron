package com.baidu.beidou.olap.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.baidu.beidou.cache.bo.UserStatInfo;
import com.baidu.unbiz.olap.constant.OlapConstants;
import com.baidu.beidou.olap.service.UserStatService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-olap-test.xml")
public class UserStatServiceTest{ 

	
	@Resource(name="userStatServiceImpl")
	UserStatService service;
	
	@Test
	public void testQueryAUserData(){
		Integer userid = 5;
		Date from = new Date();
		from.setTime(1402333200000L);  //2014-6-10
		Date to = new Date();
		to.setTime(1403370000000L);   //2014-6-22

		List<UserStatInfo> data = service.queryUsersData(userid, from, to, OlapConstants.TU_NONE);

		for(UserStatInfo item : data){
			System.out.println(item.toString());
		}
	}
}
