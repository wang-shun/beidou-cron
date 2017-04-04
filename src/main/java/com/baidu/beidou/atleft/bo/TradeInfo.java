package com.baidu.beidou.atleft.bo;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.Array;

public class TradeInfo {
	private Integer userId;
	private Integer tradeId;//二级行业ID
	private Integer firstTradeId;//一级行业ID，后来添加的
	private String firstTradeName = "";
	private String secondTradeName = "";
	private String kaTrade1 = "";
	private String kaTrade2 = "";
	
	
	
	public TradeInfo() {

	}
//	public TradeInfo(Integer userId, Integer tradeId, String firstTradeName, String secondTradeName) {
//		this.userId = userId;
//		this.tradeId = tradeId;
//		this.firstTradeName = firstTradeName;
//		this.secondTradeName = secondTradeName;
//	}
	public TradeInfo(Integer userId, Integer tradeId, Integer firstTradeId,
			String firstTradeName, String secondTradeName, String kaTrade1,
			String kaTrade2) {
			this.userId = userId;
		this.tradeId = tradeId;
		this.firstTradeId = firstTradeId;
		this.firstTradeName = firstTradeName;
		this.secondTradeName = secondTradeName;
		this.kaTrade1 = kaTrade1;
		this.kaTrade2 = kaTrade2;
	}
	
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getTradeId() {
		return tradeId;
	}
	public void setTradeId(Integer tradeId) {
		this.tradeId = tradeId;
	}
	public String getFirstTradeName() {
		return firstTradeName;
	}
	public void setFirstTradeName(String firstTradeName) {
		this.firstTradeName = firstTradeName;
	}
	public String getSecondTradeName() {
		return secondTradeName;
	}
	public void setSecondTradeName(String secondTradeName) {
		this.secondTradeName = secondTradeName;
	}
	
	public Integer getFirstTradeId() {
		return firstTradeId;
	}
	public void setFirstTradeId(Integer firstTradeId) {
		this.firstTradeId = firstTradeId;
	}
	public String getKaTrade1() {
		return kaTrade1;
	}
	public void setKaTrade1(String kaTrade1) {
		this.kaTrade1 = kaTrade1;
	}
	public String getKaTrade2() {
		return kaTrade2;
	}
	public void setKaTrade2(String kaTrade2) {
		this.kaTrade2 = kaTrade2;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstTradeId == null) ? 0 : firstTradeId.hashCode());
		result = prime * result
				+ ((firstTradeName == null) ? 0 : firstTradeName.hashCode());
		result = prime * result
				+ ((kaTrade1 == null) ? 0 : kaTrade1.hashCode());
		result = prime * result
				+ ((kaTrade2 == null) ? 0 : kaTrade2.hashCode());
		result = prime * result
				+ ((secondTradeName == null) ? 0 : secondTradeName.hashCode());
		result = prime * result + ((tradeId == null) ? 0 : tradeId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradeInfo other = (TradeInfo) obj;
		if (firstTradeId == null) {
			if (other.firstTradeId != null)
				return false;
		} else if (!firstTradeId.equals(other.firstTradeId))
			return false;
		if (firstTradeName == null) {
			if (other.firstTradeName != null)
				return false;
		} else if (!firstTradeName.equals(other.firstTradeName))
			return false;
		if (kaTrade1 == null) {
			if (other.kaTrade1 != null)
				return false;
		} else if (!kaTrade1.equals(other.kaTrade1))
			return false;
		if (kaTrade2 == null) {
			if (other.kaTrade2 != null)
				return false;
		} else if (!kaTrade2.equals(other.kaTrade2))
			return false;
		if (secondTradeName == null) {
			if (other.secondTradeName != null)
				return false;
		} else if (!secondTradeName.equals(other.secondTradeName))
			return false;
		if (tradeId == null) {
			if (other.tradeId != null)
				return false;
		} else if (!tradeId.equals(other.tradeId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("userId : ").append(userId)
		.append(" | tradeId : ").append(tradeId)
		.append(" | firstTradeName : ").append(firstTradeName)
		.append(" | secondTradeName : ").append(secondTradeName)
		.append(" | firstTradeId : ").append(firstTradeId)
		.append(" | kaTrade1 : ").append(kaTrade1)
		.append(" | kaTrade2 : ").append(kaTrade2);
		
		return sb.toString();
	}
	
	public static void main(String[] args) {
		
		TradeInfo t1 = new TradeInfo(new Integer(1231),345,3,"123","123","456","789");
		TradeInfo t2 = new TradeInfo(new Integer(1231),345,3,"123","123","456","78912");
		
		final List<TradeInfo> a = new ArrayList<TradeInfo>();
		final List<TradeInfo> b = new ArrayList<TradeInfo>();
		
		int x = 1;
		final int i = x;
		System.out.println(i);
		x = 2;
		System.out.println(i);
	
//		init(a,b);
		
		System.out.println(t1.equals(t2));
		System.out.println(a.size());
		System.out.println(b.size());
		
	}
//	private static void init(List<TradeInfo> a, List<TradeInfo> b) {
//		TradeInfo t1 = new TradeInfo(new Integer(1231),345,"123","123");
//		TradeInfo t2 = new TradeInfo(new Integer(1231),345,"123","123");
//		a.add(t1);
//		b.add(t2);
//	}
//	private static TradeInfo test1(int i) {
//		return new TradeInfo(i,i,String.valueOf(i),String.valueOf(i));
//	}
	
}
