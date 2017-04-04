package com.baidu.beidou.cprounit.service.impl;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.UbmcDriverProxy;
import com.baidu.beidou.cprounit.ubmcdriver.constant.UbmcConstant;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseAdmakerMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponsePreviewImageMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseSmartUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.parser.UbmcParser;
import com.baidu.beidou.util.service.impl.BaseDrawinRpcServiceImpl;
import com.baidu.beidou.util.string.StringUtil;
import com.baidu.ubmc.bc.ResultBean;
import com.baidu.ubmc.bc.RpcBatchResponse;
import com.baidu.ubmc.bc.RpcResponse;
import com.baidu.ubmc.bc.RpcStatusCode;
import com.baidu.ubmc.bc.Text;
import com.google.common.collect.Lists;

public class UbmcServiceImpl extends BaseDrawinRpcServiceImpl implements UbmcService {
	
	private UbmcDriverProxy ubmcDriverProxy;
	
	public UbmcServiceImpl(String syscode, String prodid) {
		super(syscode, prodid);
	}
	
	/**
	 * insert: 向ubmc插入物料
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 3, 2013
	 */
	public List<ResponseBaseMaterial> insert(List<RequestBaseMaterial> units) {
		if (CollectionUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		for (RequestBaseMaterial unit : units) {
			Text text = UbmcParser.transformRequestToFullText(unit);
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.insert(textPart, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseResult(batchResponse, textPart.size());
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}

	/**
	 * update: ubmc更新物料
	 *  mcId和value必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> update(List<RequestBaseMaterial> units) {
		if (CollectionUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		for (RequestBaseMaterial unit : units) {
			Text text = UbmcParser.transformRequestToFullText(unit);
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.update(textPart, true, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseUpdateResult(batchResponse, textPart);
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}

	/**
	 * remove: ubmc移除物料
	 * mcId必填，versionId可选填（不填默认为1）
	 * 返回结果仅包含mcId和versionId
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> remove(List<RequestBaseMaterial> units) {
		if (CollectionUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		for (RequestBaseMaterial unit : units) {
			Text text = UbmcParser.transformRequestToLiteText(unit);
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.remove(textPart, true, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseRemoveResult(batchResponse, textPart);
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}

	/**
	 * copy: ubmc拷贝物料
	 * mcId必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> copy(List<RequestBaseMaterial> units) {
		if (CollectionUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		for (RequestBaseMaterial unit : units) {
			Text text = UbmcParser.transformRequestToLiteText(unit);
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.copy(textPart, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseResult(batchResponse, textPart.size());
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}

	/**
	 * get: ubmc获取物料
	 * mcId必填，versionId可选填（不填默认为1）
	 * isPreview：用来表示是否获取预览URL，true为url，false为mediaId
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> get(List<RequestBaseMaterial> units, Boolean isPreview) {
		if (CollectionUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		for (RequestBaseMaterial unit : units) {
			Text text = UbmcParser.transformRequestToLiteText(unit);
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.get(textPart, isPreview, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseGetResult(batchResponse, textPart);
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}

	/**
	 * addVersion: ubmc为某一mcId指定版本的物料拷贝一份
	 * mcId必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public List<ResponseBaseMaterial> addVersion(List<RequestBaseMaterial> units) {
		if (CollectionUtils.isEmpty(units)) {
			return Collections.emptyList();
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		Text text = null;
		for (RequestBaseMaterial unit : units) {
			if (unit instanceof RequestLite) {
				text = UbmcParser.transformRequestToLiteText(unit);
			} else {
				text = UbmcParser.transformRequestToFullText(unit);
			}
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.addVersion(textPart, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseResult(batchResponse, textPart.size());
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}
	
	/**
	 * generateMaterUrl: 获取临时URL 
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jun 12, 2013
	 */
	public Map<RequestBaseMaterial, String> generateMaterUrl(List<RequestBaseMaterial> units) {
		Map<RequestBaseMaterial, String> result = new HashMap<RequestBaseMaterial, String>();
		if (CollectionUtils.isEmpty(units)) {
			return result;
		}
		
		List<Text> requestTexts = new ArrayList<Text>();
		for (RequestBaseMaterial unit : units) {
			Text text = UbmcParser.transformRequestToLiteText(unit);
			requestTexts.add(text);
		}
		
		List<ResponseBaseMaterial> responses = new ArrayList<ResponseBaseMaterial>();
		List<List<Text>> textPartList = doPage(requestTexts, UbmcConstant.UBMC_BATCH_WRITE_MAX);
		for (List<Text> textPart : textPartList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.get(textPart, true, getHeaders());
			List<ResponseBaseMaterial> resultPart = parseGetResult(batchResponse, textPart);
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return result;
			} else {
				responses.addAll(resultPart);
			}
		}
		
		if (CollectionUtils.isEmpty(responses) || responses.size() != units.size()) {
			return result;
		}
		for (int index = 0; index < units.size(); index++) {
			RequestBaseMaterial request = units.get(index);
			ResponseBaseMaterial response = responses.get(index);
			String url = null;
			if (response != null) {
				if (response instanceof ResponseIconUnit) {
					url = ((ResponseIconUnit)response).getFileSrc();
				} else if (response instanceof ResponseImageUnit) {
					String tmpUrl = ((ResponseImageUnit)response).getFileSrc();
					String attribute = ((ResponseImageUnit)response).getAttribute();
					String refMcId = ((ResponseImageUnit)response).getRefMcId();
					url = this.packageTmpUrl(tmpUrl, attribute, refMcId);
				} else if (response instanceof ResponseAdmakerMaterial) {
					String tmpUrl = ((ResponseAdmakerMaterial)response).getFileSrc();
					String attribute = ((ResponseAdmakerMaterial)response).getAttribute();
					String refMcId = ((ResponseAdmakerMaterial)response).getRefMcId();
					url = this.packageTmpUrl(tmpUrl, attribute, refMcId);
				} else if (response instanceof ResponseIconMaterial) {
					url  = ((ResponseIconMaterial)response).getFileSrc();
				} else if (response instanceof ResponsePreviewImageMaterial) {
					url  = ((ResponsePreviewImageMaterial)response).getFileSrc();
				} else if (response instanceof ResponseSmartUnit
						|| response instanceof ResponseTextUnit) {
					// 文字和智能创意没有url
					continue;
				}
				result.put(request, url);
			} else {
				result.put(request, url);
				LOG.error("get tmp url failed for mcid=" + request.getMcId() 
						+ " versionid=" + request.getVersionId());
				if (request.getMcId() <= 0 || request.getVersionId() <= 0) {
					LOG.error("mcId or versionId is invalid", new Exception());
				}
			}
		}
		
		return result;
	}
	
//	private void logPrintError(String msg) {
//		LOG.error(msg);
//		logStackTrace();
//	}
//	
//	private void logPrintError(String msg, Exception e) {
//		LOG.error(msg, e);
//		logStackTrace();
//	}
//	
//	private void logStackTrace() {
//		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
//		String lineSeparator = System.getProperty("line.separator");
//		StringBuilder sb = new StringBuilder(2048);
//		for (StackTraceElement e : traces) {
//			sb.append(e.toString()).append(lineSeparator);
//		}
//		LOG.error(sb.toString());
//	}

	private String packageTmpUrl(String url, String attribute, String refMcId) {
		if (StringUtil.isEmpty(attribute) || StringUtil.isEmpty(refMcId)) {
			return url;
		}
		
		try {
			String[] attrUrls = attribute.split(",");
			String[] refMcIds = refMcId.split(",");
			
			if (ArrayUtils.isEmpty(attrUrls) || ArrayUtils.isEmpty(refMcIds)
					|| attrUrls.length != refMcIds.length) {
				return url;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(url).append("?url_type=2");
			
			for (int index = 0; index < attrUrls.length; index++) {
				sb.append("&id_").append(refMcIds[index]).append("=");
				URL attrUrl = new URL(attrUrls[index]);
				String pathUrl = attrUrl.getPath();
				sb.append(URLEncoder.encode(pathUrl, "UTF-8"));
			}
			
			if (attrUrls.length > 0) {
				sb.append("&");
			}
			
			return sb.toString();
		} catch (Exception e) {
			LOG.error("generate tmp url for flash failed[url=" + url
					+ ", attribute=" + attribute + ", refMcId=" + refMcId, e);
			return url;
		}
	}
	
	/**
	 * getTmpUrl: 获取临时url
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jun 12, 2013
	 */
	public String getTmpUrl(Long mcId, Integer versionId) {
		// 构造请求
		List<RequestBaseMaterial> requests = new ArrayList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(mcId, versionId);
		requests.add(request);
		
		// 获取图片的临时URL，预览URL
		Map<RequestBaseMaterial, String> urlMap = this.generateMaterUrl(requests);
		
		if (urlMap.isEmpty()) {
			return null;
		}
		
		return urlMap.get(request);
	}
	
	/**
	 * getMediaData: 通过ubmc获取某一mcId指定版本的二进制文件
	 * mcId必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public byte[] getMediaData(Long mcId, Integer versionId) {
		List<Text> requestTexts = new ArrayList<Text>();
		
		RequestBaseMaterial request = new RequestLite(mcId, versionId);
		Text text = UbmcParser.transformRequestToLiteText(request);
		requestTexts.add(text);
		
		byte[] result = null;
		RpcBatchResponse batchResponse = ubmcDriverProxy.get(requestTexts, false, getHeaders());
		List<ResponseBaseMaterial> resultPart = parseGetResult(batchResponse, requestTexts);
		
		if (CollectionUtils.isEmpty(resultPart)) {
			return result;
		}
		
		ResponseBaseMaterial response = resultPart.get(0);
		String fileSrc = null;
		if (response instanceof ResponseIconUnit) {
			fileSrc = ((ResponseIconUnit)response).getFileSrc();
		} else if (response instanceof ResponseImageUnit) {
			fileSrc = ((ResponseImageUnit)response).getFileSrc();
		} else if (response instanceof ResponseAdmakerMaterial) {
			fileSrc = ((ResponseAdmakerMaterial)response).getFileSrc();
		} else if (response instanceof ResponseIconMaterial) {
			fileSrc = ((ResponseIconMaterial)response).getFileSrc();
		} else {
			return result;
		}
		
		Long mediaId = this.getMediaIdFromFileSrc(fileSrc);
		if (mediaId == null || mediaId <= 0) {
			return result;
		}
		
		List<Long> mediaIds = new ArrayList<Long>();
		mediaIds.add(mediaId);
		batchResponse = ubmcDriverProxy.getMedia(mediaIds, null, null, true, getHeaders());
		if (isFailed(batchResponse)) {
			return result;
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != 1)) {
			LOG.error("ubmc response failed from getting the binary data for mcId=" + mcId);
			return result;
		}
		
		RpcResponse resultItem = responseList.get(0);
		if (resultItem == null || resultItem.getStatusCode() != RpcBatchResponse.OK) {
			LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
					+ "], and error message is [" + resultItem.getErrorMsg() + "]");
			return result;
		}
		
		ResultBean resultBean = resultItem.getResult();
		if (resultBean == null) {
			LOG.error("ubmc ResultBean is null or empty");
			return result;
		}
		
		result = resultBean.getBinary();
		return result;
	}
	
	/**
	 * getMediaData: 通过mediaId获取图片信息
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 22, 2013
	 */
	public byte[] getMediaData(Long mediaId) {
		byte[] result = null;
		
		List<Long> mediaIds = new ArrayList<Long>();
		mediaIds.add(mediaId);
		RpcBatchResponse batchResponse = ubmcDriverProxy.getMedia(mediaIds, null, null, true, getHeaders());
		if (isFailed(batchResponse)) {
			return result;
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != 1)) {
			LOG.error("ubmc response failed from getting the binary data for mediaId=" + mediaId);
			return result;
		}
		
		RpcResponse resultItem = responseList.get(0);
		if (resultItem == null || resultItem.getStatusCode() != RpcBatchResponse.OK) {
			LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
					+ "], and error message is [" + resultItem.getErrorMsg() + "]");
			return result;
		}
		
		ResultBean resultBean = resultItem.getResult();
		if (resultBean == null) {
			LOG.error("ubmc ResultBean is null or empty");
			return result;
		}
		
		result = resultBean.getBinary();
		
		return result;
	}
	
	/**
	 * getMedia: 通过mediaIds批量获取图片信息
	 * 		如果某一个mediaId对应的二进制文件获取不到，则将其对应位置的返回null
	 * 		即该接口支持部分获取，传入参数列表的长度和结果保持一致
	 * 
	 * 注意：该接口需要考虑批量的数量，数量太大会占用很大内存
	 * @version cpweb-640
	 * @author genglei01
	 * @date Oct 17, 2013
	 */
	public List<byte[]> getMediaData(List<Long> mediaIds) {
		if (CollectionUtils.isEmpty(mediaIds)) {
			return Collections.emptyList();
		}
		
		int oldLen = mediaIds.size();
		List<byte[]> result = new ArrayList<byte[]>(oldLen);
		
		List<List<Long>> partList = doPage(mediaIds, UbmcConstant.UBMC_BATCH_GET_DATA_MAX);
		for (List<Long> mediaIdPartList : partList) {
			RpcBatchResponse batchResponse = ubmcDriverProxy.getMedia(mediaIdPartList, null, null, true, getHeaders());
			
			List<byte[]> resultPart = parseGetDataResult(batchResponse, mediaIdPartList);
			
			if (CollectionUtils.isEmpty(resultPart)) {
				return Collections.emptyList();
			} else {
				result.addAll(resultPart);
			}
		}
		
		return result;
	}
	
	/**
	 * getMediaData: 通过ubmc获取某一fileSrc指定版本的二进制文件
	 * fileSrc必填，versionId可选填（不填默认为1）
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	public byte[] getMediaData(String fileSrc) {
		byte[] result = null;
		if (StringUtil.isEmpty(fileSrc)) {
			return result;
		}
		
		Long mediaId = this.getMediaIdFromFileSrc(fileSrc);
		if (mediaId == null || mediaId <= 0) {
			return result;
		}
		
		List<Long> mediaIds = new ArrayList<Long>();
		mediaIds.add(mediaId);
		RpcBatchResponse batchResponse = ubmcDriverProxy.getMedia(mediaIds, null, null, true, getHeaders());
		if (isFailed(batchResponse)) {
			return result;
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != 1)) {
			LOG.error("ubmc response failed from getting the binary data for fileSrc=" + fileSrc);
			return result;
		}
		
		RpcResponse resultItem = responseList.get(0);
		if (resultItem == null || resultItem.getStatusCode() != RpcBatchResponse.OK) {
			LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
					+ "], and error message is [" + resultItem.getErrorMsg() + "]");
			return result;
		}
		
		ResultBean resultBean = resultItem.getResult();
		if (resultBean == null) {
			LOG.error("ubmc ResultBean is null or empty");
			return result;
		}
		
		result = resultBean.getBinary();
		
		return result;
	}
	
	/**
	 * getMediaIdFromFileSrc: 通过fileSrc解析出mediaId
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 18, 2013
	 */
	public Long getMediaIdFromFileSrc(String fileSrc) {
		Long mediaId = null;
		if (StringUtil.isEmpty(fileSrc)) {
			return mediaId;
		}
		
		int beginIndex = fileSrc.indexOf("mediaid=");
		if (beginIndex <= 0) {
			return mediaId = null;
		} else {
			beginIndex += 8;
		}
		int endIndex = fileSrc.indexOf('\1');
		if (endIndex <= 0) {
			endIndex = fileSrc.indexOf("%%END_MEDIA%%");
		}
		
		String mediaIdStr = fileSrc.substring(beginIndex, endIndex);
		
		mediaId = Long.valueOf(mediaIdStr);
		return mediaId;
	}

    /**
     * Function: 通过mediaId使得ubmc下线检索端的多媒体展示
     * 
     * @author genglei01
     * @param mediaId 多媒体ID
     * @return 是否成功
     */
    public boolean offlineMedia(Long mediaId) {
        if (mediaId == null || mediaId <= 0L) {
            return false;
        }
        
        List<Long> mediaIds = Lists.newArrayList();
        mediaIds.add(mediaId);
        RpcBatchResponse response = ubmcDriverProxy.manageBlockStatus(mediaIds, true, getHeaders());
        if (null == response) {
            LOG.warn("ubmc response bean is null, mediaId=" + mediaId);
            return false;
        }
        int status = response.getStatusCode();
        if (status != RpcBatchResponse.OK) {
            LOG.error("offline media in ubmc failed, mediaId=" + mediaId + ", status=" + status 
                    + ", and error message is [" + response.getErrorMsg() + "]");
            return false;
        }
        
        return true;
    }
    
    /**
     * Function: 通过mediaId获取ubmc中引用该多媒体的物料
     * 
     * @author genglei01
     * @param mediaId 多媒体ID
     * @return 返回引用该多媒体ID的mcId和versionId
     */
    public List<ResponseLite> getRelatedText(Long mediaId) {
        if (mediaId == null || mediaId <= 0L) {
            return null;
        }
        
        RpcResponse response = ubmcDriverProxy.getRelatedText(mediaId, getHeaders());
        if (null == response) {
            LOG.warn("ubmc response bean is null, mediaId=" + mediaId);
            return null;
        }
        int status = response.getStatusCode();
        ResultBean resultBean = response.getResult();
        if (status != RpcStatusCode.OK || resultBean == null) {
            LOG.error("offline media in ubmc failed, mediaId=" + mediaId + ", status=" + status 
                    + ", and error message is [" + response.getErrorMsg() + "]");
            return null;
        }
        
        List<ResponseLite> result = Lists.newArrayList();
        List<ResultBean> resultBeanList = resultBean.getNestedData();
        if (CollectionUtils.isNotEmpty(resultBeanList)) {
            // 结果非空
            for (ResultBean resultItem : resultBeanList) {
                if (resultItem == null) {
                    continue;
                }
                Map<String, String> resultMap = resultItem.getData();
                if (resultMap != null && !resultMap.isEmpty()) {
                    String mcIdStr = resultMap.get(UbmcConstant.RESULT_MAP_MCID);
                    String versionIdStr = resultMap.get(UbmcConstant.RESULT_MAP_VERSIONID);
                    if (StringUtil.isEmpty(mcIdStr) || StringUtil.isEmpty(versionIdStr)) {
                        LOG.error("the mcId or versionId in ubmc response is null");
                        continue;
                    } else {
                        Long mcId = Long.valueOf(mcIdStr);
                        Integer versionId = Integer.valueOf(versionIdStr);
                        result.add(new ResponseLite(mcId, versionId));
                    }
                }
            }
        }
        
        return result;
    }

	/**
	 * parseResult: 解析从ubmc返回的结果
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	private List<ResponseBaseMaterial> parseResult(RpcBatchResponse batchResponse, int oldLen) {
		if (isFailed(batchResponse)) {
			return constructNullList(oldLen);
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != oldLen)) {
			int newlen = 0;
			if (responseList != null) {
				newlen = responseList.size();
			}
			LOG.error("ubmc response item number [" + newlen
					+ "] != request item number [" + oldLen + "]");
			return constructNullList(oldLen);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		for (RpcResponse resultItem : responseList) {
			if (resultItem.getStatusCode() == RpcBatchResponse.OK) {
				ResultBean resultBean = resultItem.getResult();
				Map<String, String> resultMap = resultBean.getData();
				if (resultMap == null || resultMap.isEmpty()) {
					LOG.error("ubmc ResultBean is null or empty");
					result.add(null);
					continue;
				}
				
				String value = resultMap.get(UbmcConstant.RESULT_MAP_VALUE);
				if (StringUtil.isEmpty(value)) {
					LOG.error("ubmc response value is null");
					result.add(null);
					continue;
				}
				ResponseBaseMaterial response = UbmcParser.transformToObject(value);
				
				String mcIdStr = resultMap.get(UbmcConstant.RESULT_MAP_MCID);
				String versionIdStr = resultMap.get(UbmcConstant.RESULT_MAP_VERSIONID);
				if (StringUtil.isEmpty(mcIdStr) || StringUtil.isEmpty(versionIdStr)) {
					LOG.error("ubmc response mcId or versionId is null");
					result.add(null);
				} else {
					Long mcId = Long.valueOf(mcIdStr);
					Integer versionId = Integer.valueOf(versionIdStr);
					response.setMcId(mcId);
					response.setVersionId(versionId);
				}
				
				result.add(response);
			} else {
				LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
						+ "], and error message is [" + resultItem.getErrorMsg() + "]");
				result.add(null);
			}
		}
		return result;
	}
	
	/**
	 * parseUpdateResult: 解析从ubmc返回的结果
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	private List<ResponseBaseMaterial> parseUpdateResult(RpcBatchResponse batchResponse, List<Text> textList) {
		int oldLen = textList.size();
		if (isFailed(batchResponse)) {
			return constructNullList(oldLen);
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != oldLen)) {
			int newlen = 0;
			if (responseList != null) {
				newlen = responseList.size();
			}
			LOG.error("ubmc response item number [" + newlen
					+ "] != request item number [" + oldLen + "]");
			return constructNullList(oldLen);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		for (int index = 0; index < responseList.size(); index++) {
			RpcResponse resultItem = responseList.get(index);
			Text text = textList.get(index);
			
			if (resultItem.getStatusCode() == RpcBatchResponse.OK) {
				ResultBean resultBean = resultItem.getResult();
				Map<String, String> resultMap = resultBean.getData();
				if (resultMap == null || resultMap.isEmpty()) {
					LOG.error("ubmc ResultBean is null or empty");
					result.add(null);
					continue;
				}
				
				String value = resultMap.get(UbmcConstant.RESULT_MAP_VALUE);
				if (StringUtil.isEmpty(value)) {
					LOG.error("ubmc response value is null");
					result.add(null);
					continue;
				}
				ResponseBaseMaterial response = UbmcParser.transformToObject(value);
				
				String mcIdStr = resultMap.get(UbmcConstant.RESULT_MAP_MCID);
				String versionIdStr = resultMap.get(UbmcConstant.RESULT_MAP_VERSIONID);
				if (StringUtil.isEmpty(mcIdStr) || StringUtil.isEmpty(versionIdStr)) {
					LOG.error("ubmc response mcId or versionId is null");
					result.add(null);
				} else {
					Long mcId = Long.valueOf(mcIdStr);
					Integer versionId = Integer.valueOf(versionIdStr);
					response.setMcId(mcId);
					response.setVersionId(versionId);
				}
				
				result.add(response);
			} else {
				LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
						+ "], and error message is [" + resultItem.getErrorMsg()
						+ "], mcId=" + text.getMcId() + ", versionId=" + text.getVersionId());
				result.add(null);
			}
		}
		return result;
	}
	
	/**
	 * parseRemoveResult: 解析从ubmc返回的结果，返回结果仅包含mcId和versionId
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	private List<ResponseBaseMaterial> parseRemoveResult(RpcBatchResponse batchResponse, List<Text> textList) {
		int oldLen = textList.size();
		if (isFailed(batchResponse)) {
			return constructNullList(oldLen);
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != textList.size())) {
			int newlen = 0;
			if (responseList != null) {
				newlen = responseList.size();
			}
			LOG.error("ubmc response item number [" + newlen
					+ "] != request item number [" + textList.size() + "]");
			return constructNullList(oldLen);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		for (int index = 0; index < responseList.size(); index++) {
			RpcResponse resultItem = responseList.get(index);
			Text text = textList.get(index);
			
			if (resultItem.getStatusCode() == RpcBatchResponse.OK) {
				ResponseLite response = new ResponseLite(text.getMcId(), text.getVersionId());
				result.add(response);
			} else {
				LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
						+ "], and error message is [" + resultItem.getErrorMsg()
						+ "], mcId=" + text.getMcId() + ", versionId=" + text.getVersionId());
				result.add(null);
			}
		}
		return result;
	}
	
	/**
	 * parseGetResult: 解析从ubmc返回的结果
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	private List<ResponseBaseMaterial> parseGetResult(RpcBatchResponse batchResponse, List<Text> textList) {
		int oldLen = textList.size();
		if (isFailed(batchResponse)) {
			return constructNullList(oldLen);
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != textList.size())) {
			int newlen = 0;
			if (responseList != null) {
				newlen = responseList.size();
			}
			LOG.error("ubmc response item number [" + newlen
					+ "] != request item number [" + textList.size() + "]");
			return constructNullList(oldLen);
		}
		
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>();
		for (int index = 0; index < responseList.size(); index++) {
			RpcResponse resultItem = responseList.get(index);
			Text text = textList.get(index);
			
			if (resultItem.getStatusCode() == RpcBatchResponse.OK) {
				ResultBean resultBean = resultItem.getResult();
				Map<String, String> resultMap = resultBean.getData();
				if (resultMap == null || resultMap.isEmpty()) {
					LOG.error("ubmc ResultBean is null or empty");
					result.add(null);
				}
				
				String value = resultMap.get(UbmcConstant.RESULT_MAP_VALUE);
				if (StringUtil.isEmpty(value)) {
					LOG.error("ubmc response value is null");
					result.add(null);
				}
				ResponseBaseMaterial response = UbmcParser.transformToObject(value);
				response.setMcId(text.getMcId());
				response.setVersionId(text.getVersionId());
				
				result.add(response);
			} else {
				LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
						+ "], and error message is [" + resultItem.getErrorMsg() 
						+ "], mcId=" + text.getMcId() + ", versionId=" + text.getVersionId());
				result.add(null);
			}
		}
		return result;
	}
	
	/**
	 * parseGetDataResult: 解析从ubmc返回的结果
	 * @version cpweb-640
	 * @author genglei01
	 * @date Oct 17, 2013
	 */
	private List<byte[]> parseGetDataResult(RpcBatchResponse batchResponse, List<Long> mediaIdList) {
		int oldLen = mediaIdList.size();
		if (isFailed(batchResponse)) {
			return constructNullListForData(oldLen);
		}
		
		List<RpcResponse> responseList = batchResponse.getResponses();
		if ((null == responseList) || (responseList.size() != mediaIdList.size())) {
			int newlen = 0;
			if (responseList != null) {
				newlen = responseList.size();
			}
			LOG.error("ubmc response from getting the binary data item number [" + newlen
					+ "] != request item number [" + mediaIdList.size() + "]");
			return constructNullListForData(oldLen);
		}
		
		List<byte[]> result = new ArrayList<byte[]>();
		for (int index = 0; index < responseList.size(); index++) {
			RpcResponse resultItem = responseList.get(index);
			Long mediaId = mediaIdList.get(index);
			
			if (resultItem == null || resultItem.getStatusCode() != RpcBatchResponse.OK) {
				LOG.error("ubmc response status is [" + resultItem.getStatusCode() 
						+ "], and error message is [" + resultItem.getErrorMsg() 
						+ "], mediaId=" + mediaId);
				result.add(null);
				continue;
			}
			
			ResultBean resultBean = resultItem.getResult();
			if (resultBean == null) {
				LOG.error("ubmc ResultBean is null or empty for mediaId=" + mediaId);
				result.add(null);
				continue;
			}
			
			byte[] data = resultBean.getBinary();
			if (data == null) {
				LOG.error("ubmc data from ResultBean is null or empty for mediaId=" + mediaId);
				result.add(null);
				continue;
			}
			
			result.add(data);
		}
		return result;
	}
	
	/**
	 * isFailed: 检查调用结果是否全部失败
	 * 全部失败的返回false，部分成功或者全部成功的返回true
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 9, 2013
	 */
	private boolean isFailed(RpcBatchResponse batchResponse) {
		if (null == batchResponse) {
			LOG.warn("ubmc response bean is null");
			return true;
		}
		int status = batchResponse.getStatusCode();
		if (status == RpcBatchResponse.FAIL) {
			LOG.error("ubmc batch-response status is [" + status 
					+ "], and error message is [" + batchResponse.getErrorMsg() + "]");
			return true;
		}
		return false;
	}
	
	/**
	 * constructNullList: 生成指定长度的List，每个Item均为null
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 30, 2013
	 */
	private List<ResponseBaseMaterial> constructNullList(int size) {
		List<ResponseBaseMaterial> result = new ArrayList<ResponseBaseMaterial>(size);
		
		for (int index = 0; index < size; index++) {
			result.add(null);
		}

		return result;
	}
	
	/**
	 * constructNullListForData: 生成指定长度的List，每个Item均为null
	 * @version cpweb-640
	 * @author genglei01
	 * @date Oct 17, 2013
	 */
	private List<byte[]> constructNullListForData(int size) {
		List<byte[]> result = new ArrayList<byte[]>(size);
		
		for (int index = 0; index < size; index++) {
			result.add(null);
		}

		return result;
	}
	
	private <T extends Object> List<List<T>> doPage(List<T> list, int pageSize) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}

		int pageNum = (list.size() / pageSize) + 1;
		if ((list.size() % pageSize) == 0) {
			pageNum -= 1;
		}
		List<List<T>> result = new ArrayList<List<T>>(pageNum);
		for (int i = 0; i < pageNum; i++) {
			int from = i * pageSize;
			int to = (i + 1) * pageSize;
			if (to > list.size()) {
				to = list.size();
			}
			List<T> item = list.subList(from, to);
			result.add(item);
		}

		return result;
	} 

	public UbmcDriverProxy getUbmcDriverProxy() {
		return ubmcDriverProxy;
	}

	public void setUbmcDriverProxy(UbmcDriverProxy ubmcDriverProxy) {
		this.ubmcDriverProxy = ubmcDriverProxy;
	}
}
