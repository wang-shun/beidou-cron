package com.baidu.beidou.unionsite.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.baidu.beidou.unionsite.bo.WMSiteBo;
import com.baidu.beidou.unionsite.bo.WMSiteIndexBo;
import com.baidu.beidou.unionsite.dao.WM123SiteIndexDao;
import com.baidu.beidou.unionsite.vo.WMSiteIndexVo;
import com.baidu.beidou.unionsite.vo.WMSiteVisitorIndexVo;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author <a href="mailto:liangshimu@baidu.com"></a>
 * @created May 22, 2010
 * @version $Id: WM123SiteIndexDaoImpl.java,v 1.2 2010/06/03 10:41:37 scmpf Exp
 *          $
 * 
 *          refactor by kanghongwei since 2012-10-31
 */
public class WM123SiteIndexDaoImpl extends GenericDaoImpl implements WM123SiteIndexDao {

	private static final String COMMA = ",";
	private static final String SPLITTER = "|";

	public void addSiteAdditionalInfo(List<WMSiteBo> list) {
		final List<WMSiteBo> temp = list;
		if (list == null || list.size() == 0) {
			return;
		}
		String sql = "REPLACE INTO beidouurl.unionsiteadditionalstat(siteid, ip, uv, cmpdegree) VALUES (?,?,?,?)";
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

			public int getBatchSize() {
				return temp.size();
			}

			public void setValues(PreparedStatement ps, int index) throws SQLException {
				WMSiteBo bo = temp.get(index);
				if (bo == null) {
					return;
				}
				ps.setInt(1, bo.getSiteId());
				ps.setByte(2, bo.getIpLevel());
				ps.setByte(3, bo.getUvLevel());
				ps.setInt(4, bo.getSiteHeat());
			}

		});
	}

	public void addSiteIndexStat(List<WMSiteIndexBo> list) {

		final List<WMSiteIndexBo> temp = list;
		if (list == null || list.size() == 0) {
			return;
		}
		String sql = "REPLACE INTO beidouurl.unionsiteindexstat(siteid, city, gender, age, education, " + "cityvalue, gendervalue, agevalue, educationvalue, provincestat) VALUES (?,?,?,?,?,?,?,?,?,?)";
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

			public int getBatchSize() {
				return temp.size();
			}

			public void setValues(PreparedStatement ps, int index) throws SQLException {
				WMSiteIndexBo bo = temp.get(index);
				if (bo == null) {
					return;
				}
				ps.setInt(1, bo.getSiteId());
				ps.setString(2, getStringFromList(bo.getCityList(), COMMA));
				ps.setString(3, getStringFromList(bo.getGenderList(), COMMA));
				ps.setString(4, getStringFromList(bo.getAgeList(), COMMA));
				ps.setString(5, getStringFromList(bo.getEducationList(), COMMA));

				ps.setString(6, getStringFromList(bo.getCityValue(), SPLITTER));
				ps.setString(7, getStringFromList(bo.getGenderValue(), SPLITTER));
				ps.setString(8, getStringFromList(bo.getAgeValue(), SPLITTER));
				ps.setString(9, getStringFromList(bo.getEducationValue(), SPLITTER));
				ps.setString(10, getStringFromList(bo.getProvinceStatList(), SPLITTER));
			}

		});
	}

	private String getStringFromList(List list, String separator) {
		StringBuffer sb = new StringBuffer();
		for (Object o : list) {
			sb.append(separator + o);
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	public List<WMSiteIndexVo> loadAllSiteIndexInfo() {

		String sql = "SELECT a.siteid, a.region, a.gender, a.age, a.education " + "FROM beidouurl.unionsiteindex a ";

		List<WMSiteIndexVo> result = null;
		result = super.findBySql(new GenericRowMapping<WMSiteIndexVo>() {

			public WMSiteIndexVo mapRow(ResultSet rs, int rowNum) throws SQLException {

				WMSiteIndexVo bo = new WMSiteIndexVo();
				bo.setSiteId(rs.getInt(1));
				bo.setRegion(rs.getString(2));
				bo.setGender(rs.getString(3));
				bo.setAge(rs.getString(4));
				bo.setDegree(rs.getString(5));
				return bo;
			}

		}, sql, new Object[0], new int[0]);

		return result;
	}

	public static void main(String[] args) {
	}

	public void addSiteIndex(List<WMSiteIndexVo> list) {

		final List<WMSiteIndexVo> temp = list;
		if (list == null || list.size() == 0) {
			return;
		}
		String sql = "REPLACE INTO beidouurl.unionsiteindex(siteid, region, gender, age, education ) VALUES (?,?,?,?,?)";
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

			public int getBatchSize() {
				return temp.size();
			}

			public void setValues(PreparedStatement ps, int index) throws SQLException {
				WMSiteIndexVo bo = temp.get(index);
				if (bo == null) {
					return;
				}
				ps.setInt(1, bo.getSiteId());
				ps.setString(2, bo.getRegion());
				ps.setString(3, bo.getGender());
				ps.setString(4, bo.getAge());
				ps.setString(5, bo.getDegree());
			}

		});
	}

	public void addSiteVistorIndex(List<WMSiteVisitorIndexVo> list) {

		final List<WMSiteVisitorIndexVo> temp = list;
		if (list == null || list.size() == 0) {
			return;
		}
		final Timestamp curTime = new Timestamp(System.currentTimeMillis());
		String sql = "REPLACE INTO beidouurl.unionsitevisitor(siteid, tid, siteurl, site, keyword, interest, updatetime) VALUES (?,?,?,?,?,?,?)";
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {

			public int getBatchSize() {
				return temp.size();
			}

			public void setValues(PreparedStatement ps, int index) throws SQLException {
				WMSiteVisitorIndexVo bo = temp.get(index);
				if (bo == null) {
					return;
				}
				ps.setInt(1, bo.getSiteId());
				if (bo.getTid() == 26037) {
					System.out.println(bo.getSite());
				}
				ps.setInt(2, bo.getTid());
				ps.setString(3, bo.getSiteurl());
				ps.setString(4, bo.getSite());
				ps.setString(5, bo.getKeyword());
				ps.setString(6, bo.getInterest());
				ps.setTimestamp(7, curTime);
			}
		});
	}

}
