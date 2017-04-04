package com.baidu.beidou.accountmove.ubmc.util.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.baidu.beidou.accountmove.ubmc.util.TokenUtil;

/**
 * <p>ClassName:BaseDrawinRpcServiceImpl
 * <p>Function: 封装调用Darwin几个产品线RPC服务用到的基本方法和属性
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-12-27
 * @version  $Id: Exp $
 */
public abstract class BaseDrawinRpcServiceImpl extends BaseRpcServiceImpl {
	
	protected static final String HEADER_SYSCODE = "syscode";
	protected static final String HEADER_PRODID = "prodid";
	protected static final String HEADER_TOKENID = "tokenid";
	/** 字符串长度，16 */
	protected int TOKEN_LENGTH = 16;
	
	/** sysCode */
	protected String sysCode;
	/** 产品线ID，如DRMC中103表示beidou */
	protected String prodId;
	
	
	public BaseDrawinRpcServiceImpl(String syscode, String prodid) {
		this.sysCode = syscode;
		this.prodId = prodid;
	}
	
	/**
	 * <p>getToken:生成token
	 * 可以由子类自己实现
	 * @return      
	*/
	protected String getToken() {
		return TokenUtil.getTokenId(TOKEN_LENGTH);
	}

	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		if(!StringUtils.isEmpty(sysCode)) {
			headers.put(HEADER_SYSCODE, sysCode);
		}
		if(!StringUtils.isEmpty(prodId)) {
			headers.put(HEADER_PRODID, prodId);
		}
		if(isTokenNeed()) {
			headers.put(HEADER_TOKENID, getToken());
		}
		return headers;
	}
	
	/**
	 * <p>isTokenNeed:是否需要TOKEN
	 * 可以由子类自己实现
	 * @return   true代码需要TOKEN，false表示不需要
	*/
	protected boolean isTokenNeed() {
		return true;
	}

	public String getSysCode() {
		return sysCode;
	}

	public void setSysCode(String sysCode) {
		this.sysCode = sysCode;
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}
	
	
}
