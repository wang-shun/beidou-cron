package com.baidu.beidou.unionsite.vo;

/**
 * 站点每天推广数据
 * 
 * @author lvzichan
 * @since 2013-10-10
 */
public class WM123SiteCprodataVo {

	private String siteUrl; // 网站地址
	private Integer siteId; // 网站id
	private String insertDate; // 数据日期
	private float cpm; // 千次展现成本
	private float ctr; // 点击率
	private Integer uv; // 点击独立访客数
	private Integer click; // 点击次数
	private String hourClick; // 分小时点击次数

	public WM123SiteCprodataVo() {
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	public String getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(String insertDate) {
		this.insertDate = insertDate;
	}

	public float getCpm() {
		return cpm;
	}

	public void setCpm(float cpm) {
		this.cpm = cpm;
	}

	public float getCtr() {
		return ctr;
	}

	public void setCtr(float ctr) {
		this.ctr = ctr;
	}

	public Integer getUv() {
		return uv;
	}

	public void setUv(Integer uv) {
		this.uv = uv;
	}

	public Integer getClick() {
		return click;
	}

	public void setClick(Integer click) {
		this.click = click;
	}

	public String getHourClick() {
		return hourClick;
	}

	public void setHourClick(String hourClick) {
		this.hourClick = hourClick;
	}

	@Override
	public String toString() {
		return "WM123SiteCprodataVo [siteUrl=" + siteUrl + ", siteId=" + siteId
				+ ", insertDate=" + insertDate + ", cpm=" + cpm + ", ctr="
				+ ctr + ", uv=" + uv + ", click=" + click + ", hourClick="
				+ hourClick + "]";
	}

	public String toStringInFile() {
		return siteUrl + "\t" + siteId + "\t" + insertDate + "\t" + cpm + "\t"
				+ ctr + "\t" + uv + "\t" + click + "\t" + hourClick;
	}
}