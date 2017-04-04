package com.baidu.beidou.cprogroup.constant;

import java.util.List;

public class WhiteType {
	
	public static final int BAIDU_SITES = 1;
	public static final int BAIDU_TRADES = 2;
	public static final int USE_BAIDU_USERS = 3;
	public static final int BAIDU_FILM = 4;
	
	// added by hanxu @2.0.19 百度团购进入网盟推广
	public static List<Integer> BAIDU_TRADE_COMMON_LIST;

	public void setBAIDU_TRADE_COMMON_LIST(List<Integer> bAIDUTRADECOMMONLIST) {
		BAIDU_TRADE_COMMON_LIST = bAIDUTRADECOMMONLIST;
	}
	
}
