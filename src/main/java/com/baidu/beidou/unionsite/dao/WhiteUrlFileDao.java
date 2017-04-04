/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.dao;

import java.io.BufferedReader;
import java.io.IOException;

import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;

/**
 * ClassName:WhiteUrlFileDao
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  1.0.51
 * @since    TODO
 * @Date	 2010	2010-5-20		下午10:45:35
 *
 * @see 	 
 */

public interface WhiteUrlFileDao {

	/**
	 * 
	 * readRecord:读取一行记录,返回校验成功的记录,其中的siteid没有设置,具体的校验规则为:<br>
	 * 		trim, 域名小写替换，去除开头的http://, https:// ;<br>
	 *
	 * @param reader 输入文件
	 * @return      返回成功的记录, 如果为null表示读到文件结尾.
	 * @throws IOException 文件读取失败
	 * @throws ErrorFormatException  文件格式不满足要求    
	 * @since 1.0.51
	 */
	public WhiteUrl readRecord(BufferedReader reader) throws IOException,
	ErrorFormatException;
	
}
