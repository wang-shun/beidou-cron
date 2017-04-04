package com.baidu.beidou.auditmanager.vo;

public class KeyForUrl {
	private Integer userId;
	
	private String url;
	
	public KeyForUrl(Integer userId, String url) {
		this.userId = userId;
		this.url = url;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (other == null) {
			return false;
		}
		
		if (!(other instanceof KeyForUrl)) {
			return false;
		}
	
		KeyForUrl castOther = (KeyForUrl) other;
		if (this.getUserId().equals(castOther.getUserId())
				&& this.getUrl().equalsIgnoreCase(castOther.getUrl())) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
		}

	public String toString() {
		return "[userId=" + this.getUserId() 
				+ ",url=" + this.getUrl() + "]";
	}
}
