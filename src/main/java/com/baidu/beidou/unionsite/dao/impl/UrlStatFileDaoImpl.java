/*
 * Copyright (c) 1999-2010, baidu.com All Rights Reserved.
 */

package com.baidu.beidou.unionsite.dao.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.UrlStat;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.UrlStatFileDao;
import com.baidu.beidou.unionsite.vo.StatCounter;
import com.baidu.beidou.util.TestLogUtils;

/**
 * ClassName:UrlStatFileDaoImpl Function: TODO ADD FUNCTION
 * 
 * @author <a href="mailto:zengyunfeng@baidu.com">曾云峰</a>
 * @version
 * @since TODO
 * @Date 2010 2010-5-22 下午05:29:08
 * @modified by yang_yun 2010-06-25
 * @see
 */

public class UrlStatFileDaoImpl implements UrlStatFileDao {
	private static final Log LOG = LogFactory.getLog(UrlStatFileDaoImpl.class);
	private final Pattern pattern = Pattern
			.compile("^([\\u0000-\\u007f]{1,254})\t([0-2])\t([\\d]+)\t([1-3])\t([\\d\\|\\*]*)(\t?)([\\d\\|]*)(\t?)([0-9]*)$");
	private static final Pattern URL_PATTERN = Pattern.compile("^(\\w+:\\/\\/)?([^\\/:\\?]+)(.*)", Pattern.CASE_INSENSITIVE);

	private static final String SUB_FIELD_SPLITER = "\\|";
	private static final String THIRD_FIELD_SPLITER = "\\*";
	
	public static final int DISP_FIXED_FLAG = 0;	//接口文件中支持广告的固定展现方式
	private static final int DISP_FLOW_FLAG = 1;	//接口文件中尺寸广告的悬浮展现方式
	private static final int DISP_FILM_FLAG = 2;	//尺寸广告的固定展现方式，目前还没有使用
	
	private static StatCounter COUNTER = new StatCounter();
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.baidu.beidou.unionsite.dao.UrlStatFileDao#readRecord(java.io.
	 * BufferedReader)
	 */
	public UrlStat readRecord(BufferedReader reader,int minCountOfSize) throws IOException {
		if (reader == null) {
			return null;
		}
		String line = reader.readLine();
		if (line == null) {
			return null;
		}
		Matcher matcher = pattern.matcher(line);
		
		
		UrlStat result = new UrlStat();
		if (!matcher.matches()) {
			// 包含非英文字符
			return result;
		}

		int groupCnt = matcher.groupCount();
		if (groupCnt < 9) {
			LOG.warn("格式错误:" + line);
			return result;
		}
		String url = matcher.group(1);
		String displayStr = matcher.group(2); // 广告类型
		String srchStr = matcher.group(3); // 检索量
		String supportStr = matcher.group(4); // 物料类型
		String sizeArrayStr = matcher.group(5); // 尺寸类型
		String sizeShowStr = matcher.group(7); // 尺寸对应的检索量
		String textShowStr = matcher.group(9); // 文字对应的检索量
		
		boolean isSupportPic=false;

		try {
			int display = Integer.parseInt(displayStr);
			long srchs = Long.parseLong(srchStr);
			if(srchs<minCountOfSize){//总流量小于门限则抛弃
				return result;
			}
			int support = Integer.parseInt(supportStr);
			String[] sizeArray = sizeArrayStr.split(SUB_FIELD_SPLITER);
			int width = 0;
			int height = 0;
			int intSize = 0;
			int intSizeShow=0;
			int dbDisplayType = SiteConstant.DISP_FIXED_FLAG;
			switch (display) {
				
				case DISP_FIXED_FLAG:
					dbDisplayType = SiteConstant.DISP_FIXED_FLAG;
					break;
				case DISP_FLOW_FLAG:
					dbDisplayType = SiteConstant.DISP_FLOW_FLAG;
					break;
				case DISP_FILM_FLAG:
					dbDisplayType = SiteConstant.DISP_FILM_FLAG;
					break;
				default:
					LOG.warn("无效的广告类型");
					return result;
			}
			
			//删除这段代码，因为旧格式已经不存在了
//			if(sizeShowStr.equals("")&&textShowStr.equals("")){//旧格式
//			    System.out.println("");
//				for (String size : sizeArray) {
//					if ("".equals(size)) {
//						continue;
//					}
//					String[] sizeStr = size.split(THIRD_FIELD_SPLITER);
//					if (sizeStr.length < 2) {
//						LOG.warn("尺寸格式错误:" + line);
//						COUNTER.increaseSizeError();
//						continue;
//					}
//					width = Integer.parseInt(sizeStr[0]);
//					height = Integer.parseInt(sizeStr[1]);
//					
//					int[] imageSize = new int[]{width, height};
//					intSize = 0;
//					switch (dbDisplayType) {
//					
//						case SiteConstant.DISP_FIXED_FLAG:
//							intSize = SiteConstant.isSupportFixedAdSize(imageSize);
//							break;
//						case SiteConstant.DISP_FLOW_FLAG:
//							intSize = SiteConstant.isSupportFlowAdSize(imageSize);
//							break;
//						case SiteConstant.DISP_FILM_FLAG:
//							intSize = SiteConstant.isSupportFilmAdSize(imageSize);
//							break;
//					}
//					
//					if (intSize > 0) {
////						result.getSize().add(intSize);
//						isSupportPic=true;
//					} else {
//						COUNTER.increaseSizeError();
//						TestLogUtils.testInfo("无效的尺寸["+size+"],line="+line);
//						continue;
//					}
//				}
//				
//				if(SiteConstant.bitOp_supports(support,SiteConstant.WL_PIC_FLAG)
//						&& !isSupportPic){
//					TestLogUtils.testInfo("物料类型支持图片，但是没有对应的图片尺寸，line="+line);
//					COUNTER.increaseSupportError();
//					return result;
//				}
//			}else{//新格式
				if(!sizeShowStr.equals("")){//存在图片尺寸
					
					String[] sizeShowArray = sizeShowStr.split(SUB_FIELD_SPLITER);
					
						for (int i=0;i<sizeArray.length;i++) {
							
							if ("".equals(sizeArray[i])) {
								continue;
							}
							String[] sizeStr = sizeArray[i].split(THIRD_FIELD_SPLITER);
							if (sizeStr.length < 2) {
								LOG.warn("尺寸格式错误:" + line);
								COUNTER.increaseSizeError();
								continue;
							}
							width = Integer.parseInt(sizeStr[0]);
							height = Integer.parseInt(sizeStr[1]);
							if(i<sizeShowArray.length){
								intSizeShow=Integer.parseInt(sizeShowArray[i]);
							}else{
								intSizeShow=0;
							}
							
							
							int[] imageSize = new int[]{width, height};
							intSize = 0;
							switch (dbDisplayType) {
							
								case SiteConstant.DISP_FIXED_FLAG:
									intSize = SiteConstant.isSupportFixedAdSize(imageSize);
									break;
								case SiteConstant.DISP_FLOW_FLAG:
									intSize = SiteConstant.isSupportFlowAdSize(imageSize);
									break;
								case SiteConstant.DISP_FILM_FLAG:
									intSize = SiteConstant.isSupportFilmAdSize(imageSize);
									break;
							}
							if (intSize > 0){
								isSupportPic=true;
							}
							if (intSize > 0&&intSizeShow>=minCountOfSize) {
								result.getSize().put(intSize, intSizeShow);
							} else if(intSize <= 0){
								COUNTER.increaseSizeError();
								TestLogUtils.testInfo("无效的尺寸["+sizeArray[i]+"],line="+line);
								continue;
							}
						}
						
						if(result.getSize().size()==0){//尺寸都被抛弃
							if(support==SiteConstant.WL_PIC_FLAG){//如果只支持图片则抛弃
								return result;
							}else if(support==SiteConstant.WL_FULL_SUPPORT){//如果支持文字和图片则改为只支持文字
								support=SiteConstant.WL_TEXT_FLAG;
							}
						}
						
								
				}
				
				if(!textShowStr.equals("")){//存在文字流量
					Integer show=0;
					try {
						show=Integer.parseInt(textShowStr);
					} catch (RuntimeException e) {
						COUNTER.increaseSizeError();
						TestLogUtils.testInfo("line="+line+"\n"+e.getMessage());
						return result;
					}
					
					if(show<minCountOfSize){
						if(support==SiteConstant.WL_TEXT_FLAG){//如果只支持文字类型则抛弃
							return result;
						}else if(support==SiteConstant.WL_FULL_SUPPORT){//如果支持文字和图片则改为只支持图片
							support=SiteConstant.WL_PIC_FLAG;
						}
					}
				}

			
			
			matcher = URL_PATTERN.matcher(url);
			if(matcher.matches() && matcher.groupCount()>2){
				//取域名并进行大小写
				result.setUrl(matcher.group(2).toLowerCase()+matcher.group(3));
			}else{
				return result;
			}
			
			result.setDisplaytype(dbDisplayType);
			result.setSrchs(srchs);
			result.setSupporttype(support);
		} catch (NumberFormatException e) {
			LOG.warn("格式错误：line="+line);
		}
		return result;
	}

	/**
	 * cOUNTER
	 *
	 * @return  the cOUNTER
	 */
	
	public StatCounter getStatCounter() {
		return COUNTER;
	}



}
