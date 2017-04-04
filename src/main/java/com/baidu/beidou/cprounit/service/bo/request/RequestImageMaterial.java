package com.baidu.beidou.cprounit.service.bo.request;

import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;

/**
 * beidou图片/Flash物料
 * 以字节数组形式提交
 * 
 * @author yanjie
 *
 */
public class RequestImageMaterial extends BeidouMaterialBase{
	private byte[] fileSrc;
	private int MC_POS_WIDTH;
	private int MC_POS_HEIGHT;
	
	public RequestImageMaterial(String title, String showUrl, String targetUrl, byte[] fileSrc, int width, int height ,String wirelessShowUrl, String wirelessTargetUrl){
		super(title, showUrl, targetUrl, wirelessShowUrl, wirelessTargetUrl);
		this.fileSrc = fileSrc;
		this.MC_POS_WIDTH = width;
		this.MC_POS_HEIGHT = height;
	}
	
	public byte[] getFileSrc() {
		return fileSrc;
	}
	public void setFileSrc(byte[] fileSrc) {
		this.fileSrc = fileSrc;
	}
	public int getMC_POS_HEIGHT() {
		return MC_POS_HEIGHT;
	}
	public void setMC_POS_HEIGHT(int mc_pos_height) {
		MC_POS_HEIGHT = mc_pos_height;
	}
	public int getMC_POS_WIDTH() {
		return MC_POS_WIDTH;
	}
	public void setMC_POS_WIDTH(int mc_pos_width) {
		MC_POS_WIDTH = mc_pos_width;
	}
}
