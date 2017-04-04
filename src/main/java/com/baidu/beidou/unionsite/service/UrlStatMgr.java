/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.baidu.beidou.unionsite.bo.WhiteUrl;

/**
 * ClassName:UrlStatMgr
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    TODO
 * @Date	 2010	2010-5-23		上午12:53:19
 *
 * @see 	 
 */

public interface UrlStatMgr {
	
	/**
	 * 
	 * generateUrlStatFile: 读取url展现文件，生成对应的url表和站点表的数据文件
	 *
	 * @param whiteUrlList 排好序的白名单列表
	 * @param urlFile	url展现文件
	 * @param tableCnt url表的拆表个数
	 * @param outputPath	输出文件的目录，文件名为${tablename}${siteid%tableCnt}
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 * @throws IOException 
	 * @since 1.0.51
	 */
	public void generateUrlStatFile(final List<WhiteUrl> whiteUrlList,
			final String urlFile, final int tableCnt, final String outputPath) throws UnsupportedEncodingException, FileNotFoundException, IOException;
}
