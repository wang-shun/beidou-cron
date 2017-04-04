/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.dao;

import java.io.BufferedReader;
import java.io.IOException;

import com.baidu.beidou.unionsite.bo.UrlStat;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.unionsite.vo.StatCounter;

/**
 * ClassName:UrlStatFileDao 
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    TODO
 * @Date	 2010	2010-5-22		下午04:58:19
 *
 * @see 	 
 */

public interface UrlStatFileDao {

	/**
	 * readRecord: 读取url统计信息，过滤不合法的记录，有如下几种类型：<br>
	 * 	1: 格式不符合要求，列数少于<5, 类型不符合要求等;<br>
	 *  2：url不是asicc字符串;<br>
	 *  3: url字节长度超过255（包括）; <br>
	 *
	 * @param reader iso-8859-1编码的字符文件reader
	 * @param minCountOfSize 每一个尺寸的最小展现次数,url的每个尺寸的展现量如果小于该数值,则抛弃该尺寸，目的是避免作弊行为
	 * @return    解析后的记录，如果UrlStat.url==null,则表示被过滤的不合法记录；如果读到文件结尾，则返回null；<br>
	 * 				注意：没有使用抛出异常的方法，是为了性能考虑
	 * @since 1.0.51
	 */
	public UrlStat readRecord(BufferedReader reader,int minCountOfSize) throws IOException;
	
	
	public StatCounter getStatCounter() ;
}
