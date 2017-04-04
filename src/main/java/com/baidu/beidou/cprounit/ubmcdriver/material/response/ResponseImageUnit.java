package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: ResponseImageUnit
 * Function: 北斗图片或者flash物料
 * 		fileSrc为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
 * 		attribute为admaker那边制作的flash物料时保存的素材表，
 * 				其格式为一批占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL，其中以逗号隔开
 * 		refMcId作为admaker制作的多媒体物料的mdId
 *      descInfo作为admaker制作的多媒体物料的描述信息，北斗业务上无需使用，但是需要在创建北斗物料时拷贝该字段，已被后续分析物料使用
 *      snapshot作为admaker制作的多媒体物料，如果可以投放google adx的话，会包含该截图；该请求对象仅包含引用，不可修改
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
public class ResponseImageUnit extends ResponseBaseMaterial {
	
	private String title;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	private Integer width;
	private Integer height;
	private String fileSrc;
	private String fileSrcMd5;
	private String attribute;
	private String refMcId;
	private String descInfo;
	
	private String snapshot;

	public ResponseImageUnit(Integer wuliaoType, String title, String showUrl, 
			String targetUrl, String wirelessShowUrl, String wirelessTargetUrl, 
			Integer width, Integer height, String fileSrc, String fileSrcMd5,
			String attribute, String refMcId, String descInfo, String snapshot) {

		this.wuliaoType = wuliaoType;
		this.title = title;
		this.showUrl = showUrl;
		this.targetUrl = targetUrl;
		this.wirelessShowUrl = wirelessShowUrl;
		this.wirelessTargetUrl = wirelessTargetUrl;
		this.width = width;
		this.height = height;
		this.fileSrc = fileSrc;
		this.fileSrcMd5 = fileSrcMd5;
		this.attribute = attribute;
		this.refMcId = refMcId;
		this.descInfo = descInfo;
		this.snapshot = snapshot;
		
	}
	
	public static ResponseImageUnit transformToObject(Map<String, String> valueMap) {
		if (valueMap == null || valueMap.isEmpty()) {
			return null;
		}
		
		try {
			Integer wuliaoType = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WULIAO_TYPE));
			String title = valueMap.get(UbmcConstant.VALUE_ITEM_TITLE);
			String showUrl = valueMap.get(UbmcConstant.VALUE_ITEM_SHOW_URL);
			String targetUrl = valueMap.get(UbmcConstant.VALUE_ITEM_TARGET_URL);
			String wirelessShowUrl = valueMap.get(UbmcConstant.VALUE_ITEM_WIRELESS_SHOW_URL);
			String wirelessTargetUrl = valueMap.get(UbmcConstant.VALUE_ITEM_WIRELESS_TARGET_URL);
			Integer width = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WIDTH));
			Integer height = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_HEIGHT));
			String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);
			String fileSrcMd5 = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC_MD5);
			String attribute = valueMap.get(UbmcConstant.VALUE_ITEM_ATTRIBUTE);
			String refMcId = valueMap.get(UbmcConstant.VALUE_ITEM_REF_MCID);
			String descInfo = valueMap.get(UbmcConstant.VALUE_ITEM_DESC_INFO);
			String snapshot = valueMap.get(UbmcConstant.VALUE_ITEM_SNAPSHOT);
			
			return new ResponseImageUnit(wuliaoType, title, showUrl, targetUrl, wirelessShowUrl, wirelessTargetUrl,	
					width, height, fileSrc, fileSrcMd5, attribute, refMcId, descInfo, snapshot);
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

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getRefMcId() {
		return refMcId;
	}

	public void setRefMcId(String refMcId) {
		this.refMcId = refMcId;
	}

	public String getDescInfo() {
		return descInfo;
	}

	public void setDescInfo(String descInfo) {
		this.descInfo = descInfo;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
}
