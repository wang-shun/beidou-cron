package com.baidu.beidou.cprounit.service.bo.response;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;

/**
 * beidou图片/Flash物料
 * 以dr-mc提供的url形式提交
 * 
 * @author yanjie
 *
 */
public class ResponseImageMaterial extends BeidouMaterialBase{
	private final static Log log = LogFactory.getLog(ResponseImageMaterial.class);
	private String fileSrc;
	private int width;
	private int height;
	
	
	public ResponseImageMaterial(long mcid, String title, String showUrl, String targetUrl, String fileSrc, int width, int height,String wirelessShowUrl,String wirelessTargetUrl){
		super(mcid, title, showUrl, targetUrl,wirelessShowUrl,wirelessTargetUrl);
		this.fileSrc = fileSrc;
		this.width = width;
		this.height = height;
	}
	

	/**
	 * 将item解析为ResponseImageMaterial，若格式有错误则返回null
	 */
	public static ResponseImageMaterial getInstance(Map<String, String> item){
		long mcid;
		String title;
		String showUrl;
		String targetUrl;
		String fileSrc;
		int width;
		int height;
		String wirelessShowUrl;
		String wirelessTargetUrl;
		try{
			mcid = Long.valueOf(item.get("mcid"));
			title = item.get("title");
			showUrl = item.get("showUrl");
			targetUrl = item.get("targetUrl");
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

			wirelessShowUrl = item.get("wirelessShowUrl");
			if (StringUtils.isEmpty(wirelessShowUrl)) {
				wirelessShowUrl = null;
			}
			
			wirelessTargetUrl = item.get("wirelessTargetUrl");
			if (StringUtils.isEmpty(wirelessTargetUrl) || "http://".equals(wirelessTargetUrl)) {
				wirelessTargetUrl = null;
			}			

		}catch(Exception e){
			log.error(e.getMessage(), e);
			return null;
		}
		
		return new ResponseImageMaterial(mcid, title, showUrl, targetUrl, fileSrc, width, height,wirelessShowUrl,wirelessTargetUrl);
	}
	
	public String getFileSrc() {
		return fileSrc;
	}
	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
