/**
 * BescCreativeInfo.java 
 */
package com.baidu.beidou.bes.besc;

import java.util.Date;

import com.baidu.beidou.cprounit.bo.UnitMaterView;

/**
 * Besc 物料 bean
 * 
 * @author lixukun
 * @date 2014-03-10
 */
public class BescCreativeInfo {
	private Long id;
	private Date chaTime;

	private Integer wuliaoType; // tinyint(3) 否 物料类型（文字为1/图片为2/flash为3）
	private String targetUrl; // varchar(1024) 否 点击url
	
	private Integer height; // smallint(5) 是 NULL
	private Integer width; // smallint(5) 是 NULL 图片或多媒体广告展现的宽度，文字广告这个字段为空。

	private Integer userId; //add by chongjie since 535

	private Long mcId = 0L; // UBMC物料ID
	private Integer mcVersionId = 0; // UBMC物料ID指定的版本ID
	
	private int confidence_level;	// 置信度分档：0代表未评定，1代表低，2代表中，3代表高
	private int beauty_level;		// 美观度分档：0代表未评定，1代表低，2代表中，3代表高
	private int cheat_level;		// 欺诈度分档：0代表未评定，1代表是，2代表否
	private int vulgar_level;		// 低俗度分档：0代表未评定，1代表低，2代表中，3代表高
	
	private int newAdTradeId;		// 新的行业id
	
	private String website;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the chaTime
	 */
	public Date getChaTime() {
		return chaTime;
	}

	/**
	 * @param chaTime the chaTime to set
	 */
	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}

	/**
	 * @return the wuliaoType
	 */
	public Integer getWuliaoType() {
		return wuliaoType;
	}

	/**
	 * @param wuliaoType the wuliaoType to set
	 */
	public void setWuliaoType(Integer wuliaoType) {
		this.wuliaoType = wuliaoType;
	}

	/**
	 * @return the targetUrl
	 */
	public String getTargetUrl() {
		return targetUrl;
	}

	/**
	 * @param targetUrl the targetUrl to set
	 */
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * @return the mcId
	 */
	public Long getMcId() {
		return mcId;
	}

	/**
	 * @param mcId the mcId to set
	 */
	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}

	/**
	 * @return the mcVersionId
	 */
	public Integer getMcVersionId() {
		return mcVersionId;
	}

	/**
	 * @param mcVersionId the mcVersionId to set
	 */
	public void setMcVersionId(Integer mcVersionId) {
		this.mcVersionId = mcVersionId;
	}

	/**
	 * @return the confidence_level
	 */
	public int getConfidence_level() {
		return confidence_level;
	}

	/**
	 * @param confidence_level the confidence_level to set
	 */
	public void setConfidence_level(int confidence_level) {
		this.confidence_level = confidence_level;
	}

	/**
	 * @return the beauty_level
	 */
	public int getBeauty_level() {
		return beauty_level;
	}

	/**
	 * @param beauty_level the beauty_level to set
	 */
	public void setBeauty_level(int beauty_level) {
		this.beauty_level = beauty_level;
	}

	/**
	 * @return the cheat_level
	 */
	public int getCheat_level() {
		return cheat_level;
	}

	/**
	 * @param cheat_level the cheat_level to set
	 */
	public void setCheat_level(int cheat_level) {
		this.cheat_level = cheat_level;
	}

	/**
	 * @return the vulgar_level
	 */
	public int getVulgar_level() {
		return vulgar_level;
	}

	/**
	 * @param vulgar_level the vulgar_level to set
	 */
	public void setVulgar_level(int vulgar_level) {
		this.vulgar_level = vulgar_level;
	}

	/**
	 * @return the newAdTradeId
	 */
	public int getNewAdTradeId() {
		return newAdTradeId;
	}

	/**
	 * @param newAdTradeId the newAdTradeId to set
	 */
	public void setNewAdTradeId(int newAdTradeId) {
		this.newAdTradeId = newAdTradeId;
	}

	/**
	 * @return the website
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * @param website the website to set
	 */
	public void setWebsite(String website) {
		this.website = website;
	}
	
	public static BescCreativeInfo fromUnitMaterView(BescCreativeInfo src, UnitMaterView mater) {
		if (src == null) {
			src = new BescCreativeInfo();
		}
		
		src.setBeauty_level(mater.getBeauty_level());
		src.setChaTime(mater.getChaTime());
		src.setCheat_level(mater.getCheat_level());
		src.setConfidence_level(mater.getConfidence_level());
		src.setHeight(mater.getHeight());
		src.setWidth(mater.getWidth());
		src.setId(mater.getId());
		src.setMcId(mater.getMcId());
		src.setMcVersionId(mater.getMcVersionId());
		src.setNewAdTradeId(mater.getNewAdTradeId());
		src.setTargetUrl(mater.getTargetUrl());
		src.setUserId(mater.getUserId());
		src.setVulgar_level(mater.getVulgar_level());
		src.setWuliaoType(mater.getWuliaoType());
		
		return src;
	}
}
