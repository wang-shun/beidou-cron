package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request;

/**
 * ClassName: RequestLite
 * Function: 用于输入参数无需具体物料信息的场景，如ubmc中remove/copy/get等接口
 *
 * @author genglei
 * @version cpweb-587
 * @date May 9, 2013
 */
public class RequestLite extends RequestBaseMaterial {

	public RequestLite(Long mcId, Integer versionId) {
		super(mcId, versionId);
	}

	public String tranformToValueString() {
		return "";
	}

}
