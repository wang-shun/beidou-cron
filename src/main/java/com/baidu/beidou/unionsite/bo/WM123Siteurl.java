package com.baidu.beidou.unionsite.bo;

/**
 * 
 * WM123为input司南系统用的siteurl模型
 * 
 * @author zhangpeng
 *
 */
public class WM123Siteurl {

	/**
	 * 原域名
	 */
	private String ori_url;
	
	/**
	 * 如果域名为主域，加入的www.前缀后的url
	 */
	private String www_url;
	
	public WM123Siteurl(){
		
	}

	public String getOri_url() {
		return ori_url;
	}

	public void setOri_url(String ori_url) {
		this.ori_url = ori_url;
	}

	public String getWww_url() {
		return www_url;
	}

	public void setWww_url(String www_url) {
		this.www_url = www_url;
	}
	
}
