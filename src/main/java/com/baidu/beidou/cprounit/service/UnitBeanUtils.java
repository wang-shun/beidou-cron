package com.baidu.beidou.cprounit.service;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitMaterCheckView;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseSmartUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;

public final class UnitBeanUtils {

	private static final Log LOG = LogFactory.getLog(UnitBeanUtils.class);
	
	public static boolean hasSpecialChar(UnitMaterView info) {
		if (info == null) {
			return false;
		}
		
		boolean flag = hasSpecialChar(info.getTitle())
				|| hasSpecialChar(info.getDescription1())
				|| hasSpecialChar(info.getDescription2())
				|| hasSpecialChar(info.getTargetUrl())
				|| hasSpecialChar(info.getShowUrl())
				|| hasSpecialChar(info.getWirelessTargetUrl())
				|| hasSpecialChar(info.getWirelessShowUrl());
		
		if (flag) {
			LOG.info("unit has special char, unitId=" + info.getId()
					+ ", userId=" + info.getUserId());
		}
		
		return flag;
	}
	
	public static boolean hasSpecialChar(String input) {
		if (StringUtils.isEmpty(input)) {
			return false;
		}
		
		for (int index = 0; index < SPECIAL_CHAR_FILTER.length; index++) {
			int specialIndex = input.indexOf(SPECIAL_CHAR_FILTER[index]);
			if (specialIndex >= 0) {
				LOG.info("string has special char, input=" + input + ", charIndex=" + index);
				return true;
			}
		}
		
		return false;
	}

	/**
	 * filterSpecialChar: 针对UnitInfoView各字段过滤特殊字符
	 * 字段有：标题、描述1、描述2、targeturl、showurl、wirelesstargeturl、wirelessshowurl
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 21, 2013
	 */
	public static void filterSpecialChar(UnitMaterView info) {
		if (info == null) {
			return;
		}
		
		info.setTitle(filterSpecialChar(info.getTitle()));
		info.setDescription1(filterSpecialChar(info.getDescription1()));
		info.setDescription2(filterSpecialChar(info.getDescription2()));
		info.setTargetUrl(filterSpecialChar(info.getTargetUrl()));
		info.setShowUrl(filterSpecialChar(info.getShowUrl()));
		info.setWirelessTargetUrl(filterSpecialChar(info.getWirelessTargetUrl()));
		info.setWirelessShowUrl(filterSpecialChar(info.getWirelessShowUrl()));
	}
	
	/**
	 * filterSpecialCharBatch: 批量接口，针对UnitInfoView各字段过滤特殊字符
	 * 字段有：标题、描述1、描述2、targeturl、showurl、wirelesstargeturl、wirelessshowurl
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 21, 2013
	 */
	public static void filterSpecialCharBatch(List<UnitMaterView> infoList) {
		if (CollectionUtils.isEmpty(infoList)) {
			return;
		}
		
		for (UnitMaterView info : infoList) {
			filterSpecialChar(info);
		}
	}

    /**
     * 特殊字符集合
     */
    public static String[] SPECIAL_CHAR_FILTER = new String[] { "\t", "\n", "\0", "\r", "," };
    
	/**
	 * filterSpecialChar: 针对字符串，过滤特殊字符
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 21, 2013
	 */
	public static String filterSpecialChar(String input) {
		if (StringUtils.isEmpty(input)) {
			return input;
		}
		
		String output = input;
		for (int index = 0; index < SPECIAL_CHAR_FILTER.length; index++) {
			output = output.replace(SPECIAL_CHAR_FILTER[index], "");
		}
		
		return output;
	}
	
	/**
	 * Function: 检验北斗数据库db与物料中心ubmc中的物料是否一致，用于修复目前不一致的物料
	 * 		0：表示一致
	 * 		1：表示物料类型不一致
	 * 		右数第2位：表示标题或者描述不一致
	 * 		右数第3位：表示url不一致
	 * 		右数第4位: 表示图片或者flash的图片尺寸曾经发生变化（其他内容无法知晓）
	 *
	 * @author genglei01
	 * @date Jun 19, 2014
	 */
	public static int compareMaterialFromDbToUbmc(UnitMaterCheckView dbUnit, ResponseBaseMaterial ubmcResponse) {
		int ret = 0;
		
		if (dbUnit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
			if (ubmcResponse instanceof ResponseTextUnit) {
				ResponseTextUnit response = (ResponseTextUnit)ubmcResponse;
				
				if (!compareString(dbUnit.getTitle(), response.getTitle())
						|| !compareString(dbUnit.getDescription1(), response.getDescription1())
						|| !compareString(dbUnit.getDescription2(), response.getDescription2())) {
					ret |= 2;
				}
				
				if (!compareString(dbUnit.getShowUrl(), response.getShowUrl())
						|| !compareString(dbUnit.getTargetUrl(), response.getTargetUrl())
						|| !compareString(dbUnit.getWirelessShowUrl(), response.getWirelessShowUrl())
						|| !compareString(dbUnit.getWirelessTargetUrl(), response.getWirelessTargetUrl())) {
					ret |= 4;
				}
			} else {
				return 1;
			}
		} else if (dbUnit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_PICTURE
				|| dbUnit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_FLASH) {
			if (ubmcResponse instanceof ResponseImageUnit) {
				ResponseImageUnit response = (ResponseImageUnit)ubmcResponse;
				
				if (dbUnit.getWuliaoType() != response.getWuliaoType()) {
					return 1;
				}
				
				if (!compareString(dbUnit.getTitle(), response.getTitle())) {
					ret |= 2;
				}
				
				if (!compareString(dbUnit.getShowUrl(), response.getShowUrl())
						|| !compareString(dbUnit.getTargetUrl(), response.getTargetUrl())
						|| !compareString(dbUnit.getWirelessShowUrl(), response.getWirelessShowUrl())
						|| !compareString(dbUnit.getWirelessTargetUrl(), response.getWirelessTargetUrl())) {
					ret |= 4;
				}
				
				if (!dbUnit.getWidth().equals(response.getWidth()) || !dbUnit.getHeight().equals(response.getHeight())) {
					ret |= 8;
				}
			} else {
				return 1;
			}
		} else if (dbUnit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
			if (ubmcResponse instanceof ResponseIconUnit) {
				ResponseIconUnit response = (ResponseIconUnit)ubmcResponse;
				
				if (!compareString(dbUnit.getTitle(), response.getTitle())
						|| !compareString(dbUnit.getDescription1(), response.getDescription1())
						|| !compareString(dbUnit.getDescription2(), response.getDescription2())) {
					ret |= 2;
				}
				
				if (!compareString(dbUnit.getShowUrl(), response.getShowUrl())
						|| !compareString(dbUnit.getTargetUrl(), response.getTargetUrl())
						|| !compareString(dbUnit.getWirelessShowUrl(), response.getWirelessShowUrl())
						|| !compareString(dbUnit.getWirelessTargetUrl(), response.getWirelessTargetUrl())) {
					ret |= 4;
				}
			} else {
				return 1;
			}
		} else if (dbUnit.getWuliaoType() == CproUnitConstant.MATERIAL_TYPE_SMART_IDEA) {
			if (ubmcResponse instanceof ResponseSmartUnit) {
				ResponseSmartUnit response = (ResponseSmartUnit)ubmcResponse;
				if (!compareString(dbUnit.getShowUrl(), response.getShowUrl())
						|| !compareString(dbUnit.getTargetUrl(), response.getTargetUrl())
						|| !compareString(dbUnit.getWirelessShowUrl(), response.getWirelessShowUrl())
						|| !compareString(dbUnit.getWirelessTargetUrl(), response.getWirelessTargetUrl())) {
					ret |= 4;
				}
			} else {
				return 1;
			}
		}
		
		return ret;
	}
	
    /**
     * Function: 检验北斗数据库图片和flash是否与物料中心ubmc中的物料类型一致，用于修复目前不一致的物料 0：表示一致 1：ubmc预览url为空 2:db和ubmc的wuliaotype不一致
     * 3:db和ubmc一致，但和预览url后缀不一致，url为图片 4:db和ubmc一致，但和预览url后缀不一致，url为flash
     * 
     * @author doreen
     * @date Jun 19, 2015
     */
    public static int compareAdmakerMaterialFromDbToUbmc(UnitMaterCheckView dbUnit, ResponseBaseMaterial ubmcResponse) {
        int ret = 0;

        if (ubmcResponse instanceof ResponseImageUnit) {
            ResponseImageUnit response = (ResponseImageUnit) ubmcResponse;

            String previewfileSrc = response.getFileSrc();
            // 根据预览url后缀是否为swf判断是否为flash
            if (previewfileSrc == null || previewfileSrc.isEmpty()) {
                return 1;
            }
            Integer dbWuliaoType = dbUnit.getWuliaoType();
            Integer ubmcWuliaoType = response.getWuliaoType();
            
            boolean isFlash =  previewfileSrc.endsWith(".swf") || previewfileSrc.endsWith(".SWF");
            Integer previewUrlWuliaoType =
                    isFlash ? CproUnitConstant.MATERIAL_TYPE_FLASH : CproUnitConstant.MATERIAL_TYPE_PICTURE;

            if (!dbWuliaoType.equals(ubmcWuliaoType)) {
                return 2;
            } else {
                if (!dbWuliaoType.equals(previewUrlWuliaoType)) {
                    if (previewUrlWuliaoType.equals(CproUnitConstant.MATERIAL_TYPE_PICTURE)) {
                        return 3;
                    } else {
                        return 4;
                    }
                }
            }
        }

        return ret;
    }

	public static boolean compareString(String dbAttr, String ubmcAttr) {
		if (StringUtils.isEmpty(dbAttr) && StringUtils.isEmpty(ubmcAttr)) {
			return true;
		}
		
		if ((StringUtils.isNotEmpty(dbAttr) && StringUtils.isEmpty(ubmcAttr))
				|| (StringUtils.isEmpty(dbAttr) && StringUtils.isNotEmpty(ubmcAttr))) {
			return false;
		}
		
		if (dbAttr.compareTo(ubmcAttr) == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
//		String a = "a\tb\nc\0d\r";
//		System.out.println(a);
//		System.out.println(UnitBeanUtils.filterSpecialChar(a));
//		
//		String b = "a\\tb";
//		System.out.println(b);
//		System.out.println(UnitBeanUtils.filterSpecialChar(b));
		
//		String c = "\0abc";
//		System.out.println(c.indexOf("\0"));
		
//		System.out.println(UnitBeanUtils.compareString(null, ""));
//		System.out.println(UnitBeanUtils.compareString("abc ", "abcd"));
		
		UnitMaterCheckView dbUnit = new UnitMaterCheckView();
		dbUnit.setWuliaoType(2);
		dbUnit.setTitle("960x90");
		dbUnit.setShowUrl("baidu.com");
		dbUnit.setTargetUrl("http://baidu.com");
		dbUnit.setWidth(100);
		dbUnit.setHeight(90);
		
		ResponseImageUnit ubmcResponse = new ResponseImageUnit(2, "960x90", "baidu.com", "http://baidu.com", "", "", 960, 90,
				"http://10.81.13.57/media/v1/0f000nKxydeDU43SQvL_6RttBKKSPOSw.jpg", "44b3fa8696f492c57f8ef2c32e88eefb", "", "", "", "");
		System.out.println(UnitBeanUtils.compareMaterialFromDbToUbmc(dbUnit, ubmcResponse));
	}
}
