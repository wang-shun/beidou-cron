/**
 * 2010-3-12 下午07:45:15
 */
package com.baidu.beidou.salemanager.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.baidu.beidou.salemanager.vo.SalerCustInfo;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public interface SalerMgr {

	/**
	 * 获取所有用户的beidou业务数据：有效推广计划数，有效推广计划预算
	 * @author zengyunfeng
	 * @return
	 */
	public Map<Integer, SalerCustInfo> findAllCustBusiInfo();
	
	/**
	 * 读取财务文件，设置余额和投资
	 * @author zengyunfeng
	 * @param balanceFile  格式为：userid\tbalance\tinvest,  单位都为元
	 * @param custInfoMap
	 * @return
	 * @throws IOException 
	 */
	public Map<Integer, SalerCustInfo> fillBalanceInfo(final String balanceFile, final Map<Integer, SalerCustInfo> custInfoMap) throws IOException; 
	
	/**
	 * 查询管理员的客户，计算管理员的信息综述，生成到文件output中
	 * @author zengyunfeng
	 * @param output
	 * @param custInfoMap
	 * @throws IOException 
	 */
	public void outputSalerInfoFile(final String output, final Map<Integer, SalerCustInfo> custInfoMap) throws IOException;
}
