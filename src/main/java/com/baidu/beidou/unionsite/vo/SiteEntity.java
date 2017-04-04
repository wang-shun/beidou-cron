
package com.baidu.beidou.unionsite.vo;

import java.io.Serializable;


public abstract class SiteEntity implements Serializable {

    protected int siteId;

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int id) {
        this.siteId = id;
    }
    
}

