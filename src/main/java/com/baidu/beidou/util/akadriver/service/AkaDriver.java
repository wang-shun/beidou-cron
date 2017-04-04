package com.baidu.beidou.util.akadriver.service;

import java.util.List;

import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.bo.AkaCheckInfo;
import com.baidu.beidou.util.akadriver.bo.AkaKwCheckInfo;
import com.baidu.beidou.util.akadriver.bo.AkaUnitCheckInfo;
import com.baidu.beidou.util.akadriver.exception.AkaException;

public interface AkaDriver {

	/**
	 * 审核文字物料
	 * @see 获取aka处理之后的结果
	 * @param akaCheckInfoList
	 * @return
	 * @date 2008-5-29
	 */
	public List<AkaBeidouResult> getAkaUnitResultInfoListForUnit(
			final List<AkaUnitCheckInfo> akaCheckInfoList) throws AkaException;
	
	
	/**
	 * getAkaPatrolUnitResultInfoListForUnit: 
	 * 北斗aka轮询审核文字物料
	 * @version AkaDriver
	 * @author genglei01
	 * @date Aug 1, 2011
	 */
	public List<AkaBeidouResult> getAkaPatrolUnitResultInfoListForUnit(
			final List<AkaUnitCheckInfo> akaCheckInfoList) throws AkaException;
	
	/**
	 * 审核图片物料，分站点和价格也严重targetUrl
	 * @see 获取aka处理之后的结果
	 * @param akaCheckInfoList
	 * @return
	 * @date 2008-5-29
	 */
	public List<AkaBeidouResult> getAkaPicResultInfoListForUnit(
			final List<AkaCheckInfo> akaCheckInfoList) throws AkaException ;
	
	/**
	 * 审核关键词
	 * @see 获取aka处理之后的结果
	 * @param akaCheckInfoList
	 * @return
	 * @throws AkaException 
	 */
	public List<AkaBeidouResult> getAkaResultInfoListForKw(final
			List<AkaKwCheckInfo> akaCheckInfoList) throws AkaException;

}
