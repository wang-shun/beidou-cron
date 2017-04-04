package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * ClassName: ResponseTextUnit
 * Function: 北斗文字物料
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
public class ResponseTextUnit extends ResponseBaseMaterial {
	private String title;
	private String description1;
	private String description2;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	public ResponseTextUnit(Integer wuliaoType, String title, String description1, String description2, String showUrl, 
			String targetUrl, String wirelessShowUrl, String wirelessTargetUrl) {
		
		this.wuliaoType = wuliaoType;
		this.title = title;
		this.description1 = description1;
		this.description2 = description2;
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
		
	}
	
	public static ResponseTextUnit transformToObject(Map<String, String> valueMap) {
		if (valueMap == null || valueMap.isEmpty()) {
			return null;
		}
		try {
			Integer wuliaoType = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WULIAO_TYPE));
			String title = valueMap.get(UbmcConstant.VALUE_ITEM_TITLE);
			String description1 = valueMap.get(UbmcConstant.VALUE_ITEM_DESC1);
			String description2 = valueMap.get(UbmcConstant.VALUE_ITEM_DESC2);
			String showUrl = valueMap.get(UbmcConstant.VALUE_ITEM_SHOW_URL);
			String targetUrl = valueMap.get(UbmcConstant.VALUE_ITEM_TARGET_URL);
			String wirelessShowUrl = valueMap.get(UbmcConstant.VALUE_ITEM_WIRELESS_SHOW_URL);
			String wirelessTargetUrl = valueMap.get(UbmcConstant.VALUE_ITEM_WIRELESS_TARGET_URL);
			
			return new ResponseTextUnit(wuliaoType, title, description1, description2, 
					showUrl, targetUrl,	wirelessShowUrl, wirelessTargetUrl);
		} catch (NumberFormatException e) {
			log.error("failed to get wuliaoType from the ubmc-value map");
			return null;
		}
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getWirelessShowUrl() {
		return wirelessShowUrl;
	}

	public void setWirelessShowUrl(String wirelessShowUrl) {
		this.wirelessShowUrl = wirelessShowUrl;
	}

	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}

	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}
}
