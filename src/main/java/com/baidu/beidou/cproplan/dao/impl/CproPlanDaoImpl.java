package com.baidu.beidou.cproplan.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import com.baidu.beidou.cproplan.bo.CproPlan;
import com.baidu.beidou.cproplan.dao.CproPlanDao;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

public class CproPlanDaoImpl extends MultiDataSourceDaoImpl<CproPlan> implements CproPlanDao {

	private static Comparator<CproPlan> comparator = new Comparator<CproPlan>() {
		public int compare(CproPlan o1, CproPlan o2) {
			return (o1.getPlanId() - o2.getPlanId());
		}
	};

	public Long countAllPlan() {
		String sql = "select count(planid) cnt from beidou.cproplan where " + MultiDataSourceSupport.geneateUseridStr("userid");
		return super.countBySql(sql, null, null);
	}

	public List<CproPlan> findPlanInfo() {
		StringBuffer buffer = new StringBuffer("SELECT planid, planstate FROM beidou.cproplan where ");
		buffer.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		return super.findBySqlWithOrder(new GenericRowMapping<CproPlan>() {
			public CproPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
				CproPlan info = new CproPlan();
				info.setPlanId(rs.getInt("planid"));
				info.setPlanState(rs.getInt("planstate"));
				return info;
			}
		}, buffer.toString(), null, null, comparator);
	}

	public List<CproPlan> findPlanInfoOrderbyPlanId() {
		StringBuffer sql = new StringBuffer("SELECT planid, planname, userid FROM beidou.cproplan where ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));

		return super.findBySqlWithOrder(new GenericRowMapping<CproPlan>() {
			public CproPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
				CproPlan info = new CproPlan();
				info.setPlanId(rs.getInt("planid"));
				info.setPlanName(rs.getString("planname"));
				info.setUserId(rs.getInt("userid"));
				return info;
			}
		}, sql.toString(), null, null, comparator);
	}

    @Override
    public void delInvalidPlanDelInfo() {
        StringBuilder sql = new StringBuilder(128);
        sql.append("DELETE FROM plandelinfo USING plandelinfo,cproplan");
        sql.append(" WHERE plandelinfo.planid=cproplan.planid");
        sql.append(" AND cproplan.planstate<>2 and ");
        sql.append(MultiDataSourceSupport.geneateUseridStr("cproplan.userid"));

        super.updateBySql(sql.toString());
    }
}