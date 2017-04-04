package com.baidu.beidou.cprounit.mcdriver.bean.response;

import java.util.List;

public class MaterSuiteResult {
	private long amId;
	private String amName;
	private String abbUrl;
	private List<MaterResult> suite;
	
	public String getAbbUrl() {
		return abbUrl;
	}
	public void setAbbUrl(String abbUrl) {
		this.abbUrl = abbUrl;
	}
	public long getAmId() {
		return amId;
	}
	public void setAmId(long amId) {
		this.amId = amId;
	}
	public String getAmName() {
		return amName;
	}
	public void setAmName(String amName) {
		this.amName = amName;
	}
	public List<MaterResult> getSuite() {
		return suite;
	}
	public void setSuite(List<MaterResult> suite) {
		this.suite = suite;
	}
	
}
