/**
 * 2009-4-28 上午01:06:03
 */
package com.baidu.beidou.unionsite.vo;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName:SiteInfo4KeepInDB
 * Function: 站点全库入库时数据库中需要保留的字段
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-12-9
 * @since    Cpweb-215
 */
public class SiteInfo4KeepInDB implements Serializable{
	/** 网站ID */
	private int siteId;
	/**  */
	private String siteUrl;
	/** 加入时间 */
	private Date joinTime ;

	/** 截图文件名 */
	private String snapshot;
	
	/** 是否有效 */
	private boolean valid;

	public Date getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
