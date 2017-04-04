package com.baidu.beidou.cprounit.bo;

import java.util.Date;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;

public class UnitMaterView {

	private Long id;
	private Date chaTime;

	private Long wid; // bigint(20) 否 物料id
	private Integer wuliaoType; // tinyint(3) 否 物料类型（文字为1/图片为2/flash为3）
	private String title; // varchar(40) 否 标题
	private String description1; // varchar(80) 是 NULL 文字广告的描述1(图片和flash都不含)
	private String description2; // varchar(80) 是 NULL 文字广告的描述2，
									// (图片和flash都不含)
	private String showUrl; // varchar(35) 是 NULL 显示的URL地址，不包括“http(s)://”
	private String targetUrl; // varchar(1024) 否 点击url
	private String fileSrc; // varchar(255) 否 广告物料的文件源地址。对于文字广告，该字段为空
	private String fileSrcMd5; // 图片或者flash物料的md5
	
	private Integer height; // smallint(5) 是 NULL
	private Integer width; // smallint(5) 是 NULL 图片或多媒体广告展现的宽度，文字广告这个字段为空。

	private Integer userId; //add by chongjie since 535

	private String wirelessShowUrl; // 无线的显示url add for bmob
	private String wirelessTargetUrl; // 无线的点击curl add for bmob
	
	private Integer ubmcsyncflag = CproUnitConstant.UBMC_SYNC_FLAG_NO; // 是否已在UBMC同步：0表示未同步，1表示已同步
	private Long mcId = 0L; // UBMC物料ID
	private Integer mcVersionId = 0; // UBMC物料ID指定的版本ID
	
	private int confidence_level;	// 置信度分档：0代表未评定，1代表低，2代表中，3代表高
	private int beauty_level;		// 美观度分档：0代表未评定，1代表低，2代表中，3代表高
	private int cheat_level;		// 欺诈度分档：0代表未评定，1代表是，2代表否
	private int vulgar_level;		// 低俗度分档：0代表未评定，1代表低，2代表中，3代表高
	
	private int newAdTradeId;		// 新的行业id
	
	private byte[] data;
	
	private int gid;
	private int pid;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getWid() {
		return wid;
	}

	public void setWid(Long wid) {
		this.wid = wid;
	}

	public Integer getWuliaoType() {
		return wuliaoType;
	}

	public void setWuliaoType(Integer wuliaoType) {
		this.wuliaoType = wuliaoType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription1() {
		return description1;
	}

	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	public String getDescription2() {
		return description2;
	}

	public void setDescription2(String description2) {
		this.description2 = description2;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}


	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getWirelessShowUrl() {
		return wirelessShowUrl;
	}

	public void setWirelessShowUrl(String wirelessShowUrl) {
		this.wirelessShowUrl = wirelessShowUrl;
	}

	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}

	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}
	
	public Integer getUbmcsyncflag() {
		return ubmcsyncflag;
	}

	/**
	 * @return the gid
	 */
	public int getGid() {
		return gid;
	}

	/**
	 * @param gid the gid to set
	 */
	public void setGid(int gid) {
		this.gid = gid;
	}

	/**
	 * @return the pid
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(int pid) {
		this.pid = pid;
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

	public Integer getMcVersionId() {
		return mcVersionId;
	}

	public void setMcVersionId(Integer mcVersionId) {
		this.mcVersionId = mcVersionId;
	}
	
	public Date getChaTime() {
		return chaTime;
	}

	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}
	
	public String getFileSrcMd5() {
		return fileSrcMd5;
	}

	public void setFileSrcMd5(String fileSrcMd5) {
		this.fileSrcMd5 = fileSrcMd5;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * @return the confidence_level
	 */
	public int getConfidence_level() {
		return confidence_level;
	}

	/**
	 * @param confidence_level the confidence_level to set
	 */
	public void setConfidence_level(int confidence_level) {
		this.confidence_level = confidence_level;
	}

	/**
	 * @return the beauty_level
	 */
	public int getBeauty_level() {
		return beauty_level;
	}

	/**
	 * @param beauty_level the beauty_level to set
	 */
	public void setBeauty_level(int beauty_level) {
		this.beauty_level = beauty_level;
	}

	/**
	 * @return the cheat_level
	 */
	public int getCheat_level() {
		return cheat_level;
	}

	/**
	 * @param cheat_level the cheat_level to set
	 */
	public void setCheat_level(int cheat_level) {
		this.cheat_level = cheat_level;
	}

	/**
	 * @return the vulgar_level
	 */
	public int getVulgar_level() {
		return vulgar_level;
	}

	/**
	 * @param vulgar_level the vulgar_level to set
	 */
	public void setVulgar_level(int vulgar_level) {
		this.vulgar_level = vulgar_level;
	}

	/**
	 * @return the newAdTradeId
	 */
	public int getNewAdTradeId() {
		return newAdTradeId;
	}

	/**
	 * @param newAdTradeId the newAdTradeId to set
	 */
	public void setNewAdTradeId(int newAdTradeId) {
		this.newAdTradeId = newAdTradeId;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("wid").append("='").append(getWid()).append("' ");
		buffer.append("wuliaoType").append("='").append(getWuliaoType()).append("' ");
		buffer.append("fileSrc").append("='").append(getFileSrc()).append("' ");
		buffer.append("ubmcsyncflag").append("='").append(getUbmcsyncflag()).append("' ");
		buffer.append("mcId").append("='").append(getMcId()).append("' ");
		buffer.append("mcVersionId").append("='").append(getMcVersionId()).append("' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UnitMaterView))
			return false;
		UnitMaterView castOther = (UnitMaterView) other;

		return (this.getId().longValue() == castOther.getId().longValue());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getId() == null ? 0 : this.getId().hashCode());
		return result;
	}

}
