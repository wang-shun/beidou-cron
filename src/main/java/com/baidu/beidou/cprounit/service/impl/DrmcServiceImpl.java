/**
 * 2009-8-20 下午02:11:37
 * @author zengyunfeng
 */
package com.baidu.beidou.cprounit.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
//import org.apache.struts2.json.JSONUtil;

import com.baidu.beidou.cprounit.constant.CproUnitConfig;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.mcdriver.RawMcDriverProxy;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertImageRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertImageRequestBean2;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertLiteralRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertLiteralWithIconRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertLiteralWithIconRequestBean2;
import com.baidu.beidou.cprounit.mcdriver.bean.request.InsertRequestBeanBase;
import com.baidu.beidou.cprounit.mcdriver.bean.request.UpdateImageRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.request.UpdateImageRequestBean2;
import com.baidu.beidou.cprounit.mcdriver.bean.request.UpdateLiteralRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.request.UpdateLiteralWithIconRequestBean;
import com.baidu.beidou.cprounit.mcdriver.bean.request.UpdateLiteralWithIconRequestBean2;
import com.baidu.beidou.cprounit.mcdriver.bean.request.UpdateRequestBeanBase;
import com.baidu.beidou.cprounit.mcdriver.bean.response.ActiveBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.BackupResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.CopyResult;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcActiveBatchResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchBackupResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchCopyResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchRemoveResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcBatchResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcMediaResultBean;
import com.baidu.beidou.cprounit.mcdriver.bean.response.DrmcResultBeanBase;
import com.baidu.beidou.cprounit.mcdriver.bean.response.RemoveResult;
import com.baidu.beidou.cprounit.service.DrmcService;
import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial2;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralMaterial;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralWithIconMaterial;
import com.baidu.beidou.cprounit.service.bo.request.RequestLiteralWithIconMaterial2;
import com.baidu.beidou.cprounit.service.bo.response.ResponseImageMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseLiteralMaterial;
import com.baidu.beidou.cprounit.service.bo.response.ResponseLiteralWithIconMaterial;
import com.baidu.beidou.util.JsonUtils;
import com.baidu.beidou.util.service.impl.BaseDrawinRpcServiceImpl;

public class DrmcServiceImpl extends BaseDrawinRpcServiceImpl implements DrmcService {
	
	public static final String DRMC_KEY_MCID = "mcid";
	/**
	 * 用于区分drmc返回结果的物料类型
	 */
	public static final String DRMC_KEY_FILESRC = "fileSrc";
	public static final String DRMC_KEY_WULIAOTYPE = "wuliaoType";//1-文字、2-图片、3-Flash、5-图文
	public static final String DRMC_KEY_STATUS = "status";
	public static final String DRMC_KEY_EXCEPTION = "exception";
	public static final int DRMC_QUERYTYPE_SIMP = 0;
	public static final int DRMC_COPYTYPE_FRML = 0;
	public static final int DRMC_COPYTYPE_TEMP = 1;
	
	/**
	 * 用于tmpActiveBatch: 请求中的临时物料id
	 */
	private static final String REQUEST_ACTIVE_OID_KEY= "tmpmcid";
	
	/**
	 * 用于tmpActiveBatch: 请求中的正式物料id
	 */
	private static final String REQUEST_ACTIVE_NID_KEY= DRMC_KEY_MCID;
	
	private RawMcDriverProxy rawMcDriverProxy;
	
	public DrmcServiceImpl(String syscode, String prodid) {
		super(syscode, prodid);
	}

	/**
	 * 根据ids, fmids构造List<Map<String, Long>>结构，<br>
	 * 其中Map的key值对应REQUEST_ACTIVE_OID_KEY和REQUEST_ACTIVE_NID_KEY，<br>
	 * value对<=0的fmid值，构造为null.
	 * @param ids
	 * @param fmids
	 * @return
	 */
	private List<Map<String,Long>> getTmpActiveRequestId(final long[] ids, final long[] fmids){
		if(ids.length!=fmids.length){
			return new ArrayList<Map<String,Long>>(0);
		}
		List<Map<String, Long>> result = new ArrayList<Map<String,Long>>(ids.length);
		Map<String, Long> ele =null;
		int length = ids.length;
		for(int index=0; index<length; index++){
			ele = new HashMap<String, Long>();
			ele.put(REQUEST_ACTIVE_OID_KEY, ids[index]);
			if(fmids[index]>0){
				ele.put(REQUEST_ACTIVE_NID_KEY, fmids[index]);
			}else{
				ele.put(REQUEST_ACTIVE_NID_KEY, null);
			}
			result.add(ele);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.baidu.beidou.cprounit.service.DrmcService#tmpActive(long[], long[], boolean, boolean)
	 */
	public BeidouMaterialBase[] tmpActiveBatch(final long[] ids2, final long[] fmids2,
			final boolean deltmp, final boolean errorexit) {
		if(ids2==null || ids2.length==0 || fmids2==null || fmids2.length==0){
			return new BeidouMaterialBase[0];
		}else if(ids2.length!=fmids2.length){
			LOG.error("DRMC tmpActiveBatch request with wrong size!");
			return new BeidouMaterialBase[ids2.length];
		}
		
		final List<Map<String, Long>> request2 = getTmpActiveRequestId(ids2, fmids2);

		//分批处理
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>();
		List<List<Map<String, Long>>> idpartlist = doPage2(request2, CproUnitConfig.DRMC_BATCH_NUM);
		for (List<Map<String, Long>> idpart : idpartlist){
			DrmcActiveBatchResultBean resultBean = rawMcDriverProxy.tmpActiveBatch(idpart, deltmp, errorexit, getHeaders());
			if (!isInvokeSuccess(resultBean)) {
				return new BeidouMaterialBase[ids2.length];
			}
			List<ActiveBean> data = resultBean.getData();
			if(data==null ){
				//出现异常
				//是否需要重试？统一为不重试
				LOG.error("DRMC tmpActiveBatch get null data!");
				return new BeidouMaterialBase[ids2.length];
			}
			int index = 0;
			Map<String, String> tmpResult = null;
			for(ActiveBean activeBean : data){
				if(activeBean.getMcid()>0){
					tmpResult = activeBean.getValue();
					//为了复用已有代码，设置map中的key
					tmpResult.put(DRMC_KEY_MCID, String.valueOf(activeBean.getMcid()));
					
					BeidouMaterialBase mat = parseMaterialFromDrmcDataItem(tmpResult);
										
					result.add(mat);
				}else{
					result.add(null);
				}
				index++;
			}
			for(; index< idpart.size(); index++){
				result.add(null);
			}
		}
		return result.toArray(new BeidouMaterialBase[0]);

	}

	public List<BeidouMaterialBase> getElements(final long[] ids2, final boolean ismaster) {
		if ((null == ids2) || (ids2.length <= 0)){
			return Collections.emptyList();
		}
		
		//分批处理
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>();
		List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
		for (long[] idpart : idpartlist){
			DrmcBatchResultBean resultBean = rawMcDriverProxy.getElements(idpart, DRMC_QUERYTYPE_SIMP, ismaster, getHeaders());
			List<BeidouMaterialBase> part = parseBatchGetResult(resultBean, idpart.length);
			if (CollectionUtils.isEmpty(part)){
				return Collections.emptyList();
			} else {
				result.addAll(part);
			}
		}
		
		return result;
	}
	public List<BeidouMaterialBase> getTmpElements(final long[] ids2, final boolean ismaster) {
		if ((null == ids2) || (ids2.length <= 0)){
			return Collections.emptyList();
		}
		
		//分批处理
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>();
		List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
		for (long[] idpart : idpartlist){
			DrmcBatchResultBean resultBean = rawMcDriverProxy.getTmpElements(idpart, DRMC_QUERYTYPE_SIMP, ismaster, getHeaders());
			List<BeidouMaterialBase> part = parseBatchGetResult(resultBean, idpart.length);
			if (CollectionUtils.isEmpty(part)){
				return Collections.emptyList();
			} else {
				result.addAll(part);
			}
		}
		return result;
	}
	public List<BeidouMaterialBase> getHisElements(final long[] ids2, final boolean ismaster) {
		if ((null == ids2) || (ids2.length <= 0)){
			return Collections.emptyList();
		}
		
		//分批处理
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>();
		List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
		for (long[] idpart : idpartlist){
			DrmcBatchResultBean resultBean = rawMcDriverProxy.getHisElements(idpart, DRMC_QUERYTYPE_SIMP, ismaster, getHeaders());
			List<BeidouMaterialBase> part = parseBatchGetResult(resultBean, idpart.length);
			if (CollectionUtils.isEmpty(part)){
				return Collections.emptyList();
			} else {
				result.addAll(part);
			}
		}
		return result;
	}

	public List<BeidouMaterialBase> tmpInsertBatch(final List<BeidouMaterialBase> ads2, final boolean errorExit){
		if (CollectionUtils.isEmpty(ads2)){
			return Collections.emptyList();
		}
		
		List beans2 = new ArrayList(ads2.size());
		for (BeidouMaterialBase ad : ads2){
			InsertRequestBeanBase bean;
			if (ad instanceof RequestImageMaterial){
				RequestImageMaterial add = (RequestImageMaterial) ad;
				bean = new InsertImageRequestBean(add);
			} else if (ad instanceof RequestImageMaterial2){
				RequestImageMaterial2 add = (RequestImageMaterial2) ad;
				bean = new InsertImageRequestBean2(add);
			} else if (ad instanceof RequestLiteralWithIconMaterial){
				RequestLiteralWithIconMaterial add = (RequestLiteralWithIconMaterial) ad;
				bean = new InsertLiteralWithIconRequestBean(add);
			} else if (ad instanceof RequestLiteralWithIconMaterial2){
				RequestLiteralWithIconMaterial2 add = (RequestLiteralWithIconMaterial2) ad;
				bean = new InsertLiteralWithIconRequestBean2(add);
			} else if (ad instanceof RequestLiteralMaterial){
				RequestLiteralMaterial add = (RequestLiteralMaterial) ad;
				bean = new InsertLiteralRequestBean(add);
			} else {
				bean = null;
				LOG.error("input ad type error [ " + ad + " ]");
			}
			
			beans2.add(bean);
		}
		
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>();
		List<List<BeidouMaterialBase>> beanpartlist = doPage(beans2, CproUnitConfig.DRMC_BATCH_NUM);
		for (List<BeidouMaterialBase> beanpart : beanpartlist){
			DrmcBatchResultBean resultBean = rawMcDriverProxy.tmpInsertBatch(beanpart, errorExit, getHeaders());
			List<BeidouMaterialBase> part = parseBatchMaterialResult(resultBean, beanpart.size());
			if (CollectionUtils.isEmpty(part)){
				return Collections.emptyList();
			} else {
				result.addAll(part);
			}
		}
		return result;
	}
	public List<BeidouMaterialBase> tmpUpdateBatch(final LinkedHashMap<BeidouMaterialBase, Integer> ads2, final boolean errorExit){
				if ((null == ads2) || (ads2.isEmpty())){
					return Collections.emptyList();
				}
				
				List beans2 = new ArrayList(ads2.size());
				for (BeidouMaterialBase ad : ads2.keySet()){
					int oldtype = ads2.get(ad);
					UpdateRequestBeanBase bean;
					if (ad instanceof RequestLiteralWithIconMaterial){//更新为图文物料
						RequestLiteralWithIconMaterial add = (RequestLiteralWithIconMaterial) ad;
						if (add.getFileSrc() == null){ // 不更新图片实体
							RequestLiteralWithIconMaterial2 add2 = new RequestLiteralWithIconMaterial2(
									add.getTitle(), add.getShowUrl(), 
									add.getTargetUrl(), 
									add.getDescription1(),
									add.getDescription2(),
									"", 
									add.getMC_POS_WIDTH(), add.getMC_POS_HEIGHT(),
									add.getWirelessShowUrl(),
									add.getWirelessTargetUrl()
									);
							add2.setMcid(add.getMcid());
							bean = new UpdateLiteralWithIconRequestBean2(add2, oldtype);
						} else {
							bean = new UpdateLiteralWithIconRequestBean(add, oldtype);
						}
					} else if (ad instanceof RequestLiteralWithIconMaterial2){//不支持用URL进行icon更新
						bean = null;
						LOG.error("input ad type not supported to update [ " + ad + " ]");
					} else if (ad instanceof RequestLiteralMaterial){//文字物料
						RequestLiteralMaterial add = (RequestLiteralMaterial) ad;
						bean = new UpdateLiteralRequestBean(add, oldtype);
					} else if (ad instanceof RequestImageMaterial){//图片物料
						RequestImageMaterial add = (RequestImageMaterial) ad;
						if (add.getFileSrc() == null){ // 不更新图片实体
							RequestImageMaterial2 add2 = new RequestImageMaterial2(
									add.getTitle(), add.getShowUrl(), 
									add.getTargetUrl(), "", 
									add.getMC_POS_WIDTH(), add.getMC_POS_HEIGHT(),
									add.getWirelessShowUrl(),
									add.getWirelessTargetUrl()
									);
							add2.setMcid(add.getMcid());
							bean = new UpdateImageRequestBean2(add2, oldtype);
						} else {
							bean = new UpdateImageRequestBean(add, oldtype);
						}
					} else {
						bean = null;
						LOG.error("input ad type error [ " + ad + " ]");
					}
					
					beans2.add(bean);
				}
				
					List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>();
					List<List<BeidouMaterialBase>> beanpartlist = doPage(beans2, CproUnitConfig.DRMC_BATCH_NUM);
					for (List<BeidouMaterialBase> beanpart : beanpartlist){
						DrmcBatchResultBean resultBean = rawMcDriverProxy.tmpUpdateBatch(beanpart, errorExit, getHeaders());
						List<BeidouMaterialBase> part = parseBatchMaterialResult(resultBean, beanpart.size());
						if (CollectionUtils.isEmpty(part)){
							return Collections.emptyList();
						} else {
							result.addAll(part);
						}
					}
					return result;
	}
	
	public long[] copyBatch(final long[] ids2, final boolean errorExit){
				if ((null == ids2) || (ids2.length <= 0)){
					return new long[0];
				}
				
					List<Long> result = new ArrayList<Long>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchCopyResultBean resultBean = rawMcDriverProxy.copy(idpart, DRMC_COPYTYPE_FRML, errorExit, getHeaders());
						List<Long> part = parseBatchCopyResult(resultBean, idpart.length);
						if (CollectionUtils.isEmpty(part)){
							return new long[0];
						} else {
							result.addAll(part);
						}
					}
					
					return ArrayUtils.toPrimitive(
							result.toArray(new Long[0])
							);
	}
	
	public long[] unactiveBatch(final long[] ids2, final boolean errorExit){
				if ((null == ids2) || (ids2.length <= 0)){
					return new long[0];
				}
				
					List<Long> result = new ArrayList<Long>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchCopyResultBean resultBean = rawMcDriverProxy.copy(idpart, DRMC_COPYTYPE_TEMP, errorExit, getHeaders());
						List<Long> part = parseBatchCopyResult(resultBean, idpart.length);
						if (CollectionUtils.isEmpty(part)){
							return new long[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Long[0])
							);
	}
	
	public long[] tmpCopyBatch(final long[] ids2, final boolean errorExit){
				if ((null == ids2) || (ids2.length <= 0)){
					return new long[0];
				}
				
					List<Long> result = new ArrayList<Long>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchCopyResultBean resultBean = rawMcDriverProxy.tmpCopy(idpart, errorExit, getHeaders());
						List<Long> part = parseBatchCopyResult(resultBean, idpart.length);
						if (CollectionUtils.isEmpty(part)){
							return new long[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Long[0])
							);
	}
	
	public long[] backupBatch(final long[] ids2){
				if ((null == ids2) || (ids2.length <= 0)){
					return new long[0];
				}
				
					List<Long> result = new ArrayList<Long>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchBackupResultBean resultBean = rawMcDriverProxy.backup(idpart, getHeaders());
						List<Long> part = parseBatchBackupResult(resultBean, idpart.length);
						if (CollectionUtils.isEmpty(part)){
							return new long[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Long[0])
							);
	}
	
	public long[] tmpBackupBatch(final long[] ids2){
				if ((null == ids2) || (ids2.length <= 0)){
					return new long[0];
				}
				
					List<Long> result = new ArrayList<Long>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchBackupResultBean resultBean = rawMcDriverProxy.tmpBackup(idpart, getHeaders());
						List<Long> part = parseBatchBackupResult(resultBean, idpart.length);
						if (CollectionUtils.isEmpty(part)){
							return new long[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Long[0])
							);
	}
	
	public int[] removeBatch(final long[] ids2){
				if ((null == ids2) || (ids2.length <= 0)){
					return new int[0];
				}
				
					List<Integer> result = new ArrayList<Integer>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchRemoveResultBean resultBean = rawMcDriverProxy.remove(idpart, getHeaders());
						List<Integer> part = parseBatchRemoveResult(resultBean, idpart, CproUnitConstant.DRMC_MATGROUP_FORMAL);
						if (CollectionUtils.isEmpty(part)){
							return new int[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Integer[0])
							);
	}
	
	public int[] tmpRemoveBatch(final long[] ids2){
				if ((null == ids2) || (ids2.length <= 0)){
					return new int[0];
				}
				
					List<Integer> result = new ArrayList<Integer>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchRemoveResultBean resultBean = rawMcDriverProxy.tmpRemove(idpart, getHeaders());
						List<Integer> part = parseBatchRemoveResult(resultBean, idpart, CproUnitConstant.DRMC_MATGROUP_TMP);
						if (CollectionUtils.isEmpty(part)){
							return new int[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Integer[0])
							);
	}

	public int[] hisRemoveBatch(final long[] ids2){
				if ((null == ids2) || (ids2.length <= 0)){
					return new int[0];
				}
				
					List<Integer> result = new ArrayList<Integer>();
					List<long[]> idpartlist = doPage(ids2, CproUnitConfig.DRMC_BATCH_NUM);
					for (long[] idpart : idpartlist){
						DrmcBatchRemoveResultBean resultBean = rawMcDriverProxy.hisRemove(idpart, getHeaders());
						List<Integer> part = parseBatchRemoveResult(resultBean, idpart, CproUnitConstant.DRMC_MATGROUP_HISTORY);
						if (CollectionUtils.isEmpty(part)){
							return new int[0];
						} else {
							result.addAll(part);
						}
					}
					return ArrayUtils.toPrimitive(
							result.toArray(new Integer[0])
							);
	}
	
	public byte[] getMediaContent(final String url){
				if ((StringUtils.isEmpty(url))){
					return null;
				}
				
				DrmcMediaResultBean resultBean = rawMcDriverProxy.getMedia(url, getHeaders());
				byte[] result = parseMediaResult(resultBean);
				return result;
	}
	
	/**************************inner function*******************************/	
	
	private List<List<Map<String, Long>>> doPage2(List<Map<String, Long>> list, int pagesize){
		if (CollectionUtils.isEmpty(list)){
			return Collections.emptyList();
		}
		
		int pageNum = (list.size() / pagesize) + 1;
		if ((list.size() % pagesize) == 0){
			pageNum -= 1;
		}
		List<List<Map<String, Long>>> result = new ArrayList<List<Map<String, Long>>>(pageNum);
		for (int i = 0; i < pageNum; i++){
			int from = i * pagesize;
			int to = (i + 1) * pagesize;
			if (to > list.size()){
				to = list.size();
			}
			List<Map<String, Long>> item = list.subList(from, to);
			result.add(item);
		}
		
		return result;
	}
	
	private List<List<BeidouMaterialBase>> doPage(List<BeidouMaterialBase> list, int pagesize){
		if (CollectionUtils.isEmpty(list)){
			return Collections.emptyList();
		}
		
		int pageNum = (list.size() / pagesize) + 1;
		if ((list.size() % pagesize) == 0){
			pageNum -= 1;
		}
		List<List<BeidouMaterialBase>> result = new ArrayList<List<BeidouMaterialBase>>(pageNum);
		for (int i = 0; i < pageNum; i++){
			int from = i * pagesize;
			int to = (i + 1) * pagesize;
			if (to > list.size()){
				to = list.size();
			}
			List<BeidouMaterialBase> item = list.subList(from, to);
			result.add(item);
		}
		
		return result;
	}
	
	//TODO::::::::::::::::::完成用泛型分页
	private List<List<?>> doPageInternal(List<?> list, int pagesize) {

		if (CollectionUtils.isEmpty(list)){
			return Collections.emptyList();
		}
		
		int pageNum = (list.size() / pagesize) + 1;
		if ((list.size() % pagesize) == 0){
			pageNum -= 1;
		}
		List<List<?>> result = new ArrayList<List<?>>(pageNum);
		for (int i = 0; i < pageNum; i++){
			int from = i * pagesize;
			int to = (i + 1) * pagesize;
			if (to > list.size()){
				to = list.size();
			}
			List<?> item = list.subList(from, to);
			result.add(item);
		}
		
		return result;
	}
	
	private List<long[]> doPage(long[] list, int pagesize){
		if ((null == list) ||
				(0 == list.length)){
			return Collections.emptyList();
		}
		
		int pageNum = (list.length / pagesize) + 1;
		if ((list.length % pagesize) == 0){
			pageNum -= 1;
		}
		List<long[]> result = new ArrayList<long[]>(pageNum);
		for (int i = 0; i < pageNum; i++){
			int from = i * pagesize;
			int to = (i + 1) * pagesize;
			if (to > list.length){
				to = list.length;
			}
			
			long[] item = ArrayUtils.subarray(list, from, to);
			result.add(item);
		}
		
		return result;
	}
	
	private byte[] parseMediaResult(DrmcMediaResultBean resultBean){
		if (!isInvokeSuccess(resultBean)) {
			return null;
		}
		
		return resultBean.getData();
	}
	
	/**
	 * 从dr-mc返回的Bean中抽取删除状态
	 * 若resultBean为null或status不为0，或resultBean中元素个数与oldlen不一致
	 * 则重新取从drmc按ID获取物料，某ID对应返回null表明删除成功或物料不存在
	 * 若重新获取的元素个数与oldlen不一致，则返回empty array
	 */
	private List<Integer> parseBatchRemoveResult(DrmcBatchRemoveResultBean resultBean, long[] idsShouldHaveBeenRemoved, int drmcType){
		
		int oldlen = idsShouldHaveBeenRemoved.length;
		
		int newlen = 0;
		
		if (isInvokeSuccess(resultBean)
				&&
				resultBean.getData() != null) 
		{
			newlen = resultBean.getData().size();
		}
		
		List<Integer> result = new ArrayList<Integer>(oldlen);

		if (oldlen != newlen) {
			
			List<BeidouMaterialBase> regetResult = null;
			if (drmcType == CproUnitConstant.DRMC_MATGROUP_FORMAL) {
				regetResult = this.getElements(idsShouldHaveBeenRemoved, false); 
			} else if (drmcType == CproUnitConstant.DRMC_MATGROUP_TMP) {
				regetResult = this.getTmpElements(idsShouldHaveBeenRemoved, false);
			} else if (drmcType == CproUnitConstant.DRMC_MATGROUP_HISTORY) {
				regetResult = this.getHisElements(idsShouldHaveBeenRemoved, false);
			}
			if (regetResult != null && regetResult.size() == oldlen) {
				for (BeidouMaterialBase materia : regetResult) {
					result.add(materia == null ? 0:1);//若凭物料Id取回的物料为NULL，说明不存在或已经删除成功
				}
			}
			
		} else {

			List<RemoveResult> data = resultBean.getData();
			
			for (int i = 0; i < oldlen; i++){
				RemoveResult item = data.get(i);
				//删除失败时，drmc返回表示失败的status值
				result.add(item.getStatus());
			}
		}
		
		return result;

	}
		
	/**
	 * 从dr-mc返回的Bean中抽取备份后物料ID
	 * 若resultBean为null或status不为0，则返回empty array
	 * 若resultBean中元素个数与oldlen不一致，则返回empty array
	 * 若某条目错误，对应结果项为-1
	 */
	private List<Long> parseBatchBackupResult(DrmcBatchBackupResultBean resultBean, int oldlen){
		if (!isInvokeSuccess(resultBean)) {
			return Collections.emptyList();
		}
		
		List<BackupResult> data = resultBean.getData();
		if ((null == data) || (data.size() != oldlen)){
			int newlen = 0;
			if (data != null) {
				newlen = data.size();
			}
			LOG.error("drmc response item number [" + newlen + "] != request item number [" + oldlen + "]");
			return Collections.emptyList();
		}
		
		List<Long> result = new ArrayList<Long>(oldlen);
		for (int i = 0; i < oldlen; i++){
			BackupResult item = data.get(i);
			//备份失败时，drmc返回表示失败的backmcid值
			result.add(item.getBackmcid());
		}
		return result;
	}
	
	/**
	 * 从dr-mc返回的Bean中抽取拷贝后物料ID
	 * 若resultBean为null或status不为0，则返回empty array
	 * 若resultBean中元素个数与oldlen不一致，则返回empty array
	 * 若某条目错误，对应结果项为-1
	 */
	private List<Long> parseBatchCopyResult(DrmcBatchCopyResultBean resultBean, int oldlen){
		if (!isInvokeSuccess(resultBean)) {
			return Collections.emptyList();
		}
		
		List<CopyResult> data = resultBean.getData();
		if ((null == data) || (data.size() != oldlen)){
			int newlen = 0;
			if (data != null) {
				newlen = data.size();
			}
			LOG.error("drmc response item number [" + newlen + "] != request item number [" + oldlen + "]");
			return Collections.emptyList();
		}
		
		List<Long> result = new ArrayList<Long>(oldlen);
		for (int i = 0; i < oldlen; i++){
			CopyResult item = data.get(i);
			if (item.getStatus() != CproUnitConstant.DRMC_STATUS_OK){
				result.add(Long.valueOf(CproUnitConstant.DRMC_COPY_FAIL));
			} else {
				result.add(item.getToid());
			}
		}
		return result;
	}
	
	/**
	 * 从dr-mc Insert/Update返回的Bean中抽取beidou物料结构
	 * 若resultBean为null或status不为0，则返回空list
	 * 若resultBean中元素个数与oldlen不一致，则返回空list
	 * 若某条目错误，对应结果项为null
	 */
	private List<BeidouMaterialBase> parseBatchMaterialResult(DrmcBatchResultBean resultBean, int oldlen){
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
		
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>(oldlen);
		for (Map<String, String> item : data){
			boolean excp = item.containsKey(DRMC_KEY_EXCEPTION);
			
			if (excp){
				try {
					String info = item.get(DRMC_KEY_EXCEPTION);
					// modified by hexiufeng 2014-03-18
					// use jacson to parse json instead of JSONUtion class in struts, 
					// and remove struts package from pom.xml
//					Object msg = JSONUtil.deserialize(info);
					Object msg = JsonUtils.json2Object(info,HashMap.class);
					LOG.error(msg);
				} catch (Exception e){
					LOG.error(e.getMessage(), e);
				}
				result.add(null);
			} else {

				BeidouMaterialBase mat = parseMaterialFromDrmcDataItem(item);
				
				result.add(mat);
			}
		}
		return result;
	}
	
	/**
	 * 从dr-mc GET返回的Bean中抽取beidou物料结构
	 * 若resultBean为null或status不为0，则返回空list
	 * 若resultBean中元素个数与oldlen不一致，则返回空list
	 * 若某条目错误，对应结果项为null
	 */
	private List<BeidouMaterialBase> parseBatchGetResult(DrmcBatchResultBean resultBean, int oldlen){
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
		
		List<BeidouMaterialBase> result = new ArrayList<BeidouMaterialBase>(oldlen);
		for (Map<String, String> item : data){
			String status = (String) item.get(DRMC_KEY_STATUS);
			int stat = Integer.valueOf(status);
			if (stat != CproUnitConstant.DRMC_STATUS_OK){
				LOG.error("mcid [" + item.get(DRMC_KEY_MCID) + "] material response error[" + stat + "]");
				result.add(null);
			} else {
				
				BeidouMaterialBase mat = parseMaterialFromDrmcDataItem(item);
				
				result.add(mat);
			}
		}
		return result;
	}
	
	private BeidouMaterialBase parseMaterialFromDrmcDataItem(Map<String, String> item){
		
		int wuliaoType = -1;
		try {
			//危险代码
			//应该通过修复DMRC模板升级
			//像新加的图文模板一样，将旧模板的wuliaoType字段设为属性
			//cpweb-283, hejinggen
			if (item.containsKey(DRMC_KEY_WULIAOTYPE)) {//图文物料应该永远走这个分支
				wuliaoType = Integer.parseInt(item.get(DRMC_KEY_WULIAOTYPE));
			} else if (item.containsKey(DRMC_KEY_FILESRC)) {
				wuliaoType = CproUnitConstant.MATERIAL_TYPE_PICTURE;//OR CproUnitConstant.MATERIAL_TYPE_FLASH
			}

		} catch (NumberFormatException e) {
			LOG.error("mcid [" + item.get(DRMC_KEY_MCID) + "] material response wuliaotype error[" + item.get(DRMC_KEY_WULIAOTYPE) + "]");
			return null;
		}
		
		BeidouMaterialBase mat = null;
		
		if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL) {
			mat = ResponseLiteralMaterial.getInstance(item);
		} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_PICTURE || wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH){
			mat = ResponseImageMaterial.getInstance(item);
		} else if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_LITERAL_WITH_ICON){
			mat = ResponseLiteralWithIconMaterial.getInstance(item);
		} else {
			LOG.error("mcid [" + item.get(DRMC_KEY_MCID) + "] material response wuliaotype error[" + wuliaoType + "]");
		}
		
		return mat;
	}

	/**
	 * 检查调用结果是否成功
	 * 
	 * @author guojichun
	 * @param result
	 * @return
	 */
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

	public RawMcDriverProxy getRawMcDriverProxy() {
		return rawMcDriverProxy;
	}

	public void setRawMcDriverProxy(RawMcDriverProxy rawMcDriverProxy) {
		this.rawMcDriverProxy = rawMcDriverProxy;
	}

}
