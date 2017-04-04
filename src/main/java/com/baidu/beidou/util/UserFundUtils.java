package com.baidu.beidou.util;

import java.util.Map;
import java.util.Set;

import com.baidu.beidou.user.bo.User;

public class UserFundUtils {
	public static User transToUser(Map map){
		User user = new User();
		Set keySet = map.keySet();
		if(keySet.contains("id")){
			if(map.get("id")!=null){
				user.setId(Integer.valueOf((map.get("id").toString())));
			}
		}
		if(keySet.contains("userid")){
			if(map.get("userid")!=null){
				user.setUserid(Integer.valueOf((map.get("userid").toString())));
			}
		}
		if(keySet.contains("ushifenstatid")){
			if(map.get("ushifenstatid")!=null){
				user.setUshifenstatid(Integer.valueOf((map.get("ushifenstatid").toString())));
			}
		}
		if(keySet.contains("ustate")){
			if(map.get("ustate")!=null){
				user.setUstate(Integer.valueOf((map.get("ustate").toString())));
			}
		}
		if(keySet.contains("balancestat")){
			if(map.get("balancestat")!=null){
				user.setBalancestat(Integer.valueOf((map.get("balancestat").toString())));
			}
		}
		if(keySet.contains("username")){
			if(map.get("username")!=null){
				user.setUsername((map.get("username").toString()));
			}
		}
		return user;
	}
}
