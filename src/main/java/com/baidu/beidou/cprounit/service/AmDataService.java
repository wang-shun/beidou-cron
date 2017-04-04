package com.baidu.beidou.cprounit.service;

/**
 * 挂接admaker所需服务
 * 
 * @author yanjie
 * @version 1.2.3
 */
public interface AmDataService {
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

