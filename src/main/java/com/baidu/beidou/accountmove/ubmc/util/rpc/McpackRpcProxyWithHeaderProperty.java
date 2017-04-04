package com.baidu.beidou.accountmove.ubmc.util.rpc;

import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.gson.JsonElement;
import com.baidu.rpc.client.McpackRpcProxy;
import com.baidu.rpc.exception.ExceptionHandler;
import com.baidu.rpc.exception.ParseErrorException;


/**
 * <p>ClassName:McpackRpcProxyWithHeaderProperty
 * <p>Function:  mcpack2远程调用代理类，相对于基类添加了向Header中加入属性的功能
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-12-27
 * @since    Cpweb-221
 * @version  $Id: Exp $
 */
public class McpackRpcProxyWithHeaderProperty extends McpackRpcProxy {
	protected final Log LOG = LogFactory.getLog(getClass());
	
	/** 用于放置header中需要添加的属性信息 */
	protected Map<String,String> headerProperties = new HashMap<String, String>();
	
	@Override
	public Object clone() {
		McpackRpcProxyWithHeaderProperty result = new McpackRpcProxyWithHeaderProperty(url, encoding, exceptionHandler);
		result.setHeaderProperties(headerProperties);
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
	public McpackRpcProxyWithHeaderProperty(String url, String encoding, ExceptionHandler exp) {
		super(url, encoding, exp);
	}

	@Override
	protected Object parseResult(int id, JsonElement ele, Method method)
			throws Exception {
		if(LOG.isDebugEnabled()){
			LOG.debug("JsonRpc head="+headerProperties+" response="+ele);
		}
		return super.parseResult(id, ele, method);
	}
	
	@Override
	protected JsonElement makeRequest(int id, Method method, Object[] args)
			throws ParseErrorException {
		JsonElement result = super.makeRequest(id, method, args);
		if(LOG.isDebugEnabled()){
			LOG.debug("JsonRpc head="+headerProperties+" request="+result);
		}
		return result;
	}
	
	@Override
	protected void sendRequest(byte[] reqBytes, URLConnection connection) {

		if(null != headerProperties){
			for(Entry<String, String> entry : headerProperties.entrySet()){
				if(null != entry.getValue()){
					connection.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
		}
		super.sendRequest(reqBytes, connection);
	}


	public Map<String, String> getHeaderProperties() {
		return headerProperties;
	}

	public void setHeaderProperties(Map<String, String> headerProperties) {
		this.headerProperties = headerProperties;
	}
	
	public void addHeaderProperties(Map<String, String> headerProperties) {
		if(headerProperties != null) {
			if(this.headerProperties == null) {
				this.headerProperties = new HashMap<String, String>();
			}
			this.headerProperties.putAll(headerProperties);
		}
	}
	
	public void addHeaderProperties(String key, String value) {
		if(this.headerProperties == null) {
			this.headerProperties = new HashMap<String, String>();
		}
		this.headerProperties.put(key, value);
	}
}
