package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;

/**
 * 普通批量操作，drmc返回值bean（data是list，list的每个元素是Object("tmpmcid":1,"mcid":2,"value":{})，）
 * 
 * @author guojichun
 * @since 1.0.0
 */
public class DrmcActiveBatchResultBean extends DrmcResultBeanBase{
	private List<ActiveBean> data;
	
	public List<ActiveBean> getData() {
		return data;
	}
	public void setData(List<ActiveBean> data) {
		this.data = data;
	}
	
	
}
