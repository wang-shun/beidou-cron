package com.baidu.beidou.cprounit.ubmcdriver.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestMaterialForTest;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestPreviewImageMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseAdmakerMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseGroup;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseMaterialForTest;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseNichangMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponsePreviewImageMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseSmartUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;
import com.baidu.ubmc.bc.Media;
import com.baidu.ubmc.bc.Text;

public class UbmcParser {
	
	private final static Log log = LogFactory.getLog(UbmcParser.class);

	/**
	 * transformRequestToFullText: 将请求封装为ubmc接口所使用的参数形式（完整版）
	 * 用于insert/update/addVersion等接口
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public static Text transformRequestToFullText(RequestBaseMaterial request) {
		if (request == null) {
			return null;
		}
		
		Text text = new Text();
		if (request.getMcId() != null && request.getMcId() > 0L) {
			text.setMcId(request.getMcId());
		}
		if (request.getVersionId() != null && request.getVersionId() > 0) {
			text.setVersionId(request.getVersionId());
		}
		
		// 将对象中各字段的值转化为ubmc所需要的value字段
		String value = request.tranformToValueString();
		text.setValue(value);
		
		// 如果请求中包含图片的二进制文件，则需要生成Media放入Text中
		List<Media> medias = null;
		if (request instanceof RequestIconMaterial) {
			RequestIconMaterial request1 = (RequestIconMaterial)request;
			
			Media media = new Media();
			media.setBinaryData(request1.getFileSrc());
			
			medias = new ArrayList<Media>();
			medias.add(media);
			text.setMediaDatas(medias);
		} else if (request instanceof RequestIconUnitWithData) {
			RequestIconUnitWithData request1 = (RequestIconUnitWithData)request;
			
			Media media = new Media();
			media.setBinaryData(request1.getFileSrc());
			
			medias = new ArrayList<Media>();
			medias.add(media);
			text.setMediaDatas(medias);
		} else if (request instanceof RequestImageUnitWithData) {
			RequestImageUnitWithData request1 = (RequestImageUnitWithData)request;
			
			Media media = new Media();
			media.setBinaryData(request1.getFileSrc());
			media.setAttribute(request1.getAttribute());
			
			medias = new ArrayList<Media>();
			medias.add(media);
			text.setMediaDatas(medias);
		} else if (request instanceof RequestImageUnitWithMediaId) {
			// 此种情况，不会改变物料本身内容，但是可以修改flash创意的截图，因而需要判断截图snapshot是否有数据
			RequestImageUnitWithMediaId request1 = (RequestImageUnitWithMediaId)request;
			
			byte[] data = request1.getSnapshot();
			if (data != null && data.length > 0) {
				Media media = new Media();
				media.setBinaryData(data);
				
				medias = new ArrayList<Media>();
				medias.add(media);
				text.setMediaDatas(medias);
			}
		} else if (request instanceof RequestPreviewImageMaterial) {
			RequestPreviewImageMaterial request1 = (RequestPreviewImageMaterial)request;
			
			Media media = new Media();
			media.setBinaryData(request1.getFileSrc());
			
			medias = new ArrayList<Media>();
			medias.add(media);
			text.setMediaDatas(medias);
        } else if (request instanceof RequestMaterialForTest) {
            RequestMaterialForTest request1 = (RequestMaterialForTest) request;

            Media media = new Media();
            media.setBinaryData(request1.getFileSrc());

            medias = new ArrayList<Media>();
            medias.add(media);
            text.setMediaDatas(medias);
        }

        return text;
	}
	
	/**
	 * transformRequestToLiteText: 将请求封装为ubmc接口所使用的参数形式（简化版）
	 * 用于remove/copy/get等接口
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public static Text transformRequestToLiteText(RequestBaseMaterial request) {
		if (request == null) {
			return null;
		}
		
		Text text = new Text();
		text.setMcId(request.getMcId());
		text.setVersionId(request.getVersionId());
		
		return text;
	}
	
	public static ResponseBaseMaterial transformToObject(String value) {
		ResponseBaseMaterial result = null;
		
		Map<String, String> items = transformValueToMap(value);
		
		if (items == null) {
			log.error("failed to transform ubmc-value to object[value=" + value + "]");
			return null;
		}
		
		try {
            String test = items.get(UbmcConstant.VALUE_ITEM_TEST);
            if (StringUtils.isNotEmpty(test)) {
                result = ResponseMaterialForTest.transformToObject(items);
                return result;
            }
            
			String wuliaoTypeStr = items.get(UbmcConstant.VALUE_ITEM_WULIAO_TYPE);
			
			if (StringUtils.isEmpty(wuliaoTypeStr)) {
				// 如果没有物料type类型，则有可能属于推广组附加信息，有可能属于admaker制作的物料
				String groupIdStr = items.get(UbmcConstant.VALUE_ITEM_GROUPID);
				
				// 如果不包含推广组ID，则为admaker或者霓裳制作的物料；否则为推广组附加信息
				if (StringUtils.isEmpty(groupIdStr)) {
                    // 如果不包含toolType字段，说明是admaker制作的物料；
                    String toolTypeStr = items.get(UbmcConstant.VALUE_ITEM_TOOL_TYPE);
                    if (StringUtils.isEmpty(toolTypeStr)) {
                        result = ResponseAdmakerMaterial.transformToObject(items);
                    } else {
                        // 如果包含toolType，toolType=1是霓裳制作的，toolType=0是admaker制作的
                        int toolType = Integer.parseInt(toolTypeStr);
                        if (toolType == UbmcConstant.VALUE_ITEM_TOOL_TYPE_NICHANG) {
                            result = ResponseNichangMaterial.transformToObject(items);
                        } else if (toolType == UbmcConstant.VALUE_ITEM_TOOL_TYPE_ADMAKER) {
                            result = ResponseAdmakerMaterial.transformToObject(items);
                        }
                    }
				} else {
					result = ResponseGroup.transformToObject(items);
				}
			} else {
				Integer wuliaoType = Integer.parseInt(wuliaoTypeStr);
				
				if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
					result = ResponseTextUnit.transformToObject(items);
				} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE	|| wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
					result = ResponseImageUnit.transformToObject(items);
				} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON) {
					result = ResponseIconUnit.transformToObject(items);
				} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_ICON) {
					result = ResponseIconMaterial.transformToObject(items);
				} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PREVIEW_IMAGE) {
					result = ResponsePreviewImageMaterial.transformToObject(items);
				} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_SMART_IDEA) {
					result = ResponseSmartUnit.transformToObject(items);
				}
			}
			
		} catch (NumberFormatException e) {
			log.error("failed to get wuliaotype or tooltype from ubmc-value[value=" + value + "]");
			return null;
		}
		
		return result;
	}
	
	private static Map<String, String> transformValueToMap(String value) {
		if (StringUtils.isEmpty(value)) {
			log.warn("warn ubmc-value[value=" + value + "]");
			return null;
		}
		
		String[] items = value.split(UbmcConstant.VALUE_ITEM_DELIMITER);
		if (ArrayUtils.isEmpty(items)) {
			return null;
		}
		
		Map<String, String> result = new HashMap<String, String>();
		for (String item : items) {
			if (StringUtils.isEmpty(item)) {
				log.warn("warn item[item=" + item + "] from ubmc-value[value=" + value + "]");
				continue;
			}
			
			int index = item.indexOf(UbmcConstant.VALUE_ITEM_KV_DELIMITER);
			if (index <= 0 || index >= item.length()) {
				log.warn("warn item[item=" + item + "] from ubmc-value[value=" + value + "]");
				continue;
			}
			
			String itemKey = item.substring(0, index).trim();
			String itemValue = item.substring(index + 1);
			
			if (StringUtils.isEmpty(itemKey)) {
				log.error("error item[item=" + item + "] from ubmc-value[value=" + value + "]");
				return null;
			}
			if (StringUtils.isEmpty(itemValue)) {
				itemValue = "";
			}
			result.put(itemKey, itemValue);
		}
		
		return result;
	}
}
