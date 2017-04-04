package com.baidu.beidou.cprounit.bo;

import java.util.Date;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;

public class TmpUnit {

	private Long id;
	private Integer userId;
	private Date chaTime;
	
	private Long wid;
	private Integer wuliaoType;
	private String title;
	private String description1;
	private String description2;
	private String showUrl;
	private String targetUrl;
	private String fileSrc;
	private Integer height;
	private Integer width;

	private String wirelessShowUrl; // 无线的显示url add for bmob
	private String wirelessTargetUrl; // 无线的点击curl add for bmob

	private Integer ubmcsyncflag = CproUnitConstant.UBMC_SYNC_FLAG_NO; // 是否已在UBMC同步：0表示未同步，1表示已同步
	private Long mcId = 0L; // UBMC物料ID
	private Integer mcVersionId = 0; // UBMC物料ID指定的版本ID
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Date getChaTime() {
		return chaTime;
	}
	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}
	public Long getWid() {
		return wid;
	}
	public void setWid(Long wid) {
		this.wid = wid;
	}
	public Integer getWuliaoType() {
		return wuliaoType;
	}
	public void setWuliaoType(Integer wuliaoType) {
		this.wuliaoType = wuliaoType;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription1() {
		return description1;
	}
	public void setDescription1(String description1) {
		this.description1 = description1;
	}
	public String getDescription2() {
		return description2;
	}
	public void setDescription2(String description2) {
		this.description2 = description2;
	}
	public String getShowUrl() {
		return showUrl;
	}
	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}
	public String getTargetUrl() {
		return targetUrl;
	}
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	public String getFileSrc() {
		return fileSrc;
	}
	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public String getWirelessShowUrl() {
		return wirelessShowUrl;
	}
	public void setWirelessShowUrl(String wirelessShowUrl) {
		this.wirelessShowUrl = wirelessShowUrl;
	}
	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}
	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}
	public Integer getUbmcsyncflag() {
		return ubmcsyncflag;
	}
	public void setUbmcsyncflag(Integer ubmcsyncflag) {
		this.ubmcsyncflag = ubmcsyncflag;
	}
	public Long getMcId() {
		return mcId;
	}
	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}
	public Integer getMcVersionId() {
		return mcVersionId;
	}
	public void setMcVersionId(Integer mcVersionId) {
		this.mcVersionId = mcVersionId;
	}
}
