package com.baidu.beidou.cprounit.bo;


public class CproUnit implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6615023925930181201L;

	private Integer wuliaoType; // tinyint(3) 否 物料类型（文字为1/图片为2/flash为3）
	private String title; // varchar(40) 否 标题
	private String description1; // varchar(80) 是 NULL 文字广告的描述1(图片和flash都不含)
	private String description2; // varchar(80) 是 NULL 文字广告的描述2，
									// (图片和flash都不含)	
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getWuliaoType() {
		return wuliaoType;
	}
	public void setWuliaoType(Integer wuliaoType) {
		this.wuliaoType = wuliaoType;
	}	
}