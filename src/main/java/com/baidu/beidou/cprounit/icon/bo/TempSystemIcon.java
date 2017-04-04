package com.baidu.beidou.cprounit.icon.bo;

/**
 * 入库时临时封装的系统图标对象
 * @author tiejing
 *
 */
public class TempSystemIcon {
	private String firstTrade;
	private String secondTrade;
	private String purpose;
	private String tags;
	private Integer hight;
	private Integer width;
	private Long mcId;
	
	public String getFirstTrade() {
		return firstTrade;
	}

	public void setFirstTrade(String firstTrade) {
		this.firstTrade = firstTrade;
	}

	public String getSecondTrade() {
		return secondTrade;
	}

	public void setSecondTrade(String secondTrade) {
		this.secondTrade = secondTrade;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
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

	public Long getMcId() {
		return mcId;
	}

	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}

	/**
     * toString
     * @return String
     */
     public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("wid").append("='").append(getMcId()).append("' ");
      buffer.append("firstTrade").append("='").append(getFirstTrade()).append("' ");
      buffer.append("secondTrade").append("='").append(getSecondTrade()).append("' ");
      buffer.append("purpose").append("='").append(getPurpose()).append("' ");
      buffer.append("tags").append("='").append(getTags()).append("' ");
      buffer.append("hight").append("='").append(getHight()).append("' ");
      buffer.append("width").append("='").append(getWidth()).append("' ");
      buffer.append("]");
      
      return buffer.toString();
     }
}
