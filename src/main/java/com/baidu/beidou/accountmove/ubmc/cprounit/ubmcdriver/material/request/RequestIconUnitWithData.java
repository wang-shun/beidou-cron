package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.accountmove.ubmc.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestIconUnitWithData
 * Function: 北斗图文物料，用于携带二进制的多媒体物料
 * 		fileSrc作为输入时，为二进制的多媒体物料
 * 
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class RequestIconUnitWithData extends RequestBaseMaterial {

	private String title;
	private String description1;
	private String description2;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	private Integer width;
	private Integer height;
	private byte[] fileSrc;
	private String fileSrcMd5;

	public RequestIconUnitWithData(Long mcId, Integer versionId, String title, String description1, String description2, 
			String showUrl, String targetUrl, String wirelessShowUrl, String wirelessTargetUrl, 
			Integer width, Integer height, byte[] fileSrc, String fileSrcMd5) {
		
		super(mcId, versionId, CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON);
		
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
				.append(getWirelessTargetUrl())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_WIDTH)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWidth()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_HEIGHT)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getHeight()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_FILESRC).append(UbmcConstant.VALUE_ITEM_KV_DELIMITER);
		if (null == fileSrc || fileSrc.length == 0) {
			sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER_IMAGE);
		} else {
			sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER);
		}
		sb.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		
		sb.append(UbmcConstant.VALUE_ITEM_FILESRC_MD5)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getFileSrcMd5());
		
		return sb.toString();
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

	public byte[] getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(byte[] fileSrc) {
		this.fileSrc = fileSrc;
	}
	
	public String getFileSrcMd5() {
		if (fileSrcMd5 == null) {
			return "";
		}
		return fileSrcMd5;
	}

	public void setFileSrcMd5(String fileSrcMd5) {
		this.fileSrcMd5 = fileSrcMd5;
	}
}
