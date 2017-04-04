package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.accountmove.ubmc.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestPreviewImageMaterial
 * Function: 北斗预览物料，用于预览工具中上传预览图片
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class RequestPreviewImageMaterial extends RequestBaseMaterial {

	private byte[] fileSrc;
	
	public RequestPreviewImageMaterial(Long mcId, Integer versionId, byte[] fileSrc) {
		super(mcId, versionId, CproUnitConstant.MATERIAL_TYPE_PREVIEW_IMAGE);
		
		this.fileSrc = fileSrc;
	}
	
	public String tranformToValueString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(UbmcConstant.VALUE_ITEM_WULIAO_TYPE)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(getWuliaoType())
				.append(UbmcConstant.VALUE_ITEM_DELIMITER);
		sb.append(UbmcConstant.VALUE_ITEM_FILESRC)
				.append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
				.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER);
			
		return sb.toString();
	}


	public byte[] getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(byte[] fileSrc) {
		this.fileSrc = fileSrc;
	}
}
