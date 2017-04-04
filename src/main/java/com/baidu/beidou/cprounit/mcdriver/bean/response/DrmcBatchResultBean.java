package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;
import java.util.Map;


/**
 * 普通批量操作，drmc返回值bean（data是list，list的每个元素是map，map的key和value都是string）
 * 
 * @author guojichun
 * @since 1.0.0
 */
public class DrmcBatchResultBean extends DrmcResultBeanBase{
	private List<Map<String,String>> data;
	
	public List<Map<String,String>> getData() {
		return data;
	}
	public void setData(List<Map<String,String>> data) {
		this.data = data;
	}
	
	
}
