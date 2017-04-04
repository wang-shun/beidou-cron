package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: ResponsePreviewImageMaterial
 * Function: 北斗预览物料，用于预览工具中上传预览图片
 * 		fileSrc为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class ResponsePreviewImageMaterial extends ResponseBaseMaterial {
	
	private String fileSrc;
	
	public ResponsePreviewImageMaterial(Integer wuliaoType, String fileSrc) {
		this.wuliaoType = wuliaoType;
		this.fileSrc = fileSrc;
	}
	
	public static ResponsePreviewImageMaterial transformToObject(Map<String, String> valueMap) {
		if (valueMap == null || valueMap.isEmpty()) {
			return null;
		}
		try {
			Integer wuliaoType = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WULIAO_TYPE));
			String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);
			
			return new ResponsePreviewImageMaterial(wuliaoType, fileSrc);
		} catch (NumberFormatException e) {
			log.error("failed to get width or heiget from the ubmc-value map");
			return null;
		}
	}

	public String getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}
}
