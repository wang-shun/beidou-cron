package com.baidu.beidou.unionsite.vo;



/**
 * WMSiteIndexVo
 * Function: wm需要计算的站点的Index统计信息(原始信息)
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  May 22, 2010
 * @version  $Id: WMSiteIndexVo.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 */
public class WMSiteIndexVo extends SiteEntity{

    //以下字段同数据库，来自unionsiteindex
    private String region;
    private String gender;
    private String age;
    private String degree;
    
    public WMSiteIndexVo(){}

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String genger) {
        this.gender = genger;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }
}

