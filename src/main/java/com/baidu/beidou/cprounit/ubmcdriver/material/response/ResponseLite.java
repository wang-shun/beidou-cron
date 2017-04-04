package com.baidu.beidou.cprounit.ubmcdriver.material.response;

/**
 * ClassName: ResponseLite
 * Function: 用于输入参数无需具体物料信息的场景，如ubmc中remove等接口
 *
 * @author genglei
 * @version cpweb-587
 * @date May 10, 2013
 */
public class ResponseLite extends ResponseBaseMaterial {

	public ResponseLite(Long mcId, Integer versionId) {
		this.mcId = mcId;
		this.versionId = versionId;
	}
	
}
