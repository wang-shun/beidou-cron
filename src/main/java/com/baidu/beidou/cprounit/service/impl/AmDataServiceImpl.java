package com.baidu.beidou.cprounit.service.impl;

import com.baidu.beidou.cprounit.mcdriver.AmDataRevisionDriverProxy;
import com.baidu.beidou.cprounit.service.AmDataService;
import com.baidu.beidou.util.service.impl.BaseDrawinRpcServiceImpl;

public class AmDataServiceImpl extends BaseDrawinRpcServiceImpl implements AmDataService {
	
	private AmDataRevisionDriverProxy amDataDriverProxy;
	
	public AmDataServiceImpl(String syscode, String prodid){
		super(syscode, prodid);
	}

	public String downloadDrmcMaterial(String descJson, Integer tpId) {
		return amDataDriverProxy.downloadDrmcMaterial(descJson, tpId, getHeaders());
	}

	public AmDataRevisionDriverProxy getAmDataDriverProxy() {
		return amDataDriverProxy;
	}

	public void setAmDataDriverProxy(AmDataRevisionDriverProxy amDataDriverProxy) {
		this.amDataDriverProxy = amDataDriverProxy;
	}
}
