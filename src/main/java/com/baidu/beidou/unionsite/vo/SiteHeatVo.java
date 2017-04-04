package com.baidu.beidou.unionsite.vo;

/**
 * WM123用网站热度
 *
 * @author   <a href="mailto:zhangxu04@baidu.com">张旭</a>
 */
public class SiteHeatVo{
	
	/**
	 * 网站id
	 */
	private int siteid;
	
	/**
	 * 选择该网站的用户数
	 */
	private int siteUserNum;
	
	/**
	 * 选择该行业的用户数
	 */
	private int tradeUserNum;
	
	/**
	 * 热度得分，最终会存储到beidouurl库中unionsiteadditinalstat中的cmpdegree字段
	 */
	private int score;
	
	public SiteHeatVo(){
		
	}

	public int getSiteid() {
		return siteid;
	}

	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}

	public int getSiteUserNum() {
		return siteUserNum;
	}

	public void setSiteUserNum(int siteUserNum) {
		this.siteUserNum = siteUserNum;
	}

	public int getTradeUserNum() {
		return tradeUserNum;
	}

	public void setTradeUserNum(int tradeUserNum) {
		this.tradeUserNum = tradeUserNum;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	
}
