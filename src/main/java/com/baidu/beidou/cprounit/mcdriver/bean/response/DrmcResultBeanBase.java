package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.Map;

/**
 * drmc返回值bean基类
 * 
 * @author guojichun
 * @since 1.0.0
 */
public class DrmcResultBeanBase {
	private int status;
	private Map<String,String> info;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Map<String, String> getInfo() {
		return info;
	}
	public void setInfo(Map<String, String> info) {
		this.info = info;
	}
	
	
}
