package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.accountmove.ubmc.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestImageUnitWithMediaId
 * Function: 北斗图片或者flash物料，用于使用已有的多媒体物料
 * 		fileSrc作为输入时，为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）
 * 		attribute作为输入时，为admaker那边制作的flash物料时保存的素材表，其格式为一批占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%），其中以逗号隔开
 * 		refMcId作为admaker制作的多媒体物料的mdId
 *      descInfo作为admaker制作的多媒体物料的描述信息，北斗业务上无需使用，但是需要在创建北斗物料时拷贝该字段，已被后续分析物料使用
 * 		snapshot作为admaker制作的多媒体物料，如果可以投放google adx的话，会包含该截图；该请求对象携带二进制文件，可作为截图，如果数组为空，则没有截图信息
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
public class RequestImageUnitWithMediaId extends RequestBaseMaterial {
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
	
	private byte[] snapshot;

	public RequestImageUnitWithMediaId(Long mcId, Integer versionId, Integer wuliaoType, 
			String title, String showUrl, String targetUrl, String wirelessShowUrl, String wirelessTargetUrl, 
			Integer width, Integer height, String fileSrc, String fileSrcMd5, String attribute,
			String refMcId, String descInfo, byte[] snapshot) {
		
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
		sb.append(UbmcConstant.VALUE_ITEM_FILESRC)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getFileSrc()).append(UbmcConstant.VALUE_ITEM_DELIMITER);
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
		
		sb.append(UbmcConstant.VALUE_ITEM_SNAPSHOT).append(UbmcConstant.VALUE_ITEM_KV_DELIMITER);
		// snapshot二进制文件有数据时，则增加空占位符，增加新截图
		if (snapshot != null && snapshot.length > 0) {
			sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER);
		}
		
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

	public String getFileSrc() {
		if (fileSrc == null) {
			return "";
		}
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
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

	public byte[] getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(byte[] snapshot) {
		this.snapshot = snapshot;
	}
	
}
