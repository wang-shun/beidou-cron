/**
 * 2009-4-21 上午10:16:06
 */
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class UnionSiteBo implements Serializable {

	private static final long serialVersionUID = 8186636069485702417L;
	private String cname = null;
	private String siteUrl = null;
	private String siteName = null;
	private String siteDesc = null;
	private int firstTradeId =0;	///0表示没有一级行业
	private int sencondTradeId = 0; ///0表示没有二级行业
	private Byte certification = null;
	private Byte finanobj = null;
	private Integer credit = null;
	private Byte direct =null;
	private Integer channel = null;
	private Integer cheats = null;
	private String filter = null;
	
	private int showFlag = 0; //显示标识，1表示优先，0表示无优先（可随机选取一条）
	
	private int siteSource = 1; // 站点来源：整数，按位表示：1代表百度联盟站点，2代表来源于google流量
	
	/**
	 * @return the cname 计费名
	 */
	public String getCname() {
		return cname;
	}
	/**
	 * @param cname the cname to set
	 */
	public void setCname(String cname) {
		this.cname = cname;
	}
	/**
	 * @return the domain 站点url,一级域名或者二级域名，不包含http://www.
	 */
	public String getSiteUrl() {
		return siteUrl;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setSiteUrl(String domain) {
		this.siteUrl = domain;
	}
	/**
	 * @return the siteName 站点名称
	 */
	public String getSiteName() {
		return siteName;
	}
	/**
	 * @param siteName the siteName to set
	 */
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	/**
	 * @return the siteDesc 网站描述
	 */
	public String getSiteDesc() {
		return siteDesc;
	}
	/**
	 * @param siteDesc the siteDesc to set
	 */
	public void setSiteDesc(String siteDesc) {
		this.siteDesc = siteDesc;
	}
	/**
	 * @return the firstTradeId 一级行业id
	 */
	public int getFirstTradeId() {
		return firstTradeId;
	}
	/**
	 * @param firstTradeId the firstTradeId to set
	 */
	public void setFirstTradeId(int firstTradeId) {
		this.firstTradeId = firstTradeId;
	}
	/**
	 * @return the sencondTradeId 二级行业id
	 */
	public int getSencondTradeId() {
		return sencondTradeId;
	}
	/**
	 * @param sencondTradeId the sencondTradeId to set
	 */
	public void setSencondTradeId(int sencondTradeId) {
		this.sencondTradeId = sencondTradeId;
	}
	/**
	 * @return the certification 大联盟认证
	 */
	public Byte getCertification() {
		return certification;
	}
	/**
	 * @param certification the certification to set
	 */
	public void setCertification(Byte certification) {
		this.certification = certification;
	}
	/**
	 * @return the finanobj 财务对象
	 */
	public Byte getFinanobj() {
		return finanobj;
	}
	/**
	 * @param finanobj the finanobj to set
	 */
	public void setFinanobj(Byte finanobj) {
		this.finanobj = finanobj;
	}
	
	/**
	 * @return the credit 信誉等级/指数
	 */
	public Integer getCredit() {
		return credit;
	}
	
	/**
	 * @param credit the credit to set
	 */
	public void setCredit(Integer credit) {
		this.credit = credit;
	}
	/**
	 * @return the direct 直营/二级
	 */
	public Byte getDirect() {
		return direct;
	}
	/**
	 * @param direct the direct to set
	 */
	public void setDirect(Byte direct) {
		this.direct = direct;
	}
	/**
	 * @return the channel 通路
	 */
	public Integer getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(Integer channel) {
		this.channel = channel;
	}
	/**
	 * @return the cheats 作弊次数
	 */
	public Integer getCheats() {
		return cheats;
	}
	/**
	 * @param cheats the cheats to set
	 */
	public void setCheats(Integer cheats) {
		this.cheats = cheats;
	}
	/**
	 * @return the filter 过滤需求
	 */
	public String getFilter() {
		return filter;
	}
	/**
	 * @param filter the filter to set
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public int getShowFlag() {
		return showFlag;
	}
	public void setShowFlag(int showFlag) {
		this.showFlag = showFlag;
	}
	public int getSiteSource() {
		return siteSource;
	}
	public void setSiteSource(int siteSource) {
		this.siteSource = siteSource;
	}
}
