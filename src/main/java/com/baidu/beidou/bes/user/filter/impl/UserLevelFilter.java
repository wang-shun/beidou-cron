package com.baidu.beidou.bes.user.filter.impl;


import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.baidu.beidou.bes.user.filter.UserContext;
import com.baidu.beidou.bes.user.filter.UserFilter;
import com.baidu.beidou.bes.user.vo.UcUserView;
/**
 * 针对客户类型的过滤(暂未使用)
 * 例如某些adx只要KA用户信息
 * @author caichao
 */
public class UserLevelFilter implements UserFilter {

	@Override
	public void doFilter(UserContext context) {
		if (context == null || CollectionUtils.isEmpty(context.getUserList())) {
			return ;
		}
		
		List<UcUserView> users = context.getUserList();
		List<UcUserView> filterUsers = new ArrayList<UcUserView>(users.size());
//		for (UcUserView user : users){
//			if (user.getUserType() == 1) {
//				filterUsers.add(user);
//			}
//		}
		
		context.setUserList(filterUsers);
	}

}
