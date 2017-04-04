package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestIconUnitWithMediaId
 * Function: 北斗图文物料
 * 		fileSrc为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class ResponseIconUnit extends ResponseBaseMaterial {
	
	private String title;
	private String description1;
	private String description2;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	private Integer width;
	private Integer height;
	private String fileSrc;
	private String fileSrcMd5;

	public ResponseIconUnit(Integer wuliaoType, String title, String description1, String description2, 
			String showUrl, String targetUrl, String wirelessShowUrl, String wirelessTargetUrl, 
			Integer width, Integer height, String fileSrc, String fileSrcMd5) {
		
		this.wuliaoType = wuliaoType;
		this.title = title;
		this.description1 = description1;
		this.description2 = description2;
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
		this.width = width;
		this.height = height;
		this.fileSrc = fileSrc;
		this.fileSrcMd5 = fileSrcMd5;
		
	}
	
	public static ResponseIconUnit transformToObject(Map<String, String> valueMap) {
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
			Integer width = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WIDTH));
			Integer height = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_HEIGHT));
			String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);
			String fileSrcMd5 = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC_MD5);
			
			return new ResponseIconUnit(wuliaoType, title, description1, description2, showUrl, targetUrl, 
					wirelessShowUrl, wirelessTargetUrl, width, height, fileSrc, fileSrcMd5);
		} catch (NumberFormatException e) {
			log.error("failed to get width or heiget from the ubmc-value map");
			return null;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}
	
	public String getFileSrcMd5() {
		return fileSrcMd5;
	}

	public void setFileSrcMd5(String fileSrcMd5) {
		this.fileSrcMd5 = fileSrcMd5;
	}
}
