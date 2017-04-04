package com.baidu.beidou.bes.user.template;

import java.util.Map;

import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;
/**
 * 所有adx 公共的信息字段，通过spring 注入值
 * 
 * @author caichao
 */
public class AbstractImportMessage {
	private String adxUserAddFile;
	private String adxUserUpdateFile;
	private AuditUserServiceMgr userServiceMgr;
	private Map<String,Integer> companyMapping;
	public String getAdxUserAddFile() {
		return adxUserAddFile;
	}
	public void setAdxUserAddFile(String adxUserAddFile) {
		this.adxUserAddFile = adxUserAddFile;
	}
	public String getAdxUserUpdateFile() {
		return adxUserUpdateFile;
	}
	public void setAdxUserUpdateFile(String adxUserUpdateFile) {
		this.adxUserUpdateFile = adxUserUpdateFile;
	}
	public AuditUserServiceMgr getUserServiceMgr() {
		return userServiceMgr;
	}
	public void setUserServiceMgr(AuditUserServiceMgr userServiceMgr) {
		this.userServiceMgr = userServiceMgr;
	}
	public Map<String, Integer> getCompanyMapping() {
		return companyMapping;
	}
	public void setCompanyMapping(Map<String, Integer> companyMapping) {
		this.companyMapping = companyMapping;
	}
	
	
}
