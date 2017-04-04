/**
 * 2009-4-27 下午09:49:34
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.CollectionUtils;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.unionsite.bo.BDSiteBo;
import com.baidu.beidou.unionsite.bo.UnionSiteBo;
import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.BDSiteStatDao;
import com.baidu.beidou.unionsite.strategy.ISiteLinkPrefixGenerator;
import com.baidu.beidou.unionsite.vo.SiteBDStatVo;
import com.baidu.beidou.unionsite.vo.SiteInfo4KeepInDB;
import com.baidu.beidou.unionsite.vo.WM123SiteScoreVo;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.file.ObjectAccessUtil;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zengyunfeng
 * @version 1.0.7
 * 
 *          refactor by kanghongwei since 2012-10-29
 */
public class BDSiteStatDaoImpl extends GenericDaoImpl implements BDSiteStatDao {

	private ISiteLinkPrefixGenerator prefGenerator;

	public void updateSiteStatusDealing() {
		String sql = "UPDATE beidouext.unionsite SET valid=?, currentvalid=? WHERE valid=?";
		super.executeBySql(sql, new Object[] { SiteConstant.SITE_DEALING, SiteConstant.SITE_CURRENT_DEALING, SiteConstant.SITE_VALID });
	}

	public void updateSiteStatusInvalid() {
		String sql = "UPDATE beidouext.unionsite SET valid=?, currentvalid=?  WHERE valid=?";
		super.executeBySql(sql, new Object[] { SiteConstant.SITE_INVALID, SiteConstant.SITE_CURRENT_INVALID, SiteConstant.SITE_DEALING });
	}

	public int findIdByUrl(String url) {
		List<Map<String, Object>> result = super.findBySql("SELECT siteid FROM beidouext.unionsite WHERE siteurl = ?", new Object[] { url }, new int[] { Types.VARCHAR });
		if (!result.isEmpty()) {
			return ((Number) result.get(0).get("siteid")).intValue();
		}
		return 0;
	}

	public void insertBDSite(final List<BDSiteBo> bdSiteList, final List<SiteBDStatVo> siteTradeList, RandomAccessFile unionSiteReader) throws IOException, InternalException, ClassNotFoundException {
		if (bdSiteList == null || siteTradeList == null) {
			return;
		}
		if (bdSiteList.size() != siteTradeList.size()) {
			return;
		}

		// 设置siteTradeList中的firsttradeid, secondtradeid
		StringBuilder unionsiteSql = new StringBuilder();
		StringBuilder unionsitebdstatSql = new StringBuilder();
		StringBuilder unionsiteinfosSql = new StringBuilder();
		StringBuilder unionsitestatSql = new StringBuilder();

		unionsiteSql.append("REPLACE INTO beidouext.unionsite(siteid, siteurl, firsttradeid, secondtradeid, ")
				.append("isdomain, parentid, valid, invalidtime, jointime, currentvalid) VALUES ");

		unionsitebdstatSql.append("REPLACE INTO beidouext.unionsitebdstat(siteid, q1, q2, thruputtype, sizethruput, score) VALUES ");

		unionsiteinfosSql.append("REPLACE INTO beidouext.unionsiteinfos(siteid, sitename, sitedesc, filter, certification, ")
				.append("finanobj, credit, direct, channel,cheats,sitelink,snapshot,site_source) VALUES ");

		unionsitestatSql.append("REPLACE INTO beidouext.unionsitestat(siteid, srchs, adviews, ips, cookies, clks, cost, ")
				.append("suporttype, size, adblockthruput, displaytype, flow_srchs, flow_adviews, flow_clks, flow_cost,") 
				.append(" fixed_srchs, fixed_adviews, fixed_clks, fixed_cost, " + " film_srchs, film_adviews, film_clks, film_cost ) VALUES ");
		
		int unionSiteParamsSize = 9;
		int unionsiteinfosParamsNum = 13;
		int unionsitebdstatParamsNum = 6;

		Object[] unionsiteParams = new Object[bdSiteList.size() * unionSiteParamsSize];
		Object[] unionsitebdstatParams = new Object[bdSiteList.size() * unionsitebdstatParamsNum];
		Object[] unionsiteinfosParams = new Object[bdSiteList.size() * unionsiteinfosParamsNum];
		Object[] unionsitestatParams = new Object[bdSiteList.size() * 23];

		BDSiteBo site = null;
		SiteBDStatVo bdStatVo = null;
		UnionSiteBo unionSiteBo = null;

		for (int index = 0; index < bdSiteList.size(); index++) {
			site = bdSiteList.get(index);
			bdStatVo = siteTradeList.get(index);
			unionsiteSql.append(" (?,?,?,?,?,?,?,now(),?,?) ");
			unionsitebdstatSql.append(" (?,?,?,?,?,?) ");
			unionsiteinfosSql.append(" (?,?,?,?,?,?,?,?,?,?,?,?,?) ");
			unionsitestatSql.append(" (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
			if (index != bdSiteList.size() - 1) {
				unionsiteSql.append(',');
				unionsitebdstatSql.append(',');
				unionsiteinfosSql.append(',');
				unionsitestatSql.append(',');
			}
			unionSiteBo = (UnionSiteBo) ObjectAccessUtil.readObject(unionSiteReader, bdSiteList.get(index).getSite().getStart(), bdSiteList.get(index).getSite().getLength());
			unionsiteParams[index * unionSiteParamsSize] = site.getSiteid();
			unionsiteParams[index * unionSiteParamsSize + 1] = site.getSite().getDomain();
			unionsiteParams[index * unionSiteParamsSize + 2] = unionSiteBo.getFirstTradeId();
			unionsiteParams[index * unionSiteParamsSize + 3] = unionSiteBo.getSencondTradeId();
			unionsiteParams[index * unionSiteParamsSize + 4] = 1 - site.getSite().getDomainFlag(); // 数据库中1表示一级域名
			unionsiteParams[index * unionSiteParamsSize + 5] = site.getParentid();
			unionsiteParams[index * unionSiteParamsSize + 6] = SiteConstant.SITE_VALID;
			unionsiteParams[index * unionSiteParamsSize + 7] = site.getJoinTime();
			unionsiteParams[index * unionSiteParamsSize + 8] = site.isCurrentValid() ? SiteConstant.SITE_CURRENT_VALID : SiteConstant.SITE_CURRENT_INVALID;

			unionsitebdstatParams[index * unionsitebdstatParamsNum] = site.getSiteid();
			unionsitebdstatParams[index * unionsitebdstatParamsNum + 1] = site.getQValue() == null ? null : site.getQValue().getQ1();
			unionsitebdstatParams[index * unionsitebdstatParamsNum + 2] = site.getQValue() == null ? null : site.getQValue().getQ2();
			unionsitebdstatParams[index * unionsitebdstatParamsNum + 3] = site.getThruputtype();
			unionsitebdstatParams[index * unionsitebdstatParamsNum + 4] = site.getSizethruput();
			unionsitebdstatParams[index * unionsitebdstatParamsNum + 5] = site.getScore();

			unionsiteinfosParams[index * unionsiteinfosParamsNum] = site.getSiteid();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 1] = unionSiteBo.getSiteName();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 2] = unionSiteBo.getSiteDesc();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 3] = unionSiteBo.getFilter();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 4] = unionSiteBo.getCertification();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 5] = unionSiteBo.getFinanobj();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 6] = unionSiteBo.getCredit();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 7] = unionSiteBo.getDirect();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 8] = unionSiteBo.getChannel();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 9] = unionSiteBo.getCheats();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 10] = prefGenerator.generatePrefix(site.getSite().getDomain()) + site.getSite().getDomain();
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 11] = site.getSnapshot();// 添加截图
			unionsiteinfosParams[index * unionsiteinfosParamsNum + 12] = unionSiteBo.getSiteSource();// 添加站点来源

			unionsitestatParams[index * 23] = site.getSiteid();
			unionsitestatParams[index * 23 + 1] = site.getStat().getRetrieve();
			unionsitestatParams[index * 23 + 2] = site.getStat().getAds();
			unionsitestatParams[index * 23 + 3] = site.getStat().getUnique_ip();
			unionsitestatParams[index * 23 + 4] = site.getStat().getUnique_cookie();
			unionsitestatParams[index * 23 + 5] = site.getStat().getClicks();
			unionsitestatParams[index * 23 + 6] = site.getStat().getCost();
			unionsitestatParams[index * 23 + 7] = site.getStat().getWuliao();
			// 尺寸及具体的流量不需要排序，只是一一对应即可
			StringBuilder size = new StringBuilder();
			StringBuilder flow = new StringBuilder();
			if (site.getStat().getSizeFlow() != null) {
				for (Entry<Integer, Integer> cur : site.getStat().getSizeFlow().entrySet()) {
					size.append(cur.getKey()).append(SiteConstant.SIZETHRUPUTSPLITER);
					flow.append(cur.getValue()).append(SiteConstant.SIZETHRUPUTSPLITER);
				}
				size.delete(size.length() - SiteConstant.SIZETHRUPUTSPLITER.length(), size.length());
				flow.delete(flow.length() - SiteConstant.SIZETHRUPUTSPLITER.length(), flow.length());
			}

			unionsitestatParams[index * 23 + 8] = size.toString();
			unionsitestatParams[index * 23 + 9] = flow.toString();

			// mod by zhuqian @beidou1.2.33 分别记录固定、悬浮、贴片广告流量
			// 站点所支持的展现类型
			unionsitestatParams[index * 23 + 10] = site.getStat().getDispType();
			// 悬浮流量
			unionsitestatParams[index * 23 + 11] = site.getStat().getFlowRetrieve();
			unionsitestatParams[index * 23 + 12] = site.getStat().getFlowAds();
			unionsitestatParams[index * 23 + 13] = site.getStat().getFlowClicks();
			unionsitestatParams[index * 23 + 14] = site.getStat().getFlowCost();
			// 固定流量
			unionsitestatParams[index * 23 + 15] = site.getStat().getFixedRetrieve();
			unionsitestatParams[index * 23 + 16] = site.getStat().getFixedAds();
			unionsitestatParams[index * 23 + 17] = site.getStat().getFixedClicks();
			unionsitestatParams[index * 23 + 18] = site.getStat().getFixedCost();
			// 贴片流量
			unionsitestatParams[index * 23 + 19] = site.getStat().getFilmRetrieve();
			unionsitestatParams[index * 23 + 20] = site.getStat().getFilmAds();
			unionsitestatParams[index * 23 + 21] = site.getStat().getFilmClicks();
			unionsitestatParams[index * 23 + 22] = site.getStat().getFilmCost();

			// 设计用于热度计算中使用的一级，二级行业id
			bdStatVo.setFirsttradeid(unionSiteBo.getFirstTradeId());
			bdStatVo.setSecondtradeid(unionSiteBo.getSencondTradeId());
		}

		super.executeBySql(unionsiteSql.toString(), unionsiteParams);
		super.executeBySql(unionsitebdstatSql.toString(), unionsitebdstatParams);
		super.executeBySql(unionsiteinfosSql.toString(), unionsiteinfosParams);
		super.executeBySql(unionsitestatSql.toString(), unionsitestatParams);
	}

	/**
	 * 更新网站等级和热度
	 * 
	 * @author zengyunfeng
	 * @param siteList
	 * @return
	 */
	public int updateSiteScaleAndCmp(final List<SiteBDStatVo> siteList) {

		super.executeBySql("DELETE FROM beidouext.bdstattmp", new Object[0]);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO beidouext.bdstattmp(siteid, scale, ratecmp, scorecmp, cmplevel) VALUES ");
		Object[] params = new Object[siteList.size() * 5];
		int index = 0;
		for (SiteBDStatVo site : siteList) {
			sql.append(" (?,?,?,?,?) ,");
			params[index++] = site.getSiteid();
			params[index++] = site.getScale();
			params[index++] = site.getRatecmp();
			params[index++] = site.getScorecmp();
			params[index++] = site.getCmplevel();
		}
		sql.delete(sql.length() - 1, sql.length());
		super.executeBySql(sql.toString(), params);
		super.executeBySql("UPDATE beidouext.unionsitebdstat a, beidouext.bdstattmp b SET " + "a.scale=b.scale, a.ratecmp=b.ratecmp, a.scorecmp=b.scorecmp, a.cmplevel=b.cmplevel" + " WHERE a.siteid=b.siteid", new Object[0]);
		return 0;
	}

	public Map<String, Integer> getAllMainDomainId() {
		final Map<String, Integer> result = new HashMap<String, Integer>(20000);
		String sql = "SELECT siteurl, siteid FROM beidouext.unionsite WHERE isdomain=1";
		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				result.put(rs.getString(1), rs.getInt(2));
				return null;
			}

		}, sql, new Object[0], new int[0]);
		return result;
	}

	public void insertInvalidDomainSite(List<WhiteUrl> list) {
		if (list == null || list.isEmpty()) {
			return;
		}

		StringBuilder unionsiteSql = new StringBuilder();

		unionsiteSql.append("INSERT INTO beidouext.unionsite(siteid, siteurl, firsttradeid, secondtradeid, isdomain, parentid, valid, invalidtime) VALUES ");
		WhiteUrl site = null;
		Object[] unionsiteParams = new Object[list.size() * 3];
		int[] types = new int[list.size() * 3];

		for (int index = 0; index < list.size(); index++) {
			site = list.get(index);
			unionsiteSql.append(" (?,?,0,0,1,0,?,now()) ");
			if (index != list.size() - 1) {
				unionsiteSql.append(',');
			}

			unionsiteParams[index * 3] = site.getSiteid();
			unionsiteParams[index * 3 + 1] = site.getUrl();
			unionsiteParams[index * 3 + 2] = SiteConstant.SITE_INVALID;
			types[index * 3] = Types.INTEGER;
			types[index * 3 + 1] = Types.VARCHAR;
			types[index * 3 + 2] = Types.INTEGER;
		}
		super.executeBySql(unionsiteSql.toString(), unionsiteParams, types);

	}

	public Map<String, Integer> findAllValidDomainId() {
		final Map<String, Integer> result = new HashMap<String, Integer>(20000);
		String sql = "SELECT siteurl, siteid FROM beidouext.unionsite WHERE isdomain=1";
		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				result.put(rs.getString(1), rs.getInt(2));
				return null;
			}

		}, sql, new Object[0], new int[0]);
		return result;
	}

	public Map<String, SiteInfo4KeepInDB> findAllValidSiteUrlAndJointime() {
		final Map<String, SiteInfo4KeepInDB> result = new HashMap<String, SiteInfo4KeepInDB>(20000);
		String sql = "SELECT a.siteid, a.siteurl, a.jointime,b.snapshot,a.valid FROM beidouext.unionsite a left join beidouext.unionsiteinfos b" + " on a.siteid=b.siteid ";
		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				SiteInfo4KeepInDB info = new SiteInfo4KeepInDB();
				info.setSiteId(rs.getInt(1));
				info.setSiteUrl(rs.getString(2));
				info.setJoinTime(rs.getDate(3));
				info.setSnapshot(rs.getString(4));
				info.setValid(1 == rs.getInt(5));
				result.put(info.getSiteUrl(), info);
				return null;
			}

		}, sql, new Object[0], new int[0]);
		return result;
	}

	/**
	 * 根据一级行业id，获取所对应的所有网站id 此处原用于获取百度自有流量中白名单行业所对应的网站
	 */
	public List<Integer> findSiteIdByBaiduCommonTrade(List<Integer> tradeIdList) {

		final List<Integer> result = new ArrayList<Integer>();
		if (CollectionUtils.isEmpty(tradeIdList)) {
			return result;
		}

		String sql = "select siteid from beidouext.unionsite where firsttradeid in (" + StringUtil.join(",", tradeIdList) + ")";

		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				result.add(rs.getInt("siteid"));

				return null;
			}
		}, sql, new Object[0], new int[0]);

		return result;
	}

	public ISiteLinkPrefixGenerator getPrefGenerator() {
		return prefGenerator;
	}

	public void setPrefGenerator(ISiteLinkPrefixGenerator prefGenerator) {
		this.prefGenerator = prefGenerator;
	}

	/**
	 * 更新网站得分
	 * 
	 * @author lvzichan,2013-08-01
	 * @param refreshScoreList
	 * @return
	 */
	public int updateSiteScore(List<WM123SiteScoreVo> refreshScoreList) {
		
		super.executeBySql("DELETE FROM beidouext.bdstattmp", new Object[0]);

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO beidouext.bdstattmp(siteid, score) VALUES ");
		Object[] params = new Object[refreshScoreList.size() * 2];
		int index = 0;
		for (WM123SiteScoreVo site : refreshScoreList) {
			sql.append(" (?,?) ,");
			params[index++] = site.getSiteId();
			params[index++] = site.getScoreTotal();
		}
		sql.delete(sql.length() - 1, sql.length());
		super.executeBySql(sql.toString(), params);
		super.executeBySql("UPDATE beidouext.unionsitebdstat a, beidouext.bdstattmp b SET " + "a.score=b.score" + " WHERE a.siteid=b.siteid", new Object[0]);

		return 0;
	}
	
	public int findScoreById(int siteid) {
		List<Map<String, Object>> result = super.findBySql(
				"SELECT score FROM beidouext.unionsitebdstat WHERE siteid = ?",
				new Object[] { siteid }, new int[] { Types.INTEGER });
		if (!result.isEmpty()) {
			return ((Number) result.get(0).get("score")).intValue();
		}
		return 0;
	}

}
