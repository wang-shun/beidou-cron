/**
 * beidou-cron-trunk#com.baidu.beidou.unionsite.dao.impl.BDSiteStatOnAddbDaoImpl.java
 * 下午9:00:30 created by kanghongwei
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.dao.BDSiteStatOnAddbDao;
import com.baidu.beidou.unionsite.vo.UserSiteVO;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public class BDSiteStatOnAddbDaoImpl extends MultiDataSourceDaoImpl<UserSiteVO> implements BDSiteStatOnAddbDao {

	private static final Log LOG = LogFactory.getLog(BDSiteStatOnAddbDaoImpl.class);

	private static final int[] SHIFENSTAT = { SiteConstant.SHIFEN_USERSTATE_NORMAL, SiteConstant.SHIFEN_USERSTATE_NOMONEY, SiteConstant.SHIFEN_USERSTATE_UNAUDITED };

	private static Comparator<UserSiteVO> comparator = new Comparator<UserSiteVO>() {
		public int compare(UserSiteVO o1, UserSiteVO o2) {

			if (o1.getUserId() != o2.getUserId()) {
				return (o1.getUserId() - o2.getUserId());
			}

			int o1_isallsite_int = 0;
			if (o1.isallsite) {
				o1_isallsite_int = 1;
			}

			int o2_isallsite_int = 0;
			if (o2.isallsite) {
				o2_isallsite_int = 1;
			}
			return (o2_isallsite_int - o1_isallsite_int);
		}
	};

	/**
	 * 获得具有有效推广组的用户个数，用于对站点进行分批计算热度
	 * 
	 * @author zengyunfeng
	 * @return
	 */
	public int getAvailUserCount() {
		StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT a.userid) cnt FROM beidoucap.useraccount").append(" a JOIN beidou.cproplan b on a.userid=b.userid ").append(" JOIN beidou.cprogroup c on a.userid=c.userid AND b.planid=c.planid WHERE ");
		appendStatFilter(sql, "a", "b", "c");
		sql.append(" and ").append(MultiDataSourceSupport.geneateUseridStr("a.userid"));

		List<Map<String, Object>> resultList = super.findBySql(sql.toString(), null, null);
		if (CollectionUtils.isEmpty(resultList)) {
			return 0;
		}

		long cnt = 0;
		for (Map<String, Object> map : resultList) {
			if (map != null && map.get("cnt") != null) {
				cnt = cnt + (Long) map.get("cnt");
			}
		}

		return (int) cnt;
	}

	private void appendStatFilter(StringBuilder sql, String useraccttable, String plantable, String grouptable) {
		if (sql == null) {
			return;
		}
		sql.append(" ").append(useraccttable).append(".ustate=").append(SiteConstant.BEIDOU_USERSTATE_OPEN);

		sql.append(" AND ").append(useraccttable).append(".sfstattransfer=").append(SiteConstant.SHIFEN_TRANSFER_STATE_NORMAL);
		sql.append(" AND (");
		boolean first = true;
		for (int sfstat : SHIFENSTAT) {
			if (first) {
				first = false;
			} else {
				sql.append(" OR ");
			}
			sql.append(useraccttable).append(".ushifenstatid=").append(sfstat).append(' ');
		}
		sql.append(") ");

		sql.append(" AND ").append(plantable).append(".planstate=").append(SiteConstant.PLAN_STATE_NORMAL);
		sql.append(" AND ").append(grouptable).append(".groupstate=").append(SiteConstant.GROUP_STATE_NORMAL);
	}

	public List<UserSiteVO> statSiteUserVo() {
		long begin = System.currentTimeMillis();
		List<UserSiteVO> list = new ArrayList<UserSiteVO>();
		StringBuilder sql = new StringBuilder("SELECT a.userid, d.sitetradelist, d.sitelist, d.isallsite FROM beidoucap.useraccount").append(" a JOIN beidou.cproplan b on a.userid=b.userid ").append(" JOIN beidou.cprogroup c on a.userid=c.userid  AND b.planid=c.planid ")
				.append(" JOIN beidou.cprogroupinfo d on c.groupid=d.groupid WHERE ");
		appendStatFilter(sql, "a", "b", "c");
		sql.append(" and ").append(MultiDataSourceSupport.geneateUseridStr("a.userid"));

		list = super.findBySqlWithOrder(new GenericRowMapping<UserSiteVO>() {

			public UserSiteVO mapRow(ResultSet rs, int rowNum) throws SQLException {
				UserSiteVO vo = new UserSiteVO();
				vo.userId = rs.getInt(1);
				vo.siteTradeList = rs.getString(2);
				vo.siteList = rs.getString(3);
				vo.isallsite = rs.getBoolean(4);
				return vo;
			}
		}, sql.toString(), new Object[0], new int[0], comparator);
		LOG.info("Query User-Site-Count[" + list.size() + "] Info using： " + (System.currentTimeMillis() - begin) + "ms");
		return list;
	}

	public List<Integer> findAllSiteUser() {
		StringBuilder sql = new StringBuilder("SELECT DISTINCT a.userid FROM beidoucap.useraccount").append(" a JOIN beidou.cproplan b on a.userid=b.userid ").append(" JOIN beidou.cprogroup c on b.planid=c.planid ")
				.append(" JOIN beidou.cprogroupinfo d on c.groupid=d.groupid WHERE d.isallsite = 1 AND ");
		appendStatFilter(sql, "a", "b", "c");
		List<Integer> result = null;
		result = super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt(1);
			}

		}, sql.toString(), new Object[0], new int[0]);

		return result;
	}

}
