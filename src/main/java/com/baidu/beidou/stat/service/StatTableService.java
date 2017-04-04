package com.baidu.beidou.stat.service;

import java.util.Date;

public interface StatTableService {

	public void outputAllSiteUnitId(Date date) throws Exception;
	
	/**
	 * 存放全流量广告导出文件的相对路径和文件名 由于该值需要从shell脚本传入，故不再采用bean参数形式
	 */
	public void setFulladName(String name);
}
