package com.baidu.beidou.cprounit.icon.bo;

import java.util.Date;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;

/**
 * 用户上传历史图标数据库对象
 * 
 * @author tiejing
 * 
 */
public class UserUploadIcon {
	private Integer id;
	private Integer userId;
	private Long wid;
	private String fileSrc;
	private Integer hight;
	private Integer width;
	private Date addTime;

	private Integer ubmcsyncflag = CproUnitConstant.UBMC_SYNC_FLAG_NO; // 是否已在UBMC同步：0表示未同步，1表示已同步
	private Long mcId = 0L; // UBMC物料ID
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("userId").append("='").append(getUserId()).append("' ");
		buffer.append("wid").append("='").append(getWid()).append("' ");
		buffer.append("fileSrc").append("='").append(getFileSrc()).append("' ");
		buffer.append("hight").append("='").append(getHight()).append("' ");
		buffer.append("width").append("='").append(getWidth()).append("' ");
		buffer.append("addTime").append("='").append(getAddTime()).append("' ");
		buffer.append("ubmcsyncflag").append("='").append(getUbmcsyncflag()).append("' ");
		buffer.append("mcId").append("='").append(getMcId()).append("' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UserUploadIcon))
			return false;
		UserUploadIcon castOther = (UserUploadIcon) other;

		return (this.getId() == castOther.getId());
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + (int) this.getId();
		return result;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Long getWid() {
		return wid;
	}

	public void setWid(Long wid) {
		this.wid = wid;
	}

	public String getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}

	public Integer getHight() {
		return hight;
	}

	public void setHight(Integer hight) {
		this.hight = hight;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Integer getUbmcsyncflag() {
		return ubmcsyncflag;
	}

	public void setUbmcsyncflag(Integer ubmcsyncflag) {
		this.ubmcsyncflag = ubmcsyncflag;
	}

	public Long getMcId() {
		return mcId;
	}

	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}
}
