/**
 * TencentCreativeAuditApiImpl.java 
 */
package com.baidu.beidou.bes.tencent;

import static com.baidu.beidou.bes.util.BesUtil.decodeUnicode;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.lib.StringUtil;

import com.baidu.beidou.bes.util.HttpHelpers;
import com.baidu.beidou.cprounit.dao.UnitAdxDao;
import com.baidu.gson.JsonArray;
import com.baidu.gson.JsonObject;
import com.baidu.gson.JsonPrimitive;
import com.google.common.collect.Maps;

/**
 * 腾讯Api封装的实现类
 * 
 * @author lixukun
 * @date 2014-02-24
 */
public class TencentCreativeAuditApiImpl implements TencentCreativeAuditApi {
	private static final Log log = LogFactory.getLog(TencentCreativeAuditApi.class);
	private final static int DSP_ID = 162;
	private final static String TOKEN = "fb6c8886122001803fe3d2aece6f2346";
	private final static String DOMAIN = "http://opentest.adx.qq.com/";
	private final static String AUDIT_URL = "order/sync";
	private final static String GETSTATUS_URL = "order/getstatus";
	
	private int connectTimeout = 3 * 1000;
	private int readTimeout = 30 * 1000;
	
	@Override
	public boolean auditCreative(List<TencentCreative> creatives) {
		if (CollectionUtils.isEmpty(creatives)) {
			return true;
		}
		
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append(buildApiCommonPart());
		
		JsonArray array = new JsonArray();
		for (TencentCreative c : creatives) {
			if (c == null) {
				continue;
			}
			JsonObject o = new JsonObject();
			o.addProperty("dsp_order_id", c.getCreativeId());
			
			JsonArray fileUrls = new JsonArray();
			for (String url : c.getFileUrls()) {
				JsonObject urlObject = new JsonObject();
				urlObject.addProperty("file_url", url);
				fileUrls.add(urlObject);
			}
			o.add("file_info", fileUrls);
			o.addProperty("targeting_url", c.getTargetUrl());
			o.addProperty("client_name", c.getClientName());
			
			array.add(o);
		}
		contentBuilder.append("order_info=").append(array.toString());
		
		try {
			HttpURLConnection connection = (HttpURLConnection)new URL(DOMAIN + AUDIT_URL).openConnection();
			connection.setReadTimeout(readTimeout);
			connection.setConnectTimeout(connectTimeout);
			
			HttpHelpers.sendRequest(contentBuilder.toString().getBytes(), connection);
			byte[] responseBytes = HttpHelpers.readResponse(connection);
			JSONObject retObject = JSONObject.fromObject(new String(responseBytes));
			return retObject.getInt("ret_code") == 0;
		} catch (Exception ex) {
			log.error("TencentCreativeAuditApi", ex);
			return false;
		}
	}
	
	@Override
	public Map<Long, Integer> queryCreativeStatus(List<Long> creativeIds) {
		if (CollectionUtils.isEmpty(creativeIds)) {
			return Maps.<Long, Integer>newHashMapWithExpectedSize(0);
		}
		
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append(buildApiCommonPart());
		
		JsonArray array = new JsonArray();
		for (Long id : creativeIds) {
			if (id == null) {
				continue;
			}
			JsonPrimitive prim = new JsonPrimitive(id.toString());
			array.add(prim);
		}
		contentBuilder.append("dsp_order_id_info=").append(array.toString());
		
		Map<Long, Integer> results = new HashMap<Long, Integer>();
		try {
			HttpURLConnection connection = (HttpURLConnection)new URL(DOMAIN + GETSTATUS_URL).openConnection();
			connection.setReadTimeout(readTimeout);
			connection.setConnectTimeout(connectTimeout);
			
			HttpHelpers.sendRequest(contentBuilder.toString().getBytes(), connection);
			byte[] responseBytes = HttpHelpers.readResponse(connection);
			JSONObject retObject = JSONObject.fromObject(new String(responseBytes));
			
			if (retObject.getInt("ret_code") == 0) {
				JSONObject recordsObj = retObject.getJSONObject("ret_msg").getJSONObject("records");
				for (Long id : creativeIds) {
					if (id == null) {
						continue;
					}
					
					try {
						JSONObject recordObj = recordsObj.getJSONArray(id.toString()).getJSONObject(0);
						int code = transStatusToCode(recordObj.getString("status"));
						if (code < 0) {
							continue;
						}
						results.put(id, code);
					} catch (Exception ex) {
						continue;
					}
				}
			}
		} catch (Exception ex) {
			log.error("TencentCreativeAuditApi", ex);
		}		
		
		return results;
	}
	
	private String buildApiCommonPart() {
		StringBuilder sb = new StringBuilder();
		sb.append("dsp_id=").append(DSP_ID).append("&")
		  .append("token=").append(TOKEN).append("&");
		
		return sb.toString();
	}
	
	private int transStatusToCode(String status) {
		if (StringUtil.isEmpty(status)) {
			return -1;
		}
		status = decodeUnicode(status);
		if (status.equals("")) {
			return UnitAdxDao.AUDIT_APPROVED;
		} else if (status.equals("")) {
			return UnitAdxDao.AUDIT_DISAPPROVED;
		} else if (status.equals("")) {
			return UnitAdxDao.AUDIT_NOT_CHECKED;
		}
		
		return -1;
	}

	/**
	 * @return the connectTimeout
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * @return the readTimeout
	 */
	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * @param readTimeout the readTimeout to set
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
}
