package com.baidu.beidou.atleft.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.derby.client.am.Types;

import com.baidu.beidou.atleft.bo.TradeInfo;
import com.baidu.beidou.atleft.dao.AtLeftTradeDao;
import com.baidu.beidou.atleft.service.AtLeftTradeService;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class AtLeftTradeServiceImpl implements AtLeftTradeService {
	private AtLeftTradeDao atLeftTradeDao;
	
	private final static GenericRowMapping<TradeInfo> tradeMapping = new GenericRowMapping<TradeInfo>() {
		@Override
		public TradeInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			TradeInfo info = new TradeInfo();
			info.setUserId(rs.getInt(1));
			info.setTradeId(rs.getInt(2));
			info.setFirstTradeName(rs.getString(3));
			info.setSecondTradeName(rs.getString(4));
			info.setFirstTradeId(rs.getInt(5));
			info.setKaTrade1(rs.getString(6));
			info.setKaTrade2(rs.getString(7));
			return info;
		}
	};

	@Override
	public List<TradeInfo> getTradeList(List<TradeInfo> tradeInfos) {
		if (CollectionUtils.isEmpty(tradeInfos)) {
			return new ArrayList<TradeInfo>(0);
		}
		
		String sql = "select userid,tradeid,tradename1,tradename2,first_tradeid,ka_trade1,ka_trade2 from beidou.usertrade where userid in("
				+ generateWhere(tradeInfos) + ")";
		
		return atLeftTradeDao.getTradeInfo(tradeMapping, sql, null);
	}

	private String generateWhere(List<TradeInfo> tradeInfos) {
		StringBuffer where = new StringBuffer("");
		for (TradeInfo tradeInfo :tradeInfos) {
			where.append(tradeInfo.getUserId()).append(",");
		}
		
		where.deleteCharAt(where.length()-1);
		return where.toString();
	}

	@Override
	public int insertTrade(List<TradeInfo> tradeInfos) {
		int result = 0;
		if (CollectionUtils.isEmpty(tradeInfos)) {
			return 0;
		}
		for (TradeInfo tradeInfo : tradeInfos) {
			Object[] params = new Object[]{
					tradeInfo.getUserId(),
					tradeInfo.getTradeId(),
					"".equals(tradeInfo.getFirstTradeName()) ? null : tradeInfo.getFirstTradeName(),
					"".equals(tradeInfo.getSecondTradeName()) ? null : tradeInfo.getSecondTradeName(),
					tradeInfo.getFirstTradeId(),
					"".equals(tradeInfo.getKaTrade1()) ? null : tradeInfo.getKaTrade1(),
					"".equals(tradeInfo.getKaTrade2()) ? null : tradeInfo.getKaTrade2()
			};
			
			String sql = "insert into beidou.usertrade(userid,tradeid,tradename1,tradename2,first_tradeid,ka_trade1,ka_trade2,modtime)" +
					" values(?,?,?,?,?,?,?,now())";
			
			result += atLeftTradeDao.insertTrade(sql, params, new int[]{Types.INTEGER,Types.INTEGER,Types.VARCHAR,Types.VARCHAR,Types.INTEGER,Types.VARCHAR,Types.VARCHAR});
		}
		
		return result;
	}

	@Override
	public int updateTrade(List<TradeInfo> tradeInfos) {
		int result = 0;
		if (CollectionUtils.isEmpty(tradeInfos)) {
			return 0;
		}
		
		for (TradeInfo tradeInfo : tradeInfos) {
			Object[] params = new Object[]{
					tradeInfo.getTradeId(),
					"".equals(tradeInfo.getFirstTradeName()) ? null : tradeInfo.getFirstTradeName(),
					"".equals(tradeInfo.getSecondTradeName()) ? null : tradeInfo.getSecondTradeName(),
					tradeInfo.getFirstTradeId(),
					"".equals(tradeInfo.getKaTrade1()) ? null : tradeInfo.getKaTrade1(),
					"".equals(tradeInfo.getKaTrade2()) ? null : tradeInfo.getKaTrade2(),
					tradeInfo.getUserId()
			};
			String sql = "update beidou.usertrade set tradeid=?,tradename1=?,tradename2=?,first_tradeid=?,ka_trade1=?,ka_trade2=?,modtime=now()" +
					" where userid=?";
			result += atLeftTradeDao.updateTrade(sql, params, new int[]{Types.INTEGER,Types.VARCHAR,Types.VARCHAR,
					Types.INTEGER,Types.VARCHAR,Types.VARCHAR,Types.INTEGER});
		}
		
		return result;
	}

	@Override
	public int deleteTrade(List<TradeInfo> tradeInfos) {
		if (CollectionUtils.isEmpty(tradeInfos)) {
			return 0;
		}
		
		Object[] params = new Object[tradeInfos.size()] ;
		int[] types = new int[tradeInfos.size()];
		
		StringBuilder sql = new StringBuilder("delete from beidou.usertrade where userid in(");
		for (int i = 0;i < tradeInfos.size(); i++) {
			//params = new Object[tradeInfos.size()]; 
			//types = new int[tradeInfos.size()];
			sql.append("?").append(",");
			params[i] = tradeInfos.get(i).getUserId();
			types[i] = Types.INTEGER;
		}
		
		sql.deleteCharAt(sql.length() - 1).append(")");
		
		return atLeftTradeDao.deleteTrade(sql.toString(), params,types);
	}

	public AtLeftTradeDao getAtLeftTradeDao() {
		return atLeftTradeDao;
	}

	public void setAtLeftTradeDao(AtLeftTradeDao atLeftTradeDao) {
		this.atLeftTradeDao = atLeftTradeDao;
	}
	
	public static void main(String[] arg) {
		List<Integer> test = new ArrayList<Integer>();
		Object[] params = new Object[10];
		for (int i=0;i<10;i++) {
			test.add(i);
		}
		params = test.toArray();
		
		AtLeftTradeServiceImpl impl = new AtLeftTradeServiceImpl();
		List<TradeInfo> toDeleteList = new ArrayList<TradeInfo>();
		TradeInfo info3 = new TradeInfo(123, 123,1 ,"123", "123","123", "123");
		TradeInfo info6 = new TradeInfo(234, 000,2, "000", "000","123", "123");
		TradeInfo info4 = new TradeInfo(111, 456,1, "456", "456","123", "123");
		TradeInfo info11 = new TradeInfo(222, 456,2, "456", "456","123", "123");
		TradeInfo info22 = new TradeInfo(333, 456,3, "456", "456","123", "123");
		toDeleteList.add(info3);
		toDeleteList.add(info4);
		toDeleteList.add(info6);
		toDeleteList.add(info11);
		toDeleteList.add(info22);
		impl.deleteTrade(toDeleteList);
	}
}
