package com.baidu.beidou.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * ClassName: RequestTextUnit
 * Function: 北斗文字物料
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
public class RequestTextUnit extends RequestBaseMaterial {
	private String title;
	private String description1;
	private String description2;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	public RequestTextUnit(Long mcId, Integer versionId, String title, 
			String description1, String description2, String showUrl, 
			String targetUrl, String wirelessShowUrl, String wirelessTargetUrl) {
		
		super(mcId, versionId, CproUnitConstant.MATERIAL_TYPE_LITERAL);
		
		this.title = title;
		this.description1 = description1;
		this.description2 = description2;
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
		
	}
	
	public String tranformToValueString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(UbmcConstant.VALUE_ITEM_WULIAO_TYPE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWuliaoType())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_TITLE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getTitle()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_DESC1)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getDescription1())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_DESC2)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getDescription2())
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
				.append(getWirelessTargetUrl());
		
		return sb.toString();
	}

	public String getDescription1() {
		if (description1 == null) {
			return "";
		}
		return description1;
	}

	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	public String getDescription2() {
		if (description2 == null) {
			return "";
		}
		return description2;
	}

	public void setDescription2(String description2) {
		this.description2 = description2;
	}

	public String getTitle() {
		if (title == null) {
			return "";
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
}
