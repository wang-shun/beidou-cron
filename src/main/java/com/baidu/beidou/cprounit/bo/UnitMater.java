package com.baidu.beidou.cprounit.bo;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;

/**
 * @author liuhao
 */
public class UnitMater implements java.io.Serializable {

	private Long id;
	private Long wid; // bigint(20) 否 物料id
	private Integer wuliaoType; // tinyint(3) 否 物料类型（文字为1/图片为2/flash为3）
	private String keyword; // varchar(1000) 是 NULL
							// 推广单元主题词，以‘|’分隔各个关键词，结尾一个’|’，如果用户输入中有’|’，则在beidou的web端替换为‘
							// ’(空格)，并在页面上给出提示。Beidou中只负责将用户输入的可见的“|”，至于汉字中隐藏的“|”，需要cpro进行处理。
	private Integer iftitle = 0; // tinyint(3) 否 是否允许只出 标题，（只对文字广告有效），0否，1：是
	private String title; // varchar(40) 否 标题
	private String description1; // varchar(80) 是 NULL 文字广告的描述1(图片和flash都不含)
	private String description2; // varchar(80) 是 NULL 文字广告的描述2，
									// (图片和flash都不含)
	private String showUrl; // varchar(35) 是 NULL 显示的URL地址，不包括“http(s)://”
	private String targetUrl; // varchar(1024) 否 点击url
	private String fileSrc; // varchar(255) 否 广告物料的文件源地址。对于文字广告，该字段为空
	private Integer player; // tinyint(3) 是 NULL
							// 播放器版本，当用户端的播放器版本大于广告的player值，才能展现该flash广告，非flash广告时，该字段为空
	private Integer height; // smallint(5) 是 NULL
	private Integer width; // smallint(5) 是 NULL 图片或多媒体广告展现的宽度，文字广告这个字段为空。
	private Integer syncflag; // tinyint(3) 否 该推广单元在物料中心，是否已经同步
	private Integer adtradeid;
	private Unit unit;//对应的推广创意		
	private byte[] data;//物料所对应的图片数据

	private String wirelessShowUrl; // 无线的显示url add for bmob
	private String wirelessTargetUrl; // 无线的点击curl add for bmob
	
	private Integer ubmcsyncflag = CproUnitConstant.UBMC_SYNC_FLAG_YES; // 是否已在UBMC同步：0表示未同步，1表示已同步
	private Long mcId = 0L; // UBMC物料ID
	private Integer mcVersionId = 0; // UBMC物料ID指定的版本ID
	
	private String fileSrcMd5; // UBMC中fileSrc指定的多媒体图片的md5
	
	/**
	 * 记录该物料在DR-MC中占有的正式物料id，在wid不是正式物料id时，
	 * 该字段记录生效时使用的正式物料id, <=0表示没有正式物料id，需要新建
	 */
	private Long fwid = 0L;

	public UnitMater() {
	}

	public UnitMater(Long id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the wid
	 */
	public Long getWid() {
		return wid;
	}

	/**
	 * @param wid the wid to set
	 */
	public void setWid(Long wid) {
		this.wid = wid;
	}

	/**
	 * @return the wuliaoType
	 */
	public Integer getWuliaoType() {
		return wuliaoType;
	}

	/**
	 * @param wuliaoType the wuliaoType to set
	 */
	public void setWuliaoType(Integer wuliaoType) {
		this.wuliaoType = wuliaoType;
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	/**
	 * @return the iftitle
	 */
	public Integer getIftitle() {
		return CproUnitConstant.UNIT_NOT_IFTITLE;
	}

	/**
	 * @param iftitle the iftitle to set
	 */
	public void setIftitle(Integer iftitle) {
		this.iftitle = iftitle;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description1
	 */
	public String getDescription1() {
		return description1;
	}

	/**
	 * @param description1 the description1 to set
	 */
	public void setDescription1(String description1) {
		this.description1 = description1;
	}

	/**
	 * @return the description2
	 */
	public String getDescription2() {
		return description2;
	}

	/**
	 * @param description2 the description2 to set
	 */
	public void setDescription2(String description2) {
		this.description2 = description2;
	}

	/**
	 * @return the showUrl
	 */
	public String getShowUrl() {
		return showUrl;
	}

	/**
	 * @param showUrl the showUrl to set
	 */
	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
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
	 * @return the fileSrc
	 */
	public String getFileSrc() {
		return fileSrc;
	}

	/**
	 * @param fileSrc the fileSrc to set
	 */
	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}

	/**
	 * @return the player
	 */
	public Integer getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(Integer player) {
		this.player = player;
	}

	/**
	 * @return the height
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	/**
	 * @return the syncflag
	 */
	public Integer getSyncflag() {
		return CproUnitConstant.UNIT_SYN;
	}

	/**
	 * @param syncflag the syncflag to set
	 */
	public void setSyncflag(Integer syncflag) {
		this.syncflag = syncflag;
	}

	/**
	 * toString
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(
				Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("wid").append("='").append(getWid()).append("' ");
		buffer.append("wuliaoType").append("='").append(getWuliaoType())
				.append("' ");
		buffer.append("fileSrc").append("='").append(getFileSrc()).append("' ");
		buffer.append("syncflag").append("='").append(getSyncflag()).append(
				"' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof UnitMater))
			return false;
		UnitMater castOther = (UnitMater) other;

		return (this.getId().longValue() == castOther.getId().longValue());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getId() == null ? 0 : this.getId().hashCode());
		return result;
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public Integer getAdtradeid() {
		return adtradeid;
	}

	public void setAdtradeid(Integer adtradeid) {
		this.adtradeid = adtradeid;
	}

	/**
	 * @return the fwid
	 */
	public Long getFwid() {
		return fwid;
	}

	/**
	 * @param fwid the fwid to set
	 */
	public void setFwid(Long fwid) {
		this.fwid = fwid;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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

	public String getFileSrcMd5() {
		return fileSrcMd5;
	}

	public void setFileSrcMd5(String fileSrcMd5) {
		this.fileSrcMd5 = fileSrcMd5;
	}
}
