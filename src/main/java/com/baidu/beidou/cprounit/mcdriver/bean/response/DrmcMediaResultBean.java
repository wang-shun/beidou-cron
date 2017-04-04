package com.baidu.beidou.cprounit.mcdriver.bean.response;


/**
 * 获取二进制数据，drmc返回值bean（data是byte[]）
 * 
 * @author guojichun
 * @since 1.0.0
 */
public class DrmcMediaResultBean extends DrmcResultBeanBase{
	private byte[] data;
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
}
