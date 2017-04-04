package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.Map;

public class PrePicResultBean extends DrmcResultBeanBase{

private Map<String,String> data;
	
	public Map<String,String> getData() {
		return data;
	}
	public void setData(Map<String,String> data) {
		this.data = data;
	}
}
