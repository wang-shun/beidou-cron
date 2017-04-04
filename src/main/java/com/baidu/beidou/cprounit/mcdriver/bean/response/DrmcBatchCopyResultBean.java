package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;

/**
 * 调用dr-mc批量拷贝物料接口时，返回结果
 * 
 * @author yanjie
 *
 */
public class DrmcBatchCopyResultBean extends DrmcResultBeanBase{
	private List<CopyResult> data;
	
	public List<CopyResult> getData() {
		return data;
	}
	public void setData(List<CopyResult> data) {
		this.data = data;
	}

}
