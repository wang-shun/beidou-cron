package com.baidu.beidou.cprounit.service.bo.response;

import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConfig;


public class ResponseIconMaterial{
	private final static Log log = LogFactory.getLog(ResponseIconMaterial.class);
	
	private long mcid;
	private String fileSrc;
	private int width;
	private int height;
	
	public ResponseIconMaterial(long mcid,String fileSrc, int width, int height){
		this.mcid = mcid;
		this.fileSrc = fileSrc;
		this.width = width;
		this.height = height;
	}
	
	
	public static ResponseIconMaterial getInstance(Map<String, String> item){
		long mcid;
		String fileSrc;
		int width;
		int height;
		try{
			mcid = Long.valueOf(item.get("mcid"));
			fileSrc = item.get("fileSrc");
			if (fileSrc.startsWith(CproUnitConfig.DRMC_MATPREFIX)){
				fileSrc = fileSrc.substring(CproUnitConfig.DRMC_MATPREFIX.length());
			} else {
				throw new Exception("mcid[" + mcid 
						+ "]'s fileSrc[" + fileSrc 
						+ "] not start with [" + CproUnitConfig.DRMC_MATPREFIX + "]");
			}
			width = Integer.valueOf(item.get("width"));
			height = Integer.valueOf(item.get("height"));
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return null;
		}
		
		return new ResponseIconMaterial(mcid, fileSrc, width, height);
	}


	public long getMcid() {
		return mcid;
	}


	public void setMcid(long mcid) {
		this.mcid = mcid;
	}


	public String getFileSrc() {
		return fileSrc;
	}


	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}


	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public String toString() {		
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
