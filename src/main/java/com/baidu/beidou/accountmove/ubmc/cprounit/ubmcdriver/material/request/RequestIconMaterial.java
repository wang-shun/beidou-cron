package com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.accountmove.ubmc.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.constant.UbmcConstant;


/**
 * ClassName: RequestIconMaterial
 * Function: 北斗图标物料，用于上传系统图标或者用户图标
 *
 * @author genglei
 * @version cpweb-587
 * @date Apr 25, 2013
 */
public class RequestIconMaterial extends RequestBaseMaterial {

	private Integer width;
	private Integer height;
	private byte[] fileSrc;
	private String fileSrcMd5;
	
	public RequestIconMaterial(Long mcId, Integer versionId, 
			Integer width, Integer height, byte[] fileSrc, String fileSrcMd5) {
		super(mcId, versionId, CproUnitConstant.MATERIAL_TYPE_ICON);
		
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
