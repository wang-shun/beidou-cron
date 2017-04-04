/**
 * 2009-1-4 下午01:31:38
 */
package com.baidu.beidou.util.akadriver.bo;

/**
 * @author zengyunfeng
 * @version 1.1.0
 */
public class AkaBeidouResult {

	/**
	 * aka返回的标识
	 */
	private long token;
	/**
	 * 错误信息
	 */
	private String msg;
	
	private String titleError;
	private String desc1Error;
	private String desc2Error;
	private String urlError;
	private String showUrlError;
	
	/**
	 * added by genglei
	 * 为aka轮训有效性广告时返回结果使用
	 */
	private int patrolFlag;
	
	public long getToken() {
		return token;
	}
	public void setToken(long token) {
		this.token = token;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getTitleError() {
		return titleError;
	}
	public void setTitleError(String titleError) {
		this.titleError = titleError;
	}
	public String getDesc1Error() {
		return desc1Error;
	}
	public void setDesc1Error(String desc1Error) {
		this.desc1Error = desc1Error;
	}
	public String getDesc2Error() {
		return desc2Error;
	}
	public void setDesc2Error(String desc2Error) {
		this.desc2Error = desc2Error;
	}
	public String getUrlError() {
		return urlError;
	}
	public void setUrlError(String urlError) {
		this.urlError = urlError;
	}
	public String getShowUrlError() {
		return showUrlError;
	}
	public void setShowUrlError(String showUrlError) {
		this.showUrlError = showUrlError;
	}
	
	public int getPatrolFlag() {
		return patrolFlag;
	}
	public void setPatrolFlag(int patrolFlag) {
		this.patrolFlag = patrolFlag;
	}
}
