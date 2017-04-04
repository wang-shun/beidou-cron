package com.baidu.beidou.cprounit.mcdriver.bean.response;

public class MaterResult {
	private long mcId;
	private int versionId;
	private int width;
	private int height;
	private String url;
	
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public long getMcId() {
		return mcId;
	}
	public void setMcId(long mcId) {
		this.mcId = mcId;
	}
	public int getVersionId() {
		return versionId;
	}
	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}
}
