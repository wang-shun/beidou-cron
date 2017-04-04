package com.baidu.beidou.unionsite.service;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface WM123SiteScoreService {

	/**
	 * 读取从上游取得的数据文件信息，计算网站得分，并入库
	 * @author lvzichan
	 * @throws FileNotFoundException 文件不存在
	 * @throws IOException 
	 */
	public void refreshSiteScore() throws FileNotFoundException, IOException;
}
