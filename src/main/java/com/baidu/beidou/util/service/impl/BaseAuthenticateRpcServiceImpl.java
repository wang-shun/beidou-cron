package com.baidu.beidou.util.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.lang.StringUtils;


/**
 * <p>ClassName:BaseAuthenticateRpcServiceImpl,RPC服务基类
 * <p>Function: 封装RPC服务用到的基本方法和属性
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-12-27
 * @since    Cpweb-221,RPC优化
 * @version  $Id: Exp $
 */
public abstract class BaseAuthenticateRpcServiceImpl extends BaseRpcServiceImpl {

	/** 默认编码方式 */
	public static final String DEFAULT_ENCODING = "UTF-8";
	/** 授权信息 */
	private String _basicAuth;	
	/** 授权信息KEY */
	public static final String WWW_AUTH_RPC = "Authorization";
	/** 用户名 */
	protected String userName;
	/** 密码 */
	protected String password;
	/** 编码 */
	protected String encoding;
	
	public BaseAuthenticateRpcServiceImpl(String userName, String password) {
		this(userName, password, DEFAULT_ENCODING);
	}

	public BaseAuthenticateRpcServiceImpl(String userName, String password, String encoding) {
		this.userName = userName;
		this.password = password;
		if(StringUtils.isEmpty(encoding)) {
			this.encoding = DEFAULT_ENCODING;
		} else {
			this.encoding = encoding;
		}
		if(!StringUtils.isEmpty(userName)) {
			_basicAuth = "Basic " + EncodingUtil.getAsciiString(
	                Base64.encodeBase64(EncodingUtil.getBytes(userName + ":" + password, this.encoding)));
		}
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
		if(!StringUtils.isEmpty(userName)) {
			_basicAuth = "Basic " + EncodingUtil.getAsciiString(
	                Base64.encodeBase64(EncodingUtil.getBytes(userName + ":" + password, this.encoding)));
		}
	}
	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(_basicAuth)) {
			headers.put(WWW_AUTH_RPC, _basicAuth);
		}
		return headers;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		if(!StringUtils.isEmpty(userName)) {
			_basicAuth = "Basic " + EncodingUtil.getAsciiString(
	                Base64.encodeBase64(EncodingUtil.getBytes(userName + ":" + password, this.encoding)));
		}
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String get_basicAuth() {
		return _basicAuth;
	}
	
	
}
