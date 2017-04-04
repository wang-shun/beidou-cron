package com.baidu.beidou.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestImageUnitWithData
 * Function: 北斗图片或者flash物料，用于携带二进制的多媒体物料
 * 		fileSrc作为输入时，为二进制的多媒体物料
 * 		attribute作为输入时，为“”（空字符串）
 *      refMcId作为admaker制作的多媒体物料的mdId
 *      descInfo作为admaker制作的多媒体物料的描述信息，北斗业务上无需使用，但是需要在创建北斗物料时拷贝该字段，已被后续分析物料使用
 *      snapshot作为admaker制作的多媒体物料，如果可以投放google adx的话，会包含该截图；该请求对象仅包含引用，不可修改
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class RequestImageUnitWithData extends RequestBaseMaterial {

	private String title;
	private String showUrl;
	private String targetUrl;
	private String wirelessShowUrl;
	private String wirelessTargetUrl;
	
	private Integer width;
	private Integer height;
	private byte[] fileSrc;
	private String fileSrcMd5;
	private String attribute;
	private String refMcId;
	private String descInfo;
	
	private String snapshot;
	
	public RequestImageUnitWithData(Long mcId, Integer versionId, Integer wuliaoType, 
			String title, String showUrl, String targetUrl, String wirelessShowUrl, String wirelessTargetUrl, 
			Integer width, Integer height, byte[] fileSrc, String fileSrcMd5, String attribute,
			String refMcId, String descInfo, String snapshot) {
		
		super(mcId, versionId, wuliaoType);
		if (wuliaoType == null) {
			this.wuliaoType = CproUnitConstant.MATERIAL_TYPE_PICTURE;
		}
		
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
	
	public String tranformToValueString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(UbmcConstant.VALUE_ITEM_WULIAO_TYPE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWuliaoType())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_TITLE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getTitle()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
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
			if (this.wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
				sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER_FLASH);
			} else {
				sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER_IMAGE);
			}
		} else {
			sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER);
		}
		sb.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		
		sb.append(UbmcConstant.VALUE_ITEM_FILESRC_MD5)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getFileSrcMd5()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_ATTRIBUTE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getAttribute()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_REF_MCID)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getRefMcId()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_DESC_INFO)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getDescInfo()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_SNAPSHOT)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getSnapshot());
		
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

	public String getAttribute() {
		if (attribute == null) {
			return "";
		}
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String getRefMcId() {
		if (refMcId == null) {
			return "";
		}
		return refMcId;
	}

	public void setRefMcId(String refMcId) {
		this.refMcId = refMcId;
	}

	public String getDescInfo() {
		if (descInfo == null) {
			return "";
		}
		return descInfo;
	}

	public void setDescInfo(String descInfo) {
		this.descInfo = descInfo;
	}

	public String getSnapshot() {
		if (snapshot == null) {
			return "";
		}
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
}
