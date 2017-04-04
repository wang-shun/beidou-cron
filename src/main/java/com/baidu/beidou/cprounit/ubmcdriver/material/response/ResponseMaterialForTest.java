package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

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
public class ResponseMaterialForTest extends ResponseBaseMaterial {

    private String test;
    private String fileSrc;

    public ResponseMaterialForTest(String test, String fileSrc) {
        this.test = test;
        this.fileSrc = fileSrc;
    }

    public static ResponseMaterialForTest transformToObject(Map<String, String> valueMap) {
        if (valueMap == null || valueMap.isEmpty()) {
            return null;
        }

        try {
            String test = valueMap.get(UbmcConstant.VALUE_ITEM_TEST);
            String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);

            return new ResponseMaterialForTest(test, fileSrc);
        } catch (NumberFormatException e) {
            log.error("failed to get value from the ubmc-value map");
            return null;
        }
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getFileSrc() {
        return fileSrc;
    }

    public void setFileSrc(String fileSrc) {
        this.fileSrc = fileSrc;
    }

}
