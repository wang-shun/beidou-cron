package com.baidu.beidou.accountmove.ubmc.util.rpc;

import java.lang.reflect.Method;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.util.EncodingUtil;

import com.baidu.gson.JsonElement;
import com.baidu.rpc.exception.ExceptionHandler;
import com.baidu.rpc.exception.ParseErrorException;


/**
 * mcpack2远程调用代理类，相对于基类添加了向Header中加入属性和增加base密码验证的功能、
 * 
 * @author zengyunfeng
 * @since 1.2.18
 */
public class McpackRpcProxyWithAuthenticator extends McpackRpcProxyWithHeaderProperty {
	private String _basicAuth;	///授权信息
	public static final String WWW_AUTH_RPC = "Authorization";
	
	@Override
	public Object clone() {
		McpackRpcProxyWithAuthenticator result = new McpackRpcProxyWithAuthenticator(url, encoding, exceptionHandler);
		result.setHeaderProperties(headerProperties);
		result._basicAuth = _basicAuth;
		return result;
	}
	
	/**
	 * @param url
	 *            服务的url
	 * @param encoding
	 *            编码
	 * @param exp
	 *            异常处理器
	 */
	public McpackRpcProxyWithAuthenticator(String url, String encoding, ExceptionHandler exp) {
		super(url, encoding, exp);
	}
	
	/**
	 * @param url
	 *            服务的url
	 * @param encoding
	 *            编码
	 * @param exp
	 *            异常处理器
	 */
	public McpackRpcProxyWithAuthenticator(String url, String encoding, String username, String password, ExceptionHandler exp) {
		super(url, encoding, exp);
		_basicAuth ="Basic " + EncodingUtil.getAsciiString(
                Base64.encodeBase64(EncodingUtil.getBytes(username + ":" + password, encoding)));
	}

	@Override
	protected Object parseResult(int id, JsonElement ele, Method method)
			throws Exception {
		if(LOG.isDebugEnabled()){
			LOG.debug("head="+headerProperties+" response="+ele);
		}
		return super.parseResult(id, ele, method);
	}
	
	@Override
	protected JsonElement makeRequest(int id, Method method, Object[] args)
			throws ParseErrorException {
		JsonElement result = super.makeRequest(id, method, args);
		if(LOG.isDebugEnabled()){
			LOG.debug("head="+headerProperties+" request="+result);
		}
		return result;
	}
}
