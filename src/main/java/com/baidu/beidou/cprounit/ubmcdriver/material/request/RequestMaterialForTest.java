package com.baidu.beidou.cprounit.ubmcdriver.material.request;

import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * Function: 物料测试产品线的信息
 *  test作为输入时，标记测试的内容
 *  fileSrc作为输入时，为二进制的多媒体物料
 * 
 * 产品线信息：
 *  appid: 100
 *  业务端(文本和富媒体表）token：523a0eda2d1c3b74a6138703d3b6a45f   
 *  检索端（文本表）token：99202581f357ab56d5e54f295811cf4e    
 *  MM模块（富媒体表）token：ebeb3ba229445dcc497716f7710a03d3  
 * 
 * @ClassName: RequestMaterialForTest
 * @author genglei01
 * @date Feb 6, 2015 12:20:38 PM
 */
public class RequestMaterialForTest extends RequestBaseMaterial {
    
    private String test;

    private byte[] fileSrc;

    public RequestMaterialForTest(Long mcId, Integer versionId, String test, byte[] fileSrc) {
        super(mcId, versionId, null);
        this.test = test;
        this.fileSrc = fileSrc;
    }

    public String tranformToValueString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(UbmcConstant.VALUE_ITEM_TEST)
            .append(UbmcConstant.VALUE_ITEM_KV_DELIMITER)
            .append(getTest()).append(UbmcConstant.VALUE_ITEM_DELIMITER);

        sb.append(UbmcConstant.VALUE_ITEM_FILESRC).append(UbmcConstant.VALUE_ITEM_KV_DELIMITER);
        if (fileSrc != null && fileSrc.length != 0) {
            sb.append(UbmcConstant.VALUE_MEDIA_PLACEHOLDER);

        }

        return sb.toString();
    }

    public String getTest() {
        if (test == null) {
            return "";
        }
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public byte[] getFileSrc() {
        return fileSrc;
    }

    public void setFileSrc(byte[] fileSrc) {
        this.fileSrc = fileSrc;
    }

}
