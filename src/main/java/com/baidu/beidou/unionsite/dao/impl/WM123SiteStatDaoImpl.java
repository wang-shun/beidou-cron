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

import com.baidu.beidou.unionsite.bo.WMSiteBo;
import com.baidu.beidou.unionsite.dao.WM123SiteStatDao;
import com.baidu.beidou.unionsite.vo.SiteElement;
import com.baidu.beidou.unionsite.vo.SiteTradeVo;
import com.baidu.beidou.unionsite.vo.WM123SiteCprodataVo;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author <a href="mailto:liangshimu@baidu.com"></a>
 * @created May 22, 2010
 * @version $Id: WM123SiteStatDaoImpl.java,v 1.2 2010/06/03 10:41:37 scmpf Exp $
 * 
 *          refactor by kanghongwei since 2012-10-31
 */
public class WM123SiteStatDaoImpl extends GenericDaoImpl implements WM123SiteStatDao {

	private static final Log LOG = LogFactory.getLog(WM123SiteStatDaoImpl.class);

	public List<WMSiteBo> loadAllWMSite() {

		String sql = "select a.siteid, b.ips, b.cookies, a.cmplevel, a.scorecmp, a.ratecmp " + "from  beidouext.unionsite u join beidouext.unionsitebdstat a on u.siteid=a.siteid" + " join beidouext.unionsitestat b on a.siteid=b.siteid where u.valid !=0";

		List<WMSiteBo> result = null;
		result = super.findBySql(new GenericRowMapping<WMSiteBo>() {

			public WMSiteBo mapRow(ResultSet rs, int rowNum) throws SQLException {

				WMSiteBo bo = new WMSiteBo();
				bo.setSiteId(rs.getInt(1));
				bo.setIps(rs.getInt(2));
				bo.setCookies(rs.getInt(3));
				bo.setCmpLevel(rs.getByte(4));
				bo.setScoreCmp(rs.getFloat(5));
				bo.setRateCmp(rs.getDouble(6));
				return bo;
			}

		}, sql, new Object[0], new int[0]);

		return result;
	}

	public Map<Integer, String> getAllSiteIdUrlMapping() {
		String sql = "select a.siteid, a.siteurl from beidouext.unionsite a";

		final Map<Integer, String> result = new HashMap<Integer, String>(20000);
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				Integer siteId;
				String siteUrl;
				do {
					siteId = rs.getInt(1);
					siteUrl = rs.getString(2);
					if (siteId != null && !StringUtils.isEmpty(siteUrl)) {
						result.put(siteId, siteUrl);
					}
				} while (rs.next());
			}

		});
		return result;
	}

	public List<SiteElement> findSearchTopNSitesByFirstTradeId(final int tradeId, final int topN) {

		StringBuilder sql = new StringBuilder(" select site.siteid, info.sitename ").append(" from beidouext.unionsite site join beidouext.unionsitestat stat on site.siteid = stat.siteid ").append(" join beidouext.unionsiteinfos info on site.siteid = info.siteid ")
				.append(" where site.valid != 1 and site.firsttradeid is not null ").append(" and site.secondtradeid is not null and info.sitename is not null ").append(" and info.sitedesc is not null and site.firsttradeid = ? ").append(" order by stat.srchs desc limit ? ");

		return super.findBySql(new GenericRowMapping<SiteElement>() {

			public SiteElement mapRow(ResultSet rs, int rowNum) throws SQLException {
				SiteElement site = new SiteElement();
				site.setId(rs.getInt("siteid"));
				site.setName(rs.getString("sitename"));
				return site;
			}

		}, sql.toString(), new Object[] { tradeId, topN }, new int[] { java.sql.Types.INTEGER, java.sql.Types.INTEGER });

	}

	public List<SiteTradeVo> getSiteTradeList() {
		long begin = System.currentTimeMillis();
		List<SiteTradeVo> list = new ArrayList<SiteTradeVo>();
		StringBuilder sql = new StringBuilder("select siteid, firsttradeid, secondtradeid, parentid, isdomain " + "from beidouext.unionsite where valid=1;");

		list = super.findBySql(new GenericRowMapping<SiteTradeVo>() {
			public SiteTradeVo mapRow(ResultSet rs, int rowNum) throws SQLException {
				SiteTradeVo vo = new SiteTradeVo();
				vo.setSiteid(rs.getInt(1));
				vo.setFirsttradeid(rs.getInt(2));
				vo.setSecondtradeid(rs.getInt(3));
				vo.setParentid(rs.getInt(4));
				vo.setDomainFlag(rs.getByte(5));

				return vo;
			}
		}, sql.toString(), new Object[0], new int[0]);

		LOG.info("Query Site-Trade[" + list.size() + "] Info using: " + (System.currentTimeMillis() - begin) + "ms");
		return list;
	}

	public Map<String, Integer> getAllSiteUrl2IdMapping() {
		String sql = "select a.siteurl,a.siteid from beidouext.unionsite a";

		final Map<String, Integer> result = new HashMap<String, Integer>(20000);
		getJdbcTemplate().query(sql, new RowCallbackHandler() {

			public void processRow(ResultSet rs) throws SQLException {
				String siteUrl;
				Integer siteId;
				do {
					siteUrl = rs.getString(1);
					siteId = rs.getInt(2);
					if (siteId != null && !StringUtils.isEmpty(siteUrl)) {
						result.put(siteUrl, siteId);
					}
				} while (rs.next());
			}

		});
		return result;
	}

	// added by lvzichan, since cpweb650, 2013-10-10
	public void delSiteCprodataByDate(String date) {
		String sql = "DELETE FROM beidouext.unionsitecprodata WHERE insert_date = "
				+ date;
		super.executeBySql(sql, null);
	}

	public void saveSiteCprodata(List<WM123SiteCprodataVo> cprodataVos) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO beidouext.unionsitecprodata(siteid,insert_date,cpm,ctr,uv,click,hour_click) VALUES ");
		Object[] params = new Object[cprodataVos.size() * 7];
		int index = 0;
		for (WM123SiteCprodataVo site : cprodataVos) {
			sql.append(" (?,?,?,?,?,?,?) ,");
			params[index++] = site.getSiteId();
			params[index++] = site.getInsertDate();
			params[index++] = site.getCpm();
			params[index++] = site.getCtr();
			params[index++] = site.getUv();
			params[index++] = site.getClick();
			params[index++] = site.getHourClick();
		}
		sql.delete(sql.length() - 1, sql.length());
		super.executeBySql(sql.toString(), params);
	}

}
