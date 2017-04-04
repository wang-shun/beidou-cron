package com.baidu.beidou.cprounit.ubmcdriver.material.response;

import java.util.Map;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;

/**
 * Function: 霓裳图片物料
 *      toolType 代表了工具类型：0-表示创意专家制作工具，1-表示霓裳入门版
 *      fileSrc为ubmc存储的占位符（%%BEGIN_MEDIA%%...%%END_MEDIA%%）或者预览URL
 * @ClassName: ResponseNichangMaterial
 * @author genglei01
 * @date Mar 26, 2015 11:49:00 AM
 */
public class ResponseNichangMaterial extends ResponseBaseMaterial {

    private int toolType;
    private int width;
    private int height;
    private String fileSrc;
    private String fileSrcMd5;
    private String descInfo;

    /**
     * ResponseNichangMaterial: 霓裳响应构造体
     * 
     * @param toolType 代表了工具类型：0-表示创意专家制作工具，1-表示霓裳入门版
     * @param width width
     * @param height height
     * @param fileSrc fileSrc
     * @param fileSrcMd5 fileSrcMd5
     * @param descInfo descInfo
     */
    public ResponseNichangMaterial(int toolType, int width, int height, String fileSrc, String fileSrcMd5,
            String descInfo) {

        this.wuliaoType = CproUnitConstant.MATERIAL_TYPE_NICHANG;
        this.toolType = toolType;
        this.width = width;
        this.height = height;
        this.fileSrc = fileSrc;
        this.fileSrcMd5 = fileSrcMd5;
        this.descInfo = descInfo;

    }

    /**
     * Function: 霓裳生成响应结构体函数
     * 
     * @author genglei01
     * @param valueMap  从ubmc的Text解析出的文本map
     * @return  ResponseNichangMaterial
     */
    public static ResponseNichangMaterial transformToObject(Map<String, String> valueMap) {
        if (valueMap == null || valueMap.isEmpty()) {
            return null;
        }

        try {
            int toolType = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_TOOL_TYPE));
            int width = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_WIDTH));
            int height = Integer.parseInt(valueMap.get(UbmcConstant.VALUE_ITEM_HEIGHT));
            String fileSrc = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC);
            String fileSrcMd5 = valueMap.get(UbmcConstant.VALUE_ITEM_FILESRC_MD5);
            String descInfo = valueMap.get(UbmcConstant.VALUE_ITEM_DESC_INFO);

            return new ResponseNichangMaterial(toolType, width, height, fileSrc, fileSrcMd5, descInfo);
        } catch (NumberFormatException e) {
            log.error("failed to get toolType, width or heiget from the ubmc-value map");
            return null;
        }
    }

    public int getToolType() {
        return toolType;
    }

    public void setToolType(int toolType) {
        this.toolType = toolType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFileSrc() {
        return fileSrc;
    }

    public void setFileSrc(String fileSrc) {
        this.fileSrc = fileSrc;
    }

    public String getFileSrcMd5() {
        return fileSrcMd5;
    }

    public void setFileSrcMd5(String fileSrcMd5) {
        this.fileSrcMd5 = fileSrcMd5;
    }

    public String getDescInfo() {
        return descInfo;
    }

    public void setDescInfo(String descInfo) {
        this.descInfo = descInfo;
    }
}
