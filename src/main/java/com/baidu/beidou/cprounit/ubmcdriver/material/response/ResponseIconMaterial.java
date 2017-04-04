package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestIconMaterial
 * Function: 北斗图标物料，用于上传系统图标或者用户图标
 * 		fileSrc为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class ResponseIconMaterial extends ResponseBaseMaterial {
	
	private Integer width;
	private Integer height;
	private String fileSrc;
	private String fileSrcMd5;
	
	public ResponseIconMaterial(Integer wuliaoType, Integer width, 
			Integer height, String fileSrc, String fileSrcMd5) {
		this.wuliaoType = wuliaoType;
		this.width = width;
		this.height = height;
		this.fileSrc = fileSrc;
		this.fileSrcMd5 = fileSrcMd5;
	}
	
	public static ResponseIconMaterial transformToObject(Map<String, String> valueMap) {
		if (valueMap == null || valueMap.isEmpty()) {
			return null;
		}
		try {
			Integer wuliaoType = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WULIAO_TYPE));
			Integer width = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WIDTH));
			Integer height = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_HEIGHT));
			String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);
			String fileSrcMd5 = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC_MD5);
			
			return new ResponseIconMaterial(wuliaoType, width, height, fileSrc, fileSrcMd5);
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
}
