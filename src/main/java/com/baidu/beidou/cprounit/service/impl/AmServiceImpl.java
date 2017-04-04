package com.baidu.beidou.cprounit.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.mcdriver.AmDriverProxy;
import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.MaterSuiteResultPlus;
import com.baidu.beidou.cprounit.mcdriver.constant.AmConstant;
import com.baidu.beidou.cprounit.service.AmService;
import com.baidu.beidou.util.service.impl.BaseDrawinRpcServiceImpl;
import com.baidu.ubmc.bc.Text;

public class AmServiceImpl extends BaseDrawinRpcServiceImpl implements AmService {
	
	private static final Log LOG = LogFactory.getLog(AmServiceImpl.class);

	/** AM_TYPE */
	private int type;
	private int beidouUbmcAppId;
	private AmDriverProxy amDriverProxy;
	
	public AmServiceImpl(int type, int beidouUbmcAppId, String syscode, String prodid){
		super(syscode, prodid);
		this.type = type;
		this.beidouUbmcAppId = beidouUbmcAppId;
	}

	public String getInfo(final long key) {
		return amDriverProxy.getInfo(key, getHeaders());
	}

	public boolean delInfo(final long key) {
		return amDriverProxy.delInfo(key, getHeaders());
	}
	
	public MaterSuiteResultPlus getMixedList(int userId, int[] groupTypes, int page, int pageSize) {
		return amDriverProxy.getMixedList(userId, groupTypes, page, pageSize, getHeaders());
	}
	
	public boolean grantAuthorityByTextList(Long mcId, Integer versionId) {
		Text text = new Text(mcId, versionId);
		List<Text> list = new ArrayList<Text>();
		list.add(text);
		
		try {
			List<GrantResult> result = amDriverProxy.grantAuthorityByTextList(list, beidouUbmcAppId, false, getHeaders());
			if (result == null || result.isEmpty()) {
				return false;
			}
			GrantResult item = result.get(0);
			if (item == null) {
				return false;
			}
			Integer statusCode = item.getStatusCode();
			if (statusCode == null || statusCode != 0) {
				return false;
			}
		} catch (Exception e) {
			LOG.error("grantAuthority to beidou failed for mcId=" + mcId + ", versionId=" + versionId, e);
			return false;
		}
		
		
		return true;
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
				result = amDriverProxy.grantAuthority(descJson, tpId, beidouUbmcAppId, false, getHeaders());
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
	
	public byte[] downloadSwf(String descJson, Long tpId) {
		try {
			return amDriverProxy.downloadSwf(descJson, tpId, getHeaders());
		}  catch (Exception e) {
			LOG.error("download swf file from admaker failed for tpId=" + tpId + ", descJson=" + descJson, e);
			return null;
		}
	}
	
	/**
	 * downloadDrmcMaterial: 根据descJson和tpId生成drmc中物料
	 * 入参descJson： 通过admaker的jar包，解析出的swf文件描述json
	 * 入参tpid： 通过admaker提供的方法，解析出二进制中的tpid信息
	 * 返回：DRMC URL，任何异常都将返回null
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 24, 2013
	 */
	public String downloadDrmcMaterial(String descJson, Integer tpId) {
		try {
			return amDriverProxy.downloadDrmcMaterial(descJson, tpId, getHeaders());
		}  catch (Exception e) {
			LOG.error("get drmc material url from admaker failed for tpId=" + tpId + ", descJson=" + descJson, e);
			return null;
		}
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBeidouUbmcAppId() {
		return beidouUbmcAppId;
	}

	public void setBeidouUbmcAppId(int beidouUbmcAppId) {
		this.beidouUbmcAppId = beidouUbmcAppId;
	}

	public AmDriverProxy getAmDriverProxy() {
		return amDriverProxy;
	}

	public void setAmDriverProxy(AmDriverProxy amDriverProxy) {
		this.amDriverProxy = amDriverProxy;
	}

}
