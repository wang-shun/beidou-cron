package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;

/**
 * 调用dr-mc批量删除物料接口时，返回结果
 * 
 * @author yanjie
 *
 */
public class DrmcBatchRemoveResultBean extends DrmcResultBeanBase{
	private List<RemoveResult> data;
	
	public List<RemoveResult> getData() {
		return data;
	}
	public void setData(List<RemoveResult> data) {
		this.data = data;
	}
	
}
