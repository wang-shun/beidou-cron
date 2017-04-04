package com.baidu.beidou.cprounit.service;

import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.MaterSuiteResultPlus;

/**
 * 挂接admaker所需服务
 * 
 * @author yanjie
 * @version 1.2.3
 */
public interface AmService {
	
	// 查询admaker固定类型的物料
	int AM_GROUP_FIXED = 1;
	
	// 查询admaker悬浮类型的物料
	int AM_GROUP_FLOW = 2;
	
	// 查询admaker贴片类型的物料
	int AM_GROUP_FILM = 3;
	
	
	// 跳转到admaker的固定模板页面的id
	int AM_TEMPLATE_FIXED = 1;
	
	// 跳转到admaker的悬浮模板页面的id
	int AM_TEMPLATE_FLOW = 2;
	
	// 跳转到admaker的贴片模板页面的id
	int AM_TEMPLATE_FILM = 4;
	
	/**
	 * 从信息池获取指定key的info信息
	 * @param key 由drmc/admaker生成，对应于info信息
	 * @return 
	 * beidou传递给drmc/admaker的加密info信息
	 * 若异常则返回empty string
	 */
	String getInfo(long key);
	/**
	 * 从信息池删除指定key的info信息
	 * @param key 由drmc/admaker生成，对应于info信息
	 */
	boolean delInfo(long key);
	
	/**
	 * 增加分页和类别的过滤
	 * 
	 * @param userId  用户的id
	 * @param groupTypes  admaker推广组的类型。！！注意这里和北斗不一样，admaker内部的物料类型是：1-固定,2-悬浮,3-贴片
	 * @param page  获取创意列表的第几页（默认从1开始）
	 * @param pageSize 当前列表每页创意数量
	 */
	MaterSuiteResultPlus getMixedList(int userId, int[] groupTypes, int page, int pageSize);
	
	/**
	 * grantAuthorityByTextList: 通过mcId/versionId授权接口
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	public boolean grantAuthorityByTextList(Long mcId, Integer versionId);
	
	/**
	 * grantAuthority: 通过descJson/tpId授权
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	public GrantResult grantAuthority(String descJson, Long tpId);
	
	/**
	 * downloadSwf: 通过descJson/tpId调用admaker接口重新生成swf文件
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	public byte[] downloadSwf(String descJson, Long tpId);
	
	/**
	 * downloadDrmcMaterial: 根据descJson和tpId生成drmc中物料
	 * 入参descJson： 通过admaker的jar包，解析出的swf文件描述json
	 * 入参tpid： 通过admaker提供的方法，解析出二进制中的tpid信息
	 * 返回：DRMC URL，任何异常都将返回null
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 24, 2013
	 */
	public String downloadDrmcMaterial(String descJson, Integer tpId);
}

