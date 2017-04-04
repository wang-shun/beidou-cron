package com.baidu.beidou.account.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import com.baidu.beidou.account.mfcdriver.MfcAccountDriverProxy;
import com.baidu.beidou.account.mfcdriver.MfcFinanceDriverProxy;
import com.baidu.beidou.account.mfcdriver.MfcOperationDriverProxy;
import com.baidu.beidou.account.mfcdriver.MfcStatDriverProxy;
import com.baidu.beidou.account.mfcdriver.bean.CodeConstant;
import com.baidu.beidou.account.mfcdriver.bean.response.AutoTransferResult;
import com.baidu.beidou.account.mfcdriver.bean.response.BeidouVipCacheDataBean;
import com.baidu.beidou.account.mfcdriver.bean.response.BeidouVipCacheResult;
import com.baidu.beidou.account.mfcdriver.bean.response.FundToBeAddBean;
import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountCacheBean;
import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountCacheResult;
import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountDataBean;
import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountResult;
import com.baidu.beidou.account.mfcdriver.bean.response.UserProductBalanceResult;
import com.baidu.beidou.account.service.MfcService;
import com.baidu.beidou.util.TokenUtil;
import com.baidu.beidou.util.service.impl.BaseAuthenticateRpcServiceImpl;

public class MfcServiceImpl extends BaseAuthenticateRpcServiceImpl implements MfcService {
	

	private static final int TOKEN_LENGTH = 64;
	private static final int OP_SUCCESS = 0;
	private static final int ACCOUNT_SELECT_BALANCE = 0;
	private static final int ACCOUNT_SELECT_INVEST = 1;
	private int pageSize = 500;
	
	private MfcStatDriverProxy mfcStatDriverProxy;
	private MfcAccountDriverProxy mfcAccountDriverProxy;
	private MfcOperationDriverProxy mfcOperationDriverProxy;
	private MfcFinanceDriverProxy mfcFinanceDriverProxy;
	
	public MfcServiceImpl(int pageSize, String userName, String password) {
		super(userName, password);
		this.pageSize = pageSize;
	}
	

	public double[][] getUserProductBalance(final List<Integer> userIds, final List<Integer> products, final int opuid) {		
		return this.getUserProductData(ACCOUNT_SELECT_BALANCE, userIds, products, opuid);
	}
	
	public double[][] getUserProductInvest(final List<Integer> userIds, final List<Integer> products, final int opuid) {		
		return this.getUserProductData(ACCOUNT_SELECT_INVEST, userIds, products, opuid);
	}

	/**
	 * 将分页逻辑包含进来
	 * @param type
	 * @param userIds
	 * @param products
	 * @param opuid
	 * @return下午06:36:49
	 */
	private double[][] getUserProductData(final int type, final List<Integer> userIds, final List<Integer> products, final int opuid) {
		
		if(CollectionUtils.isEmpty(userIds)){
			return null;
		}
		
		if(CollectionUtils.isEmpty(products)){
			return null;
		}
		
		List<double[]> result = new ArrayList<double[]>(userIds.size());
		
		double[][] doubleResult = new double[userIds.size()][];
		
		if(userIds.size() <= this.pageSize){//用户数据量小于mfc的限制，直接返回
			result = this.getUserProductDataPrivate(type, userIds, products, opuid);
			
			return result.toArray(doubleResult);
		}			
		
		List<Integer> subUserIds = null;		

		for(int fromIndex = 0, toIndex = this.pageSize; toIndex <= userIds.size() && fromIndex != toIndex; ){
			subUserIds = userIds.subList(fromIndex, (toIndex > userIds.size() ? userIds.size() : toIndex));
			List<double[]> subResult = this.getUserProductDataPrivate(type, subUserIds, products, opuid);
			result.addAll(subResult);
			
			fromIndex = toIndex;
            int left = userIds.size() - toIndex;
            toIndex = toIndex + (left > pageSize ? pageSize :left );
		}
		
		return result.toArray(doubleResult);
	}
	/**
	 * 处理查询请求
	 * @param type
	 * @param userIds
	 * @param products
	 * @param opuid
	 * @return下午06:37:10
	 */
	private List<double[]> getUserProductDataPrivate(final int type, final List<Integer> userIds, final List<Integer> products, final int opuid) {
		
		List<double[]> dataResult = new ArrayList<double[]>(userIds.size());		
				
		UserProductBalanceResult result = null;
		if(type == ACCOUNT_SELECT_BALANCE){
			result = mfcAccountDriverProxy.getUserProductBalance(userIds, products, opuid, getHeaders());
		}else{
			result = mfcAccountDriverProxy.getUserProductInvest(userIds, products, opuid, getHeaders());
		} 
					
		
		if(result == null){
			LOG.error("getUserProductBalanceprivate result == null");
			this.addNull(dataResult);
			return dataResult;
		}
		
		if(result.getStatus() != CodeConstant.STATUS_OK){
			LOG.error("result.getStatus() == " + result.getStatus());
			
			if(result.getData() != null){
				LOG.error("result.getData().getDesc() == " + result.getData().getDesc());
			} 
			
			this.addNull(dataResult);
			return dataResult;
		}
		
		if(result.getData() == null){
			LOG.error("result.getData() == null");
			this.addNull(dataResult);
			return dataResult;
		} 
		
		
		List<double[]> mfcDataResult = result.getData().getResult();
		if(CollectionUtils.isEmpty(mfcDataResult)){
			LOG.error("result.getData().getResult() isEmpty");
			this.addNull(dataResult);
			return dataResult;
		}
		
		int code = result.getData().getCode();		
		if(code == CodeConstant.CODE_ALL_RIGHT){
			return mfcDataResult;
		}
		
		List<Integer> errNoList = result.getData().getErrno();
		
		for(int i = 0; i < mfcDataResult.size(); i++){
			if(errNoList.get(i) != OP_SUCCESS){
				mfcDataResult.set(i, null);
			}
		}		
		
		return mfcDataResult;
	}	
	/**
	 * 组装空对象
	 * @param dataResult下午06:37:25
	 */
	private void addNull(List<double[]> dataResult){
		if(dataResult == null){
			return ;
		}
		for(int i = 0; i < dataResult.size(); i ++){
			dataResult.add(null);
		}
	}

	/**
	 * 转账请求
	 */
	public int autoProductTransfer(final Integer userId, final int appIdOut, final int appIdIn, final double amount){
				
		AutoTransferResult result = mfcOperationDriverProxy.autoProductTransfer(TokenUtil.getTokenId(TOKEN_LENGTH), userId,
				appIdOut, appIdIn, amount, getHeaders());

		if(result == null){
			LOG.error("AutoTransferResult result == null");
			return CodeConstant.STATUS_NO_SERVICE;
		}
		
		if(result.getStatus() != CodeConstant.STATUS_OK){
			
			LOG.error("result.getStatus() == " + result.getStatus());
			
			if(result.getData() != null){
				LOG.error("result.getData().getDesc() == " + result.getData().getDesc());
			} 
			
			return CodeConstant.STATUS_NO_SERVICE;
		}
		
		if(result.getData() == null){
			LOG.error("result.getData() == null");
			return CodeConstant.STATUS_NO_SERVICE;
		} 
		
		List<Integer> dataResult = result.getData().getResult();
		if(CollectionUtils.isEmpty(dataResult)){
			LOG.error("result.getData().getResult() isEmpty");
			return CodeConstant.STATUS_NO_SERVICE;
		}
		//记录日志
		Integer opResult = dataResult.get(0);
		if(opResult != null && opResult == OP_SUCCESS){
			return CodeConstant.STATUS_OK;
		}
		
		List<Integer> erroResult = result.getData().getErrno();
		if(CollectionUtils.isNotEmpty(erroResult)){
			LOG.info("AutoTransferResult erroResult == " + erroResult.get(0));
			
			if(erroResult.get(0).equals(CodeConstant.ERR_NOENOUGH_FUND)){
				return CodeConstant.ERR_NOENOUGH_FUND;
			}
			if(erroResult.get(0).equals(CodeConstant.ERR_ONESTATION_FUND)){
				return CodeConstant.ERR_ONESTATION_FUND;
			}
		}
		
		return CodeConstant.STATUS_NO_SERVICE;
	}
	
	public Map<Integer, Double> statBeidouVipCache() {
		BeidouVipCacheResult result = mfcStatDriverProxy.statBeidouVipCache(getHeaders());
		
		Map<Integer, Double> dataResult = new HashMap<Integer, Double>();
		
		if(result == null){
			LOG.error("statBeidouVipCache result == null");
			return dataResult;
		}
		
		if(result.getStatus() != CodeConstant.STATUS_OK){
			LOG.error("statBeidouVipCache result.getStatus() == " + result.getStatus());
			return dataResult;
		}
		
		if(result.getData() == null){
			LOG.error("statBeidouVipCache result.getData() == null");
			return dataResult;
		} 
		
		
		List<BeidouVipCacheDataBean> mfcDataResult = result.getData();
		if(CollectionUtils.isEmpty(mfcDataResult)){
			LOG.error("statBeidouVipCache result.getData().getResult() isEmpty");
			return dataResult;
		} else {
			
			for (BeidouVipCacheDataBean userCachePair : mfcDataResult) {
				Integer userId = userCachePair.getUid();
				Double cash = userCachePair.getCnt();
				dataResult.put(userId, cash);
			}
			
		}		
		
		return dataResult;
	}	

	
	public double[][] getUserAccount(final Integer userId, final List<Integer> products, final boolean forceMaster){
		
				
		String forceMasterString = "";
		if (forceMaster) {
			forceMasterString="iamsure"; 
		}
				
		List<double[]> listResult = new ArrayList<double[]>(products.size());
		double[][] arrayResult = new double[products.size()][];	
		UserAccountResult result =  mfcAccountDriverProxy.getUserAccount(userId, products, forceMasterString, getHeaders());
		
		if(result == null){
			LOG.error("getUserAccount result == null");
			this.addNull(listResult);
			return listResult.toArray(arrayResult);
		}
		
		if(result.getStatus() != CodeConstant.STATUS_OK){
			LOG.error("getUserAccount result.getStatus() == " + result.getStatus());
			this.addNull(listResult);
			return listResult.toArray(arrayResult);
		}
		
		if(result.getData() == null){
			LOG.error("getUserAccount result.getData() == null");
			this.addNull(listResult);
			return listResult.toArray(arrayResult);
		} 
		
		
		List<UserAccountDataBean> mfcDataResult = result.getData();
		if(CollectionUtils.isEmpty(mfcDataResult)){
			LOG.error("getUserAccount result.getData() isEmpty");
			this.addNull(listResult);
			return listResult.toArray(arrayResult);
		}
		
		if (products.size() != mfcDataResult.size()) {
			LOG.error("getUserAccount 结果长度与参数'products'的长度不一致");
			this.addNull(listResult);
			return listResult.toArray(arrayResult);
		}
		
		for (int i = 0; i < products.size(); i++) {
			UserAccountDataBean userAccountPerProduct = mfcDataResult.get(i);
			if (userAccountPerProduct == null || userAccountPerProduct.getProductid() != products.get(i)) {
				LOG.error("getUserAccount 结果与参数的productId对应错误");
				this.addNull(listResult);
				return listResult.toArray(arrayResult);
			} else {
				listResult.add(new double[]{
						userAccountPerProduct.getBalance(),
						userAccountPerProduct.getConsume(),
						userAccountPerProduct.getInvest()}
				);
			}
		}		
		
		return listResult.toArray(arrayResult);
	}
	
	private Map<Integer,Double> getHeaverUserAccountCachePrivate(List<Integer> userIds, int opuid){
		
		Map<Integer,Double> userAccountCacheMap = new HashMap<Integer, Double>();
		
		List<Integer> accountIds = new ArrayList<Integer>();
		accountIds.add(MfcFinanceDriverProxy.WANGMENG_ACCOUNT); 
		UserAccountCacheResult result = mfcFinanceDriverProxy.getUserAccountCache(
				userIds, accountIds, opuid, getHeaders());
		if(result == null){
			LOG.error("getUserAccountCache result == null");
			return userAccountCacheMap;
		}
		
		if(result.getStatus() != CodeConstant.STATUS_OK){
			LOG.error("getUserAccountCache result.getStatus() == " + result.getStatus());
			return userAccountCacheMap;
		}
		
		if(result.getData() == null){
			LOG.error("getUserAccountCache result.getData() == null");
			return userAccountCacheMap;
		}
		
		UserAccountCacheBean userAccountCacheBean = result.getData();
		if(userAccountCacheBean.getCode() != CodeConstant.STATUS_OK){
			LOG.error("getUserAccountCache result.getData.getCode() == " + result.getStatus());
			return userAccountCacheMap;
		}
		if(ArrayUtils.isEmpty(userAccountCacheBean.getResult())){
			LOG.error("getUserAccountCache result.getData.getResult() == null");
			return userAccountCacheMap;
		}
		
		int userSize = userIds.size();
		int cacheBeanSize = userAccountCacheBean.getResult().length;
		List<Integer> errnoList = userAccountCacheBean.getErrno();
		if(userSize != cacheBeanSize || userSize != errnoList.size()){
			LOG.error("getUserAccountCache userIds's size do not equal cacheBeanSize'size or userIds's size do not equal errnoList's size");
			return userAccountCacheMap;
		}
		
		for(int i=0;i<userSize;i++){
			if(errnoList.get(i) != OP_SUCCESS){
				continue;
			}
			
			double sumCacheFund = 0;
			
			FundToBeAddBean[][] accountCacheArray = userAccountCacheBean.getResult()[i];
			if(ArrayUtils.isEmpty(accountCacheArray)){
				continue;
			}
			
			for(FundToBeAddBean[] fundPoolCacheArray : accountCacheArray){
				if(ArrayUtils.isEmpty(fundPoolCacheArray)){
					continue;
				}
				
				for(FundToBeAddBean fundToBeAdd : fundPoolCacheArray){
					if(fundToBeAdd == null || fundToBeAdd.getFund() <= 0){
						continue;
					}
					
					sumCacheFund += fundToBeAdd.getFund();
				}
			}
			
			if(sumCacheFund > 0){
				userAccountCacheMap.put(userIds.get(i), sumCacheFund);
			}
		}
		
		return userAccountCacheMap;
	}
	
	public Map<Integer,Double> getHeaverUserAccountCache(List<Integer> userIds, int opuid){
		
		Map<Integer,Double> userAccountCacheMap = new HashMap<Integer, Double>();
		if(CollectionUtils.isEmpty(userIds)){
			LOG.error("getUserAccountCache userIds's size is null");
			return userAccountCacheMap;
		}
		
		if(userIds.size() <= this.pageSize){//用户数据量小于mfc的限制，直接返回
			return getHeaverUserAccountCachePrivate(userIds, opuid);
		}			
		
		List<Integer> subUserIds = null;		

		for(int fromIndex = 0, toIndex = this.pageSize; toIndex <= userIds.size() && fromIndex != toIndex; ){
			subUserIds = userIds.subList(fromIndex, (toIndex > userIds.size() ? userIds.size() : toIndex));
			Map<Integer,Double> subCacheMap = getHeaverUserAccountCachePrivate(subUserIds, opuid);
			userAccountCacheMap.putAll(subCacheMap);
			
			fromIndex = toIndex;
            int left = userIds.size() - toIndex;
            toIndex = toIndex + (left > pageSize ? pageSize :left );
		}
		
		return userAccountCacheMap;		
	}
	
	public int getPageSize() {
		return pageSize;
	}


	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}


	public MfcStatDriverProxy getMfcStatDriver() {
		return mfcStatDriverProxy;
	}


	public void setMfcStatDriver(MfcStatDriverProxy mfcStatDriver) {
		this.mfcStatDriverProxy = mfcStatDriver;
	}


	public MfcAccountDriverProxy getMfcAccountDriver() {
		return mfcAccountDriverProxy;
	}


	public void setMfcAccountDriver(MfcAccountDriverProxy mfcAccountDriver) {
		this.mfcAccountDriverProxy = mfcAccountDriver;
	}


	public MfcOperationDriverProxy getMfcOperationDriver() {
		return mfcOperationDriverProxy;
	}


	public void setMfcOperationDriver(MfcOperationDriverProxy mfcOperationDriver) {
		this.mfcOperationDriverProxy = mfcOperationDriver;
	}


	public MfcFinanceDriverProxy getMfcFinanceDriver() {
		return mfcFinanceDriverProxy;
	}


	public void setMfcFinanceDriver(MfcFinanceDriverProxy mfcFinanceDriverProxy) {
		this.mfcFinanceDriverProxy = mfcFinanceDriverProxy;
	}	
	
	
}
