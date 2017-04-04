package com.baidu.beidou.cprounit.service.impl;

import com.baidu.beidou.cprounit.mcdriver.RecompileCreativeApiProxy;
import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.constant.AmConstant;
import com.baidu.beidou.cprounit.service.RecompileCreativeService;
import com.baidu.beidou.util.service.impl.BaseDrawinRpcServiceImpl;

public class RecompileCreativeServiceImpl extends BaseDrawinRpcServiceImpl 
		implements RecompileCreativeService {
	
	private int beidouUbmcAppId;
	
	private RecompileCreativeApiProxy recompileCreativeProxy;
	
	public RecompileCreativeServiceImpl(int beidouUbmcAppId, String syscode, String prodid){
		super(syscode, prodid);
		this.beidouUbmcAppId = beidouUbmcAppId;
	}

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
	public GrantResult grantAuthority(String descJson, Long tpId) {
		GrantResult result = null;
		for (int i = 0; i < AmConstant.AM_GRANT_RETRY_TIMES; i++) {
			try {
				result = recompileCreativeProxy.recompile(descJson, tpId, beidouUbmcAppId, false, getHeaders());
				// 如果result为null，（虽然不可能为null）或者result需重试
				if (result == null || result.getStatusCode() == AmConstant.AM_GRANT_STATUS_RETRY) {
					continue;
				}
				
				// 如果statusCode为正常或者失败，则跳出重试
				if (result.getStatusCode() == AmConstant.AM_GRANT_STATUS_OK
						|| result.getStatusCode() == AmConstant.AM_GRANT_STATUS_FAIL) {
					break;
				}
				
				return result;
			} catch (Exception e) {
				LOG.error("grantAuthority to beidou failed for tpId=" + tpId + ", descJson=" + descJson, e);
			}
		}
		
		// 如果重试3次后，result依然为null，那么走此分支
		if (result == null || result.getStatusCode() == AmConstant.AM_GRANT_STATUS_RETRY) {
			result = new GrantResult();
			result.setStatusCode(AmConstant.AM_GRANT_STATUS_RETRY_FAIL);
		}
		
		return result;
	}
	
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
	public GrantResult grantAuthorityForXmlMeta(String xmlMeta, Long tpId) {
		GrantResult result = null;
		for (int i = 0; i < AmConstant.AM_GRANT_RETRY_TIMES; i++) {
			try {
				result = recompileCreativeProxy.recompileXml(xmlMeta, tpId, beidouUbmcAppId, false, getHeaders());
				// 如果result为null，（虽然不可能为null）或者result需重试
				if (result == null || result.getStatusCode() == AmConstant.AM_GRANT_STATUS_RETRY) {
					continue;
				}
				
				// 如果statusCode为正常或者失败，则跳出重试
				if (result.getStatusCode() == AmConstant.AM_GRANT_STATUS_OK
						|| result.getStatusCode() == AmConstant.AM_GRANT_STATUS_FAIL) {
					break;
				}
				
				return result;
			} catch (Exception e) {
				LOG.error("grantAuthority to beidou failed for tpId=" + tpId + ", xmlMeta=" + xmlMeta, e);
			}
		}
		
		// 如果重试3次后，result依然为null，那么走此分支
		if (result == null || result.getStatusCode() == AmConstant.AM_GRANT_STATUS_RETRY) {
			result = new GrantResult();
			result.setStatusCode(AmConstant.AM_GRANT_STATUS_RETRY_FAIL);
		}
		
		return result;
	}

	public int getBeidouUbmcAppId() {
		return beidouUbmcAppId;
	}

	public void setBeidouUbmcAppId(int beidouUbmcAppId) {
		this.beidouUbmcAppId = beidouUbmcAppId;
	}

	public RecompileCreativeApiProxy getRecompileCreativeProxy() {
		return recompileCreativeProxy;
	}

	public void setRecompileCreativeProxy(
			RecompileCreativeApiProxy recompileCreativeProxy) {
		this.recompileCreativeProxy = recompileCreativeProxy;
	}
}
