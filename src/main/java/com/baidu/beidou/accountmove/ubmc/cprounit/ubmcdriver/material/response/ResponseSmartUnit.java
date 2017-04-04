package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * ClassName: ResponseSmartUnit
 * Function: 北斗智能创意物料
 *
 * @author genglei
 * @version cpweb-699
 * @date Mar 3, 2014
 */
public class ResponseSmartUnit extends ResponseBaseMaterial {
	
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	private Integer width;
	private Integer height;
	
	private Integer templateId;

	public ResponseSmartUnit(Integer wuliaoType, String showUrl, String targetUrl, 
			String wirelessShowUrl, String wirelessTargetUrl, Integer width, 
			Integer height, Integer templateId) {

		this.wuliaoType = wuliaoType;
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
		this.width = width;
		this.height = height;
		this.templateId = templateId;
		
	}
	
	public static ResponseSmartUnit transformToObject(Map<String, String> valueMap) {
		if (valueMap == null || valueMap.isEmpty()) {
			return null;
		}
		
		try {
			Integer wuliaoType = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WULIAO_TYPE));
			String showUrl = valueMap.get(UbmcConstant.VALUE_ITEM_SHOW_URL);
			String targetUrl = valueMap.get(UbmcConstant.VALUE_ITEM_TARGET_URL);
			String wirelessShowUrl = valueMap.get(UbmcConstant.VALUE_ITEM_WIRELESS_SHOW_URL);
			String wirelessTargetUrl = valueMap.get(UbmcConstant.VALUE_ITEM_WIRELESS_TARGET_URL);
			Integer width = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WIDTH));
			Integer height = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_HEIGHT));
			Integer templateId = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_TEMPLATEID));
			
			return new ResponseSmartUnit(wuliaoType, showUrl, targetUrl, wirelessShowUrl, 
					wirelessTargetUrl, width, height, templateId);
		} catch (NumberFormatException e) {
			log.error("failed to get width or heiget from the ubmc-value map");
			return null;
		}
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

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}
	
	public Integer getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}
}
