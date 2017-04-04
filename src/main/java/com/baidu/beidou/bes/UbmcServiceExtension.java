/**
 * UbmcServiceDecorator.java 
 */
package com.baidu.beidou.bes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.util.string.StringUtil;

/**
 * UbmcService的一个扩展<br/>
 * 增加了物料大小获取的方法，包括批量的和单个的<br/>
 * 
 * @author lixukun
 * @date 2013-12-25
 */
public class UbmcServiceExtension {
	private UbmcService ubmcService;
	
	/**
	 * 获取物料大小的批量接口
	 * @param requests 
	 * @return 保证返回个数一致，不成功则返回为null;空requests返回null
	 */
	public List<Integer> getMaterSizeBatch(List<RequestBaseMaterial> requests) {
		if (CollectionUtils.isEmpty(requests)) {
			return null;
		}
		
		List<ResponseBaseMaterial> result = ubmcService.get(requests, false);
		List<Integer> sizeList = new ArrayList<Integer>(requests.size());
		for (ResponseBaseMaterial ret : result) {
			if (ret == null) {
				sizeList.add(null);
				continue;
			}
			int flashSize = 0;
			try {		
				ResponseImageUnit response = (ResponseImageUnit) ret;
				String fileSrc = response.getFileSrc();

				List<Long> mediaIdList = new ArrayList<Long>();
				mediaIdList.add(ubmcService.getMediaIdFromFileSrc(fileSrc));
		
				if (!StringUtil.isEmpty(response.getAttribute())) {
					String[] attributes = StringUtil.split(response.getAttribute(), ",");
					if (!ArrayUtils.isEmpty(attributes)) {
						for (String attribute : attributes) {
							mediaIdList.add(ubmcService.getMediaIdFromFileSrc(attribute));
						}
					}
				}
		
				List<byte[]> byteList = ubmcService.getMediaData(mediaIdList);
				
				if (CollectionUtils.isNotEmpty(byteList)) {
					for (byte[] innerByte : byteList) {
						if (innerByte != null) {
							flashSize += innerByte.length;
						}
					}
				}
				
				sizeList.add(flashSize);
			} catch (Exception ex) {
				sizeList.add(null);
				continue;
			}
		}

		return sizeList;
	}
	
	/**
	 * 获取物料大小
	 * @param mcId
	 * @param mcVersionId
	 * @return 如果不成功，返回null
	 */
	public Integer getMaterSize(long mcId, int mcVersionId) {
		int flashSize = 0;

		List<RequestBaseMaterial> units = new ArrayList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(mcId, mcVersionId);
		units.add(request);

		List<Integer> retList = getMaterSizeBatch(units);
		if (CollectionUtils.isEmpty(retList)) {
			return flashSize;
		}
		
		return retList.get(0);
	}

	public void setUbmcService(UbmcService ubmcService) {
		this.ubmcService = ubmcService;
	}
}
