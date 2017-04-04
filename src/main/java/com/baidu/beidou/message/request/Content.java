package com.baidu.beidou.message.request;

public class Content {
	private String username;
	private String fromday;
	private String today;
	private String ratio;
	private String fromvalue;
	private String tovalue;
	
	
	public Content() {
	}
	public Content(String username, String fromday, String today,
			String ratio, String fromvalue, String tovalue) {
		this.username = username;
		this.fromday = fromday;
		this.today = today;
		this.ratio = ratio;
		this.fromvalue = fromvalue;
		this.tovalue = tovalue;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFromday() {
		return fromday;
	}
	public void setFromday(String fromday) {
		this.fromday = fromday;
	}
	public String getToday() {
		return today;
	}
	public void setToday(String today) {
		this.today = today;
	}
	public String getRatio() {
		return ratio;
	}
	public void setRatio(String ratio) {
		this.ratio = ratio;
	}
	public String getFromvalue() {
		return fromvalue;
	}
	public void setFromvalue(String fromvalue) {
		this.fromvalue = fromvalue;
	}
	public String getTovalue() {
		return tovalue;
	}
	public void setTovalue(String tovalue) {
		this.tovalue = tovalue;
	}
	
}
