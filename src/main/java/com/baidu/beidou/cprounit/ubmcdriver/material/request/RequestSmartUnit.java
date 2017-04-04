package com.baidu.beidou.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * ClassName: RequestSmartUnit
 * Function: 北斗智能创意物料
 *
 * @author genglei
 * @version cpweb-699
 * @date Mar 3, 2014
 */
public class RequestSmartUnit extends RequestBaseMaterial {

	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	private Integer width;
	private Integer height;
	
	private Integer templateId;	// 为智能文本创意增加模板ID
	
	public RequestSmartUnit(Long mcId, Integer versionId, Integer wuliaoType, 
			String showUrl, String targetUrl, String wirelessShowUrl, 
			String wirelessTargetUrl, Integer width, Integer height, Integer templateId) {
		
		super(mcId, versionId, wuliaoType);
		if (wuliaoType == null) {
			this.wuliaoType = CproUnitConstant.MATERIAL_TYPE_SMART_IDEA;
		}
		
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
		this.width = width;
		this.height = height;
		this.templateId = templateId;
		
	}
	
	public String tranformToValueString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(UbmcConstant.VALUE_ITEM_WULIAO_TYPE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWuliaoType())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_SHOW_URL)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getShowUrl()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_TARGET_URL)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getTargetUrl())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_WIRELESS_SHOW_URL)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWirelessShowUrl())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_WIRELESS_TARGET_URL)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWirelessTargetUrl())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_WIDTH)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWidth()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_HEIGHT)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getHeight()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_TEMPLATEID)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getTemplateId());
		
		return sb.toString();
	}

	public String getShowUrl() {
		if (showUrl == null) {
			return "";
		}
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public String getTargetUrl() {
		if (targetUrl == null) {
			return "";
		}
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getWirelessShowUrl() {
		if (wirelessShowUrl == null) {
			return "";
		}
		return wirelessShowUrl;
	}

	public void setWirelessShowUrl(String wirelessShowUrl) {
		this.wirelessShowUrl = wirelessShowUrl;
	}

	public String getWirelessTargetUrl() {
		if (wirelessTargetUrl == null) {
			return "";
		}
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
