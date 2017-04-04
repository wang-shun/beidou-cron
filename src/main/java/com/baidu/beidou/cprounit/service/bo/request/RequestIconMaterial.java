package com.baidu.beidou.cprounit.service.bo.request;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


public class RequestIconMaterial{
	private long mcid;
	private byte[] fileSrc;
	private int MC_POS_WIDTH;
	private int MC_POS_HEIGHT;
	
	public RequestIconMaterial(byte[] fileSrc, int width, int height){
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
	public long getMcid() {
		return mcid;
	}
	public void setMcid(long mcid) {
		this.mcid = mcid;
	}

	
	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
