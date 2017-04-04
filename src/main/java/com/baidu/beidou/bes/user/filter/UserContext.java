package com.baidu.beidou.bes.user.filter;

import java.util.List;

import com.baidu.beidou.bes.user.vo.UcUserView;

/**
 * 批量广告主信息过滤
 * 
 * @author caichao
 */
public class UserContext {
	/**
	 * 用户信息集合
	 */
	List<UcUserView> userList;
	
	public UserContext(List<UcUserView> userList){
		this.userList = userList;
	}

	public List<UcUserView> getUserList() {
		return userList;
	}

	public void setUserList(List<UcUserView> userList) {
		this.userList = userList;
	}
	
	
}
