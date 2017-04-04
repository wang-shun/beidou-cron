package com.baidu.beidou.bes.user.request;

import java.io.Serializable;
import java.util.List;

import com.baidu.beidou.bes.user.po.QualificationInfo;

/**
 * tencent json请求格式 
 * 
 * @author caichao
 */
public class TencentRequestType implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7226171467076022094L;
	private String name;
	private String url;
	private Boolean overwrite_qualification;
	private List<QualificationInfo> qualification_files;
	private String memo;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Boolean getOverwrite_qualification() {
		return overwrite_qualification;
	}
	public void setOverwrite_qualification(Boolean overwrite_qualification) {
		this.overwrite_qualification = overwrite_qualification;
	}
	public List<QualificationInfo> getQualification_files() {
		return qualification_files;
	}
	public void setQualification_files(List<QualificationInfo> qualification_files) {
		this.qualification_files = qualification_files;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	
	
}
