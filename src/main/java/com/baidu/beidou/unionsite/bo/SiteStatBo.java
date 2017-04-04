/**
 * 2009-4-23 下午02:13:29
 */
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * 命名规则按照cpro-stat中的接口文件命名，与数据库的命名不一致
 */
public class SiteStatBo implements Serializable {

	private static final long serialVersionUID = 6798993714182491073L;
	
	//联合主键
	private String cntn;
	private String domain;
	
	//物料类型，二进制编码格式，从低位到高位依次是：文字、图片
	private int wuliao;
	
	//独立IP数
	private int unique_ip;
	private int unique_cookie;
	
	//整体流量
	private int clicks;			//检索量
	private int cost;			//检索广告条数
	private long retrieve;		//点击
	private long ads;			//消费
	
	//展现类型，二进制编码格式，从低位到高位依次是：固定、悬浮、贴片
	private int dispType;
	
	//纯悬浮流量的统计信息
	private long fixedRetrieve = 0;	//检索量、固定
	private long fixedAds = 0;		//检索广告条数、固定
	private int fixedClicks = 0;		//点击、固定
	private int fixedCost = 0;		//消费、固定
	
	//纯悬浮流量的统计信息
	private long flowRetrieve = 0;	//检索量、悬浮
	private long flowAds = 0;		//检索广告条数、悬浮
	private int flowClicks = 0;		//点击、悬浮
	private int flowCost = 0;		//消费、悬浮
	
	//纯悬浮流量的统计信息
	private long filmRetrieve = 0;	//检索量、贴片
	private long filmAds = 0;		//检索广告条数、贴片
	private int filmClicks = 0;		//点击、贴片
	private int filmCost = 0;		//消费、贴片
	
	//尺寸流量map,以尺寸id为key,尺寸流量为value
	private Map<Integer, Integer> sizeFlow ;
	
	//四组计数器，在求平均时使用
	//在将7天平均数据写入文件时，不输出这4个字段
	private int totalCount;
	private int fixedCount;
	private int flowCount;
	private int filmCount;

	/**
	 * @return the cntn
	 */
	public String getCntn() {
		return cntn;
	}
	/**
	 * @param cntn the cntn to set
	 */
	public void setCntn(String cntn) {
		this.cntn = cntn;
	}
	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	/**
	 * @return the wuliao
	 */
	public int getWuliao() {
		return wuliao;
	}
	/**
	 * @param wuliao the wuliao to set
	 */
	public void setWuliao(int wuliao) {
		this.wuliao = wuliao;
	}
	/**
	 * @return the unique_ip
	 */
	public int getUnique_ip() {
		return unique_ip;
	}
	/**
	 * @param unique_ip the unique_ip to set
	 */
	public void setUnique_ip(int unique_ip) {
		this.unique_ip = unique_ip;
	}
	/**
	 * @return the unique_cookie
	 */
	public int getUnique_cookie() {
		return unique_cookie;
	}
	/**
	 * @param unique_cookie the unique_cookie to set
	 */
	public void setUnique_cookie(int unique_cookie) {
		this.unique_cookie = unique_cookie;
	}
	/**
	 * @return the clicks
	 */
	public int getClicks() {
		return clicks;
	}
	/**
	 * @param clicks the clicks to set
	 */
	public void setClicks(int clicks) {
		this.clicks = clicks;
	}
	/**
	 * @return the cost
	 */
	public int getCost() {
		return cost;
	}
	/**
	 * @param cost the cost to set
	 */
	public void setCost(int cost) {
		this.cost = cost;
	}
	/**
	 * @return the retrieve
	 */
	public long getRetrieve() {
		return retrieve;
	}
	/**
	 * @param retrieve the retrieve to set
	 */
	public void setRetrieve(long retrieve) {
		this.retrieve = retrieve;
	}
	/**
	 * @return the ads
	 */
	public long getAds() {
		return ads;
	}
	/**
	 * @param ads the ads to set
	 */
	public void setAds(long ads) {
		this.ads = ads;
	}
	/**
	 * @return the dispType
	 */
	public int getDispType() {
		return dispType;
	}
	/**
	 * @param dispType the dispType to set
	 */
	public void setDispType(int dispType) {
		this.dispType = dispType;
	}
	/**
	 * @return the fixedRetrieve
	 */
	public long getFixedRetrieve() {
		return fixedRetrieve;
	}
	/**
	 * @param fixedRetrieve the fixedRetrieve to set
	 */
	public void setFixedRetrieve(long fixedRetrieve) {
		this.fixedRetrieve = fixedRetrieve;
	}
	/**
	 * @return the fixedAds
	 */
	public long getFixedAds() {
		return fixedAds;
	}
	/**
	 * @param fixedAds the fixedAds to set
	 */
	public void setFixedAds(long fixedAds) {
		this.fixedAds = fixedAds;
	}
	/**
	 * @return the fixedClicks
	 */
	public int getFixedClicks() {
		return fixedClicks;
	}
	/**
	 * @param fixedClicks the fixedClicks to set
	 */
	public void setFixedClicks(int fixedClicks) {
		this.fixedClicks = fixedClicks;
	}
	/**
	 * @return the fixedCost
	 */
	public int getFixedCost() {
		return fixedCost;
	}
	/**
	 * @param fixedCost the fixedCost to set
	 */
	public void setFixedCost(int fixedCost) {
		this.fixedCost = fixedCost;
	}
	/**
	 * @return the flowRetrieve
	 */
	public long getFlowRetrieve() {
		return flowRetrieve;
	}
	/**
	 * @param flowRetrieve the flowRetrieve to set
	 */
	public void setFlowRetrieve(long flowRetrieve) {
		this.flowRetrieve = flowRetrieve;
	}
	/**
	 * @return the flowAds
	 */
	public long getFlowAds() {
		return flowAds;
	}
	/**
	 * @param flowAds the flowAds to set
	 */
	public void setFlowAds(long flowAds) {
		this.flowAds = flowAds;
	}
	/**
	 * @return the flowClicks
	 */
	public int getFlowClicks() {
		return flowClicks;
	}
	/**
	 * @param flowClicks the flowClicks to set
	 */
	public void setFlowClicks(int flowClicks) {
		this.flowClicks = flowClicks;
	}
	/**
	 * @return the flowCost
	 */
	public int getFlowCost() {
		return flowCost;
	}
	/**
	 * @param flowCost the flowCost to set
	 */
	public void setFlowCost(int flowCost) {
		this.flowCost = flowCost;
	}
	/**
	 * @return the filmRetrieve
	 */
	public long getFilmRetrieve() {
		return filmRetrieve;
	}
	/**
	 * @param filmRetrieve the filmRetrieve to set
	 */
	public void setFilmRetrieve(long filmRetrieve) {
		this.filmRetrieve = filmRetrieve;
	}
	/**
	 * @return the filmAds
	 */
	public long getFilmAds() {
		return filmAds;
	}
	/**
	 * @param filmAds the filmAds to set
	 */
	public void setFilmAds(long filmAds) {
		this.filmAds = filmAds;
	}
	/**
	 * @return the filmClicks
	 */
	public int getFilmClicks() {
		return filmClicks;
	}
	/**
	 * @param filmClicks the filmClicks to set
	 */
	public void setFilmClicks(int filmClicks) {
		this.filmClicks = filmClicks;
	}
	/**
	 * @return the filmCost
	 */
	public int getFilmCost() {
		return filmCost;
	}
	/**
	 * @param filmCost the filmCost to set
	 */
	public void setFilmCost(int filmCost) {
		this.filmCost = filmCost;
	}
	/**
	 * @return the sizeFlow
	 */
	public Map<Integer, Integer> getSizeFlow() {
		return sizeFlow;
	}
	/**
	 * @param sizeFlow the sizeFlow to set
	 */
	public void setSizeFlow(Map<Integer, Integer> sizeFlow) {
		this.sizeFlow = sizeFlow;
	}
	/**
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}
	/**
	 * @param totalCount the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	/**
	 * @return the fixedCount
	 */
	public int getFixedCount() {
		return fixedCount;
	}
	/**
	 * @param fixedCount the fixedCount to set
	 */
	public void setFixedCount(int fixedCount) {
		this.fixedCount = fixedCount;
	}
	/**
	 * @return the flowCount
	 */
	public int getFlowCount() {
		return flowCount;
	}
	/**
	 * @param flowCount the flowCount to set
	 */
	public void setFlowCount(int flowCount) {
		this.flowCount = flowCount;
	}
	/**
	 * @return the filmCount
	 */
	public int getFilmCount() {
		return filmCount;
	}
	/**
	 * @param filmCount the filmCount to set
	 */
	public void setFilmCount(int filmCount) {
		this.filmCount = filmCount;
	}


}
