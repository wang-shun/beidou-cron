package com.baidu.beidou.cprounit.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.mcdriver.RawMcDriverProxy;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertIconRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcResultBeanBase;
import com.baidu.beidou.cprounit.service.IconService;
import com.baidu.beidou.cprounit.service.bo.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseIconMaterial;
import com.baidu.beidou.util.service.impl.BaseDrawinRpcServiceImpl;

/**
 * 本类往DRMC存放的物料都存放在临时桶
 * @author hejinggen
 *
 */
public class IconServiceImpl extends BaseDrawinRpcServiceImpl implements IconService {

	private static final String DRMC_KEY_EXCEPTION = "exception";
	
	public IconServiceImpl(String syscode, String prodid) {
		super(syscode, prodid);
	}
	
	private RawMcDriverProxy rawMcDriverProxy;
	


	public List<ResponseIconMaterial> insertBatch(List<RequestIconMaterial> list) {
		
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}
		
		//封装请求参数
		List<InsertIconRequestBean> beanist = new ArrayList<InsertIconRequestBean>();
		for (RequestIconMaterial icon : list) {
			InsertIconRequestBean item = new InsertIconRequestBean(icon);
			beanist.add(item);
		}
		
		List<ResponseIconMaterial> result = new ArrayList<ResponseIconMaterial>();
		
		//处理分页
		List<List<InsertIconRequestBean>> idpartlist = doPage(beanist, CproUnitConfig.DRMC_BATCH_NUM);
		
		for (List<InsertIconRequestBean> beanpart : idpartlist){
			DrmcBatchResultBean resultBean = rawMcDriverProxy.tmpInsertBatch(beanpart, true, getHeaders());
			List<ResponseIconMaterial> part = parseBatchMaterialResult(resultBean,beanpart.size());
			if (CollectionUtils.isEmpty(part)){
				return Collections.emptyList();
			} else {
				result.addAll(part);
			}
		}

		return result;
	}

	private <T> List<List<T>> doPage(List<T> list, int pagesize){
		if (CollectionUtils.isEmpty(list)){
			return Collections.emptyList();
		}
		
		int pageNum = (list.size() / pagesize) + 1;
		if ((list.size() % pagesize) == 0){
			pageNum -= 1;
		}
		List<List<T>> result = new ArrayList<List<T>>(pageNum);
		for (int i = 0; i < pageNum; i++){
			int from = i * pagesize;
			int to = (i + 1) * pagesize;
			if (to > list.size()){
				to = list.size();
			}
			List<T> item = list.subList(from, to);
			result.add(item);
		}
		
		return result;
	}

	
	private boolean isInvokeSuccess(DrmcResultBeanBase resultBean) {
		if (null == resultBean) {
			LOG.warn("drmc response bean is null");
			return false;
		}
		int status = resultBean.getStatus();
		if (status != 0) {
			LOG.warn("drmc response status is [" + status + "]");
			return false;
		}
		return true;
	}
	
	private List<ResponseIconMaterial> parseBatchMaterialResult(DrmcBatchResultBean resultBean, int oldlen){
		if (!isInvokeSuccess(resultBean)) {
			return Collections.emptyList();
		}
		
		List<Map<String, String>> data = resultBean.getData();
		if ((null == data) || (data.size() != oldlen)){
			int newlen = 0;
			if (data != null) {
				newlen = data.size();
			}
			LOG.error("drmc response item number [" + newlen + "] != request item number [" + oldlen + "]");
			return Collections.emptyList();
		}
		
		List<ResponseIconMaterial> result = new ArrayList<ResponseIconMaterial>(oldlen);
		for (Map<String, String> item : data){
			boolean excp = item.containsKey(DRMC_KEY_EXCEPTION);
			
			if (excp){
				try {
					String info = item.get(DRMC_KEY_EXCEPTION);
					LOG.error(info);
				} catch (Exception e){
					LOG.error(e.getMessage(), e);
				}
				result.add(null);
			} else {

				ResponseIconMaterial mat = ResponseIconMaterial.getInstance(item);
				
				result.add(mat);
			}
		}
		return result;
	}
	
	//================Below are getters and setters===========================

	public RawMcDriverProxy getRawMcDriverProxy() {
		return rawMcDriverProxy;
	}

	public void setRawMcDriverProxy(RawMcDriverProxy rawMcDriverProxy) {
		this.rawMcDriverProxy = rawMcDriverProxy;
	}
	
}
