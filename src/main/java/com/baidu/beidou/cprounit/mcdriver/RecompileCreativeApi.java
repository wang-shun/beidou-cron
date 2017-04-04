package com.baidu.beidou.cprounit.mcdriver;

import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;

/**
 * ClassName: RecompileCreativeApi
 * Function: 将DRMC物料刷新为UBMC物料
 *
 * @author genglei
 * @version cpweb-567
 * @date Nov 5, 2013
 */
public interface RecompileCreativeApi {
	
	/**
	 * grantAuthority: 重新编译物料
	 * 
	 * descJson: admaker物料中，从二进制文件中解析出的
	 * tpId：admaker物料中的模板id
	 * appId	Integer	待授权方appId	必选。
	 * onlyMedias	Boolean	是否只授予富媒体的引用权限	必选。
	 * 
	 * GrantResult格式
	 * 		statusCode:  0 标识成功，其余标识错误。
	 * 		message:  成功返回success。失败返回具体错误信息。
	 * 		mcId: [0-9]+
	 * 		versionId: [0-9]+
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 1, 2013
	 */
	GrantResult recompile(String descJson, Long tpId, 
			Integer appId, Boolean onlyMedias);
	
	/**
	 * 根据tpid,xml，重新编译1.0版本物料，返回物料的ubmc信息
	 * 
	 * xmlMeta: admaker旧xml编译物料中，从二进制文件中解析出的
	 * tpId：admaker物料中的模板id
	 * appId	Integer	待授权方appId	必选。
	 * onlyMedias	Boolean	是否只授予富媒体的引用权限	必选。
	 * 
	 * GrantResult格式
	 * 		statusCode:  0 标识成功，其余标识错误。
	 * 		message:  成功返回success。失败返回具体错误信息。
	 * 		mcId: [0-9]+
	 * 		versionId: [0-9]+
	 * 
	 * @param xmlMeta xmlMeta
	 * @param tpid 创意tpid
	 * @param 授权UBMC appid
	 * @param onlyMedias 授权方式
	 * @return GrantAuthorityResultVo
	 */
	public GrantResult recompileXml(String xmlMeta, Long tpId, 
			Integer appId, Boolean onlyMedias);
}
