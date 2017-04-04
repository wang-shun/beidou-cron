package com.baidu.beidou.cprounit.service.bo.response;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConfig;

/**
 * 北斗图文混排物料
 * @author hejinggen
 *
 */
public class ResponseLiteralWithIconMaterial extends ResponseLiteralMaterial {
	private final static Log log = LogFactory.getLog(ResponseLiteralWithIconMaterial.class);
	
	private String fileSrc;
	private int width;
	private int height;
		
	public ResponseLiteralWithIconMaterial(long mcid, String title, String showUrl, String targetUrl, String desp1, String desp2,String fileSrc, int width, int height, String wirelessShowUrl, String wirelessTargetUrl){
		super(mcid, title, showUrl, targetUrl,desp1,desp2,wirelessShowUrl,wirelessTargetUrl);
		this.fileSrc = fileSrc;
		this.width = width;
		this.height = height;
		
	}
	
	/**
	 * 将item解析为ResponseLiteralWithIconMaterial，若格式有错误则返回null
	 */
	public static ResponseLiteralWithIconMaterial getInstance(Map<String, String> item){
		long mcid;
		String title;
		String showUrl;
		String targetUrl;
		String desp1;
		String desp2;
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
			desp1 = item.get("description1");
			desp2 = item.get("description2");
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
		
		return new ResponseLiteralWithIconMaterial(mcid, title, showUrl, targetUrl, desp1, desp2,fileSrc,width,height,wirelessShowUrl,wirelessTargetUrl);
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
	
}
