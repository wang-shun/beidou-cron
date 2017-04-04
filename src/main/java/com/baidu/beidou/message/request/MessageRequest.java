package com.baidu.beidou.message.request;

/**
 * 由于bigpipe要求，所有变量定义成String
 * @author work
 *
 */
public class MessageRequest {
	private String uuid;
	private String userid;
	private String typeid;
	private String time;
	private String appid;
	private Content content;
	
	public MessageRequest() {
	
	}
	
	
	public MessageRequest(String uuid, String userid, String typeid,
			String time, String appid, Content content) {
		this.uuid = uuid;
		this.userid = userid;
		this.typeid = typeid;
		this.time = time;
		this.appid = appid;
		this.content = content;
	}


	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getTypeid() {
		return typeid;
	}

	public void setTypeid(String typeid) {
		this.typeid = typeid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
}
