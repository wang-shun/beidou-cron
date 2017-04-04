/**
 * beidou-cron-trunk#com.baidu.beidou.unionsite.dao.impl.WM123SiteStatOnCapDaoImpl.java
 * 上午11:21:24 created by kanghongwei
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.baidu.beidou.unionsite.bo.RegionInfo;
import com.baidu.beidou.unionsite.dao.WM123SiteStatOnCapDao;
import com.baidu.beidou.unionsite.vo.TradeSiteElement;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public class WM123SiteStatOnCapDaoImpl extends GenericDaoImpl implements WM123SiteStatOnCapDao {

	private static final Log LOG = LogFactory.getLog(WM123SiteStatOnCapDaoImpl.class);

	public List<Integer> findExceptionalRegInfoId() {

		String sql = "select distinct t.firstregid from beidoucap.reginfo t where firstregid >6 and regtype=1 order by t.firstregid asc";
		List<Integer> result = null;
		result = super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {

				return rs.getInt(1);
			}

		}, sql, new Object[] {}, new int[] {});
		return result;
	}

	public List<RegionInfo> findAllRegInfoByType(int type) {
		String sql = "select distinct t.firstregid, t.secondregid, t.regname, t.regtype " + "from beidoucap.reginfo t where t.regtype=? order by t.firstregid asc, t.secondregid asc";
		List<RegionInfo> result = null;
		result = super.findBySql(new GenericRowMapping<RegionInfo>() {

			public RegionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				RegionInfo ri = new RegionInfo();
				ri.setFirstRegId(rs.getInt(1));
				ri.setSecnodRegId(rs.getInt(2));
				ri.setName(rs.getString(3));
				ri.setType(rs.getInt(4));
				return ri;
			}

		}, sql, new Object[] { type }, new int[] { java.sql.Types.INTEGER });

		return result;
	}

	public List<TradeSiteElement> findAllFirstTradeIdName() {

		String sql = "select tradeid, tradename from beidoucode.sitetrade where parentid = 0 and viewstate = 0";

		return super.findBySql(new GenericRowMapping<TradeSiteElement>() {

			public TradeSiteElement mapRow(ResultSet rs, int rowNum) throws SQLException {
				TradeSiteElement trade = new TradeSiteElement();
				trade.setId(rs.getInt("tradeid"));
				trade.setName(rs.getString("tradename"));
				return trade;
			}

		}, sql, new Object[] {}, new int[] {});
	}

	public void updateSysnvtab(String name, String value) {
		executeBySql("UPDATE beidoucap.sysnvtab set value=? where name=? ", new Object[] { value, name });
	}

	public List<Map<String, Object>> getTradeList() {
		long begin = System.currentTimeMillis();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		StringBuilder sql = new StringBuilder("select tradeid, parentid " + "from beidoucode.sitetrade where parentid>0;");
		list = super.findBySql(sql.toString(), new Object[0], new int[0]);

		LOG.info("Query Trade[" + list.size() + "] Info using:" + (System.currentTimeMillis() - begin) + "ms");
		return list;
	}

	// added by lvzichan, since 2013-10-10
	public Map<Integer, String> getFirstAdTradeMap() {
		String sql = "select tradeid,tradename from beidoucode.adtrade where parentid=0;";

		final Map<Integer, String> result = new HashMap<Integer, String>();
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				Integer tradeId;
				String tradeName;
				do {
					tradeId = rs.getInt(1);
					tradeName = rs.getString(2);
					if (tradeId != null && !StringUtils.isEmpty(tradeName)) {
						result.put(tradeId, tradeName);
					}
				} while (rs.next());
			}
		});
		return result;
	}

	public Map<Integer, String> getFirstSiteTradeMap() {
		String sql = "select tradeid,tradename from beidoucode.sitetrade where parentid=0;";

		final Map<Integer, String> result = new HashMap<Integer, String>();
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				Integer tradeId;
				String tradeName;
				do {
					tradeId = rs.getInt(1);
					tradeName = rs.getString(2);
					if (tradeId != null && !StringUtils.isEmpty(tradeName)) {
						result.put(tradeId, tradeName);
					}
				} while (rs.next());
			}
		});
		return result;
	}

	public Map<Integer, Integer> getSecond2FirstSiteTradeMap() {
		String sql = "select tradeid,parentid from beidoucode.sitetrade";

		final Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				Integer tradeId;
				Integer parentId;
				do {
					tradeId = rs.getInt(1);
					parentId = rs.getInt(2);
					if (tradeId != null && parentId != null) {
						parentId = (parentId == 0) ? tradeId : parentId;
						result.put(tradeId, parentId);
					}
				} while (rs.next());
			}

		});
		return result;
	}
	// end added by lvzichan, since 2013-10-10
}