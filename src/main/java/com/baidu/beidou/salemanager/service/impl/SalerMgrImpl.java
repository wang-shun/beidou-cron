/**
 * 2010-3-12 下午08:02:47
 */
package com.baidu.beidou.salemanager.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.account.bo.UserBalance;
import com.baidu.beidou.salemanager.service.SalerMgr;
import com.baidu.beidou.salemanager.vo.SalerCustInfo;
import com.baidu.beidou.user.constant.UserConstant;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.util.LogUtils;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SalerMgrImpl implements SalerMgr {
	private static final Log LOG = LogFactory.getLog(SalerMgrImpl.class);
	
	private UserDao userDao = null;
	private UserInfoMgr userInfoMgr = null;

	private static final int[] excludeUstate = new int[] { UserConstant.USER_STATE_DELETED };
	private static final int[] excludeShifenState = new int[] { UserConstant.SHIFEN_STATE_CLOSE };

	private UserBalance parseUserBalance(final String line) {
		if (line == null) {
			return null;
		}
		String[] fields = line.split("\t");
		if (fields.length != 3) {
			LOG.error("用户余额文件中记录[" + line + "]解析失败");
			return null;
		}
		UserBalance result = new UserBalance();
		try {
			result.setUserid(Integer.parseInt(fields[0]));
		} catch (NumberFormatException e) {
			LOG.error("用户余额文件中记录[" + line + "]解析失败", e);
			return null;
		}
		try {
			result.setBalance(Double.parseDouble(fields[1]));
		} catch (NumberFormatException e) {
			LOG.error("用户余额文件中记录[" + line + "]解析失败", e);
			return null;
		}

		try {
			result.setInvest(Double.parseDouble(fields[2]));
		} catch (NumberFormatException e) {
			LOG.error("用户余额文件中记录[" + line + "]解析失败", e);
			return null;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.beidou.salemanager.service.SalerMgr#fillBalanceInfo(java.lang
	 * .String, java.util.Map)
	 */
	public Map<Integer, SalerCustInfo> fillBalanceInfo(String balanceFile,
			Map<Integer, SalerCustInfo> custInfoMap) throws IOException {
		if (balanceFile == null || custInfoMap == null) {
			return null;
		}
		BufferedReader inputReader = new BufferedReader(new FileReader(
				balanceFile));
		try {
			UserBalance userBalance = null;
			for (String line = inputReader.readLine(); line != null; line = inputReader
					.readLine()) {
				userBalance = parseUserBalance(line);
				if (userBalance == null) {
					continue;
				}
				SalerCustInfo custInfo = custInfoMap.get(userBalance.getUserid());
				if(custInfo == null){
					continue;
				}
				custInfo.setBalance((int)((userBalance.getBalance())*100+0.5));
				custInfo.setTotal((long)((userBalance.getInvest())*100+0.5));
				if(LogUtils.TEST_LOG.isDebugEnabled()){
					LogUtils.TEST_LOG.debug("userid="+userBalance.getUserid()+"\t"+ custInfo.toString());
				}
			}
		} catch (IOException e) {
			LOG.fatal(e.getMessage(), e);
			throw e;
		} finally {
			inputReader.close();
		}
		return custInfoMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.salemanager.service.SalerMgr#findAllCustBusiInfo()
	 */
	public Map<Integer, SalerCustInfo> findAllCustBusiInfo() {
		return userDao.findAllCustInfo(excludeUstate, excludeShifenState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.beidou.salemanager.service.SalerMgr#outputSalerInfoFile(java
	 * .lang.String, java.util.Map)
	 */
	public void outputSalerInfoFile(String output,
			Map<Integer, SalerCustInfo> custInfoMap) throws IOException {
		if(output == null || custInfoMap == null){
			return ;
		}
		char split = '\t';
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		int[] salerArray = userInfoMgr.getAllSalerFromUc();
		if(salerArray == null || salerArray.length == 0){
			writer.close();
			return;
		}
		SalerCustInfo custInfo = null;
		int custcnt = 0;
		int plancnt = 0;
		long budget = 0; //单位为元
		long invest = 0; //单位为分
		long balance = 0; //单位为分
		try {
			for(int saler : salerArray){
				int[] custIds = userInfoMgr.getOutUcidsManagedFromUc(saler);
				if(custIds == null || custIds.length == 0){
					continue;
				}
				writer.append(String.valueOf(saler)).append(split);
				custcnt = 0;
				plancnt = 0;
				budget = 0; //单位为元
				invest = 0; //单位为分
				balance = 0; //单位为分
				for(int uid : custIds){
					custInfo = custInfoMap.get(uid);
					if(custInfo == null){
						continue;
					}
					custcnt++;
					plancnt += custInfo.getNormalPlanNumber();
					budget += custInfo.getNormalPlanBudge();
					invest += custInfo.getTotal();
					balance += custInfo.getBalance();
				}
				writer.append(String.valueOf(custcnt)).append(split);
				writer.append(String.valueOf(plancnt)).append(split);
				writer.append(String.valueOf(budget)).append(split);
				writer.append(String.valueOf(invest)).append(split);
				writer.append(String.valueOf(balance)).append('\n');
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}finally{
			writer.close();
		}

	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}


}
