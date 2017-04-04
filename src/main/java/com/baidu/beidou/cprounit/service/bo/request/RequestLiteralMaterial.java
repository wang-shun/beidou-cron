package com.baidu.beidou.cprounit.service.bo.request;

import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;

/**
 * beidou文字物料
 * @author yanjie
 *
 */
public class RequestLiteralMaterial extends BeidouMaterialBase{
	private String description1;
	private String description2;
	
	public RequestLiteralMaterial(String title, String showUrl, String targetUrl, String desp1, String desp2,String wirelessShowUrl, String wirelessTargetUrl){
		super(title, showUrl, targetUrl,wirelessShowUrl,wirelessTargetUrl);
		this.description1 = desp1;
		this.description2 = desp2;
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
