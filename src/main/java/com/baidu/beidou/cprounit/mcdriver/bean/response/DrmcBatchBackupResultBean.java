package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;

/**
 * 调用dr-mc批量备份物料接口时，返回结果
 * 
 * @author yanjie
 *
 */
public class DrmcBatchBackupResultBean extends DrmcResultBeanBase{
	private List<BackupResult> data;
	
	public List<BackupResult> getData() {
		return data;
	}
	public void setData(List<BackupResult> data) {
		this.data = data;
	}
}
