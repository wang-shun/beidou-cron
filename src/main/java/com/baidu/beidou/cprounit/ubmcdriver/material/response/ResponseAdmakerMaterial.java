package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: ResponseAdmakerMaterial
 * Function: admaker图片或者flash物料
 * 		fileSrc为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
 * 		attribute为admaker那边制作的flash物料时保存的素材表，
 * 				其格式为一批占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL，其中以逗号隔开
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 23, 2013
 */
public class ResponseAdmakerMaterial extends ResponseBaseMaterial {
	
	private Integer width;
	private Integer height;
	private String fileSrc;
	private String fileSrcMd5;
	private String attribute;
	private String refMcId;
	private String descInfo;

	public ResponseAdmakerMaterial(Integer width, Integer height, String fileSrc, String fileSrcMd5,
			String attribute, String refMcId, String descInfo) {

		this.wuliaoType = CproUnitConstant.MATERIAL_TYPE_ADMAKER;
		this.width = width;
		this.height = height;
		this.fileSrc = fileSrc;
		this.fileSrcMd5 = fileSrcMd5;
		this.attribute = attribute;
		this.refMcId = refMcId;
		this.descInfo = descInfo;
		
	}
	
	public static ResponseAdmakerMaterial transformToObject(Map<String, String> valueMap) {
		if (valueMap == null || valueMap.isEmpty()) {
			return null;
		}
		
		try {
			Integer width = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WIDTH));
			Integer height = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_HEIGHT));
			String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);
			String fileSrcMd5 = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC_MD5);
			String attribute = valueMap.get(UbmcConstant.VALUE_ITEM_ATTRIBUTE);
			String refMcId = valueMap.get(UbmcConstant.VALUE_ITEM_REF_MCID);
			String descInfo = valueMap.get(UbmcConstant.VALUE_ITEM_DESC_INFO);
			
			return new ResponseAdmakerMaterial(width, height, fileSrc, fileSrcMd5, attribute, refMcId, descInfo);
		} catch (NumberFormatException e) {
			log.error("failed to get width or heiget from the ubmc-value map");
			return null;
		}
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
}
