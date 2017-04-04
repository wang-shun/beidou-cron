package com.baidu.beidou.unionsite.service;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author lvzichan
 * @since 2013-10-10
 */
public interface WM123SiteCprodataService {

	/**
	 * 将从河图下载的站点推广数据，存入beidouext.unionsitecprodata表中
	 * 
	 * @param domainCprodataFilePath
	 *            上游取得的数据文件路径，必须以_yyyyMMdd结尾，最后的日期是文件数据的日期
	 * @param saveToDbFilePath
	 *            将最终入库的记录写入文件，便于验证
	 */
	public void saveSiteCprodata(String domainCprodataFilePath,
			String saveToDbFilePath) throws FileNotFoundException, IOException;
}
