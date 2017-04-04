/**
 * TencentCreative.java 
 */
package com.baidu.beidou.bes.tencent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lixukun
 * @date 2014-02-20
 */
public class TencentCreative {
	private long creativeId;
	private List<String> fileUrls;
	private String targetUrl;
	private String clientName;
	private long mcId;
	private int mcVersion;
	
	public TencentCreative() {
		fileUrls = new ArrayList<String>();
	}
	
	/**
	 * @return the creativeId
	 */
	public long getCreativeId() {
		return creativeId;
	}


	/**
	 * @param creativeId the creativeId to set
	 */
	public void setCreativeId(long creativeId) {
		this.creativeId = creativeId;
	}


	/**
	 * @return the fileUrls
	 */
	public List<String> getFileUrls() {
		return fileUrls;
	}


	/**
	 * @param fileUrls the fileUrls to set
	 */
	public void setFileUrls(List<String> fileUrls) {
		this.fileUrls = fileUrls;
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
	 * @return the clientName
	 */
	public String getClientName() {
		return clientName;
	}


	/**
	 * @param clientName the clientName to set
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * @return the mcId
	 */
	public long getMcId() {
		return mcId;
	}

	/**
	 * @param mcId the mcId to set
	 */
	public void setMcId(long mcId) {
		this.mcId = mcId;
	}

	/**
	 * @return the mcVersion
	 */
	public int getMcVersion() {
		return mcVersion;
	}

	/**
	 * @param mcVersion the mcVersion to set
	 */
	public void setMcVersion(int mcVersion) {
		this.mcVersion = mcVersion;
	}
	
	public void addFileUrl(String fileUrl) {
		if (fileUrl == null) {
			return;
		}
		if (fileUrls == null) {
			fileUrls = new ArrayList<String>();
		}
		fileUrls.add(fileUrl);
	}
}
