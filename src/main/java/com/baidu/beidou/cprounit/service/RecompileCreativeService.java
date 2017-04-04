package com.baidu.beidou.cprounit.service;

import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;

/**
 * ClassName: RecompileCreativeService
 * Function: 将DRMC物料刷新为UBMC物料
 *
 * @author genglei
 * @version cpweb-567
 * @date Nov 5, 2013
 */
public interface RecompileCreativeService {
	
	/**
	 * grantAuthority: 
	 * 结果返回GrantResult，必定不为null，含有相应的statusCode
	 * statusCode: 
	 * 		0：正常，包含正确的mcId和versiondId
	 * 		1: 需重试
	 * 		2: 3次重试失败，则创意创建失败
	 * 		-1：失败，说明该创意并不需要admaker进行处理，走北斗正常上传逻辑分支即可
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 6, 2013
	 */
	public GrantResult grantAuthority(String descJson, Long tpId);
	
	/**
	 * grantAuthorityForXmlMeta: 
	 * 结果返回GrantResult，必定不为null，含有相应的statusCode
	 * statusCode: 
	 * 		0：正常，包含正确的mcId和versiondId
	 * 		1: 需重试
	 * 		2: 3次重试失败，则创意创建失败
	 * 		-1：失败，说明该创意并不需要admaker进行处理，走北斗正常上传逻辑分支即可
	 * @version cpweb-679
	 * @author genglei01
	 * @date Aug 6, 2013
	 */
	public GrantResult grantAuthorityForXmlMeta(String xmlMeta, Long tpId);
}
