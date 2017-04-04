
package com.baidu.beidou.unionsite.vo;

import com.baidu.beidou.unionsite.bo.WMSiteIndexBo;


public abstract class SiteIndexHelper {

    public static WMSiteIndexBo vo2bo(WMSiteIndexVo vo) {
        
        WMSiteIndexBo bo = null;
        if ( vo != null ) {
            bo = new WMSiteIndexBo();
            bo.setSiteId(vo.getSiteId());
        }
        return bo;
    }
}

