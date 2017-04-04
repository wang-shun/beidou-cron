package com.baidu.beidou.cprounit.service.bo.response;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;

public class ResponseLiteralMaterial extends BeidouMaterialBase {
	private final static Log log = LogFactory.getLog(ResponseLiteralMaterial.class);
	
	private String description1;
	private String description2;
		
	public ResponseLiteralMaterial(long mcid, String title, String showUrl, String targetUrl, String desp1, String desp2, String wirelessShowUrl, String wirelessTargetUrl){
		super(mcid, title, showUrl, targetUrl,wirelessShowUrl,wirelessTargetUrl);
		this.description1 = desp1;
		this.description2 = desp2;
		
	}
	
	/**
	 * 将item解析为ResponseLiteralMaterial，若格式有错误则返回null
	 */
	public static ResponseLiteralMaterial getInstance(Map<String, String> item){
		long mcid;
		String title;
		String showUrl;
		String targetUrl;
		String desp1;
		String desp2;
		String wirelessShowUrl;
		String wirelessTargetUrl;
		try{
			mcid = Long.valueOf(item.get("mcid"));
			title = item.get("title");
			showUrl = item.get("showUrl");
			targetUrl = item.get("targetUrl");
			desp1 = item.get("description1");
			desp2 = item.get("description2");

			wirelessShowUrl = item.get("wirelessShowUrl");
			if (StringUtils.isEmpty(wirelessShowUrl)) {
				wirelessShowUrl = null;
			}
			
			wirelessTargetUrl = item.get("wirelessTargetUrl");
			if (StringUtils.isEmpty(wirelessTargetUrl) || "http://".equals(wirelessTargetUrl)) {
				wirelessTargetUrl = null;
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return null;
		}
		
		return  new ResponseLiteralMaterial(mcid, title, showUrl, targetUrl, desp1, desp2,wirelessShowUrl,wirelessTargetUrl);
	}
	
	public String getDescription1() {
		return description1;
	}
	public void setDescription1(String description1) {
		this.description1 = description1;
	}
	public String getDescription2() {
		return description2;
	}
	public void setDescription2(String description2) {
		this.description2 = description2;
	}
}
