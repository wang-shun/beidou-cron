
package com.baidu.beidou.unionsite.bo;

import com.baidu.beidou.unionsite.vo.SiteEntity;


/**
 * ClassName:WMSiteBo
 * Function: wm需要计算的Site
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  May 22, 2010
 * @since    TODO
 * @version  $Id: WMSiteBo.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 */
public class WMSiteBo extends SiteEntity{

    //以下字段同数据库，分别来自unionsitebdstat,unionsiteinfos
    private byte cmpLevel;
    private double rateCmp;
    private float scoreCmp;
    private int ips;
    private int cookies;
    
    /** 将ips映射到的区间值，参见 SiteConstant.CONSTANT_IP_LEVEL_1 */
    private byte ipLevel;
    /** 将uvs映射到的区间值，参见 SiteConstant.CONSTANT_UV_LEVEL_n */
    private byte uvLevel;
    /** 计算出的用于前台显示的“站点热度值”，取值范围为【0，100】 */
    private int siteHeat;
    
    public WMSiteBo(){};
    
    public byte getCmpLevel() {
        return cmpLevel;
    }
    public void setCmpLevel(byte cmpLevel) {
        this.cmpLevel = cmpLevel;
    }
    public double getRateCmp() {
        return rateCmp;
    }
    public void setRateCmp(double rateCmp) {
        this.rateCmp = rateCmp;
    }
    public float getScoreCmp() {
        return scoreCmp;
    }
    public void setScoreCmp(float scoreCmp) {
        this.scoreCmp = scoreCmp;
    }
    public int getIps() {
        return ips;
    }
    public void setIps(int ips) {
        this.ips = ips;
    }
    public int getCookies() {
        return cookies;
    }
    public void setCookies(int cookies) {
        this.cookies = cookies;
    }
    public byte getIpLevel() {
        return ipLevel;
    }
    public void setIpLevel(byte ipLevel) {
        this.ipLevel = ipLevel;
    }
    public byte getUvLevel() {
        return uvLevel;
    }
    public void setUvLevel(byte uvLevel) {
        this.uvLevel = uvLevel;
    }
    public int getSiteHeat() {
        return siteHeat;
    }
    public void setSiteHeat(int siteHeat) {
        this.siteHeat = siteHeat;
    }
}

