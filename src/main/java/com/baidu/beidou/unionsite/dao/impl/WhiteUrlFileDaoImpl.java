/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.dao.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.dao.WhiteUrlFileDao;
import com.baidu.beidou.unionsite.exception.ErrorFormatException;
import com.baidu.beidou.util.UrlParser;

/**
 * ClassName:WhiteUrlFileDaoImpl
 * Function: TODO ADD FUNCTION
 *
 * @author   <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version  
 * @since    TODO
 * @Date	 2010	2010-5-21		上午12:25:51
 *
 * @see 	 
 */

public class WhiteUrlFileDaoImpl implements WhiteUrlFileDao {
	
	private static final Pattern DOMAIN_PATTERN = Pattern.compile("^(\\w+:\\/\\/)", Pattern.CASE_INSENSITIVE);

	
	/*
	 * (non-Javadoc)
	 * @see com.baidu.beidou.unionsite.dao.WhiteUrlFileDao#readRecord(java.io.BufferedReader)
	 */
	public WhiteUrl readRecord(BufferedReader reader) throws IOException,
			ErrorFormatException {
		if(reader == null){
			return null;
		}
		String line = reader.readLine();
		if(line == null){
			return null;
		}
		String[] fields = line.split("\t", 2);
		if(fields.length<2){
			throw new ErrorFormatException("白名单文件的格式不正确："+line);
		}
		String url = fields[0].trim();
		Matcher matcher = DOMAIN_PATTERN.matcher(url);
		url = matcher.replaceFirst("");
		
		String site = UrlParser.parseUrl(url);
		if(site==null){
			throw new ErrorFormatException("白名单文件的格式不正确,无法获取站点："+line);
		}
		url = new StringBuilder(site.toLowerCase()).append(url.substring(site.length())).toString();
		
		int flag = 1;
		try {
			flag = Integer.parseInt(fields[1]);
		} catch (NumberFormatException e) {
			throw new ErrorFormatException("白名单文件的格式不正确,无法获取标识："+line);
		}
		WhiteUrl whiteUrl = new WhiteUrl();
		whiteUrl.setUrl(url);
		if(flag == 1){
			whiteUrl.setWhite(true);
		}else{
			whiteUrl.setWhite(false);
		}
		
		return whiteUrl;
	}

	public static void main(String[] args) {
		String a = "A";
		System.out.println(a.toLowerCase());
	}
}
