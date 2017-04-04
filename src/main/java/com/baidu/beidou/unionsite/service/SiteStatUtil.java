/**
 * 2009-4-27 上午12:11:35
 */
package com.baidu.beidou.unionsite.service;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import com.baidu.beidou.unionsite.bo.IPCookieBo;
import com.baidu.beidou.unionsite.bo.SiteStatBo;
import com.baidu.beidou.unionsite.bo.SiteStatExtBo;
import com.baidu.beidou.unionsite.constant.SiteConstant;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteStatUtil {

	/**
	 * 合并统计信息
	 * 
	 * @author zengyunfeng
	 * @param total
	 * @param addStat
	 * @return
	 */
	public static SiteStatBo mergeSiteStat(final SiteStatBo total,
			final SiteStatBo addStat) {
		
		SiteStatBo result = total;
		
		if (total == null) {
			
			result = new SiteStatBo();
			result.setDomain(addStat.getDomain());
			result.setCntn(addStat.getCntn());

			if (addStat.getSizeFlow() != null) {
				result.setSizeFlow(new Hashtable<Integer, Integer>(addStat
						.getSizeFlow()));
			}
			
			result.setWuliao(addStat.getWuliao());
			result.setDispType(addStat.getDispType());
			
		} else {
						
			//使用二进制编码 mod by zhuqian @beidou1.2.24
			result.setWuliao( SiteConstant.bitOp_OR(result.getWuliao(), addStat.getWuliao()) );
			
			//如果支持图片类型，则需要合并尺寸和尺寸流量相关数据
			if(SiteConstant.bitOp_supports(result.getWuliao(), SiteConstant.WL_PIC_FLAG)){
				
				if (addStat.getSizeFlow() != null) {
					Map<Integer, Integer> size = null;
					if (result.getSizeFlow() == null) {
						size = new Hashtable<Integer, Integer>(addStat
								.getSizeFlow());
					} else {
						size = result.getSizeFlow();
						for (Entry<Integer, Integer> entry : addStat
								.getSizeFlow().entrySet()) {
							Integer value = size.get(entry.getKey());
							if (value == null) {
								value = Integer.valueOf(0);
							}
							size.put(entry.getKey(), entry.getValue() + value);
						}
					}
					result.setSizeFlow(size);
				}
			}
			
			//合并展现类型
			result.setDispType( SiteConstant.bitOp_OR(result.getDispType(), addStat.getDispType()) );
			
		}
		
		//合并流量
		result.setAds(result.getAds() + addStat.getAds());
		result.setClicks(result.getClicks() + addStat.getClicks());
		result.setCost(result.getCost() + addStat.getCost());
		result.setRetrieve(result.getRetrieve() + addStat.getRetrieve());
		
		result.setFixedAds(result.getFixedAds() + addStat.getFixedAds());
		result.setFixedClicks(result.getFixedClicks() + addStat.getFixedClicks());
		result.setFixedCost(result.getFixedCost() + addStat.getFixedCost());
		result.setFixedRetrieve(result.getFixedRetrieve() + addStat.getFixedRetrieve());
		
		if(addStat.getFixedRetrieve() > 0){
			result.setFixedCount(result.getFixedCount() + 1);
		}
		
		result.setFlowAds(result.getFlowAds() + addStat.getFlowAds());
		result.setFlowClicks(result.getFlowClicks() + addStat.getFlowClicks());
		result.setFlowCost(result.getFlowCost() + addStat.getFlowCost());
		result.setFlowRetrieve(result.getFlowRetrieve() + addStat.getFlowRetrieve());
		
		if(addStat.getFlowRetrieve() > 0){
			result.setFlowCount(result.getFlowCount() + 1);
		}
		
		result.setFilmAds(result.getFilmAds() + addStat.getFilmAds());
		result.setFilmClicks(result.getFilmClicks() + addStat.getFilmClicks());
		result.setFilmCost(result.getFilmCost() + addStat.getFilmCost());
		result.setFilmRetrieve(result.getFilmRetrieve() + addStat.getFilmRetrieve());
		
		if(addStat.getFilmRetrieve() > 0){
			result.setFilmCount(result.getFilmCount() + 1);
		}
		
		return result;
	
	}

	public static IPCookieBo mergeSiteIpCookieStat(final IPCookieBo total,
			final IPCookieBo addStat) {
		IPCookieBo result = total;
		if (total == null) {
			result = new IPCookieBo();
			result.setDomain(addStat.getDomain());
			result.setUnique_cookie(addStat.getUnique_cookie());
			result.setUnique_ip(addStat.getUnique_ip());
		} else {
			result.setUnique_cookie(result.getUnique_cookie()
					+ addStat.getUnique_cookie());
			result.setUnique_ip(result.getUnique_ip() + addStat.getUnique_ip());
		}
		return result;
	}

	/**
	 * 计算平均值
	 * 
	 * @author zengyunfeng
	 * @param result
	 * @param count
	 */
	public static void averageSiteIpCookieStat(final IPCookieBo result,
			final int count) {
		if (result == null || count ==0) {
			return;
		}
		double dCount = count;
		result.setUnique_cookie((int)Math.ceil(result.getUnique_cookie() / dCount));
		result.setUnique_ip((int)Math.ceil(result.getUnique_ip() / dCount));
	}

	/**
	 * 计算平均值
	 * 
	 * @author zengyunfeng
	 * @param result
	 * @param count
	 */
	public static void averageSiteStat(final SiteStatBo result, final int count) {
		if (result == null || count ==0) {
			return;
		}
		double dCount = count;
		double fixedCount = result.getFixedCount() * 1.0d;
		double flowCount = result.getFlowCount() * 1.0d;
		double filmCount = result.getFilmCount() * 1.0d;
		
		//总流量求平均
		result.setAds((long)Math.ceil(result.getAds() / dCount));
		result.setClicks((int)Math.ceil(result.getClicks() / dCount));
		result.setCost((int)Math.ceil(result.getCost() / dCount));
		result.setRetrieve((long)Math.ceil(result.getRetrieve() / dCount));
		result.setUnique_cookie((int)Math.ceil(result.getUnique_cookie() / dCount));
		result.setUnique_ip((int)Math.ceil(result.getUnique_ip() / dCount));
		
		if (result.getSizeFlow() != null) {
			for (Entry<Integer, Integer> entry : result.getSizeFlow()
					.entrySet()) {
				entry.setValue((int)Math.ceil(entry.getValue() / dCount));
			}
		}
		
		//固定流量求平均
		if(fixedCount > 0){
			result.setFixedAds((long)Math.ceil(result.getFixedAds() / fixedCount));
			result.setFixedClicks((int)Math.ceil(result.getFixedClicks() / fixedCount));
			result.setFixedCost((int)Math.ceil(result.getFixedCost() / fixedCount));
			result.setFixedRetrieve((long)Math.ceil(result.getFixedRetrieve() / fixedCount));
		}
		
		//悬浮流量求平均
		if(flowCount > 0){
			result.setFlowAds((long)Math.ceil(result.getFlowAds() / flowCount));
			result.setFlowClicks((int)Math.ceil(result.getFlowClicks() / flowCount));
			result.setFlowCost((int)Math.ceil(result.getFlowCost() / flowCount));
			result.setFlowRetrieve((long)Math.ceil(result.getFlowRetrieve() / flowCount));
		}
		
		//贴片流量求平均
		if(filmCount > 0){
			result.setFilmAds((long)Math.ceil(result.getFilmAds() / filmCount));
			result.setFilmClicks((int)Math.ceil(result.getFilmClicks() / filmCount));
			result.setFilmCost((int)Math.ceil(result.getFilmCost() / filmCount));
			result.setFilmRetrieve((long)Math.ceil(result.getFilmRetrieve() / filmCount));
		}
		
	}
	

}
