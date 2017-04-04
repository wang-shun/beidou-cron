package com.baidu.beidou.atleft.service;

import java.util.List;

import com.baidu.beidou.atleft.bo.TradeInfo;

public interface AtLeftTradeService {
	List<TradeInfo> getTradeList(List<TradeInfo> tradeInfos);
	
	int insertTrade(List<TradeInfo> tradeInfos);
	
	int updateTrade(List<TradeInfo>  tradeInfos);
	
	int deleteTrade(List<TradeInfo> tradeInfos);
	
}
