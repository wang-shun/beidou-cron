package com.baidu.beidou.cprounit.icon.bo;

import java.util.Date;
import com.google.common.base.Objects;
/**
 * 系统图标对象
 * @author tiejing
 *
 */
public class SystemIcon {
	private Long id;//自增
	private String fileSrc;
	private Integer firstTradeId;
	private Integer secondTradeId;
	private Integer purposeId;
	private String tags;
	private Integer hight;
	private Integer width;
	private Date addTime;
	private Integer usedSum;
	private Integer ubmcsyncflag;
	private Long mcId;
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@")
				.append(Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("fileSrc").append("='").append(getFileSrc()).append("' ");
		buffer.append("firstTradeId").append("='").append(getFirstTradeId()).append("' ");
		buffer.append("secondTradeId").append("='").append(getSecondTradeId()).append("' ");
		buffer.append("purposeId").append("='").append(getPurposeId()).append("' ");
		buffer.append("tags").append("='").append(getTags()).append("' ");
		buffer.append("hight").append("='").append(getHight()).append("' ");
		buffer.append("width").append("='").append(getWidth()).append("' ");
		buffer.append("addTime").append("='").append(getAddTime()).append("' ");
		buffer.append("usedSum").append("='").append(getUsedSum()).append("' ");
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
		if (!(other instanceof SystemIcon))
			return false;
		SystemIcon castOther = (SystemIcon) other;

		return Objects.equal(this.getId(),castOther.getId());
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + (int) this.getId().intValue();
		return result;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}

	public Integer getFirstTradeId() {
		return firstTradeId;
	}

	public void setFirstTradeId(Integer firstTradeId) {
		this.firstTradeId = firstTradeId;
	}

	public Integer getSecondTradeId() {
		return secondTradeId;
	}

	public void setSecondTradeId(Integer secondTradeId) {
		this.secondTradeId = secondTradeId;
	}

	public Integer getPurposeId() {
		return purposeId;
	}

	public void setPurposeId(Integer purposeId) {
		this.purposeId = purposeId;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
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

	public Integer getUsedSum() {
		return usedSum;
	}

	public void setUsedSum(Integer usedSum) {
		this.usedSum = usedSum;
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
