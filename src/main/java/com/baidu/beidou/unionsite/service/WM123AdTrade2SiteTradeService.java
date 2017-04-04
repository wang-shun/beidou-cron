package com.baidu.beidou.unionsite.service;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface WM123AdTrade2SiteTradeService {

	/**
	 * 以推广组维度，计算广告主行业偏向选择的网站行业的次数和百分比，最终生成统计文件
	 */
	public void computeAdTrade2SiteTrade(String srcGroupFilePath,
			String adTrade2SiteTradeFilePath) throws FileNotFoundException,
			IOException;
}
