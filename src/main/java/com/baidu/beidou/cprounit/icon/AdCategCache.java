package com.baidu.beidou.cprounit.icon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.baidu.beidou.cprounit.icon.bo.AdTradeInfo;


/**
 * 广告分类的缓存
 * 
 * @author zengyunfeng
 * 
 */
public class AdCategCache  implements Serializable{
	private static final long serialVersionUID = 8705921782011951089L;

	/**
	 * 广告分类信息映射
	 */
	private Map<Integer, String> adTradeInfo = new HashMap<Integer, String>(
			300);
	
	private List<AdTradeInfo> firstTradeList= new ArrayList<AdTradeInfo>(100);
	private List<AdTradeInfo> allAdTrade= new ArrayList<AdTradeInfo>(0);
	
	private Collection adTradeView = null;

	/**
	 * @return the categories_INFO
	 */
	public Map<Integer, String> getAdTradeInfo() {
		return adTradeInfo;
	}

	/**
	 * @param categories_INFO
	 *            the categories_INFO to set
	 */
	public void setAdTradeInfo(List<AdTradeInfo> tradeInfo) {
		
		allAdTrade = tradeInfo;
		for(AdTradeInfo info : allAdTrade){
			adTradeInfo.put(info.getTradeid(), info.getTradename());
			if(info.getParentid()==0){
				firstTradeList.add(info);
			}
		}
		
		adTradeView = setAdTradeView();
	}
	
	/**
	 * 生成分类的json形式
	 * {1:"一级分类1", sub:{13: '二级分类11', 15: '二级分类12'}},
	 * {2:"一级分类2",sub:{23:'二级分类21',24:'二级分类22'}}	
	 * @return
	 */
	private Collection<Map<Object, Object>> setAdTradeView(){
		
		List<Map<Object, Object>> result = new ArrayList<Map<Object, Object>>();
		for(AdTradeInfo trade: firstTradeList){
			Map<Object, Object> row = new HashMap<Object, Object>(2, 1);
			row.put(trade.getTradeid(), trade.getTradename());
			Map<Integer, String> sub = new TreeMap<Integer, String>();
			List<AdTradeInfo> subTrade = getSencondTradeList(trade.getTradeid());
			for(AdTradeInfo secondTrade : subTrade){
				sub.put(secondTrade.getTradeid(), secondTrade.getTradename());
			}
			row.put("sub", sub);
			result.add(row);
		}
		return result;
	}
	
	/**
	 * 判断一个行业ID是否为二级行业ID
	 * @param tradeId
	 * @return
	 */
	public boolean isSencondTrade(int tradeId){
		for(AdTradeInfo info : allAdTrade){
			if(info.getParentid()==0){
				continue;
			}else if(info.getTradeid() == tradeId){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据一级广告分类获取其下属的所以二级广告分类
	 * 2009-6-26
	 * add by zengyunfeng
	 * @version 1.2.0
	 * @param firsetTradeid
	 * @return
	 */
	public List<AdTradeInfo> getSencondTradeList(int firsetTradeid){
		List<AdTradeInfo> result = new ArrayList<AdTradeInfo>(100);
		for(AdTradeInfo info : allAdTrade){
			if(info.getParentid()==firsetTradeid){
				result.add(info);
			}
		}
		return result;
	}

	/**
	 * 活动一级广告行业列表
	 * @return the firstTradeList
	 */
	public List<AdTradeInfo> getFirstTradeList() {
		return firstTradeList;
	}

	/**
	 * 获得所有的广告行业列表
	 * @return the allAdTrade
	 */
	public List<AdTradeInfo> getAllAdTrade() {
		return allAdTrade;
	}

}
