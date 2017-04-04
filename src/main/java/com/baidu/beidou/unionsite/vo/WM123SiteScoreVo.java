package com.baidu.beidou.unionsite.vo;

/**
 * ClassName: WM123SiteScoreVo
 * 记录网盟站点得分的vo
 * 
 * @author   lvzichan
 * @since	2013-08-01
 */
public class WM123SiteScoreVo{

	private String siteUrl;		//网站地址
	private Integer siteId;		//网站id
	private float scoreImpact;	//网站影响力得分
	private float scoreTraffic;	//网盟流量大小得分
	private float scoreObvious;	//推广位醒目度得分
	private float scoreQuality;	//站点星级得分
	private int scoreTotal;		//网站总得分
	
    public WM123SiteScoreVo(){}

	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public float getScoreImpact() {
		return scoreImpact;
	}

	public void setScoreImpact(float scoreImpact) {
		this.scoreImpact = scoreImpact;
	}

	public float getScoreTraffic() {
		return scoreTraffic;
	}

	public void setScoreTraffic(float scoreTraffic) {
		this.scoreTraffic = scoreTraffic;
	}

	public float getScoreObvious() {
		return scoreObvious;
	}

	public void setScoreObvious(float scoreObvious) {
		this.scoreObvious = scoreObvious;
	}

	public float getScoreQuality() {
		return scoreQuality;
	}

	public void setScoreQuality(float scoreQuality) {
		this.scoreQuality = scoreQuality;
	}

	public int getScoreTotal() {
		return scoreTotal;
	}

	public void setScoreTotal(int scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	@Override
	public String toString() {
		return "WM123SiteScoreVo [siteUrl=" + siteUrl + ", siteId=" + siteId
				+ ", scoreImpact=" + scoreImpact + ", scoreTraffic="
				+ scoreTraffic + ", scoreObvious=" + scoreObvious
				+ ", scoreQuality=" + scoreQuality + ", scoreTotal="
				+ scoreTotal + "]";
	}
}