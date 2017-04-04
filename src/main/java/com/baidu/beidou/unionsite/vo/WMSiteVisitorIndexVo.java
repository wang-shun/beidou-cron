package com.baidu.beidou.unionsite.vo;



/**
 * WMSiteVisitorIndexVo
 * Function: wm需要计算的站点的访客特征统计信息(原始信息)
 *
 * @author   <a href="mailto:zhangxu04@baidu.com">张旭</a>
 */
public class WMSiteVisitorIndexVo extends SiteEntity{
    
    private int tid;
    
    private String siteurl;
    
    private String site;
    
    private String keyword;
    
    private String interest;
    
    public WMSiteVisitorIndexVo(){
    	
    }

	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public String getSiteurl() {
		return siteurl;
	}

	public void setSiteurl(String siteurl) {
		this.siteurl = siteurl;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
	}
    
}

